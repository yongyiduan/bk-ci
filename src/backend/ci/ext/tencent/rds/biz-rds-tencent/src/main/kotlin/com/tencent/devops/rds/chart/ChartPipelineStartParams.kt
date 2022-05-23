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

package com.tencent.devops.rds.chart

import com.tencent.devops.common.pipeline.enums.BuildFormPropertyType
import com.tencent.devops.common.pipeline.pojo.BuildFormProperty
import com.tencent.devops.process.yaml.v2.models.Variable

/**
 * 通过chart生成的流水线需要预置的启动参数
 */
object ChartPipelineStartParams {
    private const val VARIABLE_PREFIX = "variables."
    //  checkout参数需要的占位符
    const val RDS_CHECKOUT_REPOSITORY_URL = "RDS_CHECKOUT_REPOSITORY_URL"
    const val RDS_CHECKOUT_PULL_TYPE = "RDS_CHECKOUT_PULL_TYPE"
    const val RDS_CHECKOUT_REF = "RDS_CHECKOUT_REF"
    const val RDS_TAPD_IDS = "RDS_TAPD_IDS"
    const val RDS_REPO_URLS = "RDS_REPO_URLS"
    const val RDS_PROJECTS = "RDS_PROJECTS"
    const val RDS_SERVICES = "RDS_SERVICES"

    // TODO: 目前全是string后续再修改
    // 将上面的参数变为空的流水线参数
    fun makePipelineParams(init: Boolean, variables: Map<String, Variable>?): List<BuildFormProperty> {
        val params = mutableListOf(
            RDS_CHECKOUT_REPOSITORY_URL,
            RDS_CHECKOUT_PULL_TYPE,
            RDS_CHECKOUT_REF
        )
        if (init) {
            params.addAll(
                listOf(
                    RDS_TAPD_IDS,
                    RDS_REPO_URLS,
                    RDS_PROJECTS,
                    RDS_SERVICES
                )
            )
        }
        val result = params.map {
            BuildFormProperty(
                id = it,
                required = true,
                type = BuildFormPropertyType.STRING,
                readOnly = false,
                defaultValue = "",
                options = null,
                desc = null,
                properties = null,
                glob = null,
                containerType = null,
                scmType = null,
                repoHashId = null,
                relativePath = null
            )
        }.toMutableList()

        // 添加yaml中定义的变量
        result.addAll(getBuildFormPropertyFromYmlVariable(variables))

        return result
    }

    private fun getBuildFormPropertyFromYmlVariable(
        variables: Map<String, Variable>?
    ): List<BuildFormProperty> {
        if (variables.isNullOrEmpty()) {
            return emptyList()
        }
        val buildFormProperties = mutableListOf<BuildFormProperty>()
        variables.forEach { (key, variable) ->
            buildFormProperties.add(
                BuildFormProperty(
                    id = VARIABLE_PREFIX + key,
                    required = false,
                    type = BuildFormPropertyType.STRING,
                    defaultValue = variable.value ?: "",
                    options = null,
                    desc = null,
                    repoHashId = null,
                    relativePath = null,
                    scmType = null,
                    containerType = null,
                    glob = null,
                    properties = null,
                    readOnly = variable.readonly
                )
            )
        }
        return buildFormProperties
    }
}
