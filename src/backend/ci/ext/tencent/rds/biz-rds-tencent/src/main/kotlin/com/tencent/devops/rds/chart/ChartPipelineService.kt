/*
 *
 *  * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *  *
 *  * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *  *
 *  * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *  *
 *  * A copy of the MIT License is included in this file.
 *  *
 *  *
 *  * Terms of the MIT License:
 *  * ---------------------------------------------------
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 *  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *  * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 *  * the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 *  * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 *  * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.tencent.devops.rds.chart

import com.tencent.devops.common.api.exception.CustomException
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.pipeline.Model
import com.tencent.devops.common.pipeline.enums.ChannelCode
import com.tencent.devops.process.api.service.ServicePipelineResource
import com.tencent.devops.rds.RdsPipelineUtils
import com.tencent.devops.rds.dao.RdsChartPipelineDao
import com.tencent.devops.rds.dao.RdsProductInfoDao
import com.tencent.devops.rds.pojo.RdsChartPipelineInfo
import com.tencent.devops.rds.pojo.RdsPipelineCreate
import javax.ws.rs.core.Response
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ChartPipelineService @Autowired constructor(
    private val client: Client,
    private val dslContext: DSLContext,
    private val chartPipelineDao: RdsChartPipelineDao,
    private val productInfoDao: RdsProductInfoDao
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ChartPipelineService::class.java)
    }

    fun createChartPipelines(
        userId: String,
        productId: String,
        chartPipelines: List<Pair<RdsPipelineCreate, Model>>
    ) {
        val productInfo = productInfoDao.getProduct(dslContext, productId)
            ?: throw CustomException(
                Response.Status.FORBIDDEN,
                "RDS项目不存在或已删除，如有疑问请联系蓝盾助手"
            )

        chartPipelines.forEach { pipelineAndModel ->
            val pipeline = pipelineAndModel.first
            val model = pipelineAndModel.second

            // 在蓝盾引擎创建项目
            val result = try {
                client.get(ServicePipelineResource::class).create(
                    userId = userId,
                    projectId = RdsPipelineUtils.genBKProjectCode(productInfo.id),
                    pipeline = model,
                    channelCode = ChannelCode.GIT
                ).data ?: run {
                    logger.warn("RDS|PIPELINE_CREATE_ERROR|pipeline=$pipeline|model=$model")
                    return@forEach
                }
            } catch (t: Throwable) {
                logger.error("RDS|PIPELINE_CREATE_ERROR|pipeline=$pipeline|model=$model", t)
                return@forEach
            }

            // 根据返回结果保存
            chartPipelineDao.create(
                dslContext = dslContext,
                pipeline = RdsChartPipelineInfo(
                    pipelineId = result.id,
                    productId = productId,
                    filePath = pipeline.filePath,
                    originYaml = pipeline.originYaml,
                    parsedYaml = pipeline.parsedYaml
                )
            )
        }
    }
}
