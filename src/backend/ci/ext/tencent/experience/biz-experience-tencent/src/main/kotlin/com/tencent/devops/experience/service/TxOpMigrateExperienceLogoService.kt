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

package com.tencent.devops.experience.service

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.devops.artifactory.api.service.ServiceBkRepoResource
import com.tencent.devops.common.api.constant.SYSTEM
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.api.util.OkhttpUtils
import com.tencent.devops.common.api.util.UUIDUtil
import com.tencent.devops.common.client.Client
import com.tencent.devops.experience.dao.TxMigrateExperienceLogoDao
import com.tencent.devops.model.experience.tables.TExperience
import com.tencent.devops.model.experience.tables.TExperiencePublic
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.util.concurrent.Executors

@Service
class TxOpMigrateExperienceLogoService @Autowired constructor(
    private val dslContext: DSLContext,
    private val txMigrateExperienceLogoDao: TxMigrateExperienceLogoDao,
    private val client: Client
) {

    companion object {
        private val logger = LoggerFactory.getLogger(TxOpMigrateExperienceLogoService::class.java)
        private const val DEFAULT_PAGE_SIZE = 100
    }

    fun migrateExperienceLogo(): Boolean {
        Executors.newFixedThreadPool(1).submit {
            logger.info("begin migrateExperienceLogo!!")
            var offset = 0
            do {
                // 查询体验logo信息记录
                val experienceLogoRecords = txMigrateExperienceLogoDao.getExperienceLogos(
                    dslContext = dslContext,
                    offset = offset,
                    limit = DEFAULT_PAGE_SIZE
                )
                val tExperience = TExperience.T_EXPERIENCE
                experienceLogoRecords?.forEach { experienceLogoRecord ->
                    val logoUrl = experienceLogoRecord[tExperience.LOGO_URL]
                    if (checkLogoUrlCondition(logoUrl)) {
                        return@forEach
                    }
                    val userId = experienceLogoRecord[tExperience.CREATOR]
                    val bkRepoLogoUrl = getBkRepoLogoUrl(logoUrl, userId)
                    if (bkRepoLogoUrl.isNullOrBlank()) {
                        return@forEach
                    }
                    // 更新体验的logo
                    val id = experienceLogoRecord[tExperience.ID]
                    txMigrateExperienceLogoDao.updateExperienceLogo(dslContext, id, bkRepoLogoUrl)
                }
                offset += DEFAULT_PAGE_SIZE
            } while (experienceLogoRecords?.size == DEFAULT_PAGE_SIZE)
            logger.info("end migrateExperienceLogo!!")
        }
        Executors.newFixedThreadPool(1).submit {
            logger.info("begin migrateExperiencePublicLogo!!")
            var offset = 0
            do {
                // 查询公开体验logo信息记录
                val experienceLogoRecords = txMigrateExperienceLogoDao.getExperiencePublicLogos(
                    dslContext = dslContext,
                    offset = offset,
                    limit = DEFAULT_PAGE_SIZE
                )
                val tExperiencePublic = TExperiencePublic.T_EXPERIENCE_PUBLIC
                experienceLogoRecords?.forEach { experienceLogoRecord ->
                    val logoUrl = experienceLogoRecord[tExperiencePublic.LOGO_URL]
                    if (checkLogoUrlCondition(logoUrl)) {
                        return@forEach
                    }
                    val bkRepoLogoUrl = getBkRepoLogoUrl(logoUrl, SYSTEM)
                    if (bkRepoLogoUrl.isNullOrBlank()) {
                        return@forEach
                    }
                    // 更新公开体验的logo
                    val id = experienceLogoRecord[tExperiencePublic.ID]
                    txMigrateExperienceLogoDao.updateExperiencePublicLogo(dslContext, id, bkRepoLogoUrl)
                }
                offset += DEFAULT_PAGE_SIZE
            } while (experienceLogoRecords?.size == DEFAULT_PAGE_SIZE)
            logger.info("end migrateExperiencePublicLogo!!")
        }
        return true
    }

    private fun checkLogoUrlCondition(logoUrl: String?) =
        logoUrl.isNullOrBlank() || logoUrl.contains("staticfile") || !logoUrl.startsWith("http")

    private fun getBkRepoLogoUrl(logoUrl: String, userId: String): String? {
        val fileType = getFileType(logoUrl)
        val tmpFile = Files.createTempFile(UUIDUtil.generate(), ".$fileType").toFile()
        val fileName = tmpFile.name
        try {
            // 从s3下载logo
            OkhttpUtils.downloadFile(logoUrl, tmpFile)
            // 把logo上传至bkrepo
            val serviceUrlPrefix = client.getServiceUrl(ServiceBkRepoResource::class)
            val destPath = "file/$fileType/$fileName"
            val serviceUrl =
                "$serviceUrlPrefix/service/bkrepo/statics/file/upload?userId=$userId&destPath=$destPath"
            OkhttpUtils.uploadFile(serviceUrl, tmpFile).use { response ->
                val responseContent = response.body()!!.string()
                if (!response.isSuccessful) {
                    logger.warn("$userId upload file:$fileName fail,responseContent:$responseContent")
                }
                val result = JsonUtil.to(responseContent, object : TypeReference<Result<String?>>() {})
                logger.info("requestUrl:$serviceUrl,result:$result")
                return result.data
            }
        } catch (ignore: Throwable) {
            logger.warn("$userId upload file:$fileName fail, error is:", ignore)
        } finally {
            // 删除临时文件
            tmpFile.delete()
        }
        return null
    }

    private fun getFileType(logoUrl: String): String {
        val paramIndex = logoUrl.lastIndexOf("?")
        val url = if (paramIndex > 0) logoUrl.substring(0, paramIndex) else logoUrl
        val index = url.lastIndexOf(".")
        return url.substring(index + 1).toLowerCase()
    }
}