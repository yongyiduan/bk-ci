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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.MissingNode
import com.tencent.devops.common.api.util.EnvUtils
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.api.util.ObjectReplaceEnvVarUtil
import com.tencent.devops.common.service.utils.SpringContextUtil
import com.tencent.devops.trigger.constant.TargetType
import com.tencent.devops.trigger.dao.EventBusDao
import com.tencent.devops.trigger.dao.EventBusRuleDao
import com.tencent.devops.trigger.dao.EventBusSourceDao
import com.tencent.devops.trigger.dao.EventRouteDao
import com.tencent.devops.trigger.dao.EventRuleExpressionDao
import com.tencent.devops.trigger.dao.EventRuleTargetDao
import com.tencent.devops.trigger.dao.EventSourceDao
import com.tencent.devops.trigger.dao.EventSourceWebhookDao
import com.tencent.devops.trigger.dao.EventTargetTemplateDao
import com.tencent.devops.trigger.dao.EventTypeDao
import com.tencent.devops.trigger.pojo.EventBus
import com.tencent.devops.trigger.pojo.EventBusRule
import com.tencent.devops.trigger.pojo.EventBusSource
import com.tencent.devops.trigger.pojo.EventRoute
import com.tencent.devops.trigger.pojo.EventRuleTarget
import com.tencent.devops.trigger.pojo.EventSource
import com.tencent.devops.trigger.pojo.EventType
import com.tencent.devops.trigger.pojo.RegisterWebhookResult
import com.tencent.devops.trigger.pojo.TriggerOn
import com.tencent.devops.trigger.pojo.TriggerRegisterRequest
import com.tencent.devops.trigger.pojo.TriggerResource
import com.tencent.devops.trigger.source.IEventSourceHandler
import com.tencent.devops.trigger.util.IdGeneratorUtil
import com.tencent.devops.trigger.util.TargetParamUtil
import io.appform.jsonrules.Expression
import io.appform.jsonrules.expressions.composite.AndExpression
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@SuppressWarnings("LongParameterList")
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
    private val eventRouteDao: EventRouteDao
) {

    companion object {
        private val logger = LoggerFactory.getLogger(RdsRegisterService::class.java)
        private const val RDS_EVENT_BUS_NAME = "rds"

        // 类型字段名
        private const val TYPE_FILTER_NAME = "type"
    }

    @Value("\${trigger.webhookUrl}")
    private val eventBusWebhookUrl = ""

    @SuppressWarnings("LongMethod", "ReturnCount")
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
        val eventRouteSet = mutableSetOf<EventRoute>()
        request.triggerOn.forEach on@{ on ->
            logger.info("$projectId|trigger ${on.id} event type")
            val eventTypeList = eventTypeDao.listByAliasName(
                dslContext = dslContext,
                aliasName = on.id
            )
            if (eventTypeList.isEmpty()) {
                logger.info("$projectId|${on.id} event type not exist")
                return@on
            }
            eventTypeList.forEach eventType@{ eventType ->
                val eventSource = eventSourceDao.get(
                    dslContext = dslContext,
                    id = eventType.sourceId
                ) ?: run {
                    logger.info("$projectId|${eventType.sourceId} event source not exist")
                    return@eventType
                }
                eventBusSourceSet.add(
                    EventBusSource(
                        busId = busId,
                        projectId = projectId,
                        name = eventSource.name,
                        creator = userId,
                        updater = userId
                    )
                )
                val webhookResults = registerWebhook(
                    projectId = projectId,
                    busId = busId,
                    sourceId = eventType.sourceId,
                    eventTypeId = eventType.id!!,
                    sourceName = eventSource.name,
                    triggerResource = request.triggerResource
                )
                if (webhookResults.isEmpty()) {
                    logger.info("$projectId|${eventSource.name}|${eventType.name}| does not need webhook registration")
                    return@eventType
                }
                webhookResults.forEach webhook@{ result ->
                    eventRouteSet.add(
                        EventRoute(
                            source = eventSource.name,
                            thirdId = result.resourceValue,
                            projectId = projectId,
                            busId = busId
                        )
                    )
                    val ruleId = eventBusRuleDao.getByName(
                        dslContext = dslContext,
                        projectId = projectId,
                        busId = busId,
                        name = "${result.id}:${result.resourceKey}"
                    )?.ruleId ?: IdGeneratorUtil.getRuleId()
                    eventBusRuleSet.add(
                        getEventBusRule(
                            projectId = projectId,
                            userId = userId,
                            busId = busId,
                            ruleId = ruleId,
                            eventType = eventType,
                            eventSource = eventSource,
                            on = on,
                            webhookResult = result
                        )
                    )
                    val pipelineKey = "${result.id}:${on.action.path}"
                    val pipelineId = request.triggerPipelines[pipelineKey] ?: return@webhook
                    val eventRuleTarget = getEventRuleTarget(
                        sourceId = eventType.sourceId,
                        eventTypeId = eventType.id!!,
                        ruleId = ruleId,
                        projectId = projectId,
                        busId = busId,
                        userId = userId,
                        on = on,
                        pipelineId = pipelineId
                    ) ?: return@webhook
                    ruleTargetSet.add(eventRuleTarget)
                }
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
            eventRouteDao.batchCreate(
                dslContext = dslContext,
                eventRoutes = eventRouteSet.toList()
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
    ): List<RegisterWebhookResult> {
        val eventSourceWebhook = eventSourceWebhookDao.get(
            dslContext = dslContext,
            sourceId = sourceId,
            eventTypeId = eventTypeId
        ) ?: return emptyList()
        val registerWebhookResults = mutableListOf<RegisterWebhookResult>()
        val propName = eventSourceWebhook.propName
        triggerResource.forEach { resource ->
            val propValue = resource.resources[propName] ?: return@forEach
            val webhookParamMap =
                TargetParamUtil.convert(MissingNode.getInstance(), eventSourceWebhook.webhookParams).toMutableMap()
            val webhookUrl = "$eventBusWebhookUrl/$sourceName/$projectId/$busId"
            val eventsourceHandler = SpringContextUtil.getBean(IEventSourceHandler::class.java, sourceName)
            if (eventsourceHandler.registerWebhook(webhookUrl, webhookParamMap.plus(propName to propValue))) {
                registerWebhookResults.add(
                    RegisterWebhookResult(
                        id = resource.id,
                        resourceKey = propName,
                        resourceValue = propValue
                    )
                )
            }
        }
        logger.info(
            "$projectId|$busId|$sourceName|Success to register webhook resource ($registerWebhookResults)"
        )
        return registerWebhookResults
    }

    private fun getEventBusRule(
        projectId: String,
        userId: String,
        busId: String,
        ruleId: String,
        eventType: EventType,
        eventSource: EventSource,
        on: TriggerOn,
        webhookResult: RegisterWebhookResult
    ): EventBusRule {
        val filterNames = on.filter.keys.toMutableList()
        filterNames.add(TYPE_FILTER_NAME)
        filterNames.add(webhookResult.resourceKey)
        val eventRuleExpressions = eventRuleExpressionDao.getByFilterNames(
            dslContext = dslContext,
            sourceId = eventType.sourceId,
            eventTypeId = eventType.id!!,
            filterNames = filterNames
        )

        val filter = on.filter.toMutableMap()
        filter[webhookResult.resourceKey] = webhookResult.resourceValue

        logger.info("$projectId|$busId|$ruleId|filter conditions: $filter")
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
            name = "${webhookResult.id}:${webhookResult.resourceKey}",
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
        busId: String,
        userId: String,
        on: TriggerOn,
        pipelineId: String
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
                "pipelineId" to pipelineId
            )
        )
        /*val targetParamList = JsonUtil.to(replaceTargetParams, object : TypeReference<MutableList<TargetParam>>() {})
        if (!on.action.variables.isNullOrEmpty()) {
            val customTargetParam = TargetParam(
                resourceKey = "customParams",
                form = TargetFormType.CONSTANT,
                value = on.action.variables
            )
            targetParamList.add(customTargetParam)
        }*/

        val targetId = eventRuleTargetDao.getByTargetName(
            dslContext = dslContext,
            projectId = projectId,
            ruleId = ruleId,
            targetName = eventTargetTemplate.targetName
        )?.targetId ?: IdGeneratorUtil.getTargetId()
        return EventRuleTarget(
            targetId = targetId,
            ruleId = ruleId,
            projectId = projectId,
            busId = busId,
            targetName = eventTargetTemplate.targetName,
            pushRetryStrategy = eventTargetTemplate.pushRetryStrategy,
            targetParams = JsonUtil.toJson(replaceTargetParams),
            creator = userId,
            updater = userId
        )
    }
}
