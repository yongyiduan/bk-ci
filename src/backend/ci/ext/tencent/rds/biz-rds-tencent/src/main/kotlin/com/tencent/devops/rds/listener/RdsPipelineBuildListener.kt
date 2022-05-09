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

package com.tencent.devops.rds.listener

import com.tencent.devops.common.event.dispatcher.pipeline.mq.MQ
import com.tencent.devops.common.event.pojo.pipeline.PipelineBuildFinishBroadCastEvent
import com.tencent.devops.common.event.pojo.pipeline.PipelineBuildStartBroadCastEvent
import com.tencent.devops.common.pipeline.enums.BuildStatus
import com.tencent.devops.rds.constants.Constants.QUEUE_PIPELINE_BUILD_FINISH_RDS
import com.tencent.devops.rds.constants.Constants.QUEUE_PIPELINE_BUILD_START_RDS
import com.tencent.devops.rds.dao.RdsProductInfoDao
import com.tencent.devops.rds.pojo.enums.ProductStatus
import com.tencent.devops.rds.service.ProductPipelineService
import com.tencent.devops.rds.utils.RdsPipelineUtils
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RdsPipelineBuildListener @Autowired constructor(
    private val dslContext: DSLContext,
    private val productPipelineService: ProductPipelineService,
    private val productInfoDao: RdsProductInfoDao
) {

    companion object {
        private val logger = LoggerFactory.getLogger(RdsPipelineBuildListener::class.java)
    }

    @RabbitListener(
        bindings = [
            (
                QueueBinding(
                    value = Queue(value = QUEUE_PIPELINE_BUILD_START_RDS, durable = "true"),
                    exchange = Exchange(
                        value = MQ.EXCHANGE_PIPELINE_BUILD_START_FANOUT,
                        durable = "true",
                        delayed = "true",
                        type = ExchangeTypes.FANOUT
                    )
                )
                )
        ]
    )
    fun listenPipelineBuildStartBroadCastEvent(buildStartEvent: PipelineBuildStartBroadCastEvent) {
        try {
            productPipelineService.doStart(buildStartEvent)
        } catch (e: Throwable) {
            logger.error("Fail to listenPipelineBuildStartBroadCastEvent(${buildStartEvent.buildId})", e)
        }
    }

    @RabbitListener(
        bindings = [
            (
                QueueBinding(
                    value = Queue(value = QUEUE_PIPELINE_BUILD_FINISH_RDS, durable = "true"),
                    exchange = Exchange(
                        value = MQ.EXCHANGE_PIPELINE_BUILD_FINISH_FANOUT,
                        durable = "true",
                        delayed = "true",
                        type = ExchangeTypes.FANOUT
                    )
                )
                )
        ]
    )
    fun listenPipelineBuildFinishBroadCastEvent(buildFinishEvent: PipelineBuildFinishBroadCastEvent) {
        try {
            productPipelineService.doFinish(buildFinishEvent)
            when (BuildStatus.valueOf(buildFinishEvent.status)) {
                BuildStatus.SUCCEED -> {
                    productInfoDao.updateProductStatus(
                        dslContext = dslContext,
                        productId = RdsPipelineUtils.genProductId(buildFinishEvent.projectId),
                        status = ProductStatus.DEPLOYED,
                        errorMsg = null
                    )
                }
                else -> {
                    // 启动失败时修改状态
                    productInfoDao.updateProductStatus(
                        dslContext = dslContext,
                        productId = RdsPipelineUtils.genProductId(buildFinishEvent.projectId),
                        status = ProductStatus.FAILED,
                        errorMsg =
                        "init pipeline ${buildFinishEvent.pipelineId} build ${buildFinishEvent.buildId} failed"
                    )
                }
            }
        } catch (e: Throwable) {
            logger.error("Fail to listenPipelineBuildFinishBroadCastEvent(${buildFinishEvent.buildId})", e)
        }
    }
}
