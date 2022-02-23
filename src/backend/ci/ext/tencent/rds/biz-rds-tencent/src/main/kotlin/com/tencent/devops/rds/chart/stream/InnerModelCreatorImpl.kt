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

package com.tencent.devops.rds.chart.stream

import com.devops.process.yaml.modelCreate.inner.ModelCreateEvent
import com.devops.process.yaml.modelCreate.inner.InnerModelCreator
import com.tencent.devops.common.ci.task.ServiceJobDevCloudInput
import com.tencent.devops.common.ci.v2.Step
import com.tencent.devops.common.ci.v2.YamlTransferData
import com.tencent.devops.common.pipeline.pojo.element.ElementAdditionalOptions
import com.tencent.devops.common.pipeline.pojo.element.market.MarketBuildAtomElement
import com.tencent.devops.process.pojo.BuildTemplateAcrossInfo
import com.tencent.devops.rds.chart.ChartPipelineStartParams
import com.tencent.devops.rds.constants.RdsConstants
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class InnerModelCreatorImpl : InnerModelCreator {

    @Value("\${stream.marketRun.enable:#{false}}")
    private val marketRunTaskData: Boolean = false

    @Value("\${stream.marketRun.atomCode:#{null}}")
    private val runPlugInAtomCodeData: String? = null

    @Value("\${stream.marketRun.atomVersion:#{null}}")
    private val runPlugInVersionData: String? = null

    companion object {
        //  checkout通过当前用户的token来拉取
        private const val CHECKOUT_AUTH_TYPE = "TICKET"
    }

    override val marketRunTask: Boolean
        get() {
            return marketRunTaskData
        }
    override val runPlugInAtomCode: String?
        get() {
            return runPlugInAtomCodeData
        }
    override val runPlugInVersion: String?
        get() {
            return runPlugInVersionData
        }

    override fun getJobTemplateAcrossInfo(
        yamlTransferData: YamlTransferData,
        gitRequestEventId: Long,
        gitProjectId: Long
    ): Map<String, BuildTemplateAcrossInfo> {
        return emptyMap()
    }

    override fun getServiceJobDevCloudInput(
        image: String,
        imageName: String,
        imageTag: String,
        params: String
    ): ServiceJobDevCloudInput? {
        return null
    }

    override fun makeCheckoutElement(
        step: Step,
        event: ModelCreateEvent,
        additionalOptions: ElementAdditionalOptions
    ): MarketBuildAtomElement {
        // checkout插件装配
        val inputMap = mutableMapOf<String, Any?>()
        if (!step.with.isNullOrEmpty()) {
            inputMap.putAll(step.with!!)
        }

        if (step.checkout == "self") {
            makeCheckoutSelf(inputMap)
        } else {
            inputMap["repositoryUrl"] = step.checkout!!
        }

        // 用户未指定时缺省为 TICKET 同时指定 产品负责人
        if (inputMap["authType"] == null) {
            inputMap["authType"] = CHECKOUT_AUTH_TYPE
            // 这里是rds创建好的ticket_id
            inputMap["ticketId"] = RdsConstants.RDS_PRODUCT_USER_GIT_PRIVATE_TOKEN
        }

        // 拼装插件固定参数
        inputMap["repositoryType"] = "URL"

        val data = mutableMapOf<String, Any>()
        data["input"] = inputMap

        return MarketBuildAtomElement(
            id = step.taskId,
            name = step.name ?: "checkout",
            stepId = step.id,
            atomCode = "checkout",
            version = "1.*",
            data = data,
            additionalOptions = additionalOptions
        )
    }

    private fun makeCheckoutSelf(inputMap: MutableMap<String, Any?>) {
        inputMap["repositoryUrl"] = ChartPipelineStartParams.RDS_CHECKOUT_REPOSITORY_URL.pipelinePlaceholder()
        inputMap["pullType"] = ChartPipelineStartParams.RDS_CHECKOUT_PULL_TYPE.pipelinePlaceholder()
        inputMap["refName"] = ChartPipelineStartParams.RDS_CHECKOUT_REF.pipelinePlaceholder()
    }

    private fun String.pipelinePlaceholder() = "\${{ $this }}"
}
