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

package com.tencent.devops.rds.service

import com.tencent.devops.common.api.pojo.Page
import com.tencent.devops.common.api.util.PageUtil
import com.tencent.devops.rds.dao.RdsProductInfoDao
import com.tencent.devops.rds.pojo.ProductDetailReq
import com.tencent.devops.rds.pojo.ProductDetailResp
import com.tencent.devops.rds.pojo.ProductListReq
import com.tencent.devops.rds.pojo.RdsProductStatusResult
import com.tencent.devops.rds.pojo.toRdsProductStatusResult
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ProductInfoService @Autowired constructor(
    private val dslContext: DSLContext,
    private val productInfoDao: RdsProductInfoDao
) {

    fun listStatus(
        userId: String,
        req: ProductListReq
    ): Page<RdsProductStatusResult> {
        val sqlLimit = PageUtil.convertPageSizeToSQLLimit(req.page, req.pageSize)
        val offset = sqlLimit.offset
        val limit = sqlLimit.limit

        val count = productInfoDao.count(dslContext, userId)
        if (count == 0) {
            return Page(
                page = req.page,
                pageSize = req.pageSize,
                count = 0,
                records = emptyList()
            )
        }
        val list = productInfoDao.listStatus(dslContext, userId, offset, limit).map { it.toRdsProductStatusResult() }
        return Page(
            page = req.page,
            pageSize = req.pageSize,
            count = count.toLong(),
            records = list
        )
    }

    fun status(productId: Long): RdsProductStatusResult? {
        return productInfoDao.status(dslContext, productId)?.toRdsProductStatusResult()
    }

    fun detail(productId: Long, req: ProductDetailReq): ProductDetailResp {
        val record = productInfoDao.get(dslContext, productId, req.resource)
        return ProductDetailResp(
            resourceYaml = record.resourceYaml
        )
    }
}
