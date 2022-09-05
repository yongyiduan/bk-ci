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

package com.tencent.devops.store.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tencent.devops.common.api.constant.APPROVE
import com.tencent.devops.common.api.constant.BEGIN
import com.tencent.devops.common.api.constant.COMMIT
import com.tencent.devops.common.api.constant.CommonMessageCode
import com.tencent.devops.common.api.constant.DOING
import com.tencent.devops.common.api.constant.EDIT
import com.tencent.devops.common.api.constant.END
import com.tencent.devops.common.api.constant.FAIL
import com.tencent.devops.common.api.constant.KEY_REPOSITORY_HASH_ID
import com.tencent.devops.common.api.constant.NUM_FIVE
import com.tencent.devops.common.api.constant.NUM_FOUR
import com.tencent.devops.common.api.constant.NUM_ONE
import com.tencent.devops.common.api.constant.NUM_SEVEN
import com.tencent.devops.common.api.constant.NUM_SIX
import com.tencent.devops.common.api.constant.NUM_THREE
import com.tencent.devops.common.api.constant.NUM_TWO
import com.tencent.devops.common.api.constant.ONLINE
import com.tencent.devops.common.api.constant.SUCCESS
import com.tencent.devops.common.api.constant.TEST
import com.tencent.devops.common.api.constant.TEST_ENV_PREPARE
import com.tencent.devops.common.api.constant.UNDO
import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.api.util.UUIDUtil
import com.tencent.devops.common.auth.api.AuthPermission
import com.tencent.devops.common.auth.api.AuthPermissionApi
import com.tencent.devops.common.auth.api.AuthResourceType
import com.tencent.devops.common.auth.code.PipelineAuthServiceCode
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.redis.RedisOperation
import com.tencent.devops.common.service.utils.MessageCodeUtil
import com.tencent.devops.project.api.service.ServiceItemResource
import com.tencent.devops.project.api.service.ServiceProjectResource
import com.tencent.devops.project.constant.ProjectConstant.ITEM_BK_SERVICE_REDIS_KEY
import com.tencent.devops.store.config.ExtServiceKubernetesNameSpaceConfig
import com.tencent.devops.store.constant.StoreMessageCode
import com.tencent.devops.store.dao.ExtServiceDao
import com.tencent.devops.store.dao.ExtServiceEnvDao
import com.tencent.devops.store.dao.ExtServiceFeatureDao
import com.tencent.devops.store.dao.ExtServiceItemRelDao
import com.tencent.devops.store.dao.ExtServiceLabelRelDao
import com.tencent.devops.store.dao.ExtServiceVersionLogDao
import com.tencent.devops.store.dao.common.StoreBuildInfoDao
import com.tencent.devops.store.dao.common.StoreMediaInfoDao
import com.tencent.devops.store.dao.common.StoreMemberDao
import com.tencent.devops.store.dao.common.StoreProjectRelDao
import com.tencent.devops.store.dao.common.StoreStatisticTotalDao
import com.tencent.devops.store.pojo.EditInfoDTO
import com.tencent.devops.store.pojo.ExtServiceCreateInfo
import com.tencent.devops.store.pojo.ExtServiceEnvCreateInfo
import com.tencent.devops.store.pojo.ExtServiceFeatureCreateInfo
import com.tencent.devops.store.pojo.ExtServiceFeatureUpdateInfo
import com.tencent.devops.store.pojo.ExtServiceItemRelCreateInfo
import com.tencent.devops.store.pojo.ExtServiceUpdateInfo
import com.tencent.devops.store.pojo.ExtServiceVersionLogCreateInfo
import com.tencent.devops.store.pojo.ExtensionJson
import com.tencent.devops.store.pojo.ItemPropCreateInfo
import com.tencent.devops.store.pojo.common.DeptInfo
import com.tencent.devops.store.pojo.common.EXTENSION_JSON_NAME
import com.tencent.devops.store.pojo.common.KEY_CODE_SRC
import com.tencent.devops.store.pojo.common.ReleaseProcessItem
import com.tencent.devops.store.pojo.common.StoreMediaInfoRequest
import com.tencent.devops.store.pojo.common.StoreProcessInfo
import com.tencent.devops.store.pojo.common.UN_RELEASE
import com.tencent.devops.store.pojo.common.enums.ReleaseTypeEnum
import com.tencent.devops.store.pojo.common.enums.StoreMemberTypeEnum
import com.tencent.devops.store.pojo.common.enums.StoreProjectTypeEnum
import com.tencent.devops.store.pojo.common.enums.StoreTypeEnum
import com.tencent.devops.store.pojo.dto.ExtSubmitDTO
import com.tencent.devops.store.pojo.dto.InitExtServiceDTO
import com.tencent.devops.store.pojo.dto.ServiceOfflineDTO
import com.tencent.devops.store.pojo.dto.SubmitDTO
import com.tencent.devops.store.pojo.enums.DescInputTypeEnum
import com.tencent.devops.store.pojo.enums.ExtServicePackageSourceTypeEnum
import com.tencent.devops.store.pojo.enums.ExtServiceStatusEnum
import com.tencent.devops.store.service.common.StoreCommonService
import com.tencent.devops.store.service.common.StoreMediaService
import com.tencent.devops.store.utils.VersionUtils
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
abstract class ExtServiceReleaseService @Autowired constructor() {

    @Autowired
    lateinit var dslContext: DSLContext
    @Autowired
    lateinit var extServiceDao: ExtServiceDao
    @Autowired
    lateinit var storeProjectRelDao: StoreProjectRelDao
    @Autowired
    lateinit var extServiceEnvDao: ExtServiceEnvDao
    @Autowired
    lateinit var storeBuildInfoDao: StoreBuildInfoDao
    @Autowired
    lateinit var storeMemberDao: StoreMemberDao
    @Autowired
    lateinit var extFeatureDao: ExtServiceFeatureDao
    @Autowired
    lateinit var extServiceItemRelDao: ExtServiceItemRelDao
    @Autowired
    lateinit var extServiceCommonService: ExtServiceCommonService
    @Autowired
    lateinit var storeCommonService: StoreCommonService
    @Autowired
    lateinit var extServiceVersionLogDao: ExtServiceVersionLogDao
    @Autowired
    lateinit var extServiceLabelDao: ExtServiceLabelRelDao
    @Autowired
    lateinit var client: Client
    @Autowired
    lateinit var mediaService: StoreMediaService
    @Autowired
    lateinit var objectMapper: ObjectMapper
    @Autowired
    lateinit var extServiceKubernetesService: ExtServiceKubernetesService
    @Autowired
    lateinit var extServiceKubernetesNameSpaceConfig: ExtServiceKubernetesNameSpaceConfig
    @Autowired
    lateinit var permissionApi: AuthPermissionApi
    @Autowired
    lateinit var storeMediaInfoDao: StoreMediaInfoDao
    @Autowired
    lateinit var storeStatisticTotalDao: StoreStatisticTotalDao
    @Autowired
    lateinit var pipelineAuthServiceCode: PipelineAuthServiceCode
    @Autowired
    lateinit var redisOperation: RedisOperation

