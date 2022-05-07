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

package com.tencent.devops.rds.dao

import com.tencent.devops.model.rds.tables.TRdsChartPipeline
import com.tencent.devops.rds.pojo.RdsChartPipelineInfo
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class RdsChartPipelineDao {

    fun createPipeline(
        dslContext: DSLContext,
        pipeline: RdsChartPipelineInfo,
        init: Boolean? = false
    ): Int {
        with(TRdsChartPipeline.T_RDS_CHART_PIPELINE) {
            return dslContext.insertInto(
                this,
                PIPELINE_ID,
                PRODUCT_ID,
                FILE_PATH,
                PROJECT_NAME,
                SERVICE_NAME,
                ORIGIN_YAML,
                PARSED_YAML,
                INIT_PIPELINE,
                CREATE_TIME,
                UPDATE_TIME
            ).values(
                pipeline.pipelineId,
                pipeline.productId,
                pipeline.filePath,
                pipeline.projectName,
                pipeline.serviceName,
                pipeline.originYaml,
                pipeline.parsedYaml,
                init,
                LocalDateTime.now(),
                LocalDateTime.now()
            ).execute()
        }
    }

    fun updatePipeline(
        dslContext: DSLContext,
        pipeline: RdsChartPipelineInfo
    ) {
        with(TRdsChartPipeline.T_RDS_CHART_PIPELINE) {
            dslContext.update(this)
                .set(ORIGIN_YAML, pipeline.originYaml)
                .set(PARSED_YAML, pipeline.parsedYaml)
                .set(FILE_PATH, pipeline.filePath)
                .set(PROJECT_NAME, pipeline.projectName)
                .set(SERVICE_NAME, pipeline.serviceName)
                .set(UPDATE_TIME, LocalDateTime.now())
                .where(PIPELINE_ID.eq(pipeline.pipelineId))
                .execute()
        }
    }

    fun getChartPipelines(dslContext: DSLContext, productId: Long): List<RdsChartPipelineInfo> {
        with(TRdsChartPipeline.T_RDS_CHART_PIPELINE) {
            return dslContext.selectFrom(this)
                .where(PRODUCT_ID.eq(productId))
                .fetch().map {
                    RdsChartPipelineInfo(
                        pipelineId = it.pipelineId,
                        productId = it.productId,
                        filePath = it.filePath,
                        projectName = it.projectName,
                        serviceName = it.serviceName,
                        originYaml = it.originYaml,
                        parsedYaml = it.parsedYaml
                    )
                }
        }
    }

    fun getProductPipelineByService(
        dslContext: DSLContext,
        productId: Long,
        filePath: String,
        projectName: String,
        serviceName: String?
    ): RdsChartPipelineInfo? {
        with(TRdsChartPipeline.T_RDS_CHART_PIPELINE) {
            val select = dslContext.selectFrom(this)
                .where(
                    PRODUCT_ID.eq(productId)
                        .and(PROJECT_NAME.eq(projectName))
                        .and(FILE_PATH.eq(filePath))
                )
            serviceName?.let { select.and(SERVICE_NAME.eq(it)) }
            return select.fetchAny()?.let {
                RdsChartPipelineInfo(
                    pipelineId = it.pipelineId,
                    productId = it.productId,
                    filePath = it.filePath,
                    projectName = it.projectName,
                    serviceName = it.serviceName,
                    originYaml = it.originYaml,
                    parsedYaml = it.parsedYaml
                )
            }
        }
    }
}
