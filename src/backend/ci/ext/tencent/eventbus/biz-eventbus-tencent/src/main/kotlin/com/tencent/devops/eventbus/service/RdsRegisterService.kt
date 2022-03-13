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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.MissingNode
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.api.util.ObjectReplaceEnvVarUtil
import com.tencent.devops.common.service.utils.SpringContextUtil
import com.tencent.devops.eventbus.constant.TargetType
import com.tencent.devops.eventbus.dao.EventBusDao
import com.tencent.devops.eventbus.dao.EventBusRuleDao
import com.tencent.devops.eventbus.dao.EventBusSourceDao
import com.tencent.devops.eventbus.dao.EventRuleExpressionDao
import com.tencent.devops.eventbus.dao.EventRuleTargetDao
import com.tencent.devops.eventbus.dao.EventSourceDao
import com.tencent.devops.eventbus.dao.EventSourceWebhookDao
import com.tencent.devops.eventbus.dao.EventTargetTemplateDao
import com.tencent.devops.eventbus.dao.EventTypeDao
import com.tencent.devops.eventbus.pojo.EventBus
import com.tencent.devops.eventbus.pojo.EventBusRule
import com.tencent.devops.eventbus.pojo.EventBusSource
import com.tencent.devops.eventbus.pojo.EventRuleTarget
import com.tencent.devops.eventbus.pojo.EventSource
import com.tencent.devops.eventbus.pojo.EventType
import com.tencent.devops.eventbus.pojo.TriggerOn
import com.tencent.devops.eventbus.pojo.TriggerRegisterRequest
import com.tencent.devops.eventbus.pojo.TriggerResource
import com.tencent.devops.eventbus.source.IEventSourceHandler
import com.tencent.devops.eventbus.util.IdGeneratorUtil
import com.tencent.devops.eventbus.util.TargetParamUtil
import io.appform.jsonrules.Expression
import io.appform.jsonrules.expressions.composite.AndExpression
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class RdsRegisterService @Autowired constructor(
    private val dslContext: DSLContext,
    private val eventTypeDao: EventTypeDao,
    private val eventSourceDao: EventSourceDao,
    private val eventSourceWebhookDao: EventSourceWebhookDao,
    private val eventRuleExpressionDao: EventRuleExpressionDao,
    private val eventTargetTemplateDao: EventTargetTemplateDao,
    private val eventBusDao: EventBusDao,
    private val eventBusSourceDao: EventBusSourceDao,
    private val eventBusRuleDao: EventBusRuleDao,
    private val eventRuleTargetDao: EventRuleTargetDao,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(RdsRegisterService::class.java)
        private const val RDS_EVENT_BUS_NAME = "rds"

        // 类型字段名
        private const val TYPE_FILTER_NAME = "type"
    }

    @Value("\${eventbus.webhookUrl}")
    private val eventBusWebhookUrl = ""

    fun register(
        userId: String,
        projectId: String,
        request: TriggerRegisterRequest
    ) {
        val busId = eventBusDao.getByName(
            dslContext = dslContext,
            projectId = projectId,
            name = RDS_EVENT_BUS_NAME
        )?.busId ?: IdGeneratorUtil.getBusId()

        val eventBusSourceSet = mutableSetOf<EventBusSource>()
        val eventBusRuleSet = mutableSetOf<EventBusRule>()
        val ruleTargetSet = mutableSetOf<EventRuleTarget>()
        request.triggerOn.forEach on@{ on ->
            val eventTypeList = eventTypeDao.listByAliasName(
                dslContext = dslContext,
                aliasName = on.id
            )
            if (eventTypeList.isEmpty()) {
                logger.info("${on.id} event type not exist")
                return@on
            }
            eventTypeList.forEach eventType@{ eventType ->
                val eventSource = eventSourceDao.get(
                    dslContext = dslContext,
                    id = eventType.sourceId
                ) ?: return@eventType
                eventBusSourceSet.add(
                    EventBusSource(
                        busId = busId,
                        projectId = projectId,
                        name = eventSource.name,
                        creator = userId,
                        updater = userId
                    )
                )
                val webhookProp = registerWebhook(
                    projectId = projectId,
                    busId = busId,
                    sourceId = eventType.sourceId,
                    eventTypeId = eventType.id!!,
                    sourceName = eventSource.name,
                    triggerResource = request.triggerResource
                )
                if (webhookProp == null || webhookProp.second.isEmpty()) {
                    logger.info("Failed to register webhook with source($eventSource.name)")
                    return@eventType
                }
                val ruleId = IdGeneratorUtil.getRuleId()
                eventBusRuleSet.add(
                    getEventBusRule(
                        projectId = projectId,
                        userId = userId,
                        busId = busId,
                        ruleId = ruleId,
                        eventType = eventType,
                        eventSource = eventSource,
                        on = on,
                        webhookProp = webhookProp
                    )
                )
                val eventRuleTarget = getEventRuleTarget(
                    sourceId = eventType.sourceId,
                    eventTypeId = eventType.id!!,
                    ruleId = ruleId,
                    projectId = projectId,
                    userId = userId,
                    on = on
                ) ?: return@eventType
                ruleTargetSet.add(eventRuleTarget)
            }
        }
        dslContext.transaction { configuration ->
            val context = DSL.using(configuration)
            eventBusDao.create(
                dslContext = context,
                eventBus = EventBus(
                    busId = busId,
                    projectId = projectId,
                    name = RDS_EVENT_BUS_NAME,
                    creator = userId,
                    updater = userId,
                )
            )

            eventBusSourceDao.batchCreate(
                dslContext = context,
                eventBusSources = eventBusSourceSet.toList()
            )
            eventBusRuleDao.batchCreate(
                dslContext = context,
                eventBusRules = eventBusRuleSet.toList()
            )
            eventRuleTargetDao.batchCreate(
                dslContext = context,
                eventRuleTargets = ruleTargetSet.toList()
            )
        }
    }

    private fun registerWebhook(
        projectId: String,
        busId: String,
        sourceId: Long,
        eventTypeId: Long,
        sourceName: String,
        triggerResource: List<TriggerResource>
    ): Pair<String, List<String>>? {
        val eventSourceWebhook = eventSourceWebhookDao.get(
            dslContext = dslContext,
            sourceId = sourceId,
            eventTypeId = eventTypeId
        ) ?: run {
            logger.info("$sourceName does not need webhook registration")
            return null
        }
        val successPropValueList = mutableListOf<String>()
        val propName = eventSourceWebhook.propName
        triggerResource.forEach { resource ->
            resource.resources[propName]?.forEach { propValue ->
                val webhookParamMap =
                    TargetParamUtil.convert(MissingNode.getInstance(), eventSourceWebhook.webhookParams).toMutableMap()
                val webhookUrl = "$eventBusWebhookUrl/$sourceName/$projectId/$busId"
                val eventsourceHandler = SpringContextUtil.getBean(IEventSourceHandler::class.java, sourceName)
                if (eventsourceHandler.registerWebhook(webhookUrl, webhookParamMap.plus(propName to propValue!!))) {
                    successPropValueList.add(propValue)
                }
            }
        }
        return Pair(propName, successPropValueList)
    }

    private fun getEventBusRule(
        projectId: String,
        userId: String,
        busId: String,
        ruleId: String,
        eventType: EventType,
        eventSource: EventSource,
        on: TriggerOn,
        webhookProp: Pair<String, List<String>>
    ): EventBusRule {
        val filterNames = on.filter.keys.toMutableList()
        filterNames.add(TYPE_FILTER_NAME)
        filterNames.add(webhookProp.first)
        val eventRuleExpressions = eventRuleExpressionDao.getByFilterNames(
            dslContext = dslContext,
            sourceId = eventType.sourceId,
            eventTypeId = eventType.id!!,
            filterNames = filterNames
        )

        val filter = on.filter.toMutableMap()
        filter[webhookProp.first] = webhookProp.second

        val ruleExpressionList = eventRuleExpressions.map { ruleExpression ->
            val replaceExpression = ObjectReplaceEnvVarUtil.replaceEnvVar(
                ruleExpression.expressions,
                filter.map { (key, value) -> Pair(key, JsonUtil.toJson(value)) }.toMap()
            )
            JsonUtil.to(replaceExpression.toString(), object : TypeReference<List<Expression>>() {})
        }.flatten()
        return EventBusRule(
            ruleId = ruleId,
            busId = busId,
            projectId = projectId,
            name = "${eventSource.name}_${eventType.aliasName}",
            source = eventSource.name,
            type = eventType.name,
            filterPattern = JsonUtil.toJson(AndExpression.builder().children(ruleExpressionList).build()),
            creator = userId,
            updater = userId
        )
    }

    private fun getEventRuleTarget(
        sourceId: Long,
        eventTypeId: Long,
        ruleId: String,
        projectId: String,
        userId: String,
        on: TriggerOn
    ): EventRuleTarget? {
        val eventTargetTemplate = eventTargetTemplateDao.getByTargetName(
            dslContext = dslContext,
            sourceId = sourceId,
            eventTypeId = eventTypeId,
            targetName = TargetType.PIPELINE
        ) ?: return null
        val replaceTargetParams = ObjectReplaceEnvVarUtil.replaceEnvVar(
            eventTargetTemplate.targetParams,
            mapOf(
                "projectId" to projectId,
                "pipelineId" to on.action.pipelineId
            )
        )
        return EventRuleTarget(
            targetId = IdGeneratorUtil.getTargetId(),
            ruleId = ruleId,
            projectId = projectId,
            targetName = eventTargetTemplate.targetName,
            pushRetryStrategy = eventTargetTemplate.pushRetryStrategy,
            targetParams = JsonUtil.toJson(replaceTargetParams),
            creator = userId,
            updater = userId
        )
    }
}
