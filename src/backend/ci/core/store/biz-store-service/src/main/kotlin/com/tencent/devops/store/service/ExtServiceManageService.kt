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

import com.tencent.devops.common.api.constant.CommonMessageCode
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.service.utils.MessageCodeUtil
import com.tencent.devops.store.constant.StoreMessageCode
import com.tencent.devops.store.dao.ExtItemServiceDao
import com.tencent.devops.store.dao.ExtServiceDao
import com.tencent.devops.store.dao.ExtServiceEnvDao
import com.tencent.devops.store.dao.ExtServiceFeatureDao
import com.tencent.devops.store.dao.ExtServiceVersionLogDao
import com.tencent.devops.store.dao.common.StoreMemberDao
import com.tencent.devops.store.dao.common.StoreProjectRelDao
import com.tencent.devops.store.pojo.common.enums.StoreTypeEnum
import com.tencent.devops.store.service.common.StoreCommonService
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
abstract class ExtServiceManageService {

    @Autowired
    lateinit var dslContext: DSLContext

    @Autowired
    lateinit var extServiceDao: ExtServiceDao

    @Autowired
    lateinit var extServiceFeatureDao: ExtServiceFeatureDao

    @Autowired
    lateinit var extServiceEnvDao: ExtServiceEnvDao

    @Autowired
    lateinit var extServiceVersionLogDao: ExtServiceVersionLogDao

    @Autowired
    lateinit var extItemServiceDao: ExtItemServiceDao

    @Autowired
    lateinit var storeMemberDao: StoreMemberDao

    @Autowired
    lateinit var storeProjectRelDao: StoreProjectRelDao

    @Autowired
    lateinit var storeCommonService: StoreCommonService

    @Autowired
    lateinit var extServiceKubernetesService: ExtServiceKubernetesService

    companion object {
        private val logger = LoggerFactory.getLogger(ExtServiceManageService::class.java)
    }

    fun deleteExtensionService(userId: String, serviceCode: String): Result<Boolean> {
        logger.info("deleteService userId: $userId , serviceCode: $serviceCode")
        val result = extServiceDao.getExtServiceIds(dslContext, serviceCode)
        var extServiceId: String? = null
        val extServiceIdList = result.map {
            val serviceId = it.value1()
            val latestFlag = it.value2()
            if (latestFlag) {
                extServiceId = serviceId
            }
            serviceId
        }
        if (extServiceId.isNullOrBlank()) {
            return MessageCodeUtil.generateResponseDataObject(
                StoreMessageCode.USER_SERVICE_NOT_EXIST,
                arrayOf(serviceCode)
            )
        }
        val type = StoreTypeEnum.SERVICE.type.toByte()
        if (!storeMemberDao.isStoreAdmin(dslContext, userId, serviceCode, type)) {
            return MessageCodeUtil.generateResponseDataObject(CommonMessageCode.PERMISSION_DENIED)
        }
        val releasedCount = extServiceDao.countReleaseServiceByCode(dslContext, serviceCode)
        logger.info("releasedCount: $releasedCount")
        if (releasedCount > 0) {
            return MessageCodeUtil.generateResponseDataObject(
                StoreMessageCode.USER_SERVICE_RELEASED_IS_NOT_ALLOW_DELETE,
                arrayOf(serviceCode)
            )
        }
        // 如果已经被安装到其他项目下使用，不能删除
        val installedCount = storeProjectRelDao.countInstalledProject(dslContext, serviceCode, type)
        logger.info("installedCount: $releasedCount")
        if (installedCount > 0) {
            return MessageCodeUtil.generateResponseDataObject(
                StoreMessageCode.USER_SERVICE_USED_IS_NOT_ALLOW_DELETE,
                arrayOf(serviceCode)
            )
        }
        val initProjectCode = storeProjectRelDao.getInitProjectCodeByStoreCode(
            dslContext = dslContext,
            storeCode = serviceCode,
            storeType = StoreTypeEnum.SERVICE.type.toByte()
        )
        // 停止微扩展灰度命名空间和正式命名空间的应用
        val serviceStopAppResult = extServiceKubernetesService.stopExtService(
            userId = userId,
            serviceCode = serviceCode,
            deploymentName = serviceCode,
            serviceName = "$serviceCode-service"
        )
        logger.info("serviceStopAppResult is :$serviceStopAppResult")
        doServiceDeleteBus(
            userId = userId,
            serviceId = extServiceId!!,
            serviceCode = serviceCode,
            initProjectCode = initProjectCode!!
        )
        dslContext.transaction { t ->
            val context = DSL.using(t)
            storeCommonService.deleteStoreInfo(context, serviceCode, StoreTypeEnum.SERVICE.type.toByte())
            extServiceEnvDao.deleteEnvInfo(context, extServiceIdList)
            extServiceFeatureDao.deleteExtFeatureServiceData(context, serviceCode)
            extServiceVersionLogDao.deleteByServiceId(context, extServiceIdList)
            extItemServiceDao.deleteByServiceId(context, extServiceIdList)
            extServiceDao.deleteExtServiceData(context, serviceCode)
        }
        return Result(true)
    }

    abstract fun doServiceDeleteBus(
        userId: String,
        serviceId: String,
        serviceCode: String,
        initProjectCode: String
    )
}
