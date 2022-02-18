/*
 *
 *  * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *  *
 *  * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *  *
 *  * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *  *
 *  * A copy of the MIT License is included in this file.
 *  *
 *  *
 *  * Terms of the MIT License:
 *  * ---------------------------------------------------
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 *  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *  * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 *  * the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 *  * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 *  * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.tencent.devops.rds.chart

import com.tencent.devops.common.api.util.YamlUtil
import com.tencent.devops.rds.repo.GitRepoServiceImpl
import com.tencent.devops.rds.utils.Yaml
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ChartService @Autowired constructor(
    private val repoService: GitRepoServiceImpl
) {
    companion object {
        private const val STREAM_YAML_DIR = "templates"
    }

    fun initChart(
        chartRepoName: String,
        resourceYaml: String,
        valuesYaml: String?
    ) {
        // 获取repo的访问信息
        val repoId = repoService.getRepoInfo(chartRepoName).repoId
        val repoToken = repoService.getRepoToken(repoId = repoId)

        // 获取当前chart下的stream流水线
        val streamPaths = repoService.getFileTree(repoId = repoId, path = STREAM_YAML_DIR, token = repoToken).map {
            it.fileName
        }

        // 读取Values文件为对象
        val values = Values.readValues(valuesYaml)

        streamPaths.forEach { path ->
            // 获取具体文件内容
            val fileContent = repoService.getFile(repoId, path)

            // 替换values
        }
    }
}
