/*
 *
 *  * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *  *
 *  * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *  *
 *  * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *  *
 *  * A copy of the MIT License is included in this file.
 *  *
 *  *
 *  * Terms of the MIT License:
 *  * ---------------------------------------------------
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 *  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *  * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 *  * the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 *  * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 *  * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.tencent.devops.rds.dao

import com.tencent.devops.model.rds.tables.TRdsProductUser
import com.tencent.devops.model.rds.tables.records.TRdsProductUserRecord
import com.tencent.devops.rds.pojo.enums.ProductUserType
import org.jooq.DSLContext
import org.jooq.types.ULong
import org.springframework.stereotype.Repository

@Repository
class RdsProductUserDao {

    fun create(
        dslContext: DSLContext,
        productId: Long,
        userId: String,
        userType: ProductUserType
    ) {
        with(TRdsProductUser.T_RDS_PRODUCT_USER) {
            dslContext.insertInto(
                this,
                PRODUCT_ID,
                USER_ID,
                TYPE
            ).values(
                ULong.valueOf(productId),
                userId,
                userType.name
            )
                .onDuplicateKeyUpdate()
                .set(TYPE, userType.name)
                .execute()
        }
    }

    fun batchSave(
        dslContext: DSLContext,
        productId: Long,
        userWithTypeMap: Map<String, ProductUserType>
    ) {
        with(TRdsProductUser.T_RDS_PRODUCT_USER) {
            userWithTypeMap.forEach { user, type ->
                dslContext.insertInto(
                    this,
                    PRODUCT_ID,
                    USER_ID,
                    TYPE
                ).values(
                    ULong.valueOf(productId),
                    user,
                    type.name
                )
                    .onDuplicateKeyUpdate()
                    .set(TYPE, type.name)
                    .execute()
            }
        }
    }

    fun batchDelete(
        dslContext: DSLContext,
        productId: Long,
        userList: List<String>
    ) {
        with(TRdsProductUser.T_RDS_PRODUCT_USER) {
            userList.forEach {
                dslContext.deleteFrom(this)
                    .where(PRODUCT_ID.eq(ULong.valueOf(productId)))
                    .and(USER_ID.eq(it))
                    .execute()
            }
        }
    }

    fun getProductUserList(
        dslContext: DSLContext,
        productId: Long
    ): List<TRdsProductUserRecord> {
        with(TRdsProductUser.T_RDS_PRODUCT_USER) {
            return dslContext.selectFrom(this)
                .where(PRODUCT_ID.eq(ULong.valueOf(productId)))
                .fetch()
        }
    }
}
