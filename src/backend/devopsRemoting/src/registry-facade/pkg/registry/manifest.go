package registry

import (
	"bytes"
	"common/logs"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"mime"
	"net/http"
	"strings"
	"time"

	"registry-facade/api"

	"github.com/containerd/containerd/content"
	"github.com/containerd/containerd/errdefs"
	"github.com/containerd/containerd/images"
	"github.com/containerd/containerd/remotes"
	distv2 "github.com/docker/distribution/registry/api/v2"
	"github.com/gorilla/handlers"
	"github.com/opencontainers/go-digest"
	ociv1 "github.com/opencontainers/image-spec/specs-go/v1"
	"github.com/pkg/errors"
	"go.uber.org/zap"
	"golang.org/x/xerrors"
)

func (reg *Registry) handleManifest(ctx context.Context, r *http.Request) http.Handler {
	t0 := time.Now()

	spname, name := getSpecProviderName(ctx)
	sp, ok := reg.SpecProvider[spname]
	if !ok {
		logs.Error("unknown spec provider", logs.String("specProvName", spname))
		return http.HandlerFunc(func(w http.ResponseWriter, _ *http.Request) {
			respondWithError(w, distv2.ErrorCodeManifestUnknown)
		})
	}
	spec, err := sp.GetSpec(ctx, name)
	if err != nil {
		logs.Error("cannot get spec", logs.String("specProvName", spname), logs.Err(err), logs.String("name", name))
		return http.HandlerFunc(func(w http.ResponseWriter, _ *http.Request) {
			respondWithError(w, distv2.ErrorCodeManifestUnknown)
		})
	}

	manifestHandler := &manifestHandler{
		Context:        ctx,
		Name:           name,
		Spec:           spec,
		Resolver:       reg.Resolver(),
		Store:          reg.Store,
		ConfigModifier: reg.ConfigModifier,
	}
	reference := getReference(ctx)
	dgst, err := digest.Parse(reference)
	if err != nil {
		manifestHandler.Tag = reference
	} else {
		manifestHandler.Digest = dgst
	}

	mhandler := handlers.MethodHandler{
		"GET":    http.HandlerFunc(manifestHandler.getManifest),
		"HEAD":   http.HandlerFunc(manifestHandler.getManifest),
		"PUT":    http.HandlerFunc(manifestHandler.putManifest),
		"DELETE": http.HandlerFunc(manifestHandler.deleteManifest),
	}

	res := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		mhandler.ServeHTTP(w, r)
		dt := time.Since(t0)
		reg.metrics.ManifestHist.Observe(dt.Seconds())
	})

	return res
}

type manifestHandler struct {
	Context context.Context

	Spec           *api.ImageSpec
	Resolver       remotes.Resolver
	Store          BlobStore
	ConfigModifier ConfigModifier

	Name   string
	Tag    string
	Digest digest.Digest
}

