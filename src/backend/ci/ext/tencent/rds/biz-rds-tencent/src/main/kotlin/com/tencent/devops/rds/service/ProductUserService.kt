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

import com.tencent.devops.common.client.Client
import com.tencent.devops.project.api.service.ServiceProjectResource
import com.tencent.devops.project.pojo.ProjectCreateUserInfo
import com.tencent.devops.rds.dao.RdsProductUserDao
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ProductUserService @Autowired constructor(
    private val client: Client,
    private val dslContext: DSLContext,
    private val productUserDao: RdsProductUserDao
) {

    private val logger = LoggerFactory.getLogger(ProductUserService::class.java)

    fun saveProductMembers(
        userId: String,
        productId: Long,
        projectId: String,
        members: List<String>?,
        masterUserId: String?
    ): Boolean {
        try {
            val member = client.get(ServiceProjectResource::class).createProjectUser(
                projectId = projectId,
                createInfo = ProjectCreateUserInfo(
                    createUserId = userId,
                    roleName = "CI管理员",
                    roleId = 9,
                    userIds = members
                )
            )
            val managerUsers = mutableListOf(userId)
            val productUsers = mutableListOf<String>()
            masterUserId?.let {
                managerUsers.add(masterUserId)
                productUsers.add(masterUserId)
            }
            val manager = client.get(ServiceProjectResource::class).createProjectUser(
                projectId = projectId,
                createInfo = ProjectCreateUserInfo(
                    createUserId = userId,
                    roleName = "管理员",
                    roleId = 2,
                    userIds = managerUsers
                )
            )
            val success = member.data == true && manager.data == true
            if (success) {
                productUserDao.batchSave(
                    dslContext = dslContext,
                    productId = productId,
                    userList = productUsers.plus(members ?: emptyList())
                )
            }
            return success
        } catch (t: Throwable) {
            logger.error("RDS|saveProductMembers failed with: ", t)
        }
        return false
    }

    fun deleteProductMembers(
        userId: String,
        productId: Long,
        projectId: String,
        members: List<String>
    ): Boolean {
        try {
            // TODO: 增加蓝盾删除项目成员（等V3支持）
//            val member = client.get(ServiceProjectResource::class).createProjectUser(
//                projectId = projectId,
//                createInfo = ProjectCreateUserInfo(
//                    createUserId = userId,
//                    roleName = "CI管理员",
//                    roleId = 9,
//                    userIds = members
//                )
//            )
            productUserDao.batchDelete(dslContext, productId, members)
            return true
        } catch (t: Throwable) {
            logger.error("RDS|saveProductMembers failed with: ", t)
        }
        return false
    }
}
