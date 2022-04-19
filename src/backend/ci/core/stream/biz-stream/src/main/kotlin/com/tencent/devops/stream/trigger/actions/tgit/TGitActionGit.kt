package com.tencent.devops.stream.trigger.actions.tgit

import com.tencent.devops.common.api.enums.ScmType
import com.tencent.devops.common.webhook.pojo.code.git.GitMergeRequestEvent
import com.tencent.devops.process.yaml.v2.models.Variable
import com.tencent.devops.stream.trigger.actions.GitBaseAction
import com.tencent.devops.stream.trigger.actions.data.ActionData
import com.tencent.devops.stream.trigger.git.pojo.tgit.TGitCred
import com.tencent.devops.stream.trigger.git.service.TGitApiService
import com.tencent.devops.stream.trigger.pojo.enums.StreamCommitCheckState
import com.tencent.devops.stream.trigger.pojo.enums.toGitState
import com.tencent.devops.stream.trigger.service.GitCheckService

/**
 * 对于stream 平台级功能的具体实现，不需要下放到具体的event
 * 对于只有一两个事件实现的，也可在平台级实现一个通用的，一两个再自己重写
 */
abstract class TGitActionGit(
    override val api: TGitApiService,
    private val gitCheckService: GitCheckService
) : GitBaseAction {
    override lateinit var data: ActionData

    override fun getProjectCode(gitProjectId: String?): String {
        return if (gitProjectId != null) {
            "git_$gitProjectId"
        } else {
            "git_${data.getGitProjectId()}"
        }
    }

    override fun getGitCred(personToken: String?): TGitCred {
        if (personToken != null) {
            return TGitCred(
                userId = null,
                accessToken = personToken,
                useAccessToken = false
            )
        }
        return TGitCred(data.setting.enableUser)
    }

    override fun getUserVariables(yamlVariables: Map<String, Variable>?): Map<String, Variable>? = null

    override fun needSaveOrUpdateBranch() = false

    override fun needSendCommitCheck() = true

    override fun sendCommitCheck(
        buildId: String,
        gitProjectName: String,
        state: StreamCommitCheckState,
        block: Boolean,
        context: String,
        targetUrl: String,
        description: String,
        reportData: Pair<List<String>, MutableMap<String, MutableList<List<String>>>>
    ) {
        gitCheckService.pushCommitCheck(
            userId = data.eventCommon.userId,
            projectCode = getProjectCode(),
            buildId = buildId,
            gitProjectId = data.eventCommon.gitProjectId,
            gitProjectName = gitProjectName,
            pipelineId = data.context.pipeline!!.pipelineId,
            commitId = data.eventCommon.commit.commitId,
            gitHttpUrl = data.setting.gitHttpUrl,
            scmType = ScmType.CODE_GIT,
            token = api.getToken(getGitCred()),
            state = state.toGitState(ScmType.CODE_GIT),
            block = block,
            context = context,
            targetUrl = targetUrl,
            description = description,
            if (data.event is GitMergeRequestEvent) {
                (data.event as GitMergeRequestEvent).object_attributes.iid
            } else {
                null
            },
            reportData
        )
    }
}
