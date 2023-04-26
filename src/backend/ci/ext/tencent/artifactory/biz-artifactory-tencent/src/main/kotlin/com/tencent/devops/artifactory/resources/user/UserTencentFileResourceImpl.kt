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

package com.tencent.devops.artifactory.resources.user

import com.tencent.bkrepo.common.artifact.path.PathUtils
import com.tencent.devops.artifactory.api.user.UserTencentFileResource
import com.tencent.devops.artifactory.pojo.CopyFileRequest
import com.tencent.devops.artifactory.service.bkrepo.BkRepoCustomDirService
import com.tencent.devops.artifactory.service.bkrepo.BkRepoService
import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.api.exception.ParamBlankException
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.archive.client.BkRepoClient
import com.tencent.devops.common.web.RestResource
import org.glassfish.jersey.media.multipart.FormDataContentDisposition
import org.springframework.beans.factory.annotation.Autowired
import java.io.InputStream

@RestResource
class UserTencentFileResourceImpl @Autowired constructor(
    val bkRepoCustomDirService: BkRepoCustomDirService,
    val bkRepoService: BkRepoService
) : UserTencentFileResource {

    override fun uploadToPath(
        userId: String,
        projectId: String,
        path: String,
        inputStream: InputStream,
        disposition: FormDataContentDisposition
    ): Result<Boolean> {
        checkParam(userId, projectId, path)
        bkRepoCustomDirService.deploy(userId, projectId, path, inputStream, disposition, 10)
        return Result(true)
    }

    override fun copy(userId: String, copyFileRequest: CopyFileRequest) {
        with(copyFileRequest) {
            val filename = PathUtils.resolveName(srcFileFullPath)
            val dstFullPath = PathUtils.combineFullPath(dstDirFullPath, filename)
            bkRepoService.copyFile(
                userId = userId,
                srcProjectId = projectId,
                srcArtifactoryType = srcArtifactoryType,
                srcFullPath = srcFileFullPath,
                dstProjectId = projectId,
                dstArtifactoryType = dstArtifactoryType,
                dstFullPath = dstFullPath
            )
        }
    }

    private fun checkParam(userId: String, projectId: String, path: String) {
        if (userId.isBlank()) {
            throw ParamBlankException("Invalid userId")
        }
        if (projectId.isBlank()) {
            throw ParamBlankException("Invalid projectId")
        }
        if (path.isBlank()) {
            throw ErrorCodeException(errorCode = BkRepoClient.INVALID_CUSTOM_ARTIFACTORY_PATH)
        }
    }
}
