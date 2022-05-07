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
import com.tencent.devops.common.pipeline.enums.ChannelCode
import com.tencent.devops.process.api.service.ServicePipelineResource
import com.tencent.devops.rds.dao.RdsChartPipelineDao
import com.tencent.devops.rds.dao.RdsProductInfoDao
import com.tencent.devops.rds.exception.ChartErrorCodeEnum
import com.tencent.devops.rds.pojo.RdsChartPipelineInfo
import com.tencent.devops.rds.pojo.RdsPipelineCreate
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ChartPipeline @Autowired constructor(
    private val client: Client,
    private val dslContext: DSLContext,
    private val chartPipelineDao: RdsChartPipelineDao,
    private val productInfoDao: RdsProductInfoDao
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ChartPipeline::class.java)
    }

    fun createChartPipeline(
        userId: String,
        productId: Long,
        projectId: String,
        pipeline: RdsPipelineCreate,
        initPipeline: Boolean? = false
    ): String {
        val result = try {
            client.get(ServicePipelineResource::class).create(
                userId = userId,
                projectId = projectId,
                pipeline = pipeline.model,
                channelCode = ChannelCode.BS
            ).data ?: run {
                logger.warn("RDS|PIPELINE_CREATE_ERROR|pipeline=$pipeline|model=${pipeline.model}")
                throw ErrorCodeException(
                    errorCode = ChartErrorCodeEnum.CREATE_CHART_PIPELINE_ERROR.errorCode,
                    defaultMessage = ChartErrorCodeEnum.CREATE_CHART_PIPELINE_ERROR.formatErrorMessage,
                    params = arrayOf(pipeline.filePath)
                )
            }
        } catch (t: Throwable) {
            logger.error("RDS|PIPELINE_CREATE_ERROR|pipeline=$pipeline|model=${pipeline.model}", t)
            throw ErrorCodeException(
                errorCode = ChartErrorCodeEnum.CREATE_CHART_PIPELINE_ERROR.errorCode,
                defaultMessage = ChartErrorCodeEnum.CREATE_CHART_PIPELINE_ERROR.formatErrorMessage,
                params = arrayOf(pipeline.filePath)
            )
        }

        // 根据返回结果保存
        chartPipelineDao.createPipeline(
            dslContext = dslContext,
            pipeline = RdsChartPipelineInfo(
                pipelineId = result.id,
                productId = productId,
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
        productId: Long,
        projectId: String,
        pipeline: RdsPipelineCreate
    ) {
        try {
            val success = client.get(ServicePipelineResource::class).edit(
                userId = userId,
                projectId = projectId,
                pipelineId = pipelineId,
                pipeline = pipeline.model,
                channelCode = ChannelCode.BS
            ).data
            if (success != true) {
                logger.warn("RDS|PIPELINE_CREATE_ERROR|pipeline=$pipeline|model=${pipeline.model}")
            }
        } catch (t: Throwable) {
            logger.error("RDS|PIPELINE_CREATE_ERROR|pipeline=$pipeline|model=${pipeline.model}", t)
            throw ErrorCodeException(
                errorCode = ChartErrorCodeEnum.UPDATE_CHART_PIPELINE_ERROR.errorCode,
                defaultMessage = ChartErrorCodeEnum.UPDATE_CHART_PIPELINE_ERROR.formatErrorMessage,
                params = arrayOf(pipeline.filePath)
            )
        }

        // 根据返回结果保存
        chartPipelineDao.updatePipeline(
            dslContext = dslContext,
            pipeline = RdsChartPipelineInfo(
                pipelineId = pipelineId,
                productId = productId,
                filePath = pipeline.filePath,
                projectName = pipeline.projectName,
                serviceName = pipeline.serviceName,
                originYaml = pipeline.originYaml,
                parsedYaml = pipeline.parsedYaml
            )
        )
    }

    fun getProductPipelines(productId: Long): List<RdsChartPipelineInfo> {
        return chartPipelineDao.getChartPipelines(dslContext, productId)
    }

    fun getProductPipelineByService(
        productId: Long,
        filePath: String,
        projectName: String?,
        serviceName: String?
    ): RdsChartPipelineInfo? {
        return chartPipelineDao.getProductPipelineByService(
            dslContext = dslContext,
            productId = productId,
            filePath = filePath,
            projectName = projectName,
            serviceName = serviceName
        )
    }
}