    fun addExtService(
        userId: String,
        extensionInfo: InitExtServiceDTO
    ): Result<Boolean> {
        val serviceCode = extensionInfo.serviceCode
        logger.info("addExtService user[$userId], serviceCode[$serviceCode], info[$extensionInfo]")
        // 校验信息
        validateAddServiceReq(extensionInfo)
        checkProjectInfo(userId, extensionInfo.projectCode)
        val handleServicePackageResult = handleServicePackage(extensionInfo, userId, serviceCode)
        logger.info("addExtService the handleServicePackage is :$handleServicePackageResult")

        if (handleServicePackageResult.isNotOk()) {
            return Result(handleServicePackageResult.status, handleServicePackageResult.message, null)
        }
        val handleServicePackageMap = handleServicePackageResult.data
        dslContext.transaction { t ->
            val context = DSL.using(t)
            val id = UUIDUtil.generate()
            // 添加微扩展基本信息
            extServiceDao.createExtService(
                dslContext = context,
                userId = userId,
                id = id,
                extServiceCreateInfo = ExtServiceCreateInfo(
                    serviceCode = extensionInfo.serviceCode,
                    serviceName = extensionInfo.serviceName,
                    latestFlag = true,
                    creatorUser = userId,
                    publisher = userId,
                    publishTime = System.currentTimeMillis(),
                    status = ExtServiceStatusEnum.INIT.status,
                    version = ""
                )
            )
            // 添加微扩展与项目关联关系，type为0代表新增微扩展时关联的初始化项目
            storeProjectRelDao.addStoreProjectRel(
                dslContext = context,
                userId = userId,
                storeCode = serviceCode,
                projectCode = extensionInfo.projectCode,
                type = StoreProjectTypeEnum.INIT.type.toByte(),
                storeType = StoreTypeEnum.SERVICE.type.toByte()
            )
            storeProjectRelDao.addStoreProjectRel(
                dslContext = context,
                userId = userId,
                storeCode = serviceCode,
                projectCode = extensionInfo.projectCode,
                type = StoreProjectTypeEnum.TEST.type.toByte(),
                storeType = StoreTypeEnum.SERVICE.type.toByte()
            )
            val extServiceEnvCreateInfo = ExtServiceEnvCreateInfo(
                serviceId = id,
                language = extensionInfo.language,
                pkgPath = "",
                pkgShaContent = "",
                dockerFileContent = "",
                imagePath = "",
                creatorUser = userId,
                modifierUser = userId
            )
            extServiceEnvDao.create(context, extServiceEnvCreateInfo) // 添加微扩展执行环境信息
            // 默认给新建微扩展的人赋予管理员权限
            storeMemberDao.addStoreMember(
                dslContext = context,
                userId = userId,
                storeCode = serviceCode,
                userName = userId,
                type = StoreMemberTypeEnum.ADMIN.type.toByte(),
                storeType = StoreTypeEnum.SERVICE.type.toByte()
            )
            // 添加微扩展特性信息
            extFeatureDao.create(
                dslContext = context,
                userId = userId,
                extServiceFeatureCreateInfo = ExtServiceFeatureCreateInfo(
                    serviceCode = serviceCode,
                    repositoryHashId = handleServicePackageMap?.get(KEY_REPOSITORY_HASH_ID) ?: "",
                    codeSrc = handleServicePackageMap?.get(KEY_CODE_SRC) ?: "",
                    creatorUser = userId,
                    modifierUser = userId,
                    visibilityLevel = extensionInfo.visibilityLevel!!.level
                )
            )
            // 初始化统计表数据
            storeStatisticTotalDao.initStatisticData(
                dslContext = context,
                storeCode = serviceCode,
                storeType = StoreTypeEnum.SERVICE.type.toByte()
            )
            extensionInfo.extensionItemList.forEach {
                // 添加微扩展扩展点
                extServiceItemRelDao.create(
                    dslContext = dslContext,
                    userId = userId,
                    extServiceItemRelCreateInfo = ExtServiceItemRelCreateInfo(
                        serviceId = id,
                        itemId = it,
                        creatorUser = userId,
                        modifierUser = userId,
                        bkServiceId = getItemBkServiceId(it)
                    )
                )
            }
            // 添加扩展点使用记录
            client.get(ServiceItemResource::class).addServiceNum(extensionInfo.extensionItemList)
        }
        return Result(true)
    }

