/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.devops.dispatch.kubernetes.service

import com.tencent.devops.dispatch.kubernetes.client.KubernetesRemoteDevClient
import com.tencent.devops.dispatch.kubernetes.interfaces.RemoteDevInterface
import com.tencent.devops.dispatch.kubernetes.pojo.GitRepo
import com.tencent.devops.dispatch.kubernetes.pojo.KubernetesWorkspace
import com.tencent.devops.dispatch.kubernetes.pojo.kubernetes.TaskStatus
import com.tencent.devops.dispatch.kubernetes.pojo.kubernetes.WorkspaceInfo
import com.tencent.devops.dispatch.kubernetes.pojo.mq.WorkspaceCreateEvent
import com.tencent.devops.dispatch.kubernetes.pojo.mq.WorkspaceOperateEvent
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service("kubernetesRemoteService")
class KubernetesRemoteDevService @Autowired constructor(
    private val kubernetesRemoteDevClient: KubernetesRemoteDevClient
) : RemoteDevInterface {
    override fun createWorkspace(userId: String, event: WorkspaceCreateEvent): Pair<String, String> {
        val workspaceId = getOnlyName(userId)
        val kubernetesWorkspace = KubernetesWorkspace(
            workspaceId = workspaceId,
            userId = userId,
            gitRepo = GitRepo(
                gitRepoName = event.devFile.dotfileRepo ?: "",
                gitRepoRef = event.branch
            ),
            gitUserName = userId,
            gitEmail = event.devFile.gitEmail ?: "",
            remotingYamlName = "",
            userFiles = emptyList()
        )

        val taskId = kubernetesRemoteDevClient.createWorkspace(userId, kubernetesWorkspace)
        return Pair(workspaceId, taskId)
    }

    override fun startWorkspace(userId: String, workspaceName: String): String {
        TODO("Not yet implemented")
    }

    override fun stopWorkspace(userId: String, workspaceName: String): String {
        TODO("Not yet implemented")
    }

    override fun deleteWorkspace(userId: String, event: WorkspaceOperateEvent): String {
        TODO("Not yet implemented")
    }

    override fun getWorkspaceUrl(userId: String, workspaceName: String): String? {
        return kubernetesRemoteDevClient.getWorkspaceUrl(userId, workspaceName)
    }

    override fun workspaceTaskCallback(taskStatus: TaskStatus): Boolean {
        TODO("Not yet implemented")
    }

    override fun getWorkspaceInfo(userId: String, workspaceName: String): WorkspaceInfo {
        TODO("Not yet implemented")
    }

    private fun getOnlyName(userId: String): String {
        val subUserId = if (userId.length > 14) {
            userId.substring(0 until 14)
        } else {
            userId
        }
        return "${subUserId.replace("_", "-")}${System.currentTimeMillis()}-" +
            RandomStringUtils.randomAlphabetic(8).toLowerCase()
    }
}