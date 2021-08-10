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

package com.tencent.devops.process.pojo.setting

import com.tencent.devops.common.api.exception.InvalidParamException
import com.tencent.devops.common.web.annotation.BkField
import com.tencent.devops.common.web.constant.BkStyleEnum
import com.tencent.devops.process.utils.PIPELINE_RES_NUM_MIN
import com.tencent.devops.process.utils.PIPELINE_SETTING_MAX_CON_QUEUE_SIZE_DEFAULT
import com.tencent.devops.process.utils.PIPELINE_SETTING_MAX_CON_QUEUE_SIZE_MAX
import com.tencent.devops.process.utils.PIPELINE_SETTING_MAX_QUEUE_SIZE_DEFAULT
import com.tencent.devops.process.utils.PIPELINE_SETTING_MAX_QUEUE_SIZE_MAX
import com.tencent.devops.process.utils.PIPELINE_SETTING_MAX_QUEUE_SIZE_MIN
import com.tencent.devops.process.utils.PIPELINE_SETTING_WAIT_QUEUE_TIME_MINUTE_DEFAULT
import com.tencent.devops.process.utils.PIPELINE_SETTING_WAIT_QUEUE_TIME_MINUTE_MAX
import com.tencent.devops.process.utils.PIPELINE_SETTING_WAIT_QUEUE_TIME_MINUTE_MIN

data class PipelineSetting(
    val projectId: String = "",
    var pipelineId: String = "",
    var pipelineName: String = "",
    val desc: String = "",
    val runLockType: PipelineRunLockType = PipelineRunLockType.SINGLE_LOCK,
    var successSubscription: Subscription = Subscription(),
    var failSubscription: Subscription = Subscription(),
    var labels: List<String> = emptyList(),
    val waitQueueTimeMinute: Int = PIPELINE_SETTING_WAIT_QUEUE_TIME_MINUTE_DEFAULT,
    val maxQueueSize: Int = PIPELINE_SETTING_MAX_QUEUE_SIZE_DEFAULT,
    var hasPermission: Boolean? = null,
    val maxPipelineResNum: Int = PIPELINE_RES_NUM_MIN, // 保存流水线编排的最大个数
    val maxConRunningQueueSize: Int = PIPELINE_SETTING_MAX_CON_QUEUE_SIZE_DEFAULT, // MULTIPLE类型时，并发构建数量限制
    var version: Int = 0,
    @field:BkField(patternStyle = BkStyleEnum.BUILD_NUM_RULE_STYLE, required = false)
    val buildNumRule: String? = null // 构建号生成规则
) {

    @Suppress("ALL")
    fun checkParam() {
        successSubscription.content.replace("\"", "\\\"")
        failSubscription.content.replace("\"", "\\\"")
        if (maxPipelineResNum < 1) {
            throw InvalidParamException(message = "流水线编排数量非法", params = arrayOf("maxPipelineResNum"))
        }
        if (runLockType == PipelineRunLockType.SINGLE ||
            runLockType == PipelineRunLockType.SINGLE_LOCK) {
            if (waitQueueTimeMinute < PIPELINE_SETTING_WAIT_QUEUE_TIME_MINUTE_MIN ||
                waitQueueTimeMinute > PIPELINE_SETTING_WAIT_QUEUE_TIME_MINUTE_MAX) {
                throw InvalidParamException("最大排队时长非法", params = arrayOf("waitQueueTimeMinute"))
            }
            if (maxQueueSize < PIPELINE_SETTING_MAX_QUEUE_SIZE_MIN ||
                maxQueueSize > PIPELINE_SETTING_MAX_QUEUE_SIZE_MAX) {
                throw InvalidParamException("最大排队数量非法", params = arrayOf("maxQueueSize"))
            }
        }
        if (maxConRunningQueueSize <= PIPELINE_SETTING_MAX_QUEUE_SIZE_MIN ||
            maxConRunningQueueSize > PIPELINE_SETTING_MAX_CON_QUEUE_SIZE_MAX) {
            throw InvalidParamException("最大并发数量非法", params = arrayOf("maxConRunningQueueSize"))
        }
    }
}
