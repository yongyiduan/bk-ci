package com.tencent.devops.stream.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tencent.devops.common.ci.v2.enums.gitEventKind.TGitObjectKind
import com.tencent.devops.common.client.Client
import com.tencent.devops.stream.pojo.GitRequestEvent
import com.tencent.devops.stream.pojo.git.GitTagPushEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class StreamTriggerMessageUtils @Autowired constructor(
    private val client: Client,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val logger = LoggerFactory.getLogger(StreamTriggerMessageUtils::class.java)
    }

    fun getEventMessageTitle(event: GitRequestEvent, gitProjectId: Long): String {
        val messageTitle = when (event.objectKind) {
            TGitObjectKind.MERGE_REQUEST.value -> {
                val branch = GitCommonUtils.checkAndGetForkBranchName(
                    gitProjectId = gitProjectId,
                    sourceGitProjectId = event.sourceGitProjectId,
                    branch = event.branch,
                    client = client
                )
                "[$branch] Merge requests [!${event.mergeRequestId}] ${event.extensionAction} by ${event.userId}"
            }
            TGitObjectKind.MANUAL.value -> {
                "[${event.branch}] Manual Triggered by ${event.userId}"
            }
            TGitObjectKind.TAG_PUSH.value -> {
                val eventMap = try {
                    objectMapper.readValue<GitTagPushEvent>(event.event)
                } catch (e: Exception) {
                    logger.error("event as GitTagPushEvent error ${e.message}")
                    null
                }
                "[${eventMap?.create_from}] Tag [${event.branch}] pushed by ${event.userId}"
            }
            else -> {
                "[${event.branch}] Commit [${event.commitId.subSequence(0, 7)}] pushed by ${event.userId}"
            }
        }
        return messageTitle
    }

    fun getCommitCheckDesc(event: GitRequestEvent, prefix: String): String {
        with(event) {
            return getCommitCheckDesc(
                prefix,
                objectKind,
                extensionAction ?: "",
                branch,
                userId
            )
        }
    }

    fun getCommitCheckDesc(
        prefix: String,
        objectKind: String,
        action: String,
        branch: String,
        userId: String
    ): String {
        val messageTitle = when (objectKind) {
            TGitObjectKind.MERGE_REQUEST.value -> {
                "$prefix, $action by $userId"
            }
            TGitObjectKind.MANUAL.value -> {
                "$prefix, [$branch] Manual Triggered by $userId"
            }
            TGitObjectKind.TAG_PUSH.value -> {
                "$prefix, Tag [$branch] pushed by $userId"
            }
            else -> {
                "$prefix, pushed by $userId"
            }
        }
        return messageTitle
    }
}
