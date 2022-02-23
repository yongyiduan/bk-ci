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

package com.tencent.devops.rds.service

import com.tencent.devops.rds.chart.ChartParser
import com.tencent.devops.rds.chart.stream.StreamService
import com.tencent.devops.rds.pojo.RdsPipelineCreate
import java.io.InputStream
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class InitService @Autowired constructor(
    private val chartParser: ChartParser,
    private val streamService: StreamService,
    private val chartPipelineService: ChartPipelineService
) {

    fun init(
        userId: String,
        chartName: String,
        inputStream: InputStream
    ): Boolean {
        // 读取并解压缓存到本地磁盘
        val cachePath = chartParser.cacheChartDisk(chartName, inputStream)

        // TODO: 通过缓存读取 main.yml/resource.yml 中的内容来获取产品信息来保存
        val productId = ""

        val pipelineFiles = chartParser.getCacheChartPipelineFiles(cachePath)
        pipelineFiles.forEach { pipelineFile ->
            // 通过stream模板替换生成流水线
            val streamBuildResult = streamService.buildModel(cachePath, pipelineFile)

            // 创建并保存流水线
            chartPipelineService.createChartPipeline(
                userId = userId,
                productId = productId,
                chartPipeline = Pair(
                    RdsPipelineCreate(
                        productId = productId,
                        filePath = pipelineFile.name,
                        originYaml = streamBuildResult.originYaml,
                        parsedYaml = streamBuildResult.parsedYaml
                    ),
                    streamBuildResult.pipelineModel
                )
            )
        }

        return true
    }
}
