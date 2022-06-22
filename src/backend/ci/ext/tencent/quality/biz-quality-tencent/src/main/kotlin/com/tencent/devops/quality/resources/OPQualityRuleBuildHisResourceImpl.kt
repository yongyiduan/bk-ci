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

package com.tencent.devops.quality.resources

import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.event.pojo.measure.QualityReportEvent
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.quality.api.op.OPQualityRuleBuildHisResource
import com.tencent.devops.quality.config.QualityDailyDispatch
import com.tencent.devops.quality.dao.HistoryDao
import com.tencent.devops.quality.service.OPQualityRuleBuildHisService
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestResource
class OPQualityRuleBuildHisResourceImpl @Autowired constructor(
    private val opQualityRuleBuildHisService: OPQualityRuleBuildHisService,
    private val historyDao: HistoryDao,
    private val dslContext: DSLContext,
    private val qualityDailyDispatch: QualityDailyDispatch
) : OPQualityRuleBuildHisResource {

    private val logger = LoggerFactory.getLogger(OPQualityRuleBuildHisResourceImpl::class.java)

    override fun updateStatus(): Result<Int> {
        return Result(opQualityRuleBuildHisService.updateRuleBuildHisStatus())
    }

    override fun dailyReport(): Result<Int> {
        val startTime = LocalDateTime.now().minusDays(1)
        val endTime = LocalDateTime.now()
        logger.info("QUALITY_REPORT|time: $startTime, $endTime")
        val result = historyDao.batchDailyTotalCount(
            dslContext = dslContext,
            startTime = startTime,
            endTime = endTime
        )
        logger.info("QUALITY_REPORT|daoResult: $result")
        result.forEach {
            val interceptCount = historyDao.countIntercept(
                dslContext = dslContext,
                projectId = it.value1(),
                pipelineId = null,
                ruleId = null,
                startTime = startTime,
                endTime = endTime
            )
            logger.info("QUALITY_REPORT|quality daily report: ${it.value1()}, ${it.value2()}, $interceptCount")
            qualityDailyDispatch.dispatch(
                QualityReportEvent(
                    statisticsTime = endTime.format(DateTimeFormatter.ISO_DATE),
                    projectId = it.value1(),
                    interceptedCount = interceptCount.toInt(),
                    totalCount = it.value2()
                )
            )
        }
        logger.info("QUALITY_REPORT|finish to send quality daily data.")
        return Result(0)
    }
}
