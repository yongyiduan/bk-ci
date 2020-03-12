<template>
    <article class="edit-service-home">
        <div class="info-header">
            <div class="title first-level" @click="toAtomStore">
                <logo :name="&quot;store&quot;" size="30" class="nav-icon" />
                <div class="title first-level"> {{ $t('store.研发商店') }} </div>
            </div>
            <i class="right-arrow"></i>
            <div class="title secondary" @click="toServiceList"> {{ $t('store.工作台') }} </div>
            <i class="right-arrow"></i>
            <div class="title third-level">{{$t('store.上架/升级扩展')}}（{{form.serviceCode}}）</div>
            <a class="develop-guide-link" target="_blank" href="http://tempdocklink/pages/viewpage.action?pageId=22118721"> {{ $t('store.扩展指引') }} </a>
        </div>
        <main v-bkloading="{ isLoading }" class="edit-content">
            <bk-form ref="serviceForm" class="edit-service" label-width="125" :model="form" v-show="!isLoading">
                <bk-form-item class="wt660" :label="$t('store.名称')" :required="true" property="serviceName" :rules="[requireRule]" ref="serviceName">
                    <bk-input v-model="form.serviceName" :placeholder="$t('store.请输入扩展名称')"></bk-input>
                </bk-form-item>
                <bk-form-item class="wt660" :label="$t('store.扩展点')" :required="true" property="extensionItemList" :rules="[requireRule]" ref="extensionItemList">
                    <bk-tag-input :placeholder="$t('store.请选择扩展点')"
                        v-model="form.extensionItemList"
                        :has-delete-icon="true"
                        :list="serviceList"
                        :use-group="true"
                        :tag-tpl="serviceTagTpl"
                        :tpl="renderServiceList"
                        save-key="itemId"
                        display-key="itemName"
                        search-key="itemName"
                        trigger="focus">
                    </bk-tag-input>
                </bk-form-item>
                <bk-form-item :label="$t('store.标签')" property="labelIdList" class="wt660">
                    <bk-tag-input v-model="form.labelIdList" :list="labelList" display-key="labelName" search-key="labelName" trigger="focus" :placeholder="$t('store.请选择标签')"></bk-tag-input>
                </bk-form-item>
                <bk-form-item :label="$t('store.简介')" property="summary" :required="true" :rules="[requireRule]" ref="summary">
                    <bk-input v-model="form.summary" :placeholder="$t('store.请输入简介')"></bk-input>
                </bk-form-item>
                <bk-form-item :label="$t('store.描述')" property="description">
                    <mavon-editor class="service-remark-input"
                        ref="mdHook"
                        preview-background="#fff"
                        v-model="form.description"
                        :toolbars="toolbars"
                        :external-link="false"
                        :box-shadow="false"
                        @imgAdd="uploadimg"
                    />
                </bk-form-item>
                <div class="version-msg">
                    <p class="form-title"> {{ $t('store.版本信息') }} </p>
                    <hr class="cut-line">
                </div>
                <bk-form-item :label="$t('store.发布类型')" :required="true" property="releaseType" class="h32" :rules="[requireRule]" ref="releaseType" v-if="form.releaseType !== 'CANCEL_RE_RELEASE'">
                    <bk-radio-group v-model="form.releaseType">
                        <bk-radio value="NEW" class="mr12" v-if="form.serviceStatus === 'INIT'"> {{ $t('store.新上架') }} </bk-radio>
                        <template v-else>
                            <bk-radio value="INCOMPATIBILITY_UPGRADE" class="mr12"> {{ $t('store.非兼容升级') }} </bk-radio>
                            <bk-radio value="COMPATIBILITY_UPGRADE" class="mr12"> {{ $t('store.兼容式功能更新') }} </bk-radio>
                            <bk-radio value="COMPATIBILITY_FIX"> {{ $t('store.兼容式问题修正') }} </bk-radio>
                        </template>
                    </bk-radio-group>
                </bk-form-item>
                <bk-form-item :label="$t('store.版本号')" property="version" class="lh30" :required="true">
                    <span>{{$t('store.semverType', [form.version])}}</span>
                    <span class="version-modify" @click="form.releaseType = 'COMPATIBILITY_FIX'" v-if="form.releaseType === 'CANCEL_RE_RELEASE'"> {{ $t('store.修改') }} </span>
                </bk-form-item>
                <bk-form-item :label="$t('store.发布者')" :required="true" property="publisher" :rules="[requireRule]" ref="publisher">
                    <bk-input v-model="form.publisher" :placeholder="$t('store.请输入发布者')"></bk-input>
                </bk-form-item>
                <bk-form-item :label="$t('store.版本日志')" :required="true" property="versionContent" :rules="[requireRule]" ref="versionContent">
                    <bk-input type="textarea" v-model="form.versionContent" :placeholder="$t('store.请输入版本日志')"></bk-input>
                </bk-form-item>
                <select-logo ref="selectLogo" label="Logo" :form="form" type="SERVICE" :is-err="logoErr" right="25"></select-logo>
            </bk-form>
            <section class="edit-service button-padding" v-show="!isLoading">
                <bk-button theme="primary" @click="submitService"> {{ $t('store.提交') }} </bk-button>
                <bk-button @click="toServiceList"> {{ $t('store.取消') }} </bk-button>
            </section>
        </main>
    </article>
