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

package com.tencent.devops.rds.dao

import com.tencent.devops.common.api.util.timestamp
import com.tencent.devops.model.rds.tables.TRdsProductInfo
import com.tencent.devops.model.rds.tables.TRdsProductUser
import com.tencent.devops.rds.pojo.RdsProductDetail
import com.tencent.devops.rds.pojo.RdsProductStatus
import com.tencent.devops.rds.pojo.enums.ProductStatus
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class RdsProductInfoDao {

    fun createProduct(
        dslContext: DSLContext,
        productCode: String,
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
    ): LocalDateTime {
        val time = LocalDateTime.now()
        with(TRdsProductInfo.T_RDS_PRODUCT_INFO) {
            dslContext.insertInto(
                this,
                PRODUCT_CODE,
                DISPLAY_NAME,
                PROJECT_ID,
                CHART_NAME,
                CHART_VERSION,
                MAIN_YAML,
                MAIN_PARSED,
                RESOURCE_YAML,
                RESOURCE_PARSED,
                REVISION,
                STATUS,
                CREATE_TIME,
                UPDATE_TIME
            ).values(
                productCode,
                productName,
                projectId,
                chartName,
                chartVersion,
                mainYaml,
                mainParsed,
                resourceYaml,
                resourceParsed,
                revision,
                status.display,
                time,
                time
            )
        }
        return time
    }

    fun updateProduct(
        dslContext: DSLContext,
        productCode: String,
        productName: String,
        chartVersion: String,
        mainYaml: String,
        mainParsed: String,
        resourceYaml: String,
        resourceParsed: String,
        revision: Int,
        status: ProductStatus
    ): LocalDateTime {
        val time = LocalDateTime.now()
        with(TRdsProductInfo.T_RDS_PRODUCT_INFO) {
            dslContext.update(this)
                .set(DISPLAY_NAME, productName)
                .set(CHART_VERSION, chartVersion)
                .set(MAIN_YAML, mainYaml)
                .set(MAIN_PARSED, mainParsed)
                .set(RESOURCE_YAML, resourceYaml)
                .set(RESOURCE_PARSED, resourceParsed)
                .set(REVISION, revision)
                .set(STATUS, status.display)
                .set(UPDATE_TIME, time)
                .where(PRODUCT_CODE.eq(productCode))
                .execute()
        }
        return time
    }

    fun updateProductStatus(
        dslContext: DSLContext,
        productCode: String,
        status: ProductStatus,
        errorMsg: String?
    ): Boolean {
        with(TRdsProductInfo.T_RDS_PRODUCT_INFO) {
            return dslContext.update(this)
                .set(STATUS, status.display)
                .set(NOTES, errorMsg)
                .where(PRODUCT_CODE.eq(productCode))
                .execute() > 0
        }
    }

    fun count(
        dslContext: DSLContext,
        userId: String
    ): Int {
        val userTable = TRdsProductUser.T_RDS_PRODUCT_USER
        val infotable = TRdsProductInfo.T_RDS_PRODUCT_INFO

        return dslContext.selectCount()
            .from(userTable.leftJoin(infotable).on(userTable.PRODUCT_CODE.eq(infotable.PRODUCT_CODE)))
            .where(userTable.USER_ID.eq(userId)).and(infotable.PRODUCT_CODE.isNotNull)
            .fetchOne(0, Int::class.java)!!
    }

    fun listStatus(
        dslContext: DSLContext,
        userId: String,
        offset: Int,
        limit: Int
    ): List<RdsProductStatus> {
        val userTable = TRdsProductUser.T_RDS_PRODUCT_USER
        val infotable = TRdsProductInfo.T_RDS_PRODUCT_INFO

        val result = mutableListOf<RdsProductStatus>()

        val records = dslContext.select(
            infotable.PRODUCT_CODE,
            infotable.DISPLAY_NAME,
            infotable.CHART_NAME,
            infotable.CHART_VERSION,
            infotable.REVISION,
            infotable.STATUS,
            infotable.NOTES,
            infotable.UPDATE_TIME
        )
            .from(userTable.leftJoin(infotable).on(userTable.PRODUCT_CODE.eq(infotable.PRODUCT_CODE)))
            .where(userTable.USER_ID.eq(userId))
            .offset(offset)
            .limit(limit)
            .fetch()

        if (records.isEmpty()) {
            return result
        }

        records.forEach { record ->
            // 可能存在 user表有 list表没有的情况
            val productCode = record.get(infotable.PRODUCT_CODE.name) ?: return@forEach
            result.add(
                RdsProductStatus(
                    productCode = productCode as String,
                    productName = record.get(infotable.PRODUCT_CODE.name) as String,
                    chartName = record.get(infotable.CHART_NAME.name) as String,
                    chartVersion = record.get(infotable.CHART_VERSION.name) as String,
                    revision = record.get(infotable.REVISION.name) as Int,
                    status = ProductStatus.displayOf(record.get(infotable.STATUS.name) as String)!!,
                    notes = record.get(infotable.NOTES.name)?.toString(),
                    updateTime = (record.get(infotable.UPDATE_TIME.name) as LocalDateTime).timestamp()
                )
            )
        }

        return result
    }

    fun status(
        dslContext: DSLContext,
        productCode: String
    ): RdsProductStatus? {
        with(TRdsProductInfo.T_RDS_PRODUCT_INFO) {
            return dslContext.select(
                PRODUCT_CODE,
                DISPLAY_NAME,
                CHART_NAME,
                CHART_VERSION,
                REVISION,
                STATUS,
                NOTES,
                UPDATE_TIME
            ).from(this)
                .where(PRODUCT_CODE.eq(productCode))
                .fetchAny()?.let {
                    RdsProductStatus(
                        productCode = it.get(PRODUCT_CODE.name) as String,
                        productName = it.get(DISPLAY_NAME.name) as String,
                        chartName = it.get(CHART_NAME.name) as String,
                        chartVersion = it.get(CHART_VERSION.name) as String,
                        revision = it.get(REVISION.name) as Int,
                        status = ProductStatus.displayOf(it.get(STATUS.name) as String)!!,
                        notes = it.get(NOTES.name)?.toString(),
                        updateTime = (it.get(UPDATE_TIME.name) as LocalDateTime).timestamp()
                    )
                }
        }
    }

    fun get(
        dslContext: DSLContext,
        productCode: String,
        resourceYaml: Boolean
    ): RdsProductDetail? {
        with(TRdsProductInfo.T_RDS_PRODUCT_INFO) {
            val record = dslContext.selectFrom(this)
                .where(PRODUCT_CODE.eq(productCode))
                .fetchAny() ?: return null

            return RdsProductDetail(
                chartName = record.chartName,
                chartVersion = record.chartVersion,
                resourceYaml = if (!resourceYaml) {
                    null
                } else {
                    record.resourceYaml
                }
            )
        }
    }

    fun getRevision(
        dslContext: DSLContext,
        productCode: String
    ): Int? {
        with(TRdsProductInfo.T_RDS_PRODUCT_INFO) {
            return dslContext.select(REVISION).from(this)
                .where(PRODUCT_CODE.eq(productCode))
                .fetchAny()?.value1()
        }
    }
}
