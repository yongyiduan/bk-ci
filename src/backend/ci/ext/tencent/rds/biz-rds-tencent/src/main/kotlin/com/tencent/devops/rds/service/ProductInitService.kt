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

import com.tencent.devops.common.api.constant.HTTP_500
import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.api.util.YamlUtil
import com.tencent.devops.common.api.util.timestampmilli
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.pipeline.enums.ChannelCode
import com.tencent.devops.common.pipeline.enums.StartType
import com.tencent.devops.process.api.service.ServiceBuildResource
import com.tencent.devops.project.api.service.ServiceProjectResource
import com.tencent.devops.project.pojo.ProjectCreateInfo
import com.tencent.devops.project.pojo.ProjectCreateUserInfo
import com.tencent.devops.rds.chart.ChartParser
import com.tencent.devops.rds.chart.ChartPipeline
import com.tencent.devops.rds.chart.StreamConverter
import com.tencent.devops.rds.chart.stream.StreamBuildResult
import com.tencent.devops.rds.constants.Constants
import com.tencent.devops.rds.constants.Constants.CHART_INIT_YAML_FILE_STORAGE
import com.tencent.devops.rds.dao.RdsProductInfoDao
import com.tencent.devops.rds.dao.RdsProductUserDao
import com.tencent.devops.rds.exception.ApiErrorCodeEnum
import com.tencent.devops.rds.exception.CommonErrorCodeEnum
import com.tencent.devops.rds.pojo.ChartResources
import com.tencent.devops.rds.pojo.ClientConfigYaml
import com.tencent.devops.rds.pojo.RdsPipelineCreate
import com.tencent.devops.rds.pojo.RdsProductInfo
import com.tencent.devops.rds.pojo.RdsProductStatusResult
import com.tencent.devops.rds.pojo.RdsYaml
import com.tencent.devops.rds.pojo.enums.ProductStatus
import com.tencent.devops.rds.pojo.enums.ProductUserType
import com.tencent.devops.rds.pojo.yaml.Main
import com.tencent.devops.rds.pojo.yaml.PreMain
import com.tencent.devops.rds.pojo.yaml.PreResource
import com.tencent.devops.rds.pojo.yaml.Resource
import com.tencent.devops.rds.utils.RdsPipelineUtils
import com.tencent.devops.rds.utils.Yaml
import org.apache.commons.io.FileUtils
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import kotlin.math.log

