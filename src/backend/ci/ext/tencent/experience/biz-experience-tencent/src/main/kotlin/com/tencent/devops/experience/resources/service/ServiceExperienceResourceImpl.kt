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

package com.tencent.devops.experience.resources.service

import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.api.exception.ParamBlankException
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.api.util.HashUtil
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.experience.api.service.ServiceExperienceResource
import com.tencent.devops.experience.constant.ExperienceConstant
import com.tencent.devops.experience.constant.ExperienceMessageCode
import com.tencent.devops.experience.dao.ExperiencePublicDao
import com.tencent.devops.experience.pojo.Experience
import com.tencent.devops.experience.pojo.ExperienceJumpInfo
import com.tencent.devops.experience.pojo.ExperienceServiceCreate
import com.tencent.devops.experience.service.ExperienceBaseService
import com.tencent.devops.experience.service.ExperienceDownloadService
import com.tencent.devops.experience.service.ExperienceService
import org.jooq.DSLContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

@RestResource
class ServiceExperienceResourceImpl @Autowired constructor(
    private val experienceService: ExperienceService,
    private val experienceBaseService: ExperienceBaseService,
    private val experienceDownloadService: ExperienceDownloadService,
    private val experiencePublicDao: ExperiencePublicDao,
    private val dslContext: DSLContext
) : ServiceExperienceResource {

    override fun create(userId: String, projectId: String, experience: ExperienceServiceCreate): Result<Boolean> {
        checkParam(userId, projectId)
        experienceService.serviceCreate(userId, projectId, experience)
        return Result(true)
    }

    override fun count(projectIds: Set<String>?, expired: Boolean?): Result<Map<String, Int>> {
        return Result(experienceService.count(projectIds ?: setOf(), expired))
    }

    override fun get(userId: String, projectId: String, experienceHashId: String): Result<Experience> {
        checkParam(userId, projectId)
        return Result(experienceService.get(userId, experienceHashId, false))
    }

    override fun check(userId: String, experienceHashId: String, organization: String?): Result<Boolean> {
        return Result(
            experienceBaseService.userCanExperience(
                userId,
                HashUtil.decodeIdToLong(experienceHashId),
                organization == ExperienceConstant.ORGANIZATION_OUTER
            )
        )
    }

    override fun jumpInfo(projectId: String, bundleIdentifier: String, platform: String): Result<ExperienceJumpInfo> {
        if (platform != "ANDROID" && platform != "IOS") {
            logger.warn("platform is illegal , {}", platform)
            throw ErrorCodeException(
                statusCode = 403,
                defaultMessage = "平台错误",
                errorCode = ExperienceMessageCode.EXPERIENCE_NO_AVAILABLE
            )
        }

        val experiencePublicRecord = experiencePublicDao.getByBundleId(
            dslContext = dslContext,
            projectId = projectId,
            bundleIdentifier = bundleIdentifier,
            platform = platform
        )

        if (null == experiencePublicRecord) {
            logger.warn(
                "can not found record , projectId:{} , bundleIdentifier:{} , platform:{}",
                projectId,
                bundleIdentifier,
                platform
            )
            throw ErrorCodeException(
                statusCode = 403,
                defaultMessage = "未找到对应的体验",
                errorCode = ExperienceMessageCode.EXPERIENCE_NO_AVAILABLE
            )
        }

        val scheme = if (platform == "ANDROID") {
            "bkdevopsapp://bkdevopsapp/app/experience/expDetail/${experiencePublicRecord.recordId}"
        } else {
            "bkdevopsapp://app/experience/expDetail/${experiencePublicRecord.recordId}"
        }

        return Result(
            ExperienceJumpInfo(
                scheme,
                experienceDownloadService.getExternalDownloadUrl(
                    "third_app",
                    experiencePublicRecord.recordId,
                    false,
                    10 * 60
                ).url
            )
        )
    }

    private fun checkParam(userId: String, projectId: String) {
        if (userId.isBlank()) {
            throw ParamBlankException("Invalid userId")
        }
        if (projectId.isBlank()) {
            throw ParamBlankException("Invalid userId")
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ServiceExperienceResourceImpl::class.java)
    }
}
