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

import com.tencent.devops.common.api.util.YamlUtil
import com.tencent.devops.common.api.util.timestampmilli
import com.tencent.devops.model.rds.tables.TRdsProductInfo
import com.tencent.devops.rds.pojo.ProductCreateInfo
import com.tencent.devops.rds.pojo.RdsProductInfo
import com.tencent.devops.rds.pojo.yaml.Main
import com.tencent.devops.rds.pojo.yaml.Resource
import org.jooq.DSLContext
import org.jooq.types.ULong
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class RdsProductInfoDao {

    fun createProduct(
        dslContext: DSLContext,
        projectId: String,
        productCreateInfo: ProductCreateInfo
    ): Int {
        with(TRdsProductInfo.T_RDS_PRODUCT_INFO) {
            return dslContext.insertInto(this)
                .set(PRODUCT_ID, ULong.valueOf(productCreateInfo.productId))
                .set(PRODUCT_NAME, productCreateInfo.productName)
                .set(MASTER, productCreateInfo.master)
                .set(PROJECT_ID, projectId)
                .execute()
        }
    }

    fun updateProductChart(
        dslContext: DSLContext,
        productId: Long,
        mainYaml: String?,
        main: Main?,
        resourceYaml: String?,
        resource: Resource?
    ) {
        with(TRdsProductInfo.T_RDS_PRODUCT_INFO) {
            dslContext.update(this)
                .set(MAIN_YAML, mainYaml)
                .set(MAIN_PARSED, main?.let { YamlUtil.toYaml(it) })
                .set(RESOURCE_YAML, resourceYaml)
                .set(RESOURCE_PARSED, resource?.let { YamlUtil.toYaml(it) })
                .where(PRODUCT_ID.eq(ULong.valueOf(productId)))
                .execute()
        }
    }

    fun updateProductInfo(
        dslContext: DSLContext,
        productInfo: RdsProductInfo
    ) {
        with(TRdsProductInfo.T_RDS_PRODUCT_INFO) {
            dslContext.update(this)
                .set(UPDATE_TIME, LocalDateTime.now())
                .where(PRODUCT_ID.eq(ULong.valueOf(productInfo.productId)))
                .execute()
        }
    }

    fun getProduct(dslContext: DSLContext, productId: Long): RdsProductInfo? {
        with(TRdsProductInfo.T_RDS_PRODUCT_INFO) {
            val record = dslContext.selectFrom(this)
                .where(PRODUCT_ID.eq(ULong.valueOf(productId)))
                .fetchAny() ?: return null
            return RdsProductInfo(
                productId = record.productId.toLong(),
                master = record.master,
                createTime = record.createTime.timestampmilli(),
                updateTime = record.updateTime.timestampmilli()
            )
        }
    }
}
