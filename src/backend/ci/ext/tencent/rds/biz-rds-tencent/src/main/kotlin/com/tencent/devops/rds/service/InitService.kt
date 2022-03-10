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

import com.tencent.devops.common.api.util.YamlUtil
import com.tencent.devops.rds.chart.ChartParser
import com.tencent.devops.rds.chart.ChartPipeline
import com.tencent.devops.rds.chart.StreamConverter
import com.tencent.devops.rds.dao.RdsChartPipelineDao
import com.tencent.devops.rds.dao.RdsProductInfoDao
import com.tencent.devops.rds.pojo.RdsPipelineCreate
import com.tencent.devops.rds.pojo.yaml.PreMain
import com.tencent.devops.rds.pojo.yaml.PreResource
import com.tencent.devops.rds.utils.RdsPipelineUtils
import java.io.InputStream
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class InitService @Autowired constructor(
    private val dslContext: DSLContext,
    private val chartParser: ChartParser,
    private val streamConverter: StreamConverter,
    private val chartPipeline: ChartPipeline,
    private val productInfoDao: RdsProductInfoDao,
    private val eventBusService: EventBusService
) {

    private val logger = LoggerFactory.getLogger(InitService::class.java)

    fun init(
        userId: String,
        chartName: String,
        inputStream: InputStream
    ): Boolean {
        // 读取并解压缓存到本地磁盘
        val cachePath = chartParser.cacheChartDisk(chartName, inputStream)

        // TODO: 创建对应的蓝盾项目
        val productId = 1L
//        val projectId = RdsPipelineUtils.genBKProjectCode(productId)
        val projectId = "rds-temp-test-project"

        val mainYamlStr = chartParser.getCacheChartMainFile(cachePath)
        logger.info("RDS|init|MainFile|mainYamlStr=$mainYamlStr")
        val mainYaml = YamlUtil.getObjectMapper().readValue(
            mainYamlStr,
            PreMain::class.java
        )
        val mainObject = mainYaml.getMainObject()
        logger.info("RDS|init|MainFile|mainObject=$mainObject|mainYaml=$mainYaml")

        val resourceYamlStr = chartParser.getCacheChartResourceFile(cachePath)
        logger.info("RDS|ResourceFile|resourceYamlStr=$resourceYamlStr")
        val resourceYaml = YamlUtil.getObjectMapper().readValue(
            resourceYamlStr,
            PreResource::class.java
        )
        val resourceObject = resourceYaml.getResourceObject()
        logger.info("RDS|init|ResourceFile|resourceObject=$resourceObject|resourceYaml=$resourceYaml")

        productInfoDao.saveProduct(
            dslContext = dslContext,
            projectId = projectId,
            creator = userId,
            mainYaml = mainYamlStr,
            main = mainObject,
            resourceYaml = resourceYamlStr,
            resource = resourceObject
        )

        // TODO: 提前创建流水线去生成质量红线
        val pipelineFiles = chartParser.getCacheChartPipelineFiles(cachePath)
        pipelineFiles.forEach { pipelineFile ->
            // 通过stream模板替换生成流水线
            val streamBuildResult = streamConverter.buildModel(
                userId = userId,
                productId = productId,
                projectId = projectId,
                cachePath = cachePath,
                pipelineFile = pipelineFile
            )

            logger.info("${pipelineFile.name} model: ${streamBuildResult.pipelineModel}")

            // 创建并保存流水线
            chartPipeline.createChartPipeline(
                userId = userId,
                productId = productId,
                projectId = projectId,
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
        eventBusService.addWebhook(productId, projectId, mainObject, resourceObject)

        return true
    }
}
