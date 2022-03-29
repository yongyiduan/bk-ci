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
import com.tencent.devops.trigger.pojo.PipelineAction
import com.tencent.devops.trigger.pojo.TriggerOn
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

    fun addWebhook(
        userId: String,
        productId: Long,
        projectId: String,
        main: Main,
        resource: Resource
    ): Boolean {
        // 取出已经注册成功的流水线
        val triggerOns = mutableListOf<TriggerOn>()
        val triggerResources = mutableListOf<TriggerResource>()
        val pathAndOn = main.on.map {
            Pair(it.action.path, it)
        }.toList()
        val pipelines = chartPipelineDao.getChartPipelines(dslContext, productId)
        pipelines.forEach nextPipeline@{ pipeline ->
            // 使用文件名匹配
            pathAndOn.forEach nextTrigger@{ (path, on) ->
                if (path != pipeline.filePath) return@nextTrigger
                triggerOns.add(TriggerOn(
                    id = on.id,
                    filter = on.filter,
                    action = PipelineAction(
                        type = on.action.type,
                        pipelineId = pipeline.pipelineId,
                        variables = on.action.with
                    )
                ))
            }
        }
        resource.projects.forEach { project ->
            val map = mutableMapOf<String, MutableList<String>>()
            project.tapdId?.let { map["tapd_id"] = mutableListOf(it) }
            project.bcsId?.let { map["bcs_id"] = mutableListOf(it) }
            project.repoUrl?.let { map["repo_url"] = mutableListOf(it) }
            project.services?.forEach { service ->
                map["repo_url"]?.add(service.repoUrl) ?: run {
                    map["repo_url"] = mutableListOf(service.repoUrl)
                }
            }
            triggerResources.add(TriggerResource(
                id = project.id,
                resources = map
            ))
        }
        logger.info("RDS|EVENT_BUS_REGISTER|triggerOns=$triggerOns|triggerResources=$triggerResources")
        return try {
            client.get(ServiceEventRegisterResource::class).register(
                userId = userId,
                projectId = projectId,
                request = TriggerRegisterRequest(
                    triggerOn = triggerOns,
                    triggerResource = triggerResources
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
}
