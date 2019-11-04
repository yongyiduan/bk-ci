/*
 * Tencent is pleased to support the open source community by making BK-REPO 蓝鲸制品库 available.
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
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.devops.quality.resources.v2

import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.api.util.HashUtil
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.quality.api.v2.UserQualityIndicatorResource
import com.tencent.devops.quality.api.v2.pojo.QualityIndicator
import com.tencent.devops.quality.api.v2.pojo.RuleIndicatorSet
import com.tencent.devops.quality.api.v2.pojo.request.IndicatorCreate
import com.tencent.devops.quality.api.v2.pojo.response.IndicatorListResponse
import com.tencent.devops.quality.api.v2.pojo.response.IndicatorStageGroup
import com.tencent.devops.quality.service.v2.QualityIndicatorService
import com.tencent.devops.quality.service.v2.QualityTemplateService
import org.springframework.beans.factory.annotation.Autowired

@RestResource
class UserQualityIndicatorResourceImpl @Autowired constructor(
    val indicatorService: QualityIndicatorService,
    val templateService: QualityTemplateService
) : UserQualityIndicatorResource {
    override fun update(userId: String, projectId: String, indicatorId: String, indicatorCreate: IndicatorCreate): Result<Boolean> {
        return Result(indicatorService.userUpdate(userId, projectId, indicatorId, indicatorCreate))
    }

    override fun create(userId: String, projectId: String, indicatorCreate: IndicatorCreate): Result<Boolean> {
        return Result(indicatorService.userCreate(userId, projectId, indicatorCreate))
    }

    override fun queryIndicatorList(projectId: String, keyword: String?): Result<IndicatorListResponse> {
        return Result(indicatorService.userQueryIndicatorList(projectId, keyword))
    }

    override fun get(projectId: String, indicatorId: String): Result<QualityIndicator> {
        return Result(indicatorService.userGet(projectId, indicatorId))
    }

    override fun listIndicators(projectId: String): Result<List<IndicatorStageGroup>> {
        return Result(indicatorService.listByLevel(projectId))
    }

    override fun listIndicatorSet(): Result<List<RuleIndicatorSet>> {
        return Result(templateService.userListIndicatorSet())
    }

    override fun delete(userId: String, projectId: String, indicatorId: String): Result<Boolean> {
        return Result(indicatorService.userDelete(projectId, HashUtil.decodeIdToLong(indicatorId)))
    }
}