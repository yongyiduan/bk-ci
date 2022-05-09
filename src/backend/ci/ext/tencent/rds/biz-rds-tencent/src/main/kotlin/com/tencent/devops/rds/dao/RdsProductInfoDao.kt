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

import com.tencent.devops.model.rds.tables.TRdsProductInfo
import com.tencent.devops.rds.pojo.enums.ProductStatus
import org.jooq.DSLContext
import org.jooq.types.ULong
import org.springframework.stereotype.Repository

@Repository
class RdsProductInfoDao {

    fun createOrUpdateProduct(
        dslContext: DSLContext,
        productId: Long,
        productName: String,
        projectId: String,
        chartName: String,
        chartVersion: String,
        mainYaml: String,
        mainParsed: String,
        resourceYaml: String,
        resourceParsed: String,
        revision: Int,
        status: ProductStatus
    ): Int {
        with(TRdsProductInfo.T_RDS_PRODUCT_INFO) {
            return dslContext.insertInto(
                this,
                PRODUCT_ID,
                PRODUCT_NAME,
                PROJECT_ID,
                CHART_NAME,
                CHART_VERSION,
                MAIN_YAML,
                MAIN_PARSED,
                RESOURCE_YAML,
                RESOURCE_PARSED,
                REVISION,
                STATUS
            ).values(
                ULong.valueOf(productId),
                productName,
                projectId,
                chartName,
                chartVersion,
                mainYaml,
                mainParsed,
                resourceYaml,
                resourceParsed,
                revision,
                status.display
            ).onDuplicateKeyUpdate()
                .set(PRODUCT_NAME, productName)
                .set(CHART_VERSION, chartVersion)
                .set(MAIN_YAML, mainYaml)
                .set(MAIN_PARSED, mainParsed)
                .set(RESOURCE_YAML, resourceYaml)
                .set(RESOURCE_PARSED, resourceParsed)
                .set(REVISION, revision)
                .set(STATUS, status.display)
                .execute()
        }
    }

    fun updateProductStatus(
        dslContext: DSLContext,
        productId: Long,
        status: ProductStatus,
        errorMsg: String?
    ): Boolean {
        with(TRdsProductInfo.T_RDS_PRODUCT_INFO) {
            return dslContext.update(this)
                .set(STATUS, status.display)
                .set(ERR_MSG, errorMsg)
                .where(PRODUCT_ID.eq(ULong.valueOf(productId)))
                .execute() > 0
        }
    }
}