func (mh *manifestHandler) getManifest(w http.ResponseWriter, r *http.Request) {
	//nolint:staticcheck,ineffassign
	// span, ctx := opentracing.StartSpanFromContext(r.Context(), "getManifest")
	ctx := r.Context()
	logFields := []zap.Field{
		logs.String("workspaceId", mh.Name),
		logs.String("tag", mh.Tag),
		logs.Any("spec", mh.Spec),
	}
	err := func() error {
		logger := logs.With(logFields...)
		logger.Debug("get manifest")
		// tracing.LogMessageSafe(span, "spec", mh.Spec)

		var (
			acceptType string
			err        error
		)
		for _, acceptHeader := range r.Header["Accept"] {
			for _, mediaType := range strings.Split(acceptHeader, ",") {
				if mediaType, _, err = mime.ParseMediaType(strings.TrimSpace(mediaType)); err != nil {
					continue
				}

				if mediaType == ociv1.MediaTypeImageManifest ||
					mediaType == images.MediaTypeDockerSchema2Manifest ||
					mediaType == "*" {

					acceptType = ociv1.MediaTypeImageManifest
					break
				}
			}
			if acceptType != "" {
				break
			}
		}
		if acceptType == "" {
			return distv2.ErrorCodeManifestUnknown.WithMessage("Accept header does not include OCIv1 or v2 manifests")
		}

		// Note: we ignore the mh.Digest for now because we always return a manifest, never a manifest index.
		ref := mh.Spec.BaseRef

		_, desc, err := mh.Resolver.Resolve(ctx, ref)
		if err != nil {
			logger.Error("cannot resolve", logs.Err(err), logs.String("ref", ref))
			// ErrInvalidAuthorization
			return err
		}

		var fcache remotes.Fetcher
		fetch := func() (remotes.Fetcher, error) {
			if fcache != nil {
				return fcache, nil
			}

			fetcher, err := mh.Resolver.Fetcher(ctx, ref)
			if err != nil {
				return nil, err
			}
			fcache = fetcher
			return fcache, nil
		}

		manifest, ndesc, err := DownloadManifest(ctx, fetch, desc, WithStore(mh.Store))
		if err != nil {
			logger.Error("cannot download manifest", logs.Err(err), logs.Any("desc", desc), logs.String("ref", ref))
			return distv2.ErrorCodeManifestUnknown.WithDetail(err)
		}
		desc = *ndesc

		var p []byte
		switch desc.MediaType {
		case images.MediaTypeDockerSchema2Manifest, ociv1.MediaTypeImageManifest:
			// download config
			cfg, err := DownloadConfig(ctx, fetch, ref, manifest.Config, WithStore(mh.Store))
			if err != nil {
				logger.Error("cannot download config", logs.Err(err))
				return err
			}

			// modify config
			addonLayer, err := mh.ConfigModifier(ctx, mh.Spec, cfg)
			if err != nil {
				logger.Error("cannot modify config", logs.Err(err))
				return err
			}
			manifest.Layers = append(manifest.Layers, addonLayer...)

			// place config in store
			rawCfg, err := json.Marshal(cfg)
			if err != nil {
				logger.Error("cannot marshal config", logs.Err(err))
				return err
			}
			cfgDgst := digest.FromBytes(rawCfg)

			// update config digest in manifest
			manifest.Config.Digest = cfgDgst
			manifest.Config.URLs = nil
			manifest.Config.Size = int64(len(rawCfg))

			// 优化：我们将配置存储在缓存中，以防客户端尝试下载配置 blob
			// 来自我们。 如果他们从尚未下载mainfast的registry facade下载它
			// 我们将即时重新创建配置。
			if w, err := mh.Store.Writer(ctx, content.WithRef(ref), content.WithDescriptor(manifest.Config)); err == nil {
				defer w.Close()

				_, err = w.Write(rawCfg)
				if err != nil {
					logger.Warn("cannot write config to store - we'll regenerate it on demand", logs.Err(err))
				}
				err = w.Commit(ctx, 0, cfgDgst, content.WithLabels(contentTypeLabel(manifest.Config.MediaType)))
				if err != nil {
					logger.Warn("cannot commit config to store - we'll regenerate it on demand", logs.Err(err))
				}
			}

			// 当提供 images.MediaTypeDockerSchema2Manifest 服务时，我们必须在清单本身中设置 mediaType。
			// 尽管与 OCI 清单规范有些兼容（请参阅 https://github.com/opencontainers/image-spec/blob/master/manifest.md），
			// 这个字段不是 OCI Go 结构的一部分。 在这种特殊情况下，我们将继续自己添加它。
			//
			// 修复 https://github.com/gitpod-io/gitpod/pull/3397
			if desc.MediaType == images.MediaTypeDockerSchema2Manifest {
				type ManifestWithMediaType struct {
					ociv1.Manifest
					MediaType string `json:"mediaType"`
				}
				p, _ = json.Marshal(ManifestWithMediaType{
					Manifest:  *manifest,
					MediaType: images.MediaTypeDockerSchema2Manifest,
				})
			} else {
				p, _ = json.Marshal(manifest)
			}
		}

		dgst := digest.FromBytes(p).String()

		w.Header().Set("Content-Type", desc.MediaType)
		w.Header().Set("Content-Length", fmt.Sprint(len(p)))
		w.Header().Set("Etag", fmt.Sprintf(`"%s"`, dgst))
		w.Header().Set("Docker-Content-Digest", dgst)
		_, _ = w.Write(p)

		logger.Debug("get manifest (end)")
		return nil
	}()

	if err != nil {
		logs.Error("cannot get manifest", logs.Err(err), logs.Any("spec", mh.Spec))
		respondWithError(w, err)
	}
	// tracing.FinishSpan(span, &err)
}

