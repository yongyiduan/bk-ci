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

package com.tencent.devops.sign.dao

import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.model.sign.tables.TSignIpaInfo
import com.tencent.devops.sign.api.pojo.AppexSignInfo
import com.tencent.devops.sign.api.pojo.IpaSignInfo
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class SignIpaInfoDao {

    fun saveSignInfo(
        resignId: String,
        dslContext: DSLContext,
        info: IpaSignInfo
    ) {
        with(TSignIpaInfo.T_SIGN_IPA_INFO) {
            dslContext.insertInto(this,
                RESIGN_ID,
                CERT_ID,
                ARCHIVE_TYPE,
                PROJECT_ID,
                PIPELINE_ID,
                BUILD_ID,
                ARCHIVE_PATH,
                MOBILE_PROVISION_ID,
                UNIVERSAL_LINK,
                REPLACE_BUNDLE,
                APPEX_SIGN_INFO,
                FILENAME,
                FILE_SIZE,
                FILE_MD5,
                USER_ID,
                REQUEST_CONTENT
            ).values(
                resignId,
                info.certId,
                info.archiveType,
                info.projectId,
                info.pipelineId,
                info.buildId,
                info.archivePath,
                info.mobileProvisionId,
                if (info.universalLinks != null) JsonUtil.toJson(info.universalLinks!!) else null,
                info.repalceBundleId,
                if (info.appexSignInfo != null) JsonUtil.toJson(info.appexSignInfo!!) else null,
                info.fileName,
                info.fileSize,
                info.md5,
                info.userId,
                JsonUtil.toJson(info)
            ).execute()
        }
    }

    fun getSignInfo(
        dslContext: DSLContext,
        resignId: String
    ): IpaSignInfo? {
        with(TSignIpaInfo.T_SIGN_IPA_INFO) {
            val record = dslContext.selectFrom(this)
                .where(RESIGN_ID.eq(resignId))
                .fetchOne()
            return if (record == null) {
                null
            } else {
                IpaSignInfo(
                    certId = record.certId,
                    archiveType = record.archiveType,
                    projectId = record.projectId,
                    pipelineId = record.pipelineId,
                    buildId = record.buildId,
                    archivePath = record.archivePath,
                    mobileProvisionId = record.mobileProvisionId,
                    universalLinks = if (record.universalLink != null) JsonUtil.getObjectMapper().readValue(record.universalLink!!, listOf<String>()::class.java) else null,
                    repalceBundleId = record.replaceBundle,
                    appexSignInfo = if (record.appexSignInfo != null) JsonUtil.getObjectMapper().readValue(record.appexSignInfo!!, listOf<AppexSignInfo>()::class.java) else null,
                    fileName = record.filename,
                    fileSize = record.fileSize,
                    md5 = record.fileMd5,
                    userId = record.userId
                )
            }
        }
    }

    fun getSignInfoContent(
        dslContext: DSLContext,
        resignId: String
    ): String? {
        with(TSignIpaInfo.T_SIGN_IPA_INFO) {
            val record = dslContext.selectFrom(this)
                .where(RESIGN_ID.eq(resignId))
                .fetchOne()
            return record?.requestContent
        }
    }

}
