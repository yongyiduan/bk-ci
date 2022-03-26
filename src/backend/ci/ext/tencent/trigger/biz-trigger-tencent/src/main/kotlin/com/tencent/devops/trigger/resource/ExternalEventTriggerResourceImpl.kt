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

package com.tencent.devops.trigger.resource

import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.trigger.api.ExternalEventTriggerResource
import com.tencent.devops.trigger.dispatcher.EventTriggerDispatcher
import com.tencent.devops.trigger.pojo.event.EventAppWebhookRequestEvent
import com.tencent.devops.trigger.pojo.event.EventWebhookRequestEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import javax.ws.rs.core.HttpHeaders

@RestResource
class ExternalEventTriggerResourceImpl @Autowired constructor(
    private val eventTriggerDispatcher: EventTriggerDispatcher
) : ExternalEventTriggerResource {

    companion object {
        private val logger = LoggerFactory.getLogger(ExternalEventTriggerResourceImpl::class.java)
    }

    override fun webhook(
        sourceName: String,
        projectId: String,
        busId: String,
        headers: HttpHeaders,
        payload: String
    ): Result<Boolean> {
        logger.info("receive webhook|$sourceName|$projectId|$busId|${headers.requestHeaders}|$payload")
        eventTriggerDispatcher.dispatch(
            EventWebhookRequestEvent(
                projectId = projectId,
                sourceName = sourceName,
                busId = busId,
                headers = headers.requestHeaders
                    .map { (key, values) -> Pair(key, values.first()) }
                    .toMap(),
                payload = payload
            )
        )
        return Result(true)
    }

    override fun appWebhook(sourceName: String, headers: HttpHeaders, payload: String): Result<Boolean> {
        logger.info("receive app webhook|$sourceName|${headers.requestHeaders}|$payload")
        eventTriggerDispatcher.dispatch(
            EventAppWebhookRequestEvent(
                sourceName = sourceName,
                headers = headers.requestHeaders
                    .map { (key, values) -> Pair(key, values.first()) }
                    .toMap(),
                payload = payload
            )
        )
        return Result(true)
    }
}