    fun submitExtService(
        userId: String,
        submitDTO: SubmitDTO
    ): Result<String> {
        logger.info("updateExtService userId[$userId],submitDTO[$submitDTO]")
        val serviceCode = submitDTO.serviceCode
        val extPackageSourceType = getExtServicePackageSourceType(serviceCode)
        logger.info("updateExtService servicePackageSourceType is :$extPackageSourceType")
        val version = submitDTO.version

        // 判断微扩展是不是首次创建版本
        val serviceCount = extServiceDao.countByCode(dslContext, serviceCode)
        if (serviceCount < 1) {
            return MessageCodeUtil.generateResponseDataObject(
                CommonMessageCode.PARAMETER_IS_INVALID,
                arrayOf(serviceCode)
            )
        }
        val serviceRecord = extServiceDao.getNewestServiceByCode(dslContext, serviceCode)!!
        // 判断更新的微扩展名称是否重复
        if (validateAddServiceReqByName(
                submitDTO.serviceName,
                submitDTO.serviceCode
            )
        ) return MessageCodeUtil.generateResponseDataObject(
            CommonMessageCode.PARAMETER_IS_EXIST,
            arrayOf(submitDTO.serviceName)
        )
        // 校验前端传的版本号是否正确
        val releaseType = submitDTO.releaseType
        val dbVersion = serviceRecord.version
        // 最近的版本处于上架中止状态，重新升级版本号不变
        val cancelFlag = serviceRecord.serviceStatus == ExtServiceStatusEnum.GROUNDING_SUSPENSION.status.toByte()
        val requireVersionList =
            if (cancelFlag && releaseType == ReleaseTypeEnum.CANCEL_RE_RELEASE) {
                listOf(dbVersion)
            } else {
                // 历史大版本下的小版本更新模式需获取要更新大版本下的最新版本
                val reqVersion = if (releaseType == ReleaseTypeEnum.HIS_VERSION_UPGRADE) {
                    extServiceDao.getExtService(
                        dslContext = dslContext,
                        serviceCode = serviceCode,
                        version = VersionUtils.convertLatestVersion(version)
                    )?.version
                } else {
                    null
                }
                storeCommonService.getRequireVersion(
                    reqVersion = reqVersion,
                    dbVersion = dbVersion,
                    releaseType = releaseType!!
                )
            }

        if (!requireVersionList.contains(version)) {
            return MessageCodeUtil.generateResponseDataObject(
                StoreMessageCode.USER_SERVICE_VERSION_IS_INVALID,
                arrayOf(version, requireVersionList.toString())
            )
        }

        // 判断最近一个微扩展版本的状态，只有处于审核驳回、已发布、上架中止和已下架的状态才允许添加新的版本
        val serviceFinalStatusList = mutableListOf(
            ExtServiceStatusEnum.AUDIT_REJECT.status.toByte(),
            ExtServiceStatusEnum.RELEASED.status.toByte(),
            ExtServiceStatusEnum.GROUNDING_SUSPENSION.status.toByte(),
            ExtServiceStatusEnum.UNDERCARRIAGED.status.toByte()
        )

        if (serviceCount == 1) {
            // 如果是首次发布，处于初始化的微扩展状态也允许添加新的版本
            serviceFinalStatusList.add(ExtServiceStatusEnum.INIT.status.toByte())
        }

        if (!serviceFinalStatusList.contains(serviceRecord.serviceStatus)) {
            return MessageCodeUtil.generateResponseDataObject(
                StoreMessageCode.USER_SERVICE_VERSION_IS_NOT_FINISH,
                arrayOf(serviceRecord.serviceName, serviceRecord.version)
            )
        }

        var serviceId = UUIDUtil.generate()
        val serviceStatus = if (extPackageSourceType == ExtServicePackageSourceTypeEnum.REPO) {
            ExtServiceStatusEnum.COMMITTING
        } else {
            ExtServiceStatusEnum.TESTING
        }

        dslContext.transaction { t ->
            val context = DSL.using(t)
            if (serviceRecord.version.isNullOrBlank() ||
                (cancelFlag && releaseType == ReleaseTypeEnum.CANCEL_RE_RELEASE)) {
                // 首次创建版本或者取消发布后不变更版本号重新上架，则在该版本的记录上做更新操作
                serviceId = serviceRecord.id
                val finalReleaseType = if (releaseType == ReleaseTypeEnum.CANCEL_RE_RELEASE) {
                    val serviceVersion = extServiceVersionLogDao.getVersionLogByServiceId(context, serviceId)
                    serviceVersion!!.releaseType
                } else {
                    releaseType.releaseType.toByte()
                }
                submitExtService(
                    userId = userId,
                    serviceId = serviceId,
                    extServiceUpdateInfo = ExtServiceUpdateInfo(
                        serviceName = submitDTO.serviceName,
                        version = submitDTO.version,
                        status = serviceStatus.status,
                        statusMsg = "",
                        logoUrl = submitDTO.logoUrl,
                        iconData = submitDTO.iconData,
                        summary = submitDTO.summary,
                        description = submitDTO.description,
                        latestFlag = null,
                        modifierUser = userId
                    ),
                    extServiceVersionLogCreateInfo = ExtServiceVersionLogCreateInfo(
                        serviceId = serviceId,
                        releaseType = finalReleaseType,
                        content = submitDTO.versionContent ?: "",
                        creatorUser = userId,
                        modifierUser = userId
                    )
                )
            } else {
                // 升级微扩展
                val serviceEnvRecord = extServiceEnvDao.getMarketServiceEnvInfoByServiceId(context, serviceRecord.id)
                // 若无已发布的扩展，则直接将当前的设置为latest
                val latestFlag = extServiceDao.getLatestFlag(context, serviceCode)
                if (latestFlag) {
                    extServiceDao.cleanLatestFlag(context, serviceCode)
                }
                upgradeMarketExtService(
                    context = context,
                    userId = userId,
                    serviceId = serviceId,
                    language = serviceEnvRecord!!.language,
                    extServiceCreateInfo = ExtServiceCreateInfo(
                        serviceCode = submitDTO.serviceCode,
                        serviceName = submitDTO.serviceName,
                        creatorUser = userId,
                        version = submitDTO.version,
                        logoUrl = submitDTO.logoUrl,
                        iconData = submitDTO.iconData,
                        latestFlag = latestFlag,
                        summary = submitDTO.summary,
                        description = submitDTO.description,
                        publisher = userId,
                        publishTime = System.currentTimeMillis(),
                        status = 0
                    ),
                    extServiceVersionLogCreateInfo = ExtServiceVersionLogCreateInfo(
                        serviceId = serviceId,
                        releaseType = submitDTO.releaseType!!.releaseType.toByte(),
                        content = submitDTO.versionContent ?: "",
                        creatorUser = userId,
                        modifierUser = userId
                    )
                )
            }
            if (submitDTO.descInputType != null) {
                val extServiceFeatureUpdateInfo = ExtServiceFeatureUpdateInfo(
                    descInputType = submitDTO.descInputType
                )
                extFeatureDao.updateExtServiceFeatureBaseInfo(
                    dslContext,
                    userId,
                    serviceCode,
                    extServiceFeatureUpdateInfo
                )
            }

            // 更新标签信息
            val labelIdList = submitDTO.labelIdList
            if (null != labelIdList) {
                extServiceLabelDao.deleteByServiceId(context, serviceId)
                if (labelIdList.isNotEmpty())
                    extServiceLabelDao.batchAdd(context, userId, serviceId, labelIdList)
            }

            // 添加扩展点
            val featureInfoRecord = extFeatureDao.getLatestServiceByCode(dslContext, serviceCode)
            val itemIdList = submitDTO.extensionItemList
            val itemCreateInfoList =
                getFileServiceProps(serviceCode, featureInfoRecord!!.repositoryHashId, EXTENSION_JSON_NAME, itemIdList)
            extServiceItemRelDao.deleteByServiceId(context, serviceId)
            extServiceItemRelDao.batchAdd(
                dslContext = dslContext,
                userId = userId,
                serviceId = serviceId,
                itemPropList = itemCreateInfoList
            )
            // 添加扩展点使用记录
            client.get(ServiceItemResource::class).addServiceNum(itemIdList)

            asyncHandleUpdateService(context, serviceId, userId)
        }
        return Result(serviceId)
    }

