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

package com.tencent.devops.rds.chart

import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.pipeline.Model
import com.tencent.devops.common.pipeline.container.Stage
import com.tencent.devops.common.pipeline.container.TriggerContainer
import com.tencent.devops.common.pipeline.enums.BuildFormPropertyType
import com.tencent.devops.common.pipeline.enums.ChannelCode
import com.tencent.devops.common.pipeline.pojo.BuildFormProperty
import com.tencent.devops.common.pipeline.pojo.element.trigger.ManualTriggerElement
import com.tencent.devops.process.api.service.ServicePipelineResource
import com.tencent.devops.process.engine.common.VMUtils
import com.tencent.devops.rds.constants.Constants.RDS_DOCKER_REGISTRY
import com.tencent.devops.rds.constants.Constants.RDS_HELM_REGISTRY
import com.tencent.devops.rds.constants.Constants.RDS_HELM_VALUES_URL
import com.tencent.devops.rds.constants.Constants.RDS_PRODUCT_CODE
import com.tencent.devops.rds.constants.Constants.RDS_PRODUCT_DISPLAY_NAME
import com.tencent.devops.rds.dao.RdsChartPipelineDao
import com.tencent.devops.rds.dao.RdsProductInfoDao
import com.tencent.devops.rds.exception.ChartErrorCodeEnum
import com.tencent.devops.rds.pojo.RdsChartPipelineInfo
import com.tencent.devops.rds.pojo.RdsPipelineCreate
import com.tencent.devops.rds.utils.RdsPipelineUtils
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ChartPipeline @Autowired constructor(
    private val client: Client,
    private val dslContext: DSLContext,
    private val chartPipelineDao: RdsChartPipelineDao,
    private val streamConverter: StreamConverter,
    private val productInfoDao: RdsProductInfoDao
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ChartPipeline::class.java)
    }

    fun createDefaultPipeline(
        userId: String,
        productCode: String,
        projectId: String,
        pipeline: RdsPipelineCreate,
        initPipeline: Boolean? = false
    ): String {
        val result = try {
            client.get(ServicePipelineResource::class).create(
                userId = userId,
                projectId = projectId,
                pipeline = Model(
                    name = RdsPipelineUtils.genBKPipelineName(
                        pipeline.filePath, pipeline.projectName, pipeline.serviceName
                    ),
                    desc = "",
                    stages = listOf(
                        Stage(
                            id = VMUtils.genStageId(1),
                            name = VMUtils.genStageId(1),
                            containers = listOf(
                                TriggerContainer(
                                    id = "0",
                                    name = "构建触发",
                                    elements = listOf(
                                        ManualTriggerElement(
                                            name = "手动触发",
                                            id = "T-1-1-1"
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                channelCode = ChannelCode.BS
            ).data ?: run {
                logger.warn("RDS|PIPELINE_CREATE_ERROR|pipeline=$pipeline|model=${pipeline.yamlObject}")
                throw ErrorCodeException(
                    errorCode = ChartErrorCodeEnum.CREATE_CHART_PIPELINE_ERROR.errorCode,
                    defaultMessage = ChartErrorCodeEnum.CREATE_CHART_PIPELINE_ERROR.formatErrorMessage
                        .format(pipeline.filePath)
                )
            }
        } catch (t: Throwable) {
            logger.error("RDS|PIPELINE_CREATE_ERROR|pipeline=$pipeline|model=${pipeline.yamlObject}", t)
            throw ErrorCodeException(
                errorCode = ChartErrorCodeEnum.CREATE_CHART_PIPELINE_ERROR.errorCode,
                defaultMessage = ChartErrorCodeEnum.CREATE_CHART_PIPELINE_ERROR.formatErrorMessage
                    .format(pipeline.filePath)
            )
        }

        // 根据返回结果保存
        chartPipelineDao.createPipeline(
            dslContext = dslContext,
            pipeline = RdsChartPipelineInfo(
                pipelineId = result.id,
                productCode = productCode,
                filePath = pipeline.filePath,
                projectName = pipeline.projectName,
                serviceName = pipeline.serviceName,
                originYaml = pipeline.originYaml,
                parsedYaml = pipeline.parsedYaml
            ),
            initPipeline = initPipeline
        )
        return result.id
    }

    fun updateChartPipeline(
        userId: String,
        pipelineId: String,
        productCode: String,
        displayName: String,
        projectId: String,
        pipeline: RdsPipelineCreate,
        helmRegistry: String?,
        dockerRegistry: String?,
        helmValuesUrl: String?,
        initPipeline: Boolean? = false
    ) {
        val startParams = mutableListOf<BuildFormProperty>()
        startParams.add(
            BuildFormProperty(
                RDS_PRODUCT_CODE, false, BuildFormPropertyType.STRING, productCode, null, null,
                null, null, null, null, null, null
            )
        )
        startParams.add(
            BuildFormProperty(
                RDS_PRODUCT_DISPLAY_NAME, false, BuildFormPropertyType.STRING, displayName, null, null,
                null, null, null, null, null, null
            )
        )
        helmRegistry?.let {
            startParams.add(
                BuildFormProperty(
                    RDS_HELM_REGISTRY, false, BuildFormPropertyType.STRING, it, null, null,
                    null, null, null, null, null, null
                )
            )
        }
        helmValuesUrl?.let {
            startParams.add(
                BuildFormProperty(
                    RDS_HELM_VALUES_URL, false, BuildFormPropertyType.STRING, it, null, null,
                    null, null, null, null, null, null
                )
            )
        }
        dockerRegistry?.let {
            startParams.add(
                BuildFormProperty(
                    RDS_DOCKER_REGISTRY, false, BuildFormPropertyType.STRING, it, null, null,
                    null, null, null, null, null, null
                )
            )
        }
        try {
            val model = streamConverter.getYamlModel(
                userId = userId,
                productCode = productCode,
                projectId = projectId,
                pipelineId = pipelineId,
                pipelineName = RdsPipelineUtils.genBKPipelineName(
                    pipeline.filePath, pipeline.projectName, pipeline.serviceName
                ),
                yamlObject = pipeline.yamlObject,
                init = initPipeline
            )

            val triggerContainer = model.stages[0].containers[0] as TriggerContainer
            triggerContainer.params = triggerContainer.params.plus(startParams)

            val success = client.get(ServicePipelineResource::class).edit(
                userId = userId,
                projectId = projectId,
                pipelineId = pipelineId,
                pipeline = model,
                channelCode = ChannelCode.BS
            ).data
            if (success != true) {
                logger.warn("RDS|PIPELINE_CREATE_ERROR|pipeline=$pipeline|model=${pipeline.yamlObject}")
            }
        } catch (t: Throwable) {
            logger.error("RDS|PIPELINE_CREATE_ERROR|pipeline=$pipeline|model=${pipeline.yamlObject}", t)
            throw ErrorCodeException(
                errorCode = ChartErrorCodeEnum.UPDATE_CHART_PIPELINE_ERROR.errorCode,
                defaultMessage = ChartErrorCodeEnum.UPDATE_CHART_PIPELINE_ERROR.formatErrorMessage
                    .format(pipeline.filePath)
            )
        }

        // 根据返回结果保存
        chartPipelineDao.updatePipeline(
            dslContext = dslContext,
            pipeline = RdsChartPipelineInfo(
                pipelineId = pipelineId,
                productCode = productCode,
                filePath = pipeline.filePath,
                projectName = pipeline.projectName,
                serviceName = pipeline.serviceName,
                originYaml = pipeline.originYaml,
                parsedYaml = pipeline.parsedYaml
            )
        )
    }

    fun getProductPipelines(productCode: String): List<RdsChartPipelineInfo> {
        return chartPipelineDao.getChartPipelines(dslContext, productCode)
    }

    fun getProductPipelineByService(
        productCode: String,
        filePath: String,
        projectName: String?,
        serviceName: String?
    ): RdsChartPipelineInfo? {
        return chartPipelineDao.getProductPipelineByService(
            dslContext = dslContext,
            productCode = productCode,
            filePath = filePath,
            projectName = projectName,
            serviceName = serviceName
        )
    }
}