</template>

<script>
    import { mapActions } from 'vuex'
    import { toolbars } from '@/utils/editor-options'
    import selectLogo from '@/components/common/selectLogo'

    export default {
        components: {
            selectLogo
        },

        data () {
            return {
                form: {
                    serviceId: '',
                    serviceName: '',
                    classifyCode: '',
                    labelIdList: [],
                    labelList: [],
                    summary: '',
                    description: '',
                    logoUrl: '',
                    releaseType: '',
                    version: '1.0.0',
                    publisher: '',
                    versionContent: '',
                    projectCode: '',
                    extensionItemList: []
                },
                classifys: [],
                labelList: [],
                serviceList: [],
                serviceVersionList: [],
                isLoading: false,
                originVersion: '',
                requireRule: {
                    required: true,
                    message: this.$t('store.必填项'),
                    trigger: 'blur'
                },
                latestRule: {
                    validator (val) {
                        return val !== 'latest'
                    },
                    message: this.$t('store.镜像tag不能是latest'),
                    trigger: 'blur'
                },
                logoErr: false,
                toolbars
            }
        },

        watch: {
            'form.releaseType': {
                handler (val) {
                    switch (val) {
                        case 'NEW':
                            this.form.version = '1.0.0'
                            break
                        case 'INCOMPATIBILITY_UPGRADE':
                            this.form.version = this.originVersion.replace(/(.+)\.(.+)\.(.+)/, (a, b, c, d) => (`${+b + 1}.0.0`))
                            break
                        case 'COMPATIBILITY_UPGRADE':
                            this.form.version = this.originVersion.replace(/(.+)\.(.+)\.(.+)/, (a, b, c, d) => (`${b}.${+c + 1}.0`))
                            break
                        case 'COMPATIBILITY_FIX':
                            this.form.version = this.originVersion.replace(/(.+)\.(.+)\.(.+)/, (a, b, c, d) => (`${b}.${c}.${+d + 1}`))
                            break
                        default:
                            break
                    }
                },
                immediate: true
            }
        },

        mounted () {
            this.getServiceDetail()
        },

        methods: {
            ...mapActions('store', [
                'requestServiceDetail',
                'requestServiceItemList',
                'requestReleaseService',
                'requestServiceLabel'
            ]),

            renderServiceList (node) {
                return (<span class="tag-list">{node.itemName}</span>)
            },

            serviceTagTpl (node) {
                return (<span style="font-size: 12px; line-height: 22px; padding: 0 3px">{`${node.parentName}：${node.itemName}`}</span>)
            },

            submitService () {
                this.$refs.serviceForm.validate().then(() => {
                    if (!this.form.logoUrl) {
                        this.logoErr = true
                        const err = { field: 'selectLogo' }
                        throw err
                    }
                    this.isLoading = true
                    this.requestReleaseService(this.form).then((serviceId) => {
                        this.$bkMessage({ message: this.$t('store.提交成功'), theme: 'success' })
                        this.$router.push({ name: 'serviceProgress', params: { serviceId } })
                    }).catch((err) => this.$bkMessage({ message: err.message || err, theme: 'error' })).finally(() => {
                        this.isLoading = false
                    })
                }).catch((validate) => {
                    const field = validate.field
                    const label = this.$refs[field].label
                    this.$bkMessage({ message: `${label + this.$t('store.输入不正确，请确认修改后再试')}`, theme: 'error' })
                })
            },

            getServiceDetail () {
                const params = this.$route.params || {}
                const serviceId = params.serviceId || ''
                this.isLoading = true

                Promise.all([
                    this.requestServiceDetail(serviceId),
                    this.requestServiceLabel(),
                    this.getServiceList()
                ]).then(([res, labels]) => {
                    this.labelList = labels || []
                    Object.assign(this.form, res)
                    if (res.serviceStatus === 'RELEASED') this.form.serviceTag = ''
                    this.form.description = this.form.description || this.$t('store.imageMdDesc')
                    this.originVersion = res.version

                    switch (res.serviceStatus) {
                        case 'INIT':
                            this.form.releaseType = 'NEW'
                            break
                        case 'GROUNDING_SUSPENSION':
                            this.form.releaseType = 'CANCEL_RE_RELEASE'
                            break
                        default:
                            this.form.releaseType = 'COMPATIBILITY_FIX'
                            break
                    }
                }).catch((err) => this.$bkMessage({ message: err.message || err, theme: 'error' })).finally(() => {
                    this.isLoading = false
                })
            },

            getServiceList () {
                const code = this.form.projectCode
                return this.requestServiceItemList(code).then((res) => {
                    this.serviceList = (res || []).map((item) => {
                        const serviceItem = item.serviceItem || {}
                        return {
                            itemId: serviceItem.itemId,
                            itemName: serviceItem.itemName,
                            children: (item.childItem || []).map((x) => {
                                x.parentName = serviceItem.itemName
                                return x
                            })
                        }
                    })
                })
            },

            async uploadimg (pos, file) {
                const formData = new FormData()
                const config = {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                }
                let message, theme
                formData.append('file', file)

                try {
                    const res = await this.$store.dispatch('store/uploadFile', {
                        formData,
                        config
                    })

                    this.$refs.mdHook.$img2Url(pos, res)
                } catch (err) {
                    message = err.message ? err.message : err
                    theme = 'error'

                    this.$bkMessage({
                        message,
                        theme
                    })
                    this.$refs.mdHook.$refs.toolbar_left.$imgDel(pos)
                }
            },

            toServiceList () {
                this.$router.push({
                    name: 'atomList',
                    params: {
                        type: 'service'
                    }
                })
            },

            toAtomStore () {
                this.$router.push({
                    name: 'atomHome'
                })
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import '@/assets/scss/conf.scss';
    .tag-list {
        padding: 0 20px;
        line-height: 32px;
        font-size: 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        color: #63656e;
    }

    /deep/ .bk-tag-selector .bk-tag-input .tag-list .remove-key {
        top: 0;
        margin-left: 6px;
    }

    .edit-service-home {
        height: 100%;
        overflow: hidden;
    }

    .dockerfile {
        height: 400px;
        overflow: auto;
        background: black;
        /deep/ .CodeMirror {
            font-family: Consolas, "Courier New", monospace;
            line-height: 1.5;
            padding: 10px;
            height: auto;
        }
    }

    .button-padding {
        padding-left: 125px;
    }

    .version-msg {
        margin: 30px 0 20px;
    }

    .mr12 {
        margin-right: 12px;
    }

    .lh30 {
        line-height: 30px;
    }

    .mt10 {
        margin-top: 10px;
    }

    .edit-content {
        height: calc(100% - 50px);
        overflow: auto;
    }

    .edit-service {
        width: 1200px;
        margin: 20px auto;
        position: relative;
        .service-remark-input {
            border: 1px solid #c4c6cc;
            height: 263px;
            &.fullscreen {
                height: auto;
            }
        }
        .bk-form-control {
            vertical-align: baseline;
        }
    }

    .version-modify {
        cursor: pointer;
        color: $primaryColor;
        margin-left: 3px;
    }

    .bk-form-item {
        padding-right: 25px;
        &.is-error .bk-select {
            border-color: $dangerColor;
        }
    }

    .wt660 {
        width: 660px;
    }

    .info-header {
        display: flex;
        padding: 14px 24px;
        width: 100%;
        height: 50px;
        border-bottom: 1px solid #DDE4EB;
        background-color: #fff;
        box-shadow:0px 2px 5px 0px rgba(51,60,72,0.03);
        .title {
            display: flex;
            align-items: center;
        }
        .first-level,
        .secondary {
            color: $primaryColor;
            cursor: pointer;
        }
        .third-leve {
            color: $fontWeightColor;
        }
        .nav-icon {
            width: 24px;
            height: 24px;
            margin-right: 10px;
        }
        .right-arrow {
            display :inline-block;
            position: relative;
            width: 19px;
            height: 36px;
            margin-right: 4px;
        }
        .right-arrow::after {
            display: inline-block;
            content: " ";
            height: 4px;
            width: 4px;
            border-width: 1px 1px 0 0;
            border-color: $lineColor;
            border-style: solid;
            transform: matrix(0.71, 0.71, -0.71, 0.71, 0, 0);
            position: absolute;
            top: 50%;
            right: 6px;
            margin-top: -9px;
        }
        .develop-guide-link {
            position: absolute;
            right: 36px;
            margin-top: 2px;
            color: $primaryColor;
            cursor: pointer;
        }
    }
</style>
