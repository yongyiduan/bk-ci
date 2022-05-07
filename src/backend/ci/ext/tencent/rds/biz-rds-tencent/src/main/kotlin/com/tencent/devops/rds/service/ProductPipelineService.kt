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

import com.tencent.devops.common.api.constant.HTTP_404
import com.tencent.devops.common.api.constant.HTTP_500
import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.event.pojo.pipeline.PipelineBuildFinishBroadCastEvent
import com.tencent.devops.common.event.pojo.pipeline.PipelineBuildStartBroadCastEvent
import com.tencent.devops.common.pipeline.enums.BuildStatus
import com.tencent.devops.rds.dao.RdsBuildHistoryDao
import com.tencent.devops.rds.dao.RdsChartPipelineDao
import com.tencent.devops.rds.exception.ApiErrorCodeEnum
import com.tencent.devops.rds.pojo.InitStatusResult
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ProductPipelineService @Autowired constructor(
    private val dslContext: DSLContext,
    private val buildHistoryDao: RdsBuildHistoryDao,
    private val chartPipelineDao: RdsChartPipelineDao,
) {

    private val logger = LoggerFactory.getLogger(ProductPipelineService::class.java)

    fun getInitPipelineStatus(productId: Long): InitStatusResult {
        val initPipeline = chartPipelineDao.getInitPipelines(dslContext, productId) ?: throw ErrorCodeException(
            statusCode = HTTP_404,
            errorCode = ApiErrorCodeEnum.UNKNOWN_ERROR.errorCode,
            params = arrayOf("未找到初始化流水线")
        )
        val history = buildHistoryDao.getHistoryByPipelineId(dslContext, initPipeline.pipelineId)
        if (history.size != 1) throw ErrorCodeException(
            statusCode = HTTP_500,
            errorCode = ApiErrorCodeEnum.UNKNOWN_ERROR.errorCode,
            params = arrayOf("初始化流水线数据错误")
        )
        return InitStatusResult(
            productId = productId,
            status = BuildStatus.values()[history.first().status].statusName,
            errorInfo = history.first().errorInfo
        )
    }

    fun doStart(buildStartEvent: PipelineBuildStartBroadCastEvent) {
        val pipeline = chartPipelineDao.getPipelineById(dslContext, buildStartEvent.pipelineId) ?: return
        logger.info("RDS|BUILD_START|buildStartEvent=$buildStartEvent")
        buildHistoryDao.save(
            dslContext = dslContext,
            buildId = buildStartEvent.buildId,
            pipelineId = pipeline.pipelineId,
            projectId = buildStartEvent.projectId,
            userId = buildStartEvent.userId,
            buildStatus = BuildStatus.RUNNING
        )
    }

    fun doFinish(buildFinishEvent: PipelineBuildFinishBroadCastEvent) {
        val pipeline = chartPipelineDao.getPipelineById(dslContext, buildFinishEvent.pipelineId) ?: return
        logger.info("RDS|BUILD_FINISH|buildFinishEvent=$buildFinishEvent")
        buildHistoryDao.finish(
            dslContext = dslContext,
            buildId = buildFinishEvent.buildId,
            pipelineId = pipeline.pipelineId,
            projectId = buildFinishEvent.projectId,
            userId = buildFinishEvent.userId,
            buildStatus = BuildStatus.parse(buildFinishEvent.status),
            errorInfo = buildFinishEvent.errorInfoList
        )
    }
}
