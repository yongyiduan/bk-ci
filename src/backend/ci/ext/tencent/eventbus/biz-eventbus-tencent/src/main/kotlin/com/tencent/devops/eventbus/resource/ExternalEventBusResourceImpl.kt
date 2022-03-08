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

package com.tencent.devops.eventbus.resource

import com.fasterxml.jackson.module.kotlin.readValue
import com.tencent.devops.eventbus.api.ExternalEventBusResource
import com.tencent.devops.eventbus.dispatcher.EventBusDispatcher
import com.tencent.devops.eventbus.pojo.WebhookInfo
import com.tencent.devops.eventbus.pojo.event.EventBusRequestEvent
import com.tencent.devops.common.api.exception.InvalidParamException
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.web.RestResource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.Base64
import javax.ws.rs.core.HttpHeaders

@RestResource
class ExternalEventBusResourceImpl @Autowired constructor(
    private val eventBusDispatcher: EventBusDispatcher
) : ExternalEventBusResource {

    companion object {
        private val logger = LoggerFactory.getLogger(ExternalEventBusResourceImpl::class.java)
    }

    override fun webhook(
        webhookId: String,
        headers: HttpHeaders,
        payload: String
    ): Result<Boolean> {
        logger.info("receive webhook|$webhookId|$payload")
        val webhookInfo = getWebhookInfo(webhookId)
        eventBusDispatcher.dispatch(
            EventBusRequestEvent(
                projectId = webhookInfo.projectId,
                source = webhookInfo.source,
                busId = webhookInfo.busId,
                headers = headers.requestHeaders
                    .map { (key, values) -> Pair(key, values.first()) }
                    .toMap(),
                payload = payload
            )
        )
        return Result(true)
    }

    private fun getWebhookInfo(webhookId: String): WebhookInfo {
        val webhookInfoStr = String(Base64.getDecoder().decode(webhookId))
        try {
            logger.info("Start read the webhook info ($webhookInfoStr)")
            return JsonUtil.getObjectMapper().readValue(webhookInfoStr)
        } catch (ignore: Throwable) {
            logger.error("Fail to read the webhook Info", ignore)
            throw InvalidParamException(
                message = "invalid webhookId",
                params = arrayOf(webhookId)
            )
        }
    }
}
