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

package com.tencent.devops.quality.cron

import com.tencent.devops.common.event.pojo.measure.QualityReportEvent
import com.tencent.devops.quality.config.QualityDailyDispatch
import com.tencent.devops.quality.dao.HistoryDao
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class QualityDailyReportJob @Autowired constructor(
    private val historyDao: HistoryDao,
    private val qualityDailyDispatch: QualityDailyDispatch,
    private val dslContext: DSLContext
) {

    private val logger = LoggerFactory.getLogger(QualityDailyReportJob::class.java)

    @Scheduled(cron = "0 0 0 * * ?")
    fun send() {
        val startTime = LocalDateTime.now().minusDays(1)
        val endTime = LocalDateTime.now()
        val result = historyDao.batchDailyTotalCount(
            dslContext = dslContext,
            startTime = startTime,
            endTime = endTime
        )
        result.forEach {
            val interceptCount = historyDao.countIntercept(
                dslContext = dslContext,
                projectId = it.value1(),
                pipelineId = null,
                ruleId = null,
                startTime = startTime,
                endTime = endTime
            )
            qualityDailyDispatch.dispatch(
                QualityReportEvent(
                    statisticsTime = endTime.format(DateTimeFormatter.ISO_DATE),
                    projectId = it.value1(),
                    interceptedCount = interceptCount.toInt(),
                    totalCount = it.value2()
                )
            )
        }
        logger.info("finish to send quality daily data.")
    }
}