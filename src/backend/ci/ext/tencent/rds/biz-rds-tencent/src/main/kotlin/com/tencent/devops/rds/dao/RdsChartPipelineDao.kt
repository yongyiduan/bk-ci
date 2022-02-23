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

import com.tencent.devops.common.api.util.timestampmilli
import com.tencent.devops.model.rds.tables.TRdsChartPipeline
import com.tencent.devops.rds.pojo.RdsChartPipelineInfo
import java.time.LocalDateTime
import org.jooq.DSLContext

class RdsChartPipelineDao {

    fun create(
        dslContext: DSLContext,
        pipeline: RdsChartPipelineInfo
    ): Int {
        with(TRdsChartPipeline.T_RDS_CHART_PIPELINE) {
            return dslContext.insertInto(this,
                PIPELINE_ID,
                PRODUCT_ID,
                FILE_PATH,
                ORIGIN_YAML,
                PARSED_YAML,
                CREATE_TIME
            ).values(
                pipeline.pipelineId,
                pipeline.productId,
                pipeline.filePath,
                pipeline.originYaml,
                pipeline.parsedYaml,
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
                .set(UPDATE_TIME, LocalDateTime.now())
                .where(PIPELINE_ID.eq(pipeline.pipelineId))
                .execute()
        }
    }

    fun getChartPipelines(dslContext: DSLContext, productId: String): List<RdsChartPipelineInfo> {
        with(TRdsChartPipeline.T_RDS_CHART_PIPELINE) {
            return dslContext.selectFrom(this)
                .where(PRODUCT_ID.eq(productId))
                .fetch().map {
                    RdsChartPipelineInfo(
                        pipelineId = it.pipelineId,
                        productId = it.productId,
                        filePath = it.filePath,
                        originYaml = it.originYaml,
                        parsedYaml = it.parsedYaml
                    )
                }
        }
    }
}