    /**
     * 异步处理上架微扩展
     */
    abstract fun asyncHandleUpdateService(
        context: DSLContext,
        serviceId: String,
        userId: String
    )

    fun getExtensionServiceProcessInfo(userId: String, serviceId: String): Result<StoreProcessInfo> {
        logger.info("getProcessInfo userId is $userId,serviceId is $serviceId")
        val record = extServiceDao.getServiceById(dslContext, serviceId)
        return if (null == record) {
            MessageCodeUtil.generateResponseDataObject(CommonMessageCode.PARAMETER_IS_INVALID, arrayOf(serviceId))
        } else {
            val serviceCode = record.serviceCode
            // 判断用户是否有查询权限
            val queryFlag =
                storeMemberDao.isStoreMember(dslContext, userId, serviceCode, StoreTypeEnum.SERVICE.type.toByte())
            if (!queryFlag) {
                return MessageCodeUtil.generateResponseDataObject(CommonMessageCode.PERMISSION_DENIED)
            }
            val status = record.serviceStatus.toInt()
            // 查看当前版本之前的版本是否有已发布的，如果有已发布的版本则只是普通的升级操作而不需要审核
            val isNormalUpgrade = getNormalUpgradeFlag(serviceCode, status)
            val processInfo = handleProcessInfo(isNormalUpgrade, status)
            val storeProcessInfo = storeCommonService.generateStoreProcessInfo(
                userId = userId,
                storeId = serviceId,
                storeCode = serviceCode,
                storeType = StoreTypeEnum.SERVICE,
                creator = record.creator,
                processInfo = processInfo
            )
            Result(storeProcessInfo)
        }
    }

    fun offlineService(userId: String, serviceCode: String, serviceOfflineDTO: ServiceOfflineDTO): Result<Boolean> {
        // 判断用户是否有权限下线
        if (!storeMemberDao.isStoreAdmin(dslContext, userId, serviceCode, StoreTypeEnum.SERVICE.type.toByte())) {
            return MessageCodeUtil.generateResponseDataObject(CommonMessageCode.PERMISSION_DENIED)
        }
        // 停止bcs灰度命名空间和正式命名空间的应用
        val bcsStopAppResult = extServiceKubernetesService.stopExtService(
            userId = userId,
            serviceCode = serviceCode,
            deploymentName = serviceCode,
            serviceName = "$serviceCode-service"
        )
        logger.info("the bcsStopAppResult is :$bcsStopAppResult")
        if (bcsStopAppResult.isNotOk()) {
            return bcsStopAppResult
        }
        // 设置微扩展状态为下架中
        extServiceDao.setServiceStatusByCode(
            dslContext = dslContext,
            serviceCode = serviceCode,
            serviceOldStatus = ExtServiceStatusEnum.RELEASED.status.toByte(),
            serviceNewStatus = ExtServiceStatusEnum.UNDERCARRIAGED.status.toByte(),
            userId = userId,
            msg = serviceOfflineDTO.reason
        )
        return Result(true)
    }

    fun listLanguage(): List<String?> {
        val records = storeBuildInfoDao.list(dslContext, StoreTypeEnum.SERVICE)
        val ret = mutableListOf<String>()
        records?.forEach {
            ret.add(
                it.language
            )
        }
        return ret
    }

    /**
     * 取消发布
     */
    fun cancelRelease(userId: String, serviceId: String): Result<Boolean> {
        logger.info("extService cancelRelease, userId=$userId, serviceId=$serviceId")
        val serviceRecord = extServiceDao.getServiceById(dslContext, serviceId)
            ?: return MessageCodeUtil.generateResponseDataObject(
                messageCode = CommonMessageCode.PARAMETER_IS_INVALID,
                params = arrayOf(serviceId),
                data = false
            )
        val status = ExtServiceStatusEnum.GROUNDING_SUSPENSION.status.toByte()
        val (checkResult, code) = checkServiceVersionOptRight(userId, serviceId, status)
        if (!checkResult) {
            return MessageCodeUtil.generateResponseDataObject(code)
        }
        // 如果该版本的状态已处于测试中及其后面的状态，取消发布则需要停掉灰度命名空间的应用
        val serviceCode = serviceRecord.serviceCode
        val bcsStopAppResult = extServiceKubernetesService.stopExtService(
            userId = userId,
            serviceCode = serviceCode,
            deploymentName = serviceCode,
            serviceName = "$serviceCode-service",
            checkPermissionFlag = true,
            grayFlag = true
        )
        logger.info("$serviceCode bcsStopAppResult is :$bcsStopAppResult")
        if (bcsStopAppResult.isNotOk()) {
            return bcsStopAppResult
        }
        dslContext.transaction { t ->
            val context = DSL.using(t)
            extFeatureDao.updateExtServiceFeatureBaseInfo(
                dslContext = context,
                serviceCode = serviceCode,
                userId = userId,
                extServiceFeatureUpdateInfo = ExtServiceFeatureUpdateInfo(
                    killGrayAppFlag = null,
                    killGrayAppMarkTime = null
                )
            )
            extServiceDao.setServiceStatusById(
                dslContext = context,
                serviceId = serviceId,
                serviceStatus = status,
                userId = userId,
                msg = MessageCodeUtil.getCodeLanMessage(UN_RELEASE)
            )
        }
        return Result(true)
    }

    /**
     * 确认通过测试，继续发布
     */
    fun passTest(userId: String, serviceId: String): Result<Boolean> {
        logger.info("passTest, userId=$userId, serviceId=$serviceId")
        extServiceDao.getServiceById(dslContext, serviceId)
            ?: return MessageCodeUtil.generateResponseDataObject(
                messageCode = CommonMessageCode.PARAMETER_IS_INVALID,
                params = arrayOf(serviceId),
                data = false
            )
        // 查看当前版本之前的版本是否有已发布的，如果有已发布的版本则只是普通的升级操作而不需要审核
        val serviceStatus = ExtServiceStatusEnum.EDIT.status.toByte()
        extServiceDao.setServiceStatusById(
            dslContext = dslContext,
            serviceId = serviceId,
            serviceStatus = serviceStatus,
            userId = userId,
            msg = ""
        )
        return Result(true)
    }

