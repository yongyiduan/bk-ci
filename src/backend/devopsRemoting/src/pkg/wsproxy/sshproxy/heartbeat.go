package sshproxy

import (
	"context"
	"devopsRemoting/common/logs"
	"devopsRemoting/src/pkg/wsproxy/clients"
	"time"
)

type Heartbeat interface {
	SendHeartbeat(workspaceId string, isClosed, ignoreIfActive bool)
}

type noHeartbeat struct{}

func (noHeartbeat) SendHeartbeat(workspaceId string, isClosed, ignoreIfActive bool) {
	logs.Infof("no send heart beat %s", time.Now().String())
}

type BackendHeartbeat struct {
	Client *clients.BackendClient
}

func (b *BackendHeartbeat) SendHeartbeat(workspaceId string, _, _ bool) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	err := b.Client.SendHeartbeat(ctx, workspaceId)
	if err != nil {
		logs.WithError(err).Warn("cannot send heartbeat for workspace instance")
	} else {
		logs.WithField("workspaceId", workspaceId).Debug("sent heartbeat to ws-manager")
	}
}
