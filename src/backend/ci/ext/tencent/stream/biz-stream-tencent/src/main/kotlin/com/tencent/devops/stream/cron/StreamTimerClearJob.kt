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

package com.tencent.devops.stream.cron

import com.tencent.devops.common.api.exception.ClientException
import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.redis.RedisOperation
import com.tencent.devops.stream.common.exception.ErrorCodeEnum
import com.tencent.devops.stream.service.StreamOauthService
import com.tencent.devops.stream.service.StreamScmService
import com.tencent.devops.stream.trigger.timer.SchedulerManager
import com.tencent.devops.stream.trigger.timer.service.StreamTimerService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import javax.ws.rs.core.Response

@Component
@Suppress("ALL")
class StreamTimerClearJob @Autowired constructor(
    private val streamTimerService: StreamTimerService,
    private val schedulerManager: SchedulerManager,
    private val streamScmService: StreamScmService,
    private val streamOauthService: StreamOauthService,
    private val redisOperation: RedisOperation
) {

    companion object {
        private val logger = LoggerFactory.getLogger(StreamTimerClearJob::class.java)
        private const val STREAM_GIT_JOB_NOT_FOUND_PREFIX = "stream:git:job:notFound:key:"
        private const val maxNouFoundCount = 5
    }

    @Scheduled(cron = "0 0 1 * * ?")
    fun streamTimerClear() {
        val allExecutingJobs = schedulerManager.getAllExecutingJobs()
        allExecutingJobs.forEach {
            // "JobKey.name = ${pipelineId}_${md5}_$projectId"
            val jobKeyName = it.name
            val pipelineId = jobKeyName.split("_")[0]
            val timer = streamTimerService.get(pipelineId)
            timer?.let {
                val gitProjectId = timer.gitProjectId
//                val token = streamGitTokenService.getToken(gitProjectId)
                try {
                    val token = streamOauthService.getAndCheckOauthToken(timer.userId).accessToken
                    retry {
                        streamScmService.getProjectInfo(token, gitProjectId.toString(), true)
                    }
                } catch (e: ErrorCodeException) {
                    // 正常获取信息应该正常返回，除非项目被删除了404
                    if (checkIsNotFound(e)) {
                        val count = incrNotFoundCount(jobKeyName)
                        logger.info("getProjectInfo:$gitProjectId 404 Not Found,Jobkey:$jobKeyName,times:$count")
                        if (count >= maxNouFoundCount) {
                            logger.info("clear timer,getProjectInfo 404 times:$count,JobKey:$jobKeyName")
                            schedulerManager.deleteJob(jobKeyName)
                            streamTimerService.deleteTimer(pipelineId, timer.userId)
                        }
                    }
                } catch (e: Exception) {
                    logger.error("streamTimerClear|getProjectInfo fail because:${e.message}")
                }
            }
        }
    }

    private fun incrNotFoundCount(gitProjectId: String): Long {
        val key = getRedisKey(gitProjectId)
        val count = redisOperation.increment(key, 1) ?: 0
        // 每次增加 Not Found次数的时候都延长一天都过期时间，也就是只有连续5天404才会删除
        redisOperation.expire(key, TimeUnit.HOURS.toSeconds(26))
        return count
    }

    private fun checkIsNotFound(e: ErrorCodeException): Boolean {
        val statusCodeNotFound = e.statusCode == Response.Status.NOT_FOUND.statusCode
        val errorCodeNotFound = ErrorCodeEnum.PROJECT_NOT_FOUND.errorCode.toString() == e.errorCode
        return statusCodeNotFound || errorCodeNotFound
    }

    private fun getRedisKey(gitProjectId: String): String {
        return "$STREAM_GIT_JOB_NOT_FOUND_PREFIX$gitProjectId"
    }

    @Throws(ClientException::class)
    fun <T> retry(retryTime: Int = 5, retryPeriodMills: Long = 500, block: () -> T): T {
        return try {
            block()
        } catch (e: ErrorCodeException) {
            if (retryTime - 1 < 0) {
                throw e
            }
            if (retryPeriodMills > 0) {
                Thread.sleep(retryPeriodMills)
            }
            retry(block = block, retryTime = retryTime - 1, retryPeriodMills = retryPeriodMills)
        }
    }
}