    fun getCompleteEditStatus(isNormalUpgrade: Boolean): Byte {
        return if (isNormalUpgrade) {
            ExtServiceStatusEnum.RELEASE_DEPLOYING.status.toByte()
        } else {
            ExtServiceStatusEnum.AUDITING.status.toByte()
        }
    }

    fun createMediaAndVisible(userId: String, serviceId: String, submitInfo: ExtSubmitDTO): Result<Boolean> {
        val mediaList = submitInfo.mediaInfoList
        val deptList = submitInfo.deptInfoList
        val serviceInfo = extServiceDao.getServiceById(dslContext, serviceId) ?:
        throw ErrorCodeException(errorCode = StoreMessageCode.USER_SERVICE_NOT_EXIST)
        val serviceCode = serviceInfo.serviceCode
        val oldStatus = serviceInfo.serviceStatus
        val isNormalUpgrade = getNormalUpgradeFlag(serviceCode, oldStatus.toInt())
        val newStatus = getCompleteEditStatus(isNormalUpgrade)
        val (checkResult, code) = checkServiceVersionOptRight(userId, serviceId, newStatus, isNormalUpgrade)

        if (!checkResult) {
            return MessageCodeUtil.generateResponseDataObject(code)
        }
        // 先集中删除，再添加媒体信息
        mediaService.deleteByStoreCode(userId, serviceCode, StoreTypeEnum.SERVICE)
        mediaList.forEach {
            mediaService.add(
                userId = userId,
                type = StoreTypeEnum.SERVICE,
                storeMediaInfo = StoreMediaInfoRequest(
                    storeCode = serviceCode,
                    mediaUrl = it.mediaUrl,
                    mediaType = it.mediaType.name,
                    modifier = userId
                )
            )
        }
        doServiceVisibleBus(userId, serviceCode, deptList)
        if (isNormalUpgrade) {
            // 正式发布最新的微扩展版本
            val deployExtServiceResult = extServiceKubernetesService.deployExtService(
                userId = userId,
                grayFlag = false,
                serviceCode = serviceCode,
                version = serviceInfo.version
            )
            logger.info("deployExtServiceResult is:$deployExtServiceResult")
            if (deployExtServiceResult.isNotOk()) {
                return deployExtServiceResult
            }
        }

        extServiceDao.setServiceStatusById(
            dslContext = dslContext,
            serviceId = serviceId,
            serviceStatus = newStatus,
            userId = userId,
            msg = "add media file "
        )
        return Result(true)
    }

    abstract fun doServiceVisibleBus(userId: String, serviceCode: String, deptList: List<DeptInfo>)

    fun backToTest(userId: String, serviceId: String): Result<Boolean> {
        logger.info("back to test： serviceId[$serviceId], userId[$serviceId]")
        val newStatus = ExtServiceStatusEnum.TESTING
        val (checkResult, code) = checkServiceVersionOptRight(userId, serviceId, newStatus.status.toByte())

        if (!checkResult) {
            return MessageCodeUtil.generateResponseDataObject(code)
        }

        extServiceDao.setServiceStatusById(
            dslContext = dslContext,
            serviceId = serviceId,
            userId = userId,
            serviceStatus = newStatus.status.toByte(),
            msg = "back to test"
        )
        return Result(true)
    }

    abstract fun handleServicePackage(
        extensionInfo: InitExtServiceDTO,
        userId: String,
        serviceCode: String
    ): Result<Map<String, String>?>

    abstract fun getExtServicePackageSourceType(serviceCode: String): ExtServicePackageSourceTypeEnum

    private fun getNormalUpgradeFlag(serviceCode: String, status: Int): Boolean {
        val releaseTotalNum = extServiceDao.countReleaseServiceByCode(dslContext, serviceCode)
        val currentNum = if (status == ExtServiceStatusEnum.RELEASED.status) 1 else 0
        return releaseTotalNum > currentNum
    }

    private fun submitExtService(
        userId: String,
        serviceId: String,
        extServiceUpdateInfo: ExtServiceUpdateInfo,
        extServiceVersionLogCreateInfo: ExtServiceVersionLogCreateInfo
    ) {
        extServiceDao.updateExtServiceBaseInfo(
            dslContext = dslContext,
            userId = userId,
            serviceId = serviceId,
            extServiceUpdateInfo = extServiceUpdateInfo
        )
        extServiceVersionLogDao.create(
            dslContext = dslContext,
            userId = userId,
            id = serviceId,
            extServiceVersionLogCreateInfo = extServiceVersionLogCreateInfo
        )
    }

    private fun upgradeMarketExtService(
        context: DSLContext,
        userId: String,
        serviceId: String,
        language: String,
        extServiceCreateInfo: ExtServiceCreateInfo,
        extServiceVersionLogCreateInfo: ExtServiceVersionLogCreateInfo
    ) {
        extServiceDao.createExtService(
            dslContext = context,
            userId = userId,
            id = serviceId,
            extServiceCreateInfo = extServiceCreateInfo
        )
        val extServiceEnvCreateInfo = ExtServiceEnvCreateInfo(
            serviceId = serviceId,
            language = language,
            pkgPath = "",
            pkgShaContent = "",
            dockerFileContent = "",
            imagePath = "",
            creatorUser = userId,
            modifierUser = userId
        )
        extServiceEnvDao.create(context, extServiceEnvCreateInfo) // 添加微扩展执行环境信息
        extServiceVersionLogDao.create(
            dslContext = context,
            userId = userId,
            id = serviceId,
            extServiceVersionLogCreateInfo = extServiceVersionLogCreateInfo
        )
    }

    private fun validateAddServiceReq(extensionInfo: InitExtServiceDTO) {
        val serviceCode = extensionInfo.serviceCode
        // 判断微扩展是否存在
        val codeInfo = extServiceDao.getServiceLatestByCode(dslContext, serviceCode)
        if (codeInfo != null) {
            throw ErrorCodeException(errorCode = CommonMessageCode.PARAMETER_IS_EXIST, params = arrayOf(serviceCode))
        }
        val serviceName = extensionInfo.serviceName
        val nameInfo = extServiceDao.getServiceByName(dslContext, serviceName)
        if (nameInfo != null) {
            throw ErrorCodeException(errorCode = CommonMessageCode.PARAMETER_IS_EXIST, params = arrayOf(serviceName))
        }
    }

