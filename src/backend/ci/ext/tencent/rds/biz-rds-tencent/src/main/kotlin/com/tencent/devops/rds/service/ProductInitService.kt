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
import com.tencent.devops.common.api.exception.RemoteServiceException
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.api.util.YamlUtil
import com.tencent.devops.common.api.util.timestamp
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.pipeline.enums.ChannelCode
import com.tencent.devops.common.pipeline.enums.StartType
import com.tencent.devops.process.api.service.ServiceBuildResource
import com.tencent.devops.project.api.service.ServiceProjectResource
import com.tencent.devops.project.api.service.service.ServiceTxProjectResource
import com.tencent.devops.project.pojo.ProjectCreateUserInfo
import com.tencent.devops.rds.chart.ChartParser
import com.tencent.devops.rds.chart.ChartPipeline
import com.tencent.devops.rds.chart.ChartPipelineStartParams.RDS_PROJECTS
import com.tencent.devops.rds.chart.ChartPipelineStartParams.RDS_REPO_URLS
import com.tencent.devops.rds.chart.ChartPipelineStartParams.RDS_SERVICES
import com.tencent.devops.rds.chart.ChartPipelineStartParams.RDS_TAPD_IDS
import com.tencent.devops.rds.chart.StreamConverter
import com.tencent.devops.rds.chart.stream.StreamBuildResult
import com.tencent.devops.rds.constants.Constants
import com.tencent.devops.rds.constants.Constants.CHART_INIT_YAML_FILE_STORAGE
import com.tencent.devops.rds.dao.RdsProductInfoDao
import com.tencent.devops.rds.dao.RdsProductUserDao
import com.tencent.devops.rds.exception.ApiErrorCodeEnum
import com.tencent.devops.rds.exception.CommonErrorCodeEnum
import com.tencent.devops.rds.pojo.ChartResources
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
import com.tencent.devops.rds.pojo.yaml.Tickets
import com.tencent.devops.rds.utils.RdsPipelineUtils
import com.tencent.devops.rds.utils.Yaml
import com.tencent.devops.ticket.api.ServiceCredentialResource
import com.tencent.devops.ticket.pojo.CredentialCreate
import com.tencent.devops.ticket.pojo.enums.CredentialType
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
        private val logger = LoggerFactory.getLogger(ProductInitService::class.java)
    }

    fun initProduct(
        userId: String,
        chartName: String,
        inputStream: InputStream
    ): RdsProductStatusResult {
        // 非异步资源，错误需要返回给客户端
        val status = ProductStatus.INITIALIZING
        val cachePath: String
        val chartResources: ChartResources
        val productCode: String
        val productInfo: RdsProductInfo
        try {
            // 读取并解压缓存到本地磁盘
            cachePath = chartParser.cacheChartDisk(chartName, inputStream)

            // 读取各类资源文件
            chartResources = readResources(cachePath)

            productCode = chartResources.resourceObject.productCode

            // 检查是否init过，init只能进行一次
            if (productInfoDao.get(dslContext, productCode, false) != null) {
                throw ErrorCodeException(
                    errorCode = ApiErrorCodeEnum.PRODUCT_REPEAT_ERROR.errorCode,
                    defaultMessage = ApiErrorCodeEnum.PRODUCT_REPEAT_ERROR.formatErrorMessage
                )
            }

            // 创建蓝盾项目以及添加人员
            createBKProject(
                masterUserId = userId,
                productCode = productCode,
                productName = chartResources.resourceObject.displayName
            )
            productInfo = saveProductInfo(chartResources, status)

            // 添加各类凭证信息
            createCred(userId, productInfo.projectId, chartResources.resourceObject.tickets)

            // 修改状态为初始化中
            productInfoDao.updateProductStatus(
                dslContext = dslContext,
                productCode = chartResources.resourceObject.productCode,
                status = status,
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
                defaultMessage = ApiErrorCodeEnum.UNKNOWN_ERROR.formatErrorMessage.format(arrayOf(e.message ?: ""))
            )
        }

        // 异步资源，错误同步到数据库
        initAsync(
            userId = userId,
            cachePath = cachePath,
            chartResources = chartResources,
            productCode = productCode,
            projectId = productInfo.projectId
        )

        return RdsProductStatusResult(
            productCode = productInfo.productCode,
            productName = productInfo.productName,
            chartName = productInfo.chartName,
            chartVersion = productInfo.chartVersion,
            lastUpdate = productInfo.updateTime,
            status = status.display,
            revision = productInfo.revision,
            notes = "your product ${productInfo.productName} is initializing"
        )
    }

    fun upgradeProduct(
        userId: String,
        chartName: String,
        inputStream: InputStream
    ): RdsProductStatusResult {
        val status = ProductStatus.UPGRADING
        // 非异步资源，错误需要返回给客户端
        val cachePath: String
        val chartResources: ChartResources
        val productCode: String
        val productInfo: RdsProductInfo
        try {
            // 读取并解压缓存到本地磁盘
            cachePath = chartParser.cacheChartDisk(chartName, inputStream)

            // 读取各类资源文件
            chartResources = readResources(cachePath)

            productCode = chartResources.resourceObject.productCode

            // 创建蓝盾项目以及添加人员
            productInfo = saveProductInfo(chartResources, status)

            // 添加各类凭证信息
            createCred(userId, productInfo.projectId, chartResources.resourceObject.tickets)

            // 修改状态为初始化中
            productInfoDao.updateProductStatus(
                dslContext = dslContext,
                productCode = chartResources.resourceObject.productCode,
                status = status,
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
            productCode = productCode,
            projectId = productInfo.projectId
        )

        return RdsProductStatusResult(
            productCode = productInfo.productCode,
            productName = productInfo.productName,
            chartName = productInfo.chartName,
            chartVersion = productInfo.chartVersion,
            lastUpdate = productInfo.updateTime,
            status = ProductStatus.UPGRADING.display,
            revision = productInfo.revision,
            notes = "your product ${productInfo.productName} is upgrading"
        )
    }

    @Async("initExecutor")
    fun initAsync(
        userId: String,
        cachePath: String,
        chartResources: ChartResources,
        productCode: String,
        projectId: String
    ) {
        // 抓总异常，执行失败后保存错误信息
        try {
            initChart(
                userId = userId,
                cachePath = cachePath,
                chartResources = chartResources,
                productCode = productCode,
                projectId = projectId
            )
        } catch (e: Throwable) {
            logger.error("RDS|initChart|failed with error: ", e)
            productInfoDao.updateProductStatus(
                dslContext = dslContext,
                productCode = productCode,
                status = ProductStatus.FAILED,
                errorMsg = e.message
            )
        }
    }

    private fun initChart(
        userId: String,
        cachePath: String,
        chartResources: ChartResources,
        productCode: String,
        projectId: String
    ) {
        val (_, mainObject, _, resourceObject) = chartResources

        // 如果配置了初始化流水线并存在该流水线，则解析并执行，传入特定的resource参数
        runInitYaml(
            mainObject = mainObject,
            cachePath = cachePath,
            userId = userId,
            productCode = productCode,
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
                val streamBuildResult = streamConverter.parseTemplate(
                    userId = userId,
                    productCode = productCode,
                    projectId = projectId,
                    cachePath = cachePath,
                    pipelineFile = file
                )
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
                        saveChartPipeline(userId, productCode, path, project.id, service.id, stream)
                    } catch (ignore: Throwable) {
                        logger.warn("RDS|init|$project|$service|$path|save pipeline error: ", ignore)
                    }
                } ?: run {
                    try {
                        saveChartPipeline(userId, productCode, path, project.id, null, stream)
                    } catch (ignore: Throwable) {
                        logger.warn("RDS|init|$project|$path|save pipeline error: ", ignore)
                    }
                }
            }
        }

        // 添加事件总线
        eventBusService.addEventBusWebhook(
            userId = userId,
            productCode = productCode,
            projectId = projectId,
            main = mainObject,
            resource = resourceObject
        )

        // init流水线会比这个过程慢，所以在那里修改状态即可
    }

    // 获取chart中各类配置数据
    private fun readResources(cachePath: String): ChartResources {
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

        return ChartResources(mainYamlStr, mainObject, resourceYamlStr, resourceObject, rdsYaml)
    }

    private fun saveProductInfo(
        chartResource: ChartResources,
        status: ProductStatus
    ): RdsProductInfo {
        val productCode = chartResource.resourceObject.productCode
        val productName = chartResource.resourceObject.displayName

        val projectId = RdsPipelineUtils.genBKProjectCode(productCode)

        val time = productInfoDao.createOrUpdateProduct(
            dslContext = dslContext,
            productCode = productCode,
            productName = productName,
            projectId = projectId,
            chartName = chartResource.rdsYaml.code,
            chartVersion = chartResource.rdsYaml.version,
            mainYaml = chartResource.mainYamlStr,
            mainParsed = Yaml.marshal(chartResource.mainObject),
            resourceYaml = chartResource.resourceYamlStr,
            resourceParsed = Yaml.marshal(chartResource.resourceObject),
            revision = 1,
            status = status
        )

        return RdsProductInfo(
            productCode = productCode,
            productName = productName,
            projectId = projectId,
            chartName = chartResource.rdsYaml.code,
            chartVersion = chartResource.rdsYaml.version,
            mainYaml = "",
            mainParsed = "",
            resourceYaml = "",
            resourceParsed = "",
            revision = 1,
            status = status,
            createTime = time.timestamp(),
            updateTime = time.timestamp()
        )
    }

    private fun createBKProject(masterUserId: String, productCode: String, productName: String): String {
        val userList = productUserDao.getProductUserList(dslContext, productCode)
        if (userList.isEmpty()) {
            throw ErrorCodeException(
                errorCode = CommonErrorCodeEnum.PRODUCT_NOT_EXISTS.errorCode,
                defaultMessage = CommonErrorCodeEnum.PRODUCT_NOT_EXISTS.formatErrorMessage,
                params = arrayOf(productCode)
            )
        }
        val userMap = userList.associate { it.userId to it.type }
        logger.info("RDS|createBKProject|productCode=$productCode|userMap=$userMap")
        if (userMap[masterUserId] != ProductUserType.MASTER.name) {
            throw ErrorCodeException(
                errorCode = CommonErrorCodeEnum.PRODUCT_PERMISSION_INVALID.errorCode,
                defaultMessage = CommonErrorCodeEnum.PRODUCT_PERMISSION_INVALID.formatErrorMessage,
                params = arrayOf(masterUserId)
            )
        }

        val projectId = RdsPipelineUtils.genBKProjectCode(productCode)
        val projectResult =
            client.get(ServiceProjectResource::class).get(englishName = projectId).data
        if (projectResult == null) {
            val createResult =
                client.get(ServiceTxProjectResource::class).getOrCreateRdsProject(
                    userId = masterUserId,
                    projectId = projectId,
                    projectName = productName
                )
            if (createResult.isNotOk() || createResult.data == null) {
                throw ErrorCodeException(
                    errorCode = CommonErrorCodeEnum.INIT_PROJECT_ERROR.errorCode,
                    defaultMessage = CommonErrorCodeEnum.INIT_PROJECT_ERROR.formatErrorMessage,
                    params = arrayOf(createResult.message ?: createResult.code.toString())
                )
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
        return projectId
    }

    private fun createCred(
        masterUserId: String,
        projectId: String,
        tickets: Tickets
    ) {
        // 创建工蜂凭据
        if (!checkTicketExist(projectId, Constants.RDS_GIT_TICKET)) {
            createCred(
                projectId, masterUserId,
                CredentialCreate(
                    credentialId = Constants.RDS_GIT_TICKET,
                    credentialName = "RDS使用的内置凭证-TGIT-请勿修改",
                    credentialType = CredentialType.ACCESSTOKEN,
                    credentialRemark = "RDS使用的内置凭证，用于工蜂，请勿修改",
                    v1 = tickets.tGit.accessToken
                )
            )
        }

        // 创建bkrepo凭据
        if (!checkTicketExist(projectId, Constants.RDS_BKREPO_TICKET)) {
            createCred(
                projectId, masterUserId,
                CredentialCreate(
                    credentialId = Constants.RDS_BKREPO_TICKET,
                    credentialName = "RDS使用的内置凭证-BKREPO-请勿修改",
                    credentialType = CredentialType.USERNAME_PASSWORD,
                    credentialRemark = "RDS使用的内置凭证，用于制品库，请勿修改",
                    v1 = tickets.bkRepo.username,
                    v2 = tickets.bkRepo.accessToken
                )
            )
        }

        // 创建image凭据
        if (!checkTicketExist(projectId, Constants.RDS_IMAGE_TICKET)) {
            createCred(
                projectId, masterUserId,
                CredentialCreate(
                    credentialId = Constants.RDS_IMAGE_TICKET,
                    credentialName = "RDS使用的内置凭证-IMAGE-请勿修改",
                    credentialType = CredentialType.USERNAME_PASSWORD,
                    credentialRemark = "RDS使用的内置凭证，用于镜像仓库，请勿修改",
                    v1 = tickets.image.username,
                    v2 = tickets.image.accessToken
                )
            )
        }
    }

    private fun checkTicketExist(
        projectId: String,
        credId: String
    ): Boolean {
        try {
            client.get(ServiceCredentialResource::class).check(
                projectId = projectId,
                credentialId = credId
            )
            return true
        } catch (exception: RemoteServiceException) {
            if (exception.httpStatus != 404) {
                throw exception
            }
        }
        return false
    }

    private fun createCred(
        projectId: String,
        masterUserId: String,
        credCreate: CredentialCreate
    ) {
        try {
            val result = client.get(ServiceCredentialResource::class).create(
                userId = masterUserId,
                projectId = projectId,
                credential = credCreate
            ).data
            if (result == null || !result) {
                throw RuntimeException("创建结果为空或false")
            }
        } catch (e: Throwable) {
            logger.error("create rds ticket error", e)
            throw RuntimeException("创建RDS凭据失败 ${e.message}")
        }
    }

    private fun runInitYaml(
        mainObject: Main,
        cachePath: String,
        userId: String,
        productCode: String,
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
            val streamBuildResult = streamConverter.parseTemplate(
                userId = userId,
                productCode = productCode,
                projectId = projectId,
                cachePath = cachePath,
                pipelineFile = initYamlFile,
                init = true
            )
            val pipelineId = saveChartPipeline(
                userId = userId,
                productCode = productCode,
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
                    RDS_TAPD_IDS to JsonUtil.toJson(tapdIds),
                    RDS_REPO_URLS to JsonUtil.toJson(repoUrls),
                    RDS_PROJECTS to JsonUtil.toJson(projects),
                    RDS_SERVICES to JsonUtil.toJson(services)
                ),
                channelCode = ChannelCode.BS,
                startType = StartType.SERVICE
            )
        }
    }

    private fun saveChartPipeline(
        userId: String,
        productCode: String,
        filePath: String,
        projectName: String?,
        serviceName: String?,
        stream: StreamBuildResult,
        initPipeline: Boolean? = false
    ): String {
        val existsPipeline = chartPipeline.getProductPipelineByService(
            productCode = productCode,
            filePath = filePath,
            projectName = projectName,
            serviceName = serviceName
        )
        val pipelineId = existsPipeline?.pipelineId
            ?: chartPipeline.createDefaultPipeline(
                userId = userId,
                productCode = productCode,
                projectId = RdsPipelineUtils.genBKProjectCode(productCode),
                pipeline = RdsPipelineCreate(
                    productCode = productCode,
                    filePath = filePath,
                    projectName = projectName,
                    serviceName = serviceName,
                    originYaml = stream.originYaml,
                    parsedYaml = stream.parsedYaml,
                    yamlObject = stream.yamlObject
                ),
                initPipeline = initPipeline
            )

        // 更新已有流水线
        chartPipeline.updateChartPipeline(
            userId = userId,
            productCode = productCode,
            projectId = RdsPipelineUtils.genBKProjectCode(productCode),
            pipelineId = pipelineId,
            pipeline = RdsPipelineCreate(
                productCode = productCode,
                filePath = filePath,
                projectName = projectName,
                serviceName = serviceName,
                originYaml = stream.originYaml,
                parsedYaml = stream.parsedYaml,
                yamlObject = stream.yamlObject
            ),
            initPipeline = initPipeline
        )
        return pipelineId
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
