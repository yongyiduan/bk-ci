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

package com.tencent.devops.trigger.target.pipeline

import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.client.Client
import com.tencent.devops.trigger.constant.TargetType
import com.tencent.devops.trigger.target.IEventTargetInvoker
import com.tencent.devops.process.api.service.ServiceWebhookBuildResource
import com.tencent.devops.process.pojo.webhook.WebhookTriggerParams
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component(TargetType.PIPELINE)
class PipelineEventTargetInvoker @Autowired constructor(
    private val client: Client
) : IEventTargetInvoker<PipelineRequestParam> {

    companion object {
        private val logger = LoggerFactory.getLogger(PipelineEventTargetInvoker::class.java)
    }

    override fun invoke(targetRequestParam: PipelineRequestParam, runtimeVariables: Map<String, String>) {
        with(targetRequestParam) {
            try {
                logger.info(
                    "$projectId|$pipelineId|Start to invoke [pipeline] event target by params $targetRequestParam"
                )
                val buildId = client.get(ServiceWebhookBuildResource::class).webhookTrigger(
                    userId = userId,
                    projectId = projectId,
                    pipelineId = pipelineId,
                    params = WebhookTriggerParams(params = values.plus(runtimeVariables)),
                    channelCode = channelCode
                ).data
                logger.info("$projectId|$pipelineId|$buildId|Success to invoke [pipeline] event target")
            } catch (ignore: Throwable) {
                logger.error("$projectId|$pipelineId|Failed to invoke [pipeline] event target", ignore)
            }
        }
    }

    override fun getTargetRequestParam(targetRequestParamMap: Map<String, Any>): PipelineRequestParam {
        return JsonUtil.mapTo(targetRequestParamMap, PipelineRequestParam::class.java)
    }
}
