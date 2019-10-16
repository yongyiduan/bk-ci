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

package com.tencent.devops.project.service

import com.tencent.devops.common.auth.code.AuthServiceCode
import com.tencent.devops.project.pojo.*
import com.tencent.devops.project.pojo.enums.ProjectValidateType
import org.glassfish.jersey.media.multipart.FormDataContentDisposition
import java.io.InputStream

interface ProjectService {

    /**
     * 校验项目名称/英文名称是否合法
     */
    fun validate(validateType: ProjectValidateType, name: String, projectId: String? = null)

    /**
     * 创建项目信息
     */
    fun create(userId: String, projectCreateInfo: ProjectCreateInfo): String

    /**
     * 创建项目信息
     */
    fun create(userId: String, accessToken: String, projectCreateInfo: ProjectCreateInfo)

    /**
     * 根据项目ID/英文ID获取项目信息对象
     * @param englishName projectCode 英文ID
     * @return ProjectVO 如果没有则为null
     */
    fun getByEnglishName(englishName: String): ProjectVO?

    fun getByEnglishName(accessToken: String, englishName: String): ProjectVO

    /**
     * 修改项目信息
     */
    fun update(userId: String, projectId: String, projectUpdateInfo: ProjectUpdateInfo): Boolean

    fun update(userId: String, accessToken: String, projectId: String, projectUpdateInfo: ProjectUpdateInfo)

        /**
     * 更新Logo
     */
    fun updateLogo(
        userId: String,
        projectId: String,
        inputStream: InputStream,
        disposition: FormDataContentDisposition
    ): Result<Boolean>

    fun updateLogo(
            userId: String,
            accessToken: String,
            projectId: String,
            inputStream: InputStream,
            disposition: FormDataContentDisposition
    ): Result<Boolean>

    /**
     * 获取所有项目信息
     */
    fun list(userId: String): List<ProjectVO>

    fun list(accessToken: String, includeDisable: Boolean? = null): List<ProjectVO>

    fun list(projectCodes: Set<String>): List<ProjectVO>

    fun getAllProject(): List<ProjectVO>

    /**
     * 获取用户已的可访问项目列表=
     */
    fun getProjectByUser(userName: String): List<ProjectVO>

    fun getNameByCode(projectCodes: String): HashMap<String, String>
    fun grayProjectSet(): Set<String>
    fun updateEnabled(userId: String, accessToken: String, projectId: String, enabled: Boolean): Result<Boolean>

    //TODO: 带bg属性，需考虑只用在内部版
    fun getProjectEnNamesByOrganization(
            userId: String,
            bgId: Long?,
            deptName: String?,
            centerName: String?,
            interfaceName: String? = "Anon interface"
    ): List<String>

    fun getOrCreatePreProject(userId: String, accessToken: String): ProjectVO

    //TODO: 带bg属性，需考虑只用在内部版
    fun getProjectByGroup(userId: String, bgName: String?, deptName: String?, centerName: String?): List<ProjectVO>

    fun updateUsableStatus(userId: String, projectId: String, enabled: Boolean)

    fun getProjectUsers(accessToken: String, userId: String, projectCode: String): Result<List<String>?>

    fun getProjectUserRoles(
            accessToken: String,
            userId: String,
            projectCode: String,
            serviceCode: AuthServiceCode
    ): List<UserRole>
}