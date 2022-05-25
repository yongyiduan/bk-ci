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

package com.tencent.devops.trigger.source.tgit

import com.fasterxml.jackson.databind.node.ObjectNode
import com.jayway.jsonpath.JsonPath
import com.tencent.devops.common.api.enums.ScmType
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.client.Client
import com.tencent.devops.repository.api.scm.ServiceGitResource
import com.tencent.devops.repository.api.scm.ServiceScmResource
import com.tencent.devops.scm.utils.code.git.GitUtils
import com.tencent.devops.trigger.constant.SourceType
import com.tencent.devops.trigger.source.IEventSourceHandler
import com.tencent.devops.trigger.util.CredentialUtil
import io.appform.jsonrules.utils.ComparisonUtils
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import java.nio.charset.StandardCharsets

@Component(SourceType.TGIT)
class TGitEventSourceHandler(
    private val client: Client
) : IEventSourceHandler<TGitWebhookRequestParam> {

    companion object {
        private val logger = LoggerFactory.getLogger(TGitEventSourceHandler::class.java)
    }

    override fun toCloudEvent(projectId: String?, headers: Map<String, String>, payload: String): CloudEvent? {
        val eventType = headers["X-Event"] ?: return null
        val secretToken = headers["X-Token"]
        val traceId = headers["X-TRACE-ID"]

        val data = when (eventType) {
            "Merge Request Hook" -> mrHookData(payload = payload, projectId = projectId)
            "Review Hook" -> reviewHookData(payload = payload, projectId = projectId)
            else -> payload
        }

        return CloudEventBuilder.v1()
            .withId(traceId)
            .withType(eventType)
            .withSource(URI.create(SourceType.TGIT))
            .withData("application/json", data.toByteArray(StandardCharsets.UTF_8))
            .build()
    }

    private fun mrHookData(payload: String, projectId: String?): String {
        try {
            val ctx = JsonPath.using(ComparisonUtils.SUPPRESS_EXCEPTION_CONFIG).parse(payload)
            val gitProjectId = ctx.read("$.object_attributes.target_project_id", String::class.java)
            val gitUrl = ctx.read("$.object_attributes.target.http_url", String::class.java)
            val mrId = ctx.read("$.object_attributes.id", Long::class.java)
            val iid = ctx.read("$.object_attributes.iid", Long::class.java)
            val token = CredentialUtil.getCredential(
                client = client,
                projectId = projectId!!,
                credentialId = "RDS_PERSONAL_ACCESS_TOKEN"
            )
            val mrInfo = client.get(ServiceScmResource::class).getMrInfo(
                projectName = gitProjectId,
                url = gitUrl,
                type = ScmType.CODE_GIT,
                token = token,
                mrId = mrId
            ).data
            val mrChangeInfo = client.get(ServiceScmResource::class).getMergeRequestChangeInfo(
                projectName = gitProjectId,
                url = gitUrl,
                type = ScmType.CODE_GIT,
                token = token,
                mrId = mrId
            ).data
            val changeFiles = mrChangeInfo?.files?.map {
                if (it.deletedFile) {
                    it.oldPath
                } else {
                    it.newPath
                }
            } ?: emptyList()
            val review = client.get(ServiceScmResource::class).getMrReviewInfo(
                projectName = gitProjectId,
                url = gitUrl,
                type = ScmType.CODE_GIT,
                token = token,
                mrId = mrId
            ).data
            val tapd = client.get(ServiceGitResource::class).getTapdWorkItems(
                accessToken = token,
                gitProjectId = gitProjectId,
                type = "mr",
                iid = iid
            ).data
            val mapper = JsonUtil.getObjectMapper()
            val rootNode = mapper.readTree(payload)
            val objectAttributesNode = rootNode.get("object_attributes") as ObjectNode
            objectAttributesNode.set<ObjectNode>(
                "labels",
                mapper.valueToTree(mrInfo?.labels ?: emptyList<String>())
            )
            objectAttributesNode.set<ObjectNode>(
                "paths",
                mapper.valueToTree(changeFiles)
            )
            objectAttributesNode.set<ObjectNode>(
                "review",
                mapper.valueToTree(review)
            )
            objectAttributesNode.set<ObjectNode>(
                "tapd",
                mapper.valueToTree(tapd)
            )
            return mapper.writeValueAsString(rootNode)
        } catch (ignore: Throwable) {
            logger.warn("Failed to convert merge request hook payload", ignore)
        }
        return payload
    }

    private fun reviewHookData(payload: String, projectId: String?): String {
        try {
            val ctx = JsonPath.using(ComparisonUtils.SUPPRESS_EXCEPTION_CONFIG).parse(payload)
            val reviewableType = ctx.read("$.reviewable_type", String::class.java)
            if (reviewableType == "merge_request") {
                val gitProjectId = ctx.read("$.project_id", String::class.java)
                val gitUrl = ctx.read("$.repository.git_http_url", String::class.java)
                val token = CredentialUtil.getCredential(
                    client = client,
                    projectId = projectId!!,
                    credentialId = "RDS_PERSONAL_ACCESS_TOKEN"
                )
                val reviewableId = ctx.read("$.reviewable_id", Long::class.java)
                val mrInfo = client.get(ServiceScmResource::class).getMrInfo(
                    projectName = gitProjectId,
                    url = gitUrl,
                    type = ScmType.CODE_GIT,
                    token = token,
                    mrId = reviewableId
                ).data
                val mapper = JsonUtil.getObjectMapper()
                val rootNode = mapper.readTree(payload) as ObjectNode
                rootNode.set<ObjectNode>(
                    "merge_request",
                    mapper.valueToTree(mrInfo)
                )
                return mapper.writeValueAsString(rootNode)
            }
        } catch (ignore: Throwable) {
            logger.warn("Failed to convert review hook payload", ignore)
        }
        return payload
    }

    override fun registerWebhook(webhookUrl: String, webhookRequestParam: TGitWebhookRequestParam): Boolean {
        with(webhookRequestParam) {
            logger.info("Started to register tgit webhook with url($repoUrl) event($event)")
            try {
                val projectName = GitUtils.getProjectName(repoUrl)
                client.get(ServiceScmResource::class).addWebHook(
                    projectName = projectName,
                    url = repoUrl,
                    type = ScmType.CODE_GIT,
                    privateKey = null,
                    passPhrase = null,
                    token = token,
                    region = null,
                    userName = "trigger",
                    event = event,
                    hookUrl = webhookUrl
                )
                return true
            } catch (ignore: Throwable) {
                logger.error("Failed to register tgit webhook with url($repoUrl) event($event)", ignore)
                return false
            }
        }
    }

    override fun getWebhookRequestParam(webhookParamMap: Map<String, Any>): TGitWebhookRequestParam {
        return JsonUtil.mapTo(webhookParamMap, TGitWebhookRequestParam::class.java)
    }
}