    private fun handleProcessInfo(isNormalUpgrade: Boolean, status: Int): List<ReleaseProcessItem> {
        val processInfo = initProcessInfo(isNormalUpgrade)
        val totalStep = if (isNormalUpgrade) NUM_SIX else NUM_SEVEN
        when (status) {
            ExtServiceStatusEnum.INIT.status -> {
                storeCommonService.setProcessInfo(processInfo, totalStep, NUM_ONE, DOING)
            }
            ExtServiceStatusEnum.BUILDING.status, ExtServiceStatusEnum.COMMITTING.status,
            ExtServiceStatusEnum.DEPLOYING.status -> {
                storeCommonService.setProcessInfo(processInfo, totalStep, NUM_TWO, DOING)
            }
            ExtServiceStatusEnum.BUILD_FAIL.status, ExtServiceStatusEnum.DEPLOY_FAIL.status -> {
                storeCommonService.setProcessInfo(processInfo, totalStep, NUM_TWO, FAIL)
            }
            ExtServiceStatusEnum.TESTING.status -> {
                storeCommonService.setProcessInfo(processInfo, totalStep, NUM_THREE, DOING)
            }
            ExtServiceStatusEnum.EDIT.status -> {
                storeCommonService.setProcessInfo(processInfo, totalStep, NUM_FOUR, DOING)
            }
            ExtServiceStatusEnum.AUDITING.status -> {
                storeCommonService.setProcessInfo(processInfo, totalStep, NUM_FIVE, DOING)
            }
            ExtServiceStatusEnum.AUDIT_REJECT.status -> {
                storeCommonService.setProcessInfo(processInfo, totalStep, NUM_FIVE, FAIL)
            }
            ExtServiceStatusEnum.RELEASE_DEPLOYING.status -> {
                storeCommonService.setProcessInfo(
                    processInfo = processInfo,
                    totalStep = totalStep,
                    currStep = if (isNormalUpgrade) NUM_FIVE else NUM_SIX, status = DOING
                )
            }
            ExtServiceStatusEnum.RELEASE_DEPLOY_FAIL.status -> {
                storeCommonService.setProcessInfo(
                    processInfo = processInfo,
                    totalStep = totalStep,
                    currStep = if (isNormalUpgrade) NUM_FIVE else NUM_SIX, status = FAIL)
            }

            ExtServiceStatusEnum.RELEASED.status -> {
                val currStep = if (isNormalUpgrade) NUM_SIX else NUM_SEVEN
                storeCommonService.setProcessInfo(processInfo, totalStep, currStep, SUCCESS)
            }
        }
        return processInfo
    }

    private fun initProcessInfo(isNormalUpgrade: Boolean): List<ReleaseProcessItem> {
        val processInfo = mutableListOf<ReleaseProcessItem>()
        processInfo.add(ReleaseProcessItem(MessageCodeUtil.getCodeLanMessage(BEGIN), BEGIN, NUM_ONE, SUCCESS))
        processInfo.add(
            ReleaseProcessItem(
                name = MessageCodeUtil.getCodeLanMessage(TEST_ENV_PREPARE),
                code = TEST_ENV_PREPARE,
                step = NUM_TWO,
                status = UNDO
            )
        )
        processInfo.add(ReleaseProcessItem(MessageCodeUtil.getCodeLanMessage(TEST), TEST, NUM_THREE, UNDO))
        processInfo.add(ReleaseProcessItem(MessageCodeUtil.getCodeLanMessage(EDIT), COMMIT, NUM_FOUR, UNDO))
        if (isNormalUpgrade) {
            processInfo.add(ReleaseProcessItem(MessageCodeUtil.getCodeLanMessage(ONLINE), ONLINE, NUM_FIVE, UNDO))
            processInfo.add(ReleaseProcessItem(MessageCodeUtil.getCodeLanMessage(END), END, NUM_SIX, UNDO))
        } else {
            processInfo.add(ReleaseProcessItem(MessageCodeUtil.getCodeLanMessage(APPROVE), APPROVE, NUM_FIVE, UNDO))
            processInfo.add(ReleaseProcessItem(MessageCodeUtil.getCodeLanMessage(ONLINE), ONLINE, NUM_SIX, UNDO))
            processInfo.add(ReleaseProcessItem(MessageCodeUtil.getCodeLanMessage(END), END, NUM_SEVEN, UNDO))
        }
        return processInfo
    }

    private fun validateAddServiceReqByName(serviceName: String, serviceCode: String): Boolean {
        var flag = false
        val count = extServiceDao.countByName(dslContext, serviceName)
        if (count > 0) {
            // 判断微扩展名称是否重复（微扩展升级允许名称一样）
            flag = extServiceDao.countByName(
                dslContext = dslContext,
                serviceName = serviceName,
                serviceCode = serviceCode
            ) < count
        }
        return flag
    }

    private fun getFileServiceProps(
        serviceCode: String,
        repositoryHashId: String,
        fileName: String,
        inputItemList: Set<String>
    ): List<ItemPropCreateInfo> {
        val itemCreateList = mutableListOf<ItemPropCreateInfo>()
        val inputItemMap = mutableMapOf<String, ItemPropCreateInfo>()
        // 匹配页面输入的itemId，是否在json文件内有对应的props，若没有则为空。有则替换。  json文件内多余的itemCode无视
        inputItemList.forEach {
            inputItemMap[it] = ItemPropCreateInfo(
                itemId = it,
                props = "",
                bkServiceId = getItemBkServiceId(it)
            )
        }

        // 从工蜂拉取文件
        val fileStr = getFileStr(serviceCode,"",fileName, repositoryHashId)
        if (fileStr.isNullOrEmpty()) {
            // 文件数据为空，直接返回输入数据
            return returnInputData(inputItemList)
        }
        val taskDataMap = objectMapper.readValue<ExtensionJson>(fileStr)
        logger.info("getServiceProps taskDataMap[$taskDataMap]")
        val fileServiceCode = taskDataMap.serviceCode
        val fileItemList = taskDataMap.itemList
        if (fileServiceCode != serviceCode) {
            logger.warn("getServiceProps input serviceCode[$serviceCode], extension.json serviceCode[$fileServiceCode] ")
            throw ErrorCodeException(errorCode = StoreMessageCode.USER_SERVICE_CODE_DIFF)
        }

        if (fileItemList == null) {
            // 文件数据为空，直接返回输入数据
            return returnInputData(inputItemList)
        }

        // 文件与输入itemCode取交集，若文件内有props，以文件props为准
        val filePropMap = mutableMapOf<String, String>()
        fileItemList.forEach {
            filePropMap[it.itemCode] = JsonUtil.toJson(it.props ?: "")
        }
        val itemRecords = client.get(ServiceItemResource::class).getItemInfoByIds(inputItemList).data
        itemRecords?.forEach {
            val itemCode = it.itemCode
            val itemId = it.itemId
            if (filePropMap.keys.contains(itemCode)) {
                val propsInfo = filePropMap[itemCode]
                inputItemMap[itemId] = ItemPropCreateInfo(
                    itemId = itemId,
                    props = propsInfo ?: "",
                    bkServiceId = getItemBkServiceId(itemId)
                )
            }
        }

        // 返回取完交集后的数据
        inputItemMap.forEach { (_, u) ->
            itemCreateList.add(u)
        }
        logger.info("getServiceProps itemCreateList[$itemCreateList], filePropMap[$filePropMap]")

        return itemCreateList
    }

