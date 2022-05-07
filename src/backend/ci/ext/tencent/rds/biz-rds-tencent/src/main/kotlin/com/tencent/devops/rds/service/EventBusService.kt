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

package com.tencent.devops.rds.service

import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.client.Client
import com.tencent.devops.rds.dao.RdsChartPipelineDao
import com.tencent.devops.rds.exception.ChartErrorCodeEnum
import com.tencent.devops.rds.pojo.yaml.Main
import com.tencent.devops.rds.pojo.yaml.Resource
import com.tencent.devops.trigger.api.ServiceEventRegisterResource
import com.tencent.devops.trigger.pojo.TriggerAction
import com.tencent.devops.trigger.pojo.TriggerOn
import com.tencent.devops.trigger.pojo.TriggerOnRule
import com.tencent.devops.trigger.pojo.TriggerRegisterRequest
import com.tencent.devops.trigger.pojo.TriggerResource
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EventBusService @Autowired constructor(
    private val client: Client,
    private val dslContext: DSLContext,
    private val chartPipelineDao: RdsChartPipelineDao
) {

    private val logger = LoggerFactory.getLogger(EventBusService::class.java)

    fun addEventBusWebhook(
        userId: String,
        productId: Long,
        projectId: String,
        main: Main,
        resource: Resource
    ): Boolean {
        val triggerOns = generateTriggerOn(main)
        val triggerResources = generateTriggerResource(resource)
        val triggerPipelines = chartPipelineDao.getChartPipelines(dslContext, productId).associate { pipeline ->
            if (pipeline.serviceName.isNullOrBlank()) {
                "${pipeline.projectName}:${pipeline.filePath}" to pipeline.pipelineId
            } else {
                "${pipeline.projectName}:${pipeline.serviceName}:${pipeline.filePath}" to pipeline.pipelineId
            }
        }
        logger.info(
            "RDS|EVENT_BUS_REGISTER|triggerOns=$triggerOns|triggerResources=$triggerResources|" +
                "triggerPipelines=$triggerPipelines"
        )
        return try {
            client.get(ServiceEventRegisterResource::class).register(
                userId = userId,
                projectId = projectId,
                request = TriggerRegisterRequest(
                    triggerOn = triggerOns,
                    triggerResource = triggerResources,
                    triggerPipelines = triggerPipelines
                )
            ).data ?: run {
                logger.warn("RDS|EVENT_BUS_ERROR|triggerOns=$triggerOns|triggerResources=$triggerResources")
                throw ErrorCodeException(
                    errorCode = ChartErrorCodeEnum.CREATE_CHART_EVENT_BUS_ERROR.errorCode,
                    defaultMessage = ChartErrorCodeEnum.CREATE_CHART_EVENT_BUS_ERROR.formatErrorMessage
                )
            }
        } catch (t: Throwable) {
            logger.error("RDS|EVENT_BUS_ERROR|triggerOns=$triggerOns|triggerResources=$triggerResources", t)
            throw ErrorCodeException(
                errorCode = ChartErrorCodeEnum.CREATE_CHART_EVENT_BUS_ERROR.errorCode,
                defaultMessage = ChartErrorCodeEnum.CREATE_CHART_EVENT_BUS_ERROR.formatErrorMessage
            )
        }
    }

    private fun generateTriggerResource(
        resource: Resource
    ): List<TriggerResource> {
        val triggerResources = mutableListOf<TriggerResource>()
        resource.projects.forEach { project ->
            project.services?.forEach nextService@{ service ->
                val map = service.getServiceResource()
                triggerResources.add(
                    TriggerResource(
                        id = "${project.id}:${service.id}",
                        resources = map
                    )
                )
            } ?: run {
                val map = project.getProjectResource()
                triggerResources.add(
                    TriggerResource(
                        id = project.id,
                        resources = map
                    )
                )
            }
        }
        return triggerResources
    }

    private fun generateTriggerOn(
        main: Main
    ): List<TriggerOn> {
        return main.on.map { on ->
            TriggerOn(
                id = on.id,
                rules = on.rules.map { rule ->
                    TriggerOnRule(
                        filter = rule.filter,
                        action = rule.action.map { TriggerAction(
                            type = it.type,
                            path = it.path,
                            variables = it.with
                        ) }
                    )
                }.toList()
            )
        }.toList()
    }
}
