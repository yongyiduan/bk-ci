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

package com.tencent.devops.eventbus.service

import com.fasterxml.jackson.databind.JsonNode
import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.service.utils.SpringContextUtil
import com.tencent.devops.eventbus.constant.EventBusMessageCode.SOURCE_NOT_SUPPORT
import com.tencent.devops.eventbus.dao.EventBusDao
import com.tencent.devops.eventbus.dao.EventBusRuleDao
import com.tencent.devops.eventbus.dao.EventRuleTargetDao
import com.tencent.devops.eventbus.dispatcher.EventBusDispatcher
import com.tencent.devops.eventbus.pojo.event.EventBusRequestEvent
import com.tencent.devops.eventbus.pojo.event.EventTargetRunEvent
import com.tencent.devops.eventbus.source.IEventSourceHandler
import com.tencent.devops.eventbus.util.CloudEventJsonUtil
import com.tencent.devops.eventbus.util.RuleUtil
import com.tencent.devops.eventbus.util.TargetParamUtil
import io.cloudevents.CloudEvent
import io.cloudevents.http.HttpMessageFactory
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EventBusRequestService @Autowired constructor(
    private val dslContext: DSLContext,
    private val eventBusDao: EventBusDao,
    private val eventBusRuleDao: EventBusRuleDao,
    private val eventbusRuleTargetDao: EventRuleTargetDao,
    private val eventBusDispatcher: EventBusDispatcher
) {

    companion object {
        private val logger = LoggerFactory.getLogger(EventBusRequestService::class.java)
    }

    fun handle(event: EventBusRequestEvent) {
        with(event) {
            val cloudEvent = toCloudEvent()
            logger.info(
                "start to handle event bus request:${busId},${cloudEvent.source},${cloudEvent.type},${cloudEvent.id}"
            )
            val eventBus = eventBusDao.getByBusId(
                dslContext = dslContext,
                projectId = projectId,
                busId = busId
            )
            if (eventBus == null) {
                logger.info("$projectId|$busId| event bus not exist")
                return
            }
            val ruleList = eventBusRuleDao.listBySource(
                dslContext = dslContext,
                projectId = projectId,
                busId = busId,
                source = cloudEvent.source.toString(),
                type = cloudEvent.type
            )
            val node = CloudEventJsonUtil.serializeAsJsonNode(event = cloudEvent)
            ruleList.forEach { rule ->
                if (RuleUtil.matches(node = node, filterPattern = rule.filterPattern)) {
                    dispatchRuleTarget(
                        projectId = projectId,
                        ruleId = rule.ruleId,
                        node = node
                    )
                }
            }
        }
    }

    private fun EventBusRequestEvent.toCloudEvent(): CloudEvent {
        val eventSourceHandler = try {
            SpringContextUtil.getBean(IEventSourceHandler::class.java, sourceName)
        } catch (ignore: Throwable) {
            logger.warn("$sourceName event source plugin not found")
            null
        }
        return eventSourceHandler?.toCloudEvent(
            headers = headers,
            payload = payload
        ) ?: HttpMessageFactory.createReader(headers, payload.toByteArray()).toEvent()
        ?: throw ErrorCodeException(
            errorCode = SOURCE_NOT_SUPPORT,
            params = arrayOf(sourceName),
            defaultMessage = "event source ($sourceName) not support"
        )
    }

    private fun dispatchRuleTarget(
        projectId: String,
        ruleId: String,
        node: JsonNode
    ) {
        val targetList = eventbusRuleTargetDao.listByRuleId(
            dslContext = dslContext,
            projectId = projectId,
            ruleId = ruleId
        )
        val targetRunEvents = targetList.map { target ->
            val targetParamMap = TargetParamUtil.convert(node = node, targetParams = target.targetParams)
            EventTargetRunEvent(
                projectId = projectId,
                ruleId = ruleId,
                targetName = target.targetName,
                pushRetryStrategy = target.pushRetryStrategy,
                targetParamMap = targetParamMap
            )
        }.toList()
        eventBusDispatcher.dispatch(*targetRunEvents.toTypedArray())
    }
}