    /**
     * 获取文件信息
     */
    abstract fun getFileStr(
        serviceCode: String,
        version: String,
        fileName: String,
        repositoryHashId: String? = null
    ): String?

    private fun returnInputData(inputItem: Set<String>): List<ItemPropCreateInfo> {
        // 默认添加页面选中的扩展点, props给空
        val itemCreateList = mutableListOf<ItemPropCreateInfo>()
        inputItem.forEach {
            itemCreateList.add(
                ItemPropCreateInfo(
                    itemId = it,
                    props = "",
                    bkServiceId = getItemBkServiceId(it)
                )
            )
        }
        return itemCreateList
    }

    /**
     * 检查版本发布过程中的操作权限
     */
    fun checkServiceVersionOptRight(
        userId: String,
        serviceId: String,
        status: Byte,
        isNormalUpgrade: Boolean? = null
    ): Pair<Boolean, String> {
        logger.info("checkServiceVersionOptRight params[$userId|$serviceId|$status|$isNormalUpgrade]")
        val record =
            extServiceDao.getServiceById(dslContext, serviceId) ?: return Pair(
                false,
                CommonMessageCode.PARAMETER_IS_INVALID
            )
        val serviceCode = record.serviceCode
        val owner = record.owner
        val recordStatus = record.serviceStatus

        // 判断用户是否有权限(当前版本的创建者和管理员可以操作)
        if (!(storeMemberDao.isStoreAdmin(
                dslContext = dslContext,
                userId = userId,
                storeCode = serviceCode,
                storeType = StoreTypeEnum.SERVICE.type.toByte()
            ) || owner == userId)
        ) {
            return Pair(false, CommonMessageCode.PERMISSION_DENIED)
        }
        val allowDeployStatus = if (isNormalUpgrade != null && isNormalUpgrade) ExtServiceStatusEnum.EDIT
        else ExtServiceStatusEnum.AUDITING
        var validateFlag = true
        if (status == ExtServiceStatusEnum.COMMITTING.status.toByte() &&
            recordStatus != ExtServiceStatusEnum.INIT.status.toByte()
        ) {
            validateFlag = false
        } else if (status == ExtServiceStatusEnum.BUILDING.status.toByte() &&
            recordStatus !in (
                listOf(
                    ExtServiceStatusEnum.COMMITTING.status.toByte(),
                    ExtServiceStatusEnum.BUILD_FAIL.status.toByte(),
                    ExtServiceStatusEnum.TESTING.status.toByte()
                ))
        ) {
            validateFlag = false
        } else if (status == ExtServiceStatusEnum.BUILD_FAIL.status.toByte() &&
            recordStatus !in (
                listOf(
                    ExtServiceStatusEnum.COMMITTING.status.toByte(),
                    ExtServiceStatusEnum.BUILDING.status.toByte(),
                    ExtServiceStatusEnum.BUILD_FAIL.status.toByte(),
                    ExtServiceStatusEnum.TESTING.status.toByte()
                ))
        ) {
            validateFlag = false
        } else if (status == ExtServiceStatusEnum.TESTING.status.toByte() &&
            recordStatus !in (
                listOf(
                    ExtServiceStatusEnum.BUILDING.status.toByte(),
                    ExtServiceStatusEnum.EDIT.status.toByte()
                ))
        ) {
            validateFlag = false
        } else if (status == ExtServiceStatusEnum.EDIT.status.toByte() &&
            recordStatus != ExtServiceStatusEnum.TESTING.status.toByte()
        ) {
            validateFlag = false
        } else if (status == ExtServiceStatusEnum.AUDITING.status.toByte() &&
            recordStatus != ExtServiceStatusEnum.EDIT.status.toByte()
        ) {
            validateFlag = false
        } else if (status == ExtServiceStatusEnum.AUDIT_REJECT.status.toByte() &&
            recordStatus != ExtServiceStatusEnum.AUDITING.status.toByte()
        ) {
            validateFlag = false
        } else if (status == ExtServiceStatusEnum.RELEASE_DEPLOYING.status.toByte() &&
            recordStatus != allowDeployStatus.status.toByte()
        ) {
            validateFlag = false
        } else if (status == ExtServiceStatusEnum.RELEASE_DEPLOY_FAIL.status.toByte() &&
            recordStatus != ExtServiceStatusEnum.RELEASE_DEPLOYING.status.toByte()
        ) {
            validateFlag = false
        } else if (status == ExtServiceStatusEnum.RELEASED.status.toByte() &&
            recordStatus != ExtServiceStatusEnum.RELEASE_DEPLOYING.status.toByte()
        ) {
            validateFlag = false
        } else if (status == ExtServiceStatusEnum.GROUNDING_SUSPENSION.status.toByte() &&
            recordStatus == ExtServiceStatusEnum.RELEASED.status.toByte()
        ) {
            validateFlag = false
        } else if (status == ExtServiceStatusEnum.UNDERCARRIAGING.status.toByte() &&
            recordStatus == ExtServiceStatusEnum.RELEASED.status.toByte()
        ) {
            validateFlag = false
        } else if (status == ExtServiceStatusEnum.UNDERCARRIAGED.status.toByte() &&
            recordStatus !in (
                listOf(
                    ExtServiceStatusEnum.UNDERCARRIAGING.status.toByte(),
                    ExtServiceStatusEnum.RELEASED.status.toByte()
                ))
        ) {
            validateFlag = false
        }
        return if (validateFlag) Pair(true, "") else Pair(false, StoreMessageCode.USER_SERVICE_RELEASE_STEPS_ERROR)
    }

