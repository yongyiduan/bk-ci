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

package com.tencent.devops.rds.pojo.yaml

import com.fasterxml.jackson.annotation.JsonProperty

data class PreResource(
    @JsonProperty("product_id")
    val productId: Long,
    @JsonProperty("product_name")
    val productName: String,
    val projects: List<Map<Any, PreProject>>
) {
    fun getResourceObject(): Resource {
        val projectList = mutableListOf<Project>()
        projects.forEach { projectMap ->
            projectMap.map { (k, v) ->
                projectList.add(
                    Project(
                        id = k.toString(),
                        tapdId = v.tapdId.toString(),
                        bcsId = v.bcsId.toString(),
                        repoUrl = v.repoUrl.toString(),
                        services = v.services?.let { getServiceList(it) }
                    )
                )
            }.toList()
        }
        return Resource(
            productId = productId,
            productName = productName,
            projects = projectList
        )
    }

    private fun getServiceList(
        services: List<Map<Any, PreService>>
    ): List<Service> {
        val serviceList = mutableListOf<Service>()
        services.forEach { serviceMap ->
            serviceMap.forEach { (k, v) ->
                serviceList.add(
                    Service(
                        id = k.toString(),
                        repoUrl = v.repoUrl.toString()
                    )
                )
            }
        }
        return serviceList
    }
}
