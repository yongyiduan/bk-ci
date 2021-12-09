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

package com.tencent.devops.stream.trigger.parsers.modelCreate

import com.fasterxml.jackson.databind.ObjectMapper
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.api.util.YamlUtil
import com.tencent.devops.common.ci.v2.IfType
import com.tencent.devops.common.ci.v2.Job
import com.tencent.devops.common.ci.v2.JobRunsOnType
import com.tencent.devops.common.ci.v2.Resources
import com.tencent.devops.common.ci.v2.StreamDispatchInfo
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.pipeline.container.Container
import com.tencent.devops.common.pipeline.container.NormalContainer
import com.tencent.devops.common.pipeline.container.VMBuildContainer
import com.tencent.devops.common.pipeline.enums.DependOnType
import com.tencent.devops.common.pipeline.enums.JobRunCondition
import com.tencent.devops.common.pipeline.enums.VMBaseOS
import com.tencent.devops.common.pipeline.option.JobControlOption
import com.tencent.devops.common.pipeline.option.MatrixControlOption
import com.tencent.devops.common.pipeline.option.MatrixControlOption.Companion.MATRIX_CONTEXT_KEY_PREFIX
import com.tencent.devops.common.pipeline.pojo.element.Element
import com.tencent.devops.process.util.StreamDispatchUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ModelContainer @Autowired constructor(
    private val client: Client,
    private val objectMapper: ObjectMapper
) {
    // 公共镜像默认从配置获取
    @Value("\${container.defaultImage:#{null}}")
    val defaultImage: String? = null

    companion object {
        private val logger = LoggerFactory.getLogger(ModelContainer::class.java)
    }

    fun addVmBuildContainer(
        job: Job,
        elementList: List<Element>,
        containerList: MutableList<Container>,
        jobIndex: Int,
        projectCode: String,
        finalStage: Boolean = false,
        resources: Resources? = null
    ) {
        val defaultImage = defaultImage ?: "http://mirrors.tencent.com/ci/tlinux3_ci:0.1.1.0"
        val dispatchInfo = if (JsonUtil.toJson(job.runsOn).contains("\${{ $MATRIX_CONTEXT_KEY_PREFIX")) {
            StreamDispatchInfo(
                name = "dispatchInfo_${job.name}",
                job = job,
                projectCode = projectCode,
                defaultImage = defaultImage,
                resources = resources
            )
        } else null
        val vmContainer = VMBuildContainer(
            jobId = job.id,
            name = job.name ?: "Job-${jobIndex + 1}",
            elements = elementList,
            baseOS = StreamDispatchUtils.getBaseOs(job),
            vmNames = setOf(),
            maxQueueMinutes = 60,
            maxRunningMinutes = job.timeoutMinutes ?: 900,
            buildEnv = if (job.runsOn.selfHosted == false) {
                job.runsOn.needs
            } else {
                null
            },
            customBuildEnv = job.env,
            jobControlOption = getJobControlOption(job, finalStage),
            dispatchType = StreamDispatchUtils.getDispatchType(
                client = client,
                objectMapper = objectMapper,
                job = job,
                projectCode = projectCode,
                defaultImage = defaultImage,
                resources = resources,
                containsMatrix = dispatchInfo != null
            ),
            matrixGroupFlag = job.strategy != null,
            matrixControlOption = getMatrixControlOption(job, dispatchInfo)
        )
        containerList.add(vmContainer)
    }

    private fun getMatrixControlOption(
        job: Job,
        dispatchInfo: StreamDispatchInfo?
    ): MatrixControlOption? {

        val strategy = job.strategy ?: return null

        with(strategy) {
            if (matrix is Map<*, *>) {
                val yaml = matrix as MutableMap<String, Any>
                val include = if ("include" in yaml.keys && yaml["include"] != null) {
                    YamlUtil.toYaml(yaml["include"]!!)
                } else {
                    null
                }
                val exclude = if ("exclude" in yaml.keys && yaml["exclude"] != null) {
                    YamlUtil.toYaml(yaml["exclude"]!!)
                } else {
                    null
                }
                val json = matrix as MutableMap<String, Any>
                json.remove("include")
                json.remove("exclude")

                return MatrixControlOption(
                    strategyStr = JsonUtil.toJson(json),
                    includeCaseStr = include,
                    excludeCaseStr = exclude,
                    fastKill = fastKill,
                    maxConcurrency = maxParallel,
                    customDispatchInfo = dispatchInfo
                )
            } else {
                return MatrixControlOption(
                    strategyStr = matrix.toString(),
                    fastKill = fastKill,
                    maxConcurrency = maxParallel,
                    customDispatchInfo = dispatchInfo
                )
            }
        }
    }

    fun addNormalContainer(
        job: Job,
        elementList: List<Element>,
        containerList: MutableList<Container>,
        jobIndex: Int,
        finalStage: Boolean = false
    ) {

        containerList.add(
            NormalContainer(
                jobId = job.id,
                containerId = null,
                id = job.id,
                name = job.name ?: "Job-${jobIndex + 1}",
                elements = elementList,
                status = null,
                startEpoch = null,
                systemElapsed = null,
                elementElapsed = null,
                enableSkip = false,
                conditions = null,
                canRetry = false,
                jobControlOption = getJobControlOption(job, finalStage),
                mutexGroup = null
            )
        )
    }

    private fun getJobControlOption(
        job: Job,
        finalStage: Boolean = false
    ): JobControlOption {
        return if (!job.ifField.isNullOrBlank()) {
            if (finalStage) {
                JobControlOption(
                    timeout = job.timeoutMinutes,
                    runCondition = when (job.ifField) {
                        IfType.SUCCESS.name -> JobRunCondition.PREVIOUS_STAGE_SUCCESS
                        IfType.FAILURE.name -> JobRunCondition.PREVIOUS_STAGE_FAILED
                        IfType.CANCELLED.name -> JobRunCondition.PREVIOUS_STAGE_CANCEL
                        else -> JobRunCondition.STAGE_RUNNING
                    },
                    dependOnType = DependOnType.ID,
                    dependOnId = job.dependOn,
                    prepareTimeout = job.runsOn.queueTimeoutMinutes,
                    continueWhenFailed = job.continueOnError
                )
            } else {
                JobControlOption(
                    timeout = job.timeoutMinutes,
                    runCondition = JobRunCondition.CUSTOM_CONDITION_MATCH,
                    customCondition = job.ifField.toString(),
                    dependOnType = DependOnType.ID,
                    dependOnId = job.dependOn,
                    prepareTimeout = job.runsOn.queueTimeoutMinutes,
                    continueWhenFailed = job.continueOnError
                )
            }
        } else {
            JobControlOption(
                timeout = job.timeoutMinutes,
                dependOnType = DependOnType.ID,
                dependOnId = job.dependOn,
                prepareTimeout = job.runsOn.queueTimeoutMinutes,
                continueWhenFailed = job.continueOnError
            )
        }
    }
}