// DownloadConfig 下载desc指定的 OCIv2 镜像配置
func DownloadConfig(ctx context.Context, fetch FetcherFunc, ref string, desc ociv1.Descriptor, options ...ManifestDownloadOption) (cfg *ociv1.Image, err error) {
	if desc.MediaType != images.MediaTypeDockerSchema2Config &&
		desc.MediaType != ociv1.MediaTypeImageConfig {

		return nil, xerrors.Errorf("unsupported media type: %s", desc.MediaType)
	}

	var opts manifestDownloadOptions
	for _, o := range options {
		o(&opts)
	}

	var rc io.ReadCloser
	if opts.Store != nil {
		r, err := opts.Store.ReaderAt(ctx, desc)
		if errors.Is(err, errdefs.ErrNotFound) {
			// not cached yet
		} else if err != nil {
			logs.Warn("cannot read config from store - fetching again", logs.Err(err), logs.Any("desc", desc))
		} else {
			defer r.Close()
			rc = io.NopCloser(content.NewReader(r))
		}
	}
	if rc == nil {
		fetcher, err := fetch()
		if err != nil {
			return nil, err
		}
		rc, err = fetcher.Fetch(ctx, desc)
		if err != nil {
			return nil, xerrors.Errorf("cannot download config: %w", err)
		}
		defer rc.Close()
	}

	buf, err := io.ReadAll(rc)
	if err != nil {
		return nil, xerrors.Errorf("cannot read config: %w", err)
	}

	var res ociv1.Image
	err = json.Unmarshal(buf, &res)
	if err != nil {
		return nil, xerrors.Errorf("cannot decode config: %w", err)
	}

	if opts.Store != nil && ref != "" {
		// ref can be empty for some users of DownloadConfig. However, some store implementations
		// (e.g. the default containerd store) expect ref to be set. This would lead to stray errors.

		err := func() error {
			w, err := opts.Store.Writer(ctx, content.WithDescriptor(desc), content.WithRef(ref))
			if err != nil {
				return err
			}
			defer w.Close()

			n, err := w.Write(buf)
			if err != nil {
				return err
			}
			if n != len(buf) {
				return io.ErrShortWrite
			}

			return w.Commit(ctx, int64(len(buf)), digest.FromBytes(buf), content.WithLabels(contentTypeLabel(desc.MediaType)))
		}()
		if err != nil && !strings.Contains(err.Error(), "already exists") {
			logs.Warn("cannot cache config", logs.Err(err), logs.String("ref", ref), logs.Any("desc", desc))
		}
	}

	return &res, nil
}

type FetcherFunc func() (remotes.Fetcher, error)

func AsFetcherFunc(f remotes.Fetcher) FetcherFunc {
	return func() (remotes.Fetcher, error) { return f, nil }
}