    fun getItemBkServiceId(itemId: String): Long {
        var bkServiceId = redisOperation.hget(ITEM_BK_SERVICE_REDIS_KEY, itemId)
        // redis中没有取到蓝盾服务id，则去数据库中取
        if (null == bkServiceId) {
            val serviceItem = client.get(ServiceItemResource::class).getItemById(itemId).data
            if (null == serviceItem) {
                throw ErrorCodeException(errorCode = CommonMessageCode.PARAMETER_IS_INVALID, params = arrayOf(itemId))
            } else {
                bkServiceId = serviceItem.parentId
            }
        }
        return bkServiceId.toLong()
    }

    fun updateExtInfo(
        userId: String,
        serviceId: String,
        serviceCode: String,
        infoResp: EditInfoDTO,
        checkPermissionFlag: Boolean = true
    ): Result<Boolean> {
        logger.info("updateExtInfo: serviceId[$serviceId], serviceCode[$serviceCode] infoResp[$infoResp]")
        // 判断当前用户是否是该扩展的成员
        if (checkPermissionFlag && !storeMemberDao.isStoreMember(
                dslContext = dslContext,
                userId = userId,
                storeCode = serviceCode,
                storeType = StoreTypeEnum.SERVICE.type.toByte()
            )
        ) {
            return MessageCodeUtil.generateResponseDataObject(CommonMessageCode.PERMISSION_DENIED)
        }
        // 查询扩展的最新记录
        val newestServiceRecord = extServiceDao.getNewestServiceByCode(dslContext, serviceCode)
            ?: throw ErrorCodeException(
                errorCode = CommonMessageCode.PARAMETER_IS_INVALID,
                params = arrayOf(serviceCode)
            )
        val editFlag = extServiceCommonService.checkEditCondition(serviceCode)
        if (!editFlag) {
            throw ErrorCodeException(
                errorCode = StoreMessageCode.USER_ATOM_VERSION_IS_NOT_FINISH,
                params = arrayOf(newestServiceRecord.serviceName, newestServiceRecord.version)
            )
        }
        val baseInfo = infoResp.baseInfo
        val settingInfo = infoResp.settingInfo
        if (baseInfo != null) {
            extServiceDao.updateExtServiceBaseInfo(
                dslContext = dslContext,
                userId = userId,
                serviceId = serviceId,
                extServiceUpdateInfo = ExtServiceUpdateInfo(
                    serviceName = baseInfo.serviceName,
                    logoUrl = baseInfo.logoUrl,
                    iconData = baseInfo.iconData,
                    summary = baseInfo.summary,
                    description = baseInfo.description,
                    modifierUser = userId,
                    status = null,
                    latestFlag = null
                )
            )

            // 更新标签信息
            val labelIdList = baseInfo.labels
            if (null != labelIdList) {
                extServiceLabelDao.deleteByServiceId(dslContext, serviceId)
                if (labelIdList.isNotEmpty())
                    extServiceLabelDao.batchAdd(dslContext, userId, serviceId, labelIdList)
            }
            val itemIds = baseInfo.itemIds
            if (itemIds != null) {
                val featureInfoRecord = extFeatureDao.getLatestServiceByCode(dslContext, serviceCode)
                val itemCreateInfoList = getFileServiceProps(
                    serviceCode = serviceCode,
                    repositoryHashId = featureInfoRecord!!.repositoryHashId,
                    fileName = EXTENSION_JSON_NAME,
                    inputItemList = itemIds
                )
                extServiceItemRelDao.deleteByServiceId(dslContext, serviceId)
                extServiceItemRelDao.batchAdd(
                    dslContext = dslContext,
                    userId = userId,
                    serviceId = serviceId,
                    itemPropList = itemCreateInfoList
                )
                // 添加扩展点使用记录
                client.get(ServiceItemResource::class).addServiceNum(itemIds)
            }
        }

        mediaService.deleteByStoreCode(userId, serviceCode, StoreTypeEnum.SERVICE)
        infoResp.mediaInfo?.forEach {
            storeMediaInfoDao.add(
                dslContext = dslContext,
                userId = userId,
                type = StoreTypeEnum.SERVICE.type.toByte(),
                id = UUIDUtil.generate(),
                storeMediaInfoReq = StoreMediaInfoRequest(
                    storeCode = serviceCode,
                    mediaUrl = it.mediaUrl,
                    mediaType = it.mediaType.name,
                    modifier = userId
                )
            )
        }

        if (settingInfo != null) {
            extFeatureDao.updateExtServiceFeatureBaseInfo(
                dslContext = dslContext,
                userId = userId,
                serviceCode = serviceCode,
                extServiceFeatureUpdateInfo = ExtServiceFeatureUpdateInfo(
                    publicFlag = settingInfo.publicFlag,
                    recommentFlag = settingInfo.recommendFlag,
                    certificationFlag = settingInfo.certificationFlag,
                    weight = settingInfo.weight,
                    modifierUser = userId,
                    serviceTypeEnum = settingInfo.type,
                    descInputType = baseInfo?.descInputType ?: DescInputTypeEnum.MANUAL
                )
            )
        }

        return Result(true)
    }

    private fun checkProjectInfo(userId: String, projectCode: String) {
        val permissionCheck = permissionApi.validateUserResourcePermission(
            user = userId,
            projectCode = projectCode,
            serviceCode = pipelineAuthServiceCode,
            resourceType = AuthResourceType.PIPELINE_DEFAULT,
            resourceCode = "*",
            permission = AuthPermission.CREATE
        )
        if (!permissionCheck) {
            throw ErrorCodeException(errorCode = StoreMessageCode.USER_SERVICE_PROJECT_NOT_PERMISSION)
        }
        val projectInfo = client.get(ServiceProjectResource::class).get(projectCode).data
        if (projectInfo == null) {
            throw ErrorCodeException(errorCode = StoreMessageCode.USER_SERVICE_PROJECT_UNENABLE)
        } else {
            if (projectInfo.enabled == false) {
                throw ErrorCodeException(errorCode = StoreMessageCode.USER_SERVICE_PROJECT_UNENABLE)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExtServiceReleaseService::class.java)
    }
}
