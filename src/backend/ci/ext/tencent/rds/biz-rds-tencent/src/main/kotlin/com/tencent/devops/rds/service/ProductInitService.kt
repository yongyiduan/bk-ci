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

import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.api.util.YamlUtil
import com.tencent.devops.rds.chart.ChartParser
import com.tencent.devops.rds.chart.ChartPipeline
import com.tencent.devops.rds.chart.StreamConverter
import com.tencent.devops.rds.chart.stream.StreamBuildResult
import com.tencent.devops.rds.dao.RdsProductInfoDao
import com.tencent.devops.rds.exception.CommonErrorCodeEnum
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
            throw ErrorCodeException(
                errorCode = CommonErrorCodeEnum.PARAMS_FORMAT_ERROR.errorCode,
                defaultMessage = CommonErrorCodeEnum.PARAMS_FORMAT_ERROR.formatErrorMessage,
                params = arrayOf(productId.toString())
            )
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

    fun initChart(
        userId: String,
        chartName: String,
        inputStream: InputStream
    ): Boolean {
        try {
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
            // TODO: 待定项目ID以及渠道
            val projectId = RdsPipelineUtils.genBKProjectCode(productId)

            productInfoDao.updateProduct(
                dslContext = dslContext,
                productId = productId,
                mainYaml = mainYamlStr,
                main = mainObject,
                resourceYaml = resourceYamlStr,
                resource = resourceObject
            )

            // 每个流水线YAML文件与对应编排的映射
            val filePipelineMap = mutableMapOf<String, StreamBuildResult>()
            chartParser.getCacheChartPipelineFiles(cachePath).forEach { file ->
                try {
                    // 生成未指定名称的流水线模型
                    val streamBuildResult = streamConverter.buildModel(
                        userId = userId,
                        productId = productId,
                        projectId = projectId,
                        cachePath = cachePath,
                        pipelineFile = file
                    )
                    logger.info("RDS|init|${file.name}|generate model: ${streamBuildResult.pipelineModel}")
                    filePipelineMap[file.name] = streamBuildResult
                } catch (ignore: Throwable) {
                    logger.warn("RDS|init|${file.name}|generate model with error:", ignore)
                }
            }

            // 对每一个project下的每一个service分别创建对应流水线
            filePipelineMap.forEach nextPipeline@{ (path, stream) ->
                resourceObject.projects.forEach nextProject@{ project ->
                    // 对于没有细分service的直接按project创建
                    project.services?.forEach nextService@{ service ->
                        saveChartPipeline(userId, productId, path, project.id, service.id, stream)
                    } ?: run {
                        val map = project.getProjectResource()
                        saveChartPipeline(userId, productId, path, project.id, null, stream)
                    }
                }
            }

            eventBusService.addEventBusWebhook(
                userId = userId,
                productId = productId,
                projectId = projectId,
                main = mainObject,
                resource = resourceObject
            )
        } catch (t: Throwable) {
            logger.error("RDS|init error|userId=$userId|chartName=$chartName", t)
            return false
        }
        return true
    }

    private fun saveChartPipeline(
        userId: String,
        productId: Long,
        filePath: String,
        projectName: String,
        serviceName: String?,
        stream: StreamBuildResult
    ) {
        val existsPipeline = chartPipeline.getProductPipelineByService(
            productId = productId,
            filePath = filePath,
            projectName = projectName,
            serviceName = serviceName
        )
        // TODO: 提前创建流水线去生成质量红线
        if (existsPipeline == null) {
            // 创建并保存流水线
            chartPipeline.createChartPipeline(
                userId = userId,
                productId = productId,
                projectId = projectName,
                pipeline = RdsPipelineCreate(
                    productId = productId,
                    filePath = filePath,
                    projectName = projectName,
                    serviceName = serviceName,
                    originYaml = stream.originYaml,
                    parsedYaml = stream.parsedYaml,
                    model = stream.pipelineModel
                )
            )
        } else {
            // 更新已有流水线
            chartPipeline.updateChartPipeline(
                userId = userId,
                productId = productId,
                projectId = projectName,
                pipelineId = existsPipeline.pipelineId,
                pipeline = RdsPipelineCreate(
                    productId = productId,
                    filePath = filePath,
                    projectName = projectName,
                    serviceName = serviceName,
                    originYaml = stream.originYaml,
                    parsedYaml = stream.parsedYaml,
                    model = stream.pipelineModel
                )
            )
        }
    }
}
