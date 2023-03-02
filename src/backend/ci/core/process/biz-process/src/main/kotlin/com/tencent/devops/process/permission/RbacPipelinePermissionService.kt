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

package com.tencent.devops.process.permission

import com.tencent.devops.common.api.exception.PermissionForbiddenException
import com.tencent.devops.common.api.util.HashUtil
import com.tencent.devops.common.auth.api.AuthPermission
import com.tencent.devops.common.auth.api.AuthPermissionApi
import com.tencent.devops.common.auth.api.AuthProjectApi
import com.tencent.devops.common.auth.api.AuthResourceApi
import com.tencent.devops.common.auth.api.AuthResourceType
import com.tencent.devops.common.auth.api.pojo.AuthResourceInstance
import com.tencent.devops.common.auth.api.pojo.BkAuthGroup
import com.tencent.devops.common.auth.code.PipelineAuthServiceCode
import com.tencent.devops.process.engine.dao.PipelineInfoDao
import com.tencent.devops.process.service.view.PipelineViewGroupService
import org.jooq.DSLContext

@Suppress("LongParameterList")
class RbacPipelinePermissionService constructor(
    val authPermissionApi: AuthPermissionApi,
    val authProjectApi: AuthProjectApi,
    val pipelineAuthServiceCode: PipelineAuthServiceCode,
    val dslContext: DSLContext,
    val pipelineInfoDao: PipelineInfoDao,
    val pipelineViewGroupService: PipelineViewGroupService,
    val authResourceApi: AuthResourceApi
) : PipelinePermissionService {

    override fun checkPipelinePermission(userId: String, projectId: String, permission: AuthPermission): Boolean {
        return authPermissionApi.validateUserResourcePermission(
            user = userId,
            serviceCode = pipelineAuthServiceCode,
            resourceType = resourceType,
            permission = permission,
            projectCode = projectId
        )
    }

    override fun checkPipelinePermission(
        userId: String,
        projectId: String,
        pipelineId: String,
        permission: AuthPermission
    ): Boolean {
        val parents = mutableListOf<AuthResourceInstance>()
        val projectInstance = AuthResourceInstance(
            resourceType = AuthResourceType.PROJECT.value,
            resourceCode = projectId
        )
        parents.add(projectInstance)
        pipelineViewGroupService.listViewIdsByPipelineId(projectId, pipelineId).forEach { viewId ->
            parents.add(
                AuthResourceInstance(
                    resourceType = AuthResourceType.PIPELINE_GROUP.value,
                    resourceCode = HashUtil.encodeLongId(viewId),
                    parents = listOf(projectInstance)
                )
            )
        }
        val pipelineInstance = AuthResourceInstance(
            resourceType = resourceType.value,
            resourceCode = pipelineId,
            parents = parents
        )
        return authPermissionApi.validateUserResourcePermission(
            user = userId,
            serviceCode = pipelineAuthServiceCode,
            projectCode = projectId,
            permission = permission,
            resource = pipelineInstance
        )
    }

    override fun validPipelinePermission(
        userId: String,
        projectId: String,
        pipelineId: String,
        permission: AuthPermission,
        message: String?
    ) {
        if (pipelineId == "*") {
            if (!checkPipelinePermission(
                    userId = userId,
                    projectId = projectId,
                    permission = permission
                )
            ) {
                throw PermissionForbiddenException(message)
            }
            return
        }

        val permissionCheck = checkPipelinePermission(
            userId = userId,
            projectId = projectId,
            pipelineId = pipelineId,
            permission = permission
        )
        if (!permissionCheck) {
            throw PermissionForbiddenException(message)
        }
    }

    override fun getResourceByPermission(userId: String, projectId: String, permission: AuthPermission): List<String> {
        // 先获取项目下有权限的流水线id列表
        val authPipelineIds = authPermissionApi.getUserResourceByPermission(
            user = userId,
            serviceCode = pipelineAuthServiceCode,
            projectCode = projectId,
            permission = permission,
            supplier = null,
            resourceType = resourceType
        )

        // 如果由所有流水线权限,则不再查流水线组的权限
        if (authPipelineIds.contains("*")) {
            return pipelineInfoDao.searchByProject(
                dslContext = dslContext, projectId = projectId
            )?.map { it.pipelineId }?.toList() ?: emptyList()
        }

        // 再获取有权限的流水线组Id列表,通过流水线组ID获取流水线ID列表
        val resources = mutableListOf<AuthResourceInstance>()
        val projectResource = AuthResourceInstance(
            resourceType = AuthResourceType.PROJECT.value,
            resourceCode = projectId
        )
        pipelineViewGroupService.listViewIdsByProjectId(projectId = projectId).forEach { viewId ->
            val pipelineGroupResource = AuthResourceInstance(
                resourceType = AuthResourceType.PIPELINE_GROUP.value,
                resourceCode = HashUtil.encodeLongId(viewId),
                parents = listOf(projectResource)
            )
            val pipelineResource = AuthResourceInstance(
                resourceType = AuthResourceType.PIPELINE_DEFAULT.value,
                resourceCode = HashUtil.encodeLongId(viewId),
                parents = listOf(pipelineGroupResource)
            )
            resources.add(pipelineResource)
        }
        val authViewIds = authPermissionApi.filterUserResourceByPermission(
            user = userId,
            serviceCode = pipelineAuthServiceCode,
            projectCode = projectId,
            permission = permission,
            resourceType = resourceType,
            resources = resources
        )
        val viewPipelineIds = pipelineViewGroupService.listPipelineIdsByViewIds(projectId, authViewIds)

        val pipelineIds = mutableSetOf<String>()
        pipelineIds.addAll(authPipelineIds)
        pipelineIds.addAll(viewPipelineIds)
        return pipelineIds.toList()
    }

    override fun createResource(userId: String, projectId: String, pipelineId: String, pipelineName: String) {
        return authResourceApi.createResource(
            user = userId,
            projectCode = projectId,
            serviceCode = pipelineAuthServiceCode,
            resourceType = resourceType,
            resourceCode = pipelineId,
            resourceName = pipelineName
        )
    }

    override fun modifyResource(projectId: String, pipelineId: String, pipelineName: String) {
        authResourceApi.modifyResource(
            serviceCode = pipelineAuthServiceCode,
            resourceType = resourceType,
            projectCode = projectId,
            resourceCode = pipelineId,
            resourceName = pipelineName
        )
    }

    override fun deleteResource(projectId: String, pipelineId: String) {
        authResourceApi.deleteResource(
            serviceCode = pipelineAuthServiceCode,
            resourceType = resourceType,
            projectCode = projectId,
            resourceCode = pipelineId
        )
    }

    override fun isProjectUser(userId: String, projectId: String, group: BkAuthGroup?): Boolean {
        return authProjectApi.isProjectUser(
            user = userId,
            projectCode = projectId,
            group = group,
            serviceCode = pipelineAuthServiceCode
        )
    }

    override fun checkProjectManager(userId: String, projectId: String): Boolean {
        return authProjectApi.checkProjectManager(userId, pipelineAuthServiceCode, projectId)
    }

    companion object {
        private val resourceType = AuthResourceType.PIPELINE_DEFAULT
    }
}
