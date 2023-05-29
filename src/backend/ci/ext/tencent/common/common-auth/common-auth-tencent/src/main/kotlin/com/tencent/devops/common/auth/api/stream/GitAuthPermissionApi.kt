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
 *
 */

package com.tencent.devops.common.auth.api.stream

import com.tencent.devops.auth.api.service.ServicePermissionAuthResource
import com.tencent.devops.common.auth.api.AuthPermission
import com.tencent.devops.common.auth.api.AuthPermissionApi
import com.tencent.devops.common.auth.api.AuthResourceType
import com.tencent.devops.common.auth.api.pojo.AuthResourceInstance
import com.tencent.devops.common.auth.code.AuthServiceCode
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.client.ClientTokenService

class GitAuthPermissionApi(
    private val client: Client,
    val tokenService: ClientTokenService
) : AuthPermissionApi {
    override fun validateUserResourcePermission(
        user: String,
        serviceCode: AuthServiceCode,
        resourceType: AuthResourceType,
        projectCode: String,
        permission: AuthPermission
    ): Boolean {
        return client.get(ServicePermissionAuthResource::class).validateUserResourcePermission(
            token = tokenService.getSystemToken(null)!!,
            userId = user,
            action = permission.value,
            projectCode = projectCode,
            resourceCode = resourceType.value
        ).data!!
    }

    override fun validateUserResourcePermission(
        user: String,
        serviceCode: AuthServiceCode,
        resourceType: AuthResourceType,
        projectCode: String,
        resourceCode: String,
        permission: AuthPermission,
        relationResourceType: AuthResourceType?
    ): Boolean {
        return client.get(ServicePermissionAuthResource::class).validateUserResourcePermissionByRelation(
            token = tokenService.getSystemToken(null)!!,
            userId = user,
            resourceType = resourceType.value,
            projectCode = projectCode,
            action = permission.value,
            resourceCode = resourceCode,
            relationResourceType = relationResourceType?.value
        ).data!!
    }

    override fun validateUserResourcePermission(
        user: String,
        serviceCode: AuthServiceCode,
        projectCode: String,
        permission: AuthPermission,
        resource: AuthResourceInstance
    ): Boolean {
        return validateUserResourcePermission(
            user = user,
            serviceCode = serviceCode,
            resourceType = AuthResourceType.get(resource.resourceType),
            projectCode = projectCode,
            resourceCode = resource.resourceCode,
            permission = permission,
            relationResourceType = null
        )
    }

    override fun getUserResourceByPermission(
        user: String,
        serviceCode: AuthServiceCode,
        resourceType: AuthResourceType,
        projectCode: String,
        permission: AuthPermission,
        supplier: (() -> List<String>)?
    ): List<String> {
        return client.get(ServicePermissionAuthResource::class).getUserResourceByPermission(
            token = tokenService.getSystemToken(null)!!,
            userId = user,
            projectCode = projectCode,
            action = permission.value,
            resourceType = resourceType.value
        ).data ?: emptyList()
    }

    override fun getUserResourcesByPermissions(
        user: String,
        serviceCode: AuthServiceCode,
        resourceType: AuthResourceType,
        projectCode: String,
        permissions: Set<AuthPermission>,
        supplier: (() -> List<String>)?
    ): Map<AuthPermission, List<String>> {
        return client.get(ServicePermissionAuthResource::class).getUserResourcesByPermissions(
            token = tokenService.getSystemToken(null)!!,
            userId = user,
            resourceType = resourceType.value,
            projectCode = projectCode,
            action = permissions.map { it.value }
        ).data ?: emptyMap()
    }

    override fun getUserResourcesByPermissions(
        userId: String,
        scopeType: String,
        scopeId: String,
        resourceType: AuthResourceType,
        permissions: Set<AuthPermission>,
        systemId: AuthServiceCode,
        supplier: (() -> List<String>)?
    ): Map<AuthPermission, List<String>> {
        return client.get(ServicePermissionAuthResource::class).getUserResourcesByPermissions(
            token = tokenService.getSystemToken(null)!!,
            userId = userId,
            resourceType = resourceType.value,
            projectCode = scopeId,
            action = permissions.map { it.value }
        ).data ?: emptyMap()
    }

    override fun getUserResourceAndParentByPermission(
        user: String,
        serviceCode: AuthServiceCode,
        projectCode: String,
        permission: AuthPermission,
        resourceType: AuthResourceType
    ): Map<String, List<String>> {
        return emptyMap()
    }

    override fun filterResourcesByPermissions(
        user: String,
        serviceCode: AuthServiceCode,
        resourceType: AuthResourceType,
        projectCode: String,
        permissions: Set<AuthPermission>,
        resources: List<AuthResourceInstance>
    ): Map<AuthPermission, List<String>> {
        return emptyMap()
    }

    override fun addResourcePermissionForUsers(
        userId: String,
        projectCode: String,
        serviceCode: AuthServiceCode,
        permission: AuthPermission,
        resourceType: AuthResourceType,
        resourceCode: String,
        userIdList: List<String>,
        supplier: (() -> List<String>)?
    ): Boolean {
        return true
    }
}