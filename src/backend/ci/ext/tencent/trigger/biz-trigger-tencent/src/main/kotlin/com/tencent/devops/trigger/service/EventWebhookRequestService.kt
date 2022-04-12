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

package com.tencent.devops.trigger.service

import com.fasterxml.jackson.databind.JsonNode
import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.service.utils.SpringContextUtil
import com.tencent.devops.trigger.constant.CloudEventExtensionKey.THIRD_ID
import com.tencent.devops.trigger.constant.EventBusMessageCode.SOURCE_NOT_SUPPORT
import com.tencent.devops.trigger.dao.EventBusDao
import com.tencent.devops.trigger.dao.EventBusRuleDao
import com.tencent.devops.trigger.dao.EventRouteDao
import com.tencent.devops.trigger.dao.EventRuleTargetDao
import com.tencent.devops.trigger.dispatcher.EventTriggerDispatcher
import com.tencent.devops.trigger.pojo.EventBusRule
import com.tencent.devops.trigger.pojo.event.EventAppWebhookRequestEvent
import com.tencent.devops.trigger.pojo.event.EventTargetRunEvent
import com.tencent.devops.trigger.pojo.event.EventWebhookRequestEvent
import com.tencent.devops.trigger.source.IEventSourceHandler
import com.tencent.devops.trigger.util.CloudEventJsonUtil
import com.tencent.devops.trigger.util.RuleUtil
import com.tencent.devops.trigger.util.TargetParamUtil
import io.cloudevents.CloudEvent
import io.cloudevents.http.HttpMessageFactory
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EventWebhookRequestService @Autowired constructor(
    private val dslContext: DSLContext,
    private val eventBusDao: EventBusDao,
    private val eventBusRuleDao: EventBusRuleDao,
    private val triggerRuleTargetDao: EventRuleTargetDao,
    private val eventTriggerDispatcher: EventTriggerDispatcher,
    private val eventRouteDao: EventRouteDao
) {

    companion object {
        private val logger = LoggerFactory.getLogger(EventWebhookRequestService::class.java)
    }

    fun webhook(event: EventWebhookRequestEvent) {
        with(event) {
            logger.info(
                "$projectId|$busId|start to handle event webhook request," +
                    "sourceName:$sourceName,payload:$payload,headers:$headers"
            )
            val cloudEvent = toCloudEvent(
                sourceName = sourceName,
                headers = headers,
                payload = payload
            )
            logger.info(
                "$projectId|$busId|toCloudEvent,source=${cloudEvent.source},type=${cloudEvent.type},id=${cloudEvent.id}"
            )
            handleWebhook(
                projectId = projectId,
                busId = busId,
                cloudEvent = cloudEvent
            )
        }
    }

    fun appWebhook(event: EventAppWebhookRequestEvent) {
        with(event) {
            logger.info(
                "start to handle event app webhook request,sourceName:$sourceName,payload:$payload,headers:$headers"
            )
            val cloudEvent = toCloudEvent(
                sourceName = sourceName,
                headers = headers,
                payload = payload
            )
            logger.info(
                "toCloudEvent,${cloudEvent.source},${cloudEvent.type},${cloudEvent.id}"
            )
            val thirdId = cloudEvent.getExtension(THIRD_ID)?.toString() ?: run {
                logger.warn("$sourceName| third id is null")
                return
            }
            val eventRouteList = eventRouteDao.listByThirdId(
                dslContext = dslContext,
                source = sourceName,
                thirdId = thirdId
            )
            if (eventRouteList.isEmpty()) {
                logger.warn("$sourceName|event route not found")
                return
            }
            eventRouteList.forEach { eventRoute ->
                handleWebhook(
                    projectId = eventRoute.projectId,
                    busId = eventRoute.busId,
                    cloudEvent = cloudEvent
                )
            }
        }
    }

    private fun handleWebhook(
        projectId: String,
        busId: String,
        cloudEvent: CloudEvent
    ) {
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
        if (ruleList.isEmpty()) {
            logger.info("$projectId|$busId|${cloudEvent.source}|${cloudEvent.type}| event rule is empty")
            return
        }
        val node = CloudEventJsonUtil.serializeAsJsonNode(event = cloudEvent)
        ruleList.forEach { rule ->
            matchAndDispatch(projectId, rule, node)
        }
    }

    private fun matchAndDispatch(
        projectId: String,
        rule: EventBusRule,
        node: JsonNode
    ) {
        try {
            if (RuleUtil.matches(
                    projectId = projectId,
                    ruleId = rule.ruleId,
                    node = node,
                    filterPattern = rule.filterPattern
                )
            ) {
                dispatchRuleTarget(
                    projectId = projectId,
                    ruleId = rule.ruleId,
                    node = node
                )
            }
        } catch (ignore: Throwable) {
            logger.warn("$projectId|${rule.ruleId}|match and dispatch error", ignore)
        }
    }

    private fun toCloudEvent(
        sourceName: String,
        headers: Map<String, String>,
        payload: String
    ): CloudEvent {
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
        val targetList = triggerRuleTargetDao.listByRuleId(
            dslContext = dslContext,
            projectId = projectId,
            ruleId = ruleId
        )
        if (targetList.isEmpty()) {
            logger.info("$projectId|$ruleId|event target is empty")
            return
        }
        val targetRunEvents = targetList.map { target ->
            val targetParamMap = TargetParamUtil.convert(
                projectId = projectId,
                node = node,
                targetParams = target.targetParams
            )
            EventTargetRunEvent(
                projectId = projectId,
                ruleId = ruleId,
                targetName = target.targetName,
                pushRetryStrategy = target.pushRetryStrategy,
                targetParamMap = targetParamMap
            )
        }.toList()
        targetRunEvents.forEach { event ->
            eventTriggerDispatcher.dispatch(event)
        }
    }
}
