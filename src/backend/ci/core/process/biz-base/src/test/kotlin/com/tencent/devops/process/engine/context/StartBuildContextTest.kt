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

package com.tencent.devops.process.engine.context

import com.tencent.devops.common.pipeline.enums.BuildStatus
import com.tencent.devops.common.pipeline.enums.ChannelCode
import com.tencent.devops.common.pipeline.enums.StartType
import com.tencent.devops.process.TestBase
import com.tencent.devops.process.utils.PIPELINE_RETRY_COUNT
import com.tencent.devops.process.utils.PIPELINE_RETRY_START_TASK_ID
import com.tencent.devops.process.utils.PIPELINE_SKIP_FAILED_TASK
import com.tencent.devops.process.utils.PIPELINE_START_CHANNEL
import com.tencent.devops.process.utils.PIPELINE_START_PARENT_BUILD_ID
import com.tencent.devops.process.utils.PIPELINE_START_PARENT_BUILD_TASK_ID
import com.tencent.devops.process.utils.PIPELINE_START_TYPE
import com.tencent.devops.process.utils.PIPELINE_START_USER_ID
import com.tencent.devops.process.utils.PIPELINE_START_USER_NAME
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class StartBuildContextTest : TestBase() {

    private val params = mutableMapOf<String, Any>()

    @Before
    fun setUp2() {
        params.clear()
        params[PIPELINE_START_USER_NAME] = "your name"
        params[PIPELINE_START_USER_ID] = "id123"
        params[PIPELINE_START_TYPE] = StartType.MANUAL.name
        params[PIPELINE_RETRY_START_TASK_ID] = "e-12345678901234567890123456789012"
        params[PIPELINE_RETRY_COUNT] = "1"
        params[PIPELINE_START_PARENT_BUILD_ID] = "1"
        params[PIPELINE_START_PARENT_BUILD_TASK_ID] = "12"
        params[PIPELINE_START_CHANNEL] = ChannelCode.BS.name
    }

    @Test
    fun needSkipWhenStageFailRetry() {
        params.remove(PIPELINE_RETRY_START_TASK_ID)
        params.remove(PIPELINE_RETRY_COUNT)
        val context = StartBuildContext.init(params)
        val stage = genStages(stageSize = 2, jobSize = 2, elementSize = 2, needFinally = false)[1]
        val needSkipWhenStageFailRetry = context.needSkipWhenStageFailRetry(stage)
        println("needSkipWhenStageFailRetry=$needSkipWhenStageFailRetry")
        Assert.assertEquals(false, needSkipWhenStageFailRetry)
    }

    @Test
    fun needSkipContainerWhenFailRetry() {
        params[PIPELINE_RETRY_COUNT] = "abc"
        var context = StartBuildContext.init(params)
        Assert.assertEquals(context.retryCount, 0)
        params[PIPELINE_RETRY_COUNT] = "3"
        context = StartBuildContext.init(params)
        Assert.assertEquals(context.retryCount, 3)

        params[PIPELINE_START_CHANNEL] = ChannelCode.AM.name
        context = StartBuildContext.init(params)
        Assert.assertEquals(ChannelCode.AM, context.channelCode)
        params.remove(PIPELINE_START_CHANNEL)

        context = StartBuildContext.init(params)
        Assert.assertEquals(ChannelCode.BS, context.channelCode)

        val stage = genStages(stageSize = 2, jobSize = 2, elementSize = 2, needFinally = false)[1]
        val container = stage.containers[0]
        val needSkipContainerWhenFailRetry = context.needSkipContainerWhenFailRetry(stage, container)
        println("needSkipContainerWhenFailRetry=$needSkipContainerWhenFailRetry")
        Assert.assertEquals(false, needSkipContainerWhenFailRetry)
    }

    @Test
    fun needSkipTaskWhenRetry() {
        val context = StartBuildContext.init(params)
        val stage = genStages(stageSize = 2, jobSize = 2, elementSize = 2, needFinally = false)[1]
        val needSkipTaskWhenRetry = context.needSkipTaskWhenRetry(stage, taskId = "1")
        println("needSkipTaskWhenRetry=$needSkipTaskWhenRetry")
        Assert.assertEquals(true, needSkipTaskWhenRetry)
        // 确认id不是当前要跳过的插件
        val element211 = stage.containers[0].elements[0]
        Assert.assertEquals(false, context.inSkipStage(stage, element211))
    }

    @Test
    fun needSkipTaskWhenRetrySkip() {
        // 跳过Stage-2下所有失败插件
        params[PIPELINE_RETRY_START_TASK_ID] = "stage-2"
        params[PIPELINE_SKIP_FAILED_TASK] = true

        val stage2Context = StartBuildContext.init(params)
        val stages = genStages(stageSize = 2, jobSize = 2, elementSize = 2, needFinally = false)
        val stage1 = stages[1]
        val stage2 = stages[2]

        var needSkipTaskWhenRetrySkip = stage2Context.needSkipTaskWhenRetry(stage2, stage2.containers[0].elements[0].id)
        println("needSkipTaskWhenRetrySkip=$needSkipTaskWhenRetrySkip")
        Assert.assertEquals(false, needSkipTaskWhenRetrySkip)

        needSkipTaskWhenRetrySkip = stage2Context.needSkipTaskWhenRetry(stage1, stage1.containers[0].elements[0].id)
        println("needSkipTaskWhenRetrySkip=$needSkipTaskWhenRetrySkip")
        Assert.assertEquals(true, needSkipTaskWhenRetrySkip)

        // 指定跳过插件是 Stage-1 里的 插件
        params[PIPELINE_RETRY_START_TASK_ID] = stage1.containers[0].elements[0].id!!
        params[PIPELINE_SKIP_FAILED_TASK] = true
        val skipElement = StartBuildContext.init(params)
        needSkipTaskWhenRetrySkip = skipElement.needSkipTaskWhenRetry(stages[2], stage2.containers[0].elements[0].id)
        println("needSkipTaskWhenRetrySkip=$needSkipTaskWhenRetrySkip")
        Assert.assertEquals(true, needSkipTaskWhenRetrySkip)
    }

    @Test
    fun inSkipStage() {
        // 跳过Stage-2下所有失败插件
        params[PIPELINE_RETRY_START_TASK_ID] = "stage-2"
        params[PIPELINE_SKIP_FAILED_TASK] = true

        val stage2Context = StartBuildContext.init(params)
        val stages = genStages(stageSize = 2, jobSize = 2, elementSize = 2, needFinally = false)
        val stage1 = stages[1]
        val stage2 = stages[2]
        // 在重试要跳过的Stage-2里面
        stage2.containers.forEach { c ->
            c.elements.forEach { e ->
                e.status = BuildStatus.FAILED.name
                Assert.assertEquals(true, stage2Context.inSkipStage(stage2, e))
            }
        }

        // Stage-1的不受影响
        stage1.containers.forEach { c ->
            c.elements.forEach { e ->
                e.status = BuildStatus.FAILED.name
                Assert.assertEquals(false, stage2Context.inSkipStage(stage1, e))
            }
        }

        // 指定跳过插件是 Stage-1 里 插件
        params[PIPELINE_RETRY_START_TASK_ID] = stage1.containers[0].elements[0].id!!
        params[PIPELINE_SKIP_FAILED_TASK] = true
        val skipElement = StartBuildContext.init(params)
        // Stage-2 的插件不在重试时跳过的Stage内范围
        stage2.containers.forEach { c ->
            c.elements.forEach { e ->
                e.status = BuildStatus.FAILED.name
                Assert.assertEquals(false, skipElement.inSkipStage(stage2, e))
            }
        }

        // Stage-1 里面只有指定的跳过插件
        stage1.containers.forEach { c ->
            c.elements.forEach { e ->
                e.status = BuildStatus.FAILED.name
                if (e.id == params[PIPELINE_RETRY_START_TASK_ID]) {
                    println("Stage-1 里面只有指定的跳过插件: ${stage1.id}, ${e.id}")
                    Assert.assertEquals(true, skipElement.inSkipStage(stage1, e))
                } else {
                    Assert.assertEquals(false, skipElement.inSkipStage(stage1, e))
                }
            }
        }
    }
}