// DownloadManifest 下载desc指定的manifest，如果返回列表选择第一个
func DownloadManifest(ctx context.Context, fetch FetcherFunc, desc ociv1.Descriptor, options ...ManifestDownloadOption) (cfg *ociv1.Manifest, rdesc *ociv1.Descriptor, err error) {
	var opts manifestDownloadOptions
	for _, o := range options {
		o(&opts)
	}

	var (
		placeInStore bool
		rc           io.ReadCloser
		mediaType    = desc.MediaType
	)
	if opts.Store != nil {
		func() {
			nfo, err := opts.Store.Info(ctx, desc.Digest)
			if errors.Is(err, errdefs.ErrNotFound) {
				// not in store yet
				return
			}
			if err != nil {
				logs.Warn("cannot get manifest from store", logs.Err(err), logs.Any("desc", desc))
				return
			}
			if nfo.Labels["Content-Type"] == "" {
				// we have broken data in the store - ignore it and overwrite
				return
			}

			r, err := opts.Store.ReaderAt(ctx, desc)
			if errors.Is(err, errdefs.ErrNotFound) {
				// not in store yet
				return
			}
			if err != nil {
				logs.Warn("cannot get manifest from store", logs.Err(err), logs.Any("desc", desc))
				return
			}

			mediaType, rc = nfo.Labels["Content-Type"], &reader{ReaderAt: r}
		}()
	}
	if rc == nil {
		// 没有在缓存中或者缓存中读取失败
		placeInStore = true

		var fetcher remotes.Fetcher
		fetcher, err = fetch()
		if err != nil {
			return
		}

		rc, err = fetcher.Fetch(ctx, desc)
		if err != nil {
			err = xerrors.Errorf("cannot fetch manifest: %w", err)
			return
		}
		mediaType = desc.MediaType
	}

	inpt, err := io.ReadAll(rc)
	rc.Close()
	if err != nil {
		err = xerrors.Errorf("cannot download manifest: %w", err)
		return
	}

	rdesc = &desc
	rdesc.MediaType = mediaType

	switch rdesc.MediaType {
	case images.MediaTypeDockerSchema2ManifestList, ociv1.MediaTypeImageIndex:
		logs.Debug("resolving image index", logs.Any("desc", rdesc))

		var list ociv1.Index
		err = json.Unmarshal(inpt, &list)
		if err != nil {
			err = xerrors.Errorf("cannot unmarshal index: %w", err)
			return
		}
		if len(list.Manifests) == 0 {
			err = xerrors.Errorf("empty manifest")
			return
		}

		var fetcher remotes.Fetcher
		fetcher, err = fetch()
		if err != nil {
			return
		}

		// TODO: 根据平台选择而不是只选择第一个
		md := list.Manifests[0]
		rc, err = fetcher.Fetch(ctx, md)
		if err != nil {
			err = xerrors.Errorf("cannot download config: %w", err)
			return
		}
		rdesc = &md
		inpt, err = io.ReadAll(rc)
		rc.Close()
		if err != nil {
			err = xerrors.Errorf("cannot download manifest: %w", err)
			return
		}
	}

	switch rdesc.MediaType {
	case images.MediaTypeDockerSchema2Manifest, ociv1.MediaTypeImageManifest:
	default:
		err = xerrors.Errorf("unsupported media type: %s", rdesc.MediaType)
		return
	}

	var res ociv1.Manifest
	err = json.Unmarshal(inpt, &res)
	if err != nil {
		err = xerrors.Errorf("cannot decode config: %w", err)
		return
	}

	if opts.Store != nil && placeInStore {
		// 这里将manifest写到image desc，这样下一次获取镜像就不用去解析index
		w, err := opts.Store.Writer(ctx, content.WithDescriptor(desc), content.WithRef(desc.Digest.String()))
		if err != nil {
			if err != nil && !strings.Contains(err.Error(), "already exists") {
				logs.Warn("cannot create store writer", logs.Err(err), logs.Any("desc", *rdesc))
			}
		} else {
			_, err = io.Copy(w, bytes.NewReader(inpt))
			if err != nil {
				logs.Warn("cannot copy manifest", logs.Err(err), logs.Any("desc", *rdesc))
			}

			err = w.Commit(ctx, 0, digest.FromBytes(inpt), content.WithLabels(map[string]string{"Content-Type": rdesc.MediaType}))
			if err != nil {
				logs.Warn("cannot store manifest", logs.Err(err), logs.Any("desc", *rdesc))
			}
			w.Close()
		}
	}

	cfg = &res
	return
}

type BlobStore interface {
	ReaderAt(ctx context.Context, desc ociv1.Descriptor) (content.ReaderAt, error)

	Writer(ctx context.Context, opts ...content.WriterOpt) (content.Writer, error)

	Info(ctx context.Context, dgst digest.Digest) (content.Info, error)
}

func contentTypeLabel(mt string) map[string]string {
	return map[string]string{"Content-Type": mt}
}

type manifestDownloadOptions struct {
	Store BlobStore
}

// ManifestDownloadOption 改变默认的mainfest下载行为
type ManifestDownloadOption func(*manifestDownloadOptions)

// WithStore caches a downloaded manifest in a store
func WithStore(store BlobStore) ManifestDownloadOption {
	return func(o *manifestDownloadOptions) {
		o.Store = store
	}
}

func (mh *manifestHandler) putManifest(w http.ResponseWriter, r *http.Request) {
	respondWithError(w, distv2.ErrorCodeManifestInvalid)
}

func (mh *manifestHandler) deleteManifest(w http.ResponseWriter, r *http.Request) {
	respondWithError(w, distv2.ErrorCodeManifestUnknown)
}