@Service
class ProductInitService @Autowired constructor(
    private val client: Client,
    private val dslContext: DSLContext,
    private val chartParser: ChartParser,
    private val streamConverter: StreamConverter,
    private val chartPipeline: ChartPipeline,
    private val productInfoDao: RdsProductInfoDao,
    private val productUserDao: RdsProductUserDao,
    private val eventBusService: EventBusService
) {

    companion object {
        private const val VARIABLE_PREFIX = "variables."
        private val logger = LoggerFactory.getLogger(ProductInitService::class.java)
    }

    fun init(
        userId: String,
        chartName: String,
        inputStream: InputStream
    ): RdsProductStatusResult {
        // 非异步资源，错误需要返回给客户端
        var cachePath: String
        var chartResources: ChartResources
        var productId: Long
        var productInfo: RdsProductInfo
        try {
            // 读取并解压缓存到本地磁盘
            cachePath = chartParser.cacheChartDisk(chartName, inputStream)

            // 读取各类资源文件
            chartResources = readResources(cachePath)

            productId = chartResources.resourceObject.productId

            // 创建蓝盾项目以及添加人员
            productInfo = createProduct(userId, chartResources)

            // 修改状态为初始化中
            productInfoDao.updateProductStatus(
                dslContext = dslContext,
                productId = productId,
                status = ProductStatus.INITIALIZING,
                errorMsg = null
            )
        } catch (e: Throwable) {
            when (e) {
                is ErrorCodeException -> throw e
            }
            logger.error("RDS|init error|userId=$userId|chartName=$chartName", e)
            throw ErrorCodeException(
                statusCode = HTTP_500,
                errorCode = ApiErrorCodeEnum.UNKNOWN_ERROR.errorCode,
                params = arrayOf(e.message ?: "")
            )
        }

        // 异步资源，错误同步到数据库
        initAsync(
            userId = userId,
            cachePath = cachePath,
            chartResources = chartResources,
            productId = productId,
            projectId = productInfo.projectId
        )

        return RdsProductStatusResult(
            productId = productInfo.productId,
            productName = productInfo.productName,
            lastUpdate = productInfo.updateTime,
            status = ProductStatus.INITIALIZING.display,
            revision = productInfo.revision,
            notes = "your product ${productInfo.productName} is initializing"
        )
    }

    @Async("initExecutor")
    fun initAsync(
        userId: String,
        cachePath: String,
        chartResources: ChartResources,
        productId: Long,
        projectId: String
    ) {
        // 抓总异常，执行失败后保存错误信息
        try {
            initChart(
                userId = userId,
                cachePath = cachePath,
                chartResources = chartResources,
                productId = productId,
                projectId = projectId
            )
        } catch (e: Throwable) {
            productInfoDao.updateProductStatus(
                dslContext = dslContext,
                productId = productId,
                status = ProductStatus.FAILED,
                errorMsg = e.message
            )
        }
    }

    private fun initChart(
        userId: String,
        cachePath: String,
        chartResources: ChartResources,
        productId: Long,
        projectId: String
    ) {
        val (_, _, mainObject, _, resourceObject) = chartResources

        // 如果配置了初始化流水线并存在该流水线，则解析并执行，传入特定的resource参数
        runInitYaml(
            mainObject = mainObject,
            cachePath = cachePath,
            userId = userId,
            productId = productId,
            projectId = projectId,
            resourceObject = resourceObject
        )

        // 每个流水线YAML文件与对应编排的映射
        val filePipelineMap = mutableMapOf<String, StreamBuildResult>()
        val actionPipelines = mainObject.getPipelineYamlNames()
        chartParser.getCacheChartPipelineFiles(cachePath).forEach { file ->
            if (!actionPipelines.contains(file.name)) {
                logger.warn("RDS|init|${file.name}|skip")
                return@forEach
            }
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
                    try {
                        saveChartPipeline(userId, productId, path, project.id, service.id, stream)
                    } catch (ignore: Throwable) {
                        logger.warn("RDS|init|$project|$service|$path|save pipeline error: ", ignore)
                    }

                } ?: run {
                    try {
                        saveChartPipeline(userId, productId, path, project.id, null, stream)
                    } catch (ignore: Throwable) {
                        logger.warn("RDS|init|$project|$path|save pipeline error: ", ignore)
                    }
                }
            }
        }

        // 添加事件总线
        eventBusService.addEventBusWebhook(
            userId = userId,
            productId = productId,
            projectId = projectId,
            main = mainObject,
            resource = resourceObject
        )

        // init流水线会比这个过程慢，所以在那里修改状态即可
    }

    // 获取chart中各类配置数据
    private fun readResources(cachePath: String): ChartResources {
        // 读取用户配置文件，获取各类token
        val clientConfigStr = chartParser.getCacheChartFile(cachePath, Constants.CHART_CLIENT_CONFIG_YAML_FILE)
        logger.info("RDS|init|clientConfig|clientConfigStr=$clientConfigStr")
        val clientConfig = YamlUtil.getObjectMapper().readValue(
            clientConfigStr,
            ClientConfigYaml::class.java
        )

        val mainYamlStr = chartParser.getCacheChartFile(cachePath, Constants.CHART_MAIN_YAML_FILE)!!
        logger.info("RDS|init|MainFile|mainYamlStr=$mainYamlStr")
        val mainYaml = YamlUtil.getObjectMapper().readValue(
            mainYamlStr,
            PreMain::class.java
        )
        val mainObject = mainYaml.getMainObject()
        logger.info("RDS|init|MainFile|mainObject=$mainObject|mainYaml=$mainYaml")

        val resourceYamlStr = chartParser.getCacheChartFile(cachePath, Constants.CHART_RESOURCE_YAML_FILE)!!
        logger.info("RDS|ResourceFile|resourceYamlStr=$resourceYamlStr")
        val resourceYaml = YamlUtil.getObjectMapper().readValue(
            resourceYamlStr,
            PreResource::class.java
        )
        val resourceObject = resourceYaml.getResourceObject()
        logger.info("RDS|init|ResourceFile|resourceObject=$resourceObject|resourceYaml=$resourceYaml")

        val rdsYamlStr = chartParser.getCacheChartFile(cachePath, Constants.CHART_RDS_YAML_FILE)
        logger.info("RDS|RdsFile|rdsYamlStr=$rdsYamlStr")
        val rdsYaml = YamlUtil.getObjectMapper().readValue(
            rdsYamlStr,
            RdsYaml::class.java
        )

        return ChartResources(clientConfig, mainYamlStr, mainObject, resourceYamlStr, resourceObject, rdsYaml)
    }

    private fun createProduct(masterUserId: String, chartResource: ChartResources): RdsProductInfo {
        val productId = chartResource.resourceObject.productId
        val productName = chartResource.resourceObject.productName

        val userMap = productUserDao.getProductUserList(dslContext, productId)
            .associate { it.userId to it.type }
        if (userMap[masterUserId] != ProductUserType.MASTER.name) {
            throw ErrorCodeException(
                errorCode = CommonErrorCodeEnum.PRODUCT_NOT_EXISTS.errorCode,
                defaultMessage = CommonErrorCodeEnum.PRODUCT_NOT_EXISTS.formatErrorMessage,
                params = arrayOf(productId.toString())
            )
        }

        val projectId = RdsPipelineUtils.genBKProjectCode(productId)
        val projectResult =
            client.get(ServiceProjectResource::class).get(englishName = projectId).data
        if (projectResult == null){
            val createResult =
                client.get(ServiceProjectResource::class).create(
                    userId = masterUserId,
                    projectCreateInfo = ProjectCreateInfo(
                        projectName = productName,
                        englishName = projectId,
                        description = "RDS project with product id: $productId"
                    )
                )
            if (createResult.isNotOk()) {
                throw RuntimeException("Create git ci project in devops failed," +
                    " msg: ${createResult.message}")
            }
        } else {
            logger.warn("RDS project($projectId) already exists.")
        }

        // 增加所有项目成员
        val managers = userMap.filter { it.value == ProductUserType.MASTER.name }.keys.toList()
        client.get(ServiceProjectResource::class).createProjectUser(
            projectId = projectId,
            createInfo = ProjectCreateUserInfo(
                createUserId = masterUserId,
                roleName = "管理员",
                roleId = 2,
                userIds = managers
            )
        ).data?.let {
            if (!it) {
                throw RuntimeException("Create project $projectId managers $managers failed")
            }
        }

        client.get(ServiceProjectResource::class).createProjectUser(
            projectId = projectId,
            createInfo = ProjectCreateUserInfo(
                createUserId = masterUserId,
                roleName = "CI管理员",
                roleId = 9,
                userIds = userMap.keys.toList()
            )
        ).data?.let {
            if (!it) {
                throw RuntimeException("Create project $projectId members ${userMap.keys.toList()} failed")
            }
        }

        val time = productInfoDao.createOrUpdateProduct(
            dslContext = dslContext,
            productId = productId,
            productName = productName,
            projectId = projectId,
            chartName = chartResource.rdsYaml.code,
            chartVersion = chartResource.rdsYaml.version,
            mainYaml = chartResource.mainYamlStr,
            mainParsed = Yaml.marshal(chartResource.mainObject),
            resourceYaml = chartResource.resourceYamlStr,
            resourceParsed = Yaml.marshal(chartResource.resourceObject),
            revision = 1,
            status = ProductStatus.INITIALIZING
        )

        return RdsProductInfo(
            productId = productId,
            productName = productName,
            projectId = projectId,
            chartName = chartResource.rdsYaml.code,
            chartVersion = chartResource.rdsYaml.version,
            mainYaml = "",
            mainParsed = "",
            resourceYaml = "",
            resourceParsed = "",
            revision = 1,
            status = ProductStatus.INITIALIZING,
            createTime = time.timestampmilli(),
            updateTime = time.timestampmilli()
        )
    }

    private fun runInitYaml(
        mainObject: Main,
        cachePath: String,
        userId: String,
        productId: Long,
        projectId: String,
        resourceObject: Resource
    ) {
        mainObject.init?.streamPath?.let { streamPath ->
            val initYamlFile = File(
                Paths.get(
                    cachePath,
                    Constants.CHART_TEMPLATE_DIR + File.separator + streamPath
                ).toUri()
            )
            if (!initYamlFile.exists()) {
                logger.warn("RDS|init|Init pipeline file not found: ${initYamlFile.canonicalPath}")
                return@let
            }
            logger.info(
                "RDS|init|InitPipelineFile|" +
                    "initYamlStr=${FileUtils.readFileToString(initYamlFile, StandardCharsets.UTF_8)}"
            )
            val streamBuildResult = streamConverter.buildModel(
                userId = userId,
                productId = productId,
                projectId = projectId,
                cachePath = cachePath,
                pipelineFile = initYamlFile
            )
            val pipelineId = saveChartPipeline(
                userId = userId,
                productId = productId,
                filePath = CHART_INIT_YAML_FILE_STORAGE,
                projectName = null,
                serviceName = null,
                stream = streamBuildResult,
                initPipeline = true
            )
            val (tapdIds, repoUrls) = resourceObject.getAllTapdIdsAndRepoUrls()
            val (projects, services) = resourceObject.getAllProjectAndServiceNames()
            logger.info("RDS|init|resourceInit|tapdIds=$tapdIds|repoUrls=repoUrls")
            client.get(ServiceBuildResource::class).manualStartupNew(
                userId = userId,
                projectId = projectId,
                pipelineId = pipelineId,
                // 传入特定的resource参数
                values = mapOf(
                    "${VARIABLE_PREFIX}tapd_ids" to JsonUtil.toJson(tapdIds),
                    "${VARIABLE_PREFIX}repo_urls" to JsonUtil.toJson(repoUrls),
                    "${VARIABLE_PREFIX}projects" to JsonUtil.toJson(projects),
                    "${VARIABLE_PREFIX}services" to JsonUtil.toJson(services)
                ),
                channelCode = ChannelCode.BS,
                startType = StartType.SERVICE
            )
        }
    }

    private fun saveChartPipeline(
        userId: String,
        productId: Long,
        filePath: String,
        projectName: String?,
        serviceName: String?,
        stream: StreamBuildResult,
        initPipeline: Boolean? = false
    ): String {
        val existsPipeline = chartPipeline.getProductPipelineByService(
            productId = productId,
            filePath = filePath,
            projectName = projectName,
            serviceName = serviceName
        )
        // TODO: 提前创建流水线去生成质量红线
        return if (existsPipeline == null) {
            // 创建并保存流水线
            chartPipeline.createChartPipeline(
                userId = userId,
                productId = productId,
                projectId = RdsPipelineUtils.genBKProjectCode(productId),
                pipeline = RdsPipelineCreate(
                    productId = productId,
                    filePath = filePath,
                    projectName = projectName,
                    serviceName = serviceName,
                    originYaml = stream.originYaml,
                    parsedYaml = stream.parsedYaml,
                    model = stream.pipelineModel.copy(
                        name = RdsPipelineUtils.genBKPipelineName(
                            filePath, projectName, serviceName
                        )
                    )
                ),
                initPipeline = initPipeline
            )
        } else {
            // 更新已有流水线
            chartPipeline.updateChartPipeline(
                userId = userId,
                productId = productId,
                projectId = RdsPipelineUtils.genBKProjectCode(productId),
                pipelineId = existsPipeline.pipelineId,
                pipeline = RdsPipelineCreate(
                    productId = productId,
                    filePath = filePath,
                    projectName = projectName,
                    serviceName = serviceName,
                    originYaml = stream.originYaml,
                    parsedYaml = stream.parsedYaml,
                    model = stream.pipelineModel.copy(
                        name = RdsPipelineUtils.genBKPipelineName(
                            filePath, projectName, serviceName
                        )
                    )
                )
            )
            existsPipeline.pipelineId
        }
    }

    // 注意：内部定制方法，无法开源
    private fun Resource.getAllTapdIdsAndRepoUrls(): Pair<List<String>, List<String>> {
        with(this) {
            val ids = mutableListOf<String>()
            val urls = mutableListOf<String>()
            this.projects.forEach { project ->
                project.tapdId?.let { ids.add(it) }
                project.repoUrl?.let { urls.add(it) }
                project.services?.forEach { service ->
                    service.repoUrl.let { urls.add(it) }
                }
            }
            return Pair(ids, urls)
        }
    }

    // 注意：内部定制方法，无法开源
    private fun Resource.getAllProjectAndServiceNames(): Pair<List<String>, List<String>> {
        with(this) {
            val projects = mutableListOf<String>()
            val services = mutableListOf<String>()
            this.projects.forEach { project ->
                projects.add(project.id)
                project.services?.forEach { service ->
                    services.add(service.id)
                }
            }
            return Pair(projects, services)
        }
    }
}
