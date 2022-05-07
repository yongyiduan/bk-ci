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

import com.tencent.devops.common.pipeline.enums.BuildStatus
import com.tencent.devops.model.rds.tables.TRdsBuildHistory
import com.tencent.devops.model.rds.tables.records.TRdsBuildHistoryRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class RdsBuildHistoryDao {

    fun getHistoryByPipelineId(dslContext: DSLContext, pipelineId: String): List<TRdsBuildHistoryRecord> {
        with(TRdsBuildHistory.T_RDS_BUILD_HISTORY) {
            return dslContext.selectFrom(this)
                .where(PIPELINE_ID.eq(pipelineId))
                .fetch()
        }
    }

    fun save(
        dslContext: DSLContext,
        buildId: String,
        pipelineId: String,
        projectId: String,
        userId: String,
        buildStatus: BuildStatus
    ): Int {
        with(TRdsBuildHistory.T_RDS_BUILD_HISTORY) {
            return dslContext.insertInto(this)
                .set(BUILD_ID, buildId)
                .set(PIPELINE_ID, pipelineId)
                .set(PROJECT_ID, projectId)
                .set(USER_ID, userId)
                .set(STATUS, buildStatus.ordinal)
                .set(START_TIME, LocalDateTime.now())
                .execute()
        }
    }

    fun finish(
        dslContext: DSLContext,
        buildId: String,
        pipelineId: String,
        projectId: String,
        userId: String,
        buildStatus: BuildStatus,
        errorInfo: String?
    ) {
        with(TRdsBuildHistory.T_RDS_BUILD_HISTORY) {
            dslContext.insertInto(this)
                .set(BUILD_ID, buildId)
                .set(PIPELINE_ID, pipelineId)
                .set(PROJECT_ID, projectId)
                .set(USER_ID, userId)
                .set(STATUS, buildStatus.ordinal)
                .set(START_TIME, LocalDateTime.now())
                .onDuplicateKeyUpdate()
                .set(STATUS, buildStatus.ordinal)
                .set(END_TIME, LocalDateTime.now())
                .set(ERROR_INFO, errorInfo)
                .execute()
        }
    }
}
