package sshproxy

import (
	"common/devops"
	"common/logs"
	"context"
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
	Client *devops.RemoteDevClient
}

func (b *BackendHeartbeat) SendHeartbeat(workspaceId string, _, _ bool) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	err := b.Client.SendHeartbeat(ctx, workspaceId)
	if err != nil {
		logs.Error("cannot send heartbeat for workspace instance", logs.Err(err))
	} else {
		logs.Debug("sent heartbeat to ws-manager", logs.String("workspaceId", workspaceId))
	}
}
