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
import com.tencent.devops.common.client.Client
import com.tencent.devops.rds.chart.ChartParser
import com.tencent.devops.rds.chart.ChartPipeline
import com.tencent.devops.rds.chart.StreamConverter
import com.tencent.devops.rds.dao.RdsProductInfoDao
import com.tencent.devops.rds.exception.CommonErrorCodeEnum
import com.tencent.devops.rds.exception.RdsErrorCodeException
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
class ProductInitService @Autowired constructor(
    private val client: Client,
    private val dslContext: DSLContext,
    private val chartParser: ChartParser,
    private val streamConverter: StreamConverter,
    private val chartPipeline: ChartPipeline,
    private val productInfoDao: RdsProductInfoDao,
    private val eventBusService: EventBusService
) {

    private val logger = LoggerFactory.getLogger(ProductInitService::class.java)

    fun createProduct(productId: Long, userId: String, rdsProjectName: String?) {
        val origin = productInfoDao.getProduct(dslContext, productId)
        if (origin != null) {
            throw RdsErrorCodeException(CommonErrorCodeEnum.PARAMS_FORMAT_ERROR, arrayOf(productId.toString()))
        }
        // TODO 创建什么样的蓝盾项目待定
//        val projectResult =
//            client.get(ServiceTxProjectResource::class).createGitCIProject(
//                gitProjectId = productId,
//                userId = userId,
//                gitProjectName = rdsProjectName
//            )
//        if (projectResult.isNotOk()) {
//            throw RuntimeException("Create git ci project in devops failed, msg: ${projectResult.message}")
//        }
        val projectId = RdsPipelineUtils.genBKProjectCode(productId)
        productInfoDao.createProduct(dslContext, projectId, userId)
    }

    fun init(
        userId: String,
        chartName: String,
        inputStream: InputStream
    ): Boolean {
        // 读取并解压缓存到本地磁盘
        val cachePath = chartParser.cacheChartDisk(chartName, inputStream)

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
        val productId = resourceObject.productId
        val projectId = RdsPipelineUtils.genBKProjectCode(productId)

        productInfoDao.saveProductInfo(
            dslContext = dslContext,
            projectId = projectId,
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
        eventBusService.addWebhook(
            userId = userId,
            productId = productId,
            projectId = projectId,
            main = mainObject,
            resource = resourceObject
        )

        return true
    }
}
