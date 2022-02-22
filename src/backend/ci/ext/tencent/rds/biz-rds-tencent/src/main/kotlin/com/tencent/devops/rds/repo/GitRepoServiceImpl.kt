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

package com.tencent.devops.rds.repo

import com.tencent.devops.common.api.exception.ClientException
import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.api.exception.RemoteServiceException
import com.tencent.devops.common.client.Client
import com.tencent.devops.rds.common.exception.ApiErrorCodeEnum
import com.tencent.devops.rds.pojo.repo.RepoFile
import com.tencent.devops.rds.pojo.repo.RepoFileTreeInfo
import com.tencent.devops.rds.pojo.repo.RepoInfo
import com.tencent.devops.rds.utils.RetryUtils
import com.tencent.devops.scm.api.ServiceGitCiResource
import com.tencent.devops.scm.api.ServiceGitResource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GitRepoServiceImpl @Autowired constructor(
    private val client: Client
) : RepoService {

    companion object {
        private val logger = LoggerFactory.getLogger(GitRepoServiceImpl::class.java)

        // 保存公共chart仓库的默认分支，后续修改多可以放到配置
        private const val CHART_REPO_BRANCH = "master"
    }

    override fun getRepoInfo(repoName: String): RepoInfo {
        // TODO: 目前的demo版只有一个仓库，所以替换为固定的git仓库ID，后续看设计如何获取仓库ID
        val gitProjectID = 821711
        return RepoInfo(repoId = gitProjectID.toString())
    }

    override fun getRepoToken(repoId: String): String {
        return retryFun(
            log = "$repoId get token fail",
            apiErrorCode = ApiErrorCodeEnum.GET_TOKEN_ERROR,
            action = {
                client.getScm(ServiceGitCiResource::class).getToken(repoId).data!!.accessToken
            }
        )
    }

    override fun getFileTree(
        repoId: String,
        path: String?,
        ref: String?,
        token: String?
    ): List<RepoFileTreeInfo> {
        val gitProjectId = repoId.toLong()
        return retryFun(
            log = "$gitProjectId get $path file tree error",
            apiErrorCode = ApiErrorCodeEnum.GET_GIT_FILE_TREE_ERROR,
            action = {
                client.getScm(ServiceGitResource::class).getGitCIFileTree(
                    gitProjectId = gitProjectId,
                    path = path,
                    token = token!!,
                    ref = ref ?: CHART_REPO_BRANCH,
                    recursive = null
                ).data?.map { RepoFileTreeInfo(fileName = it.name) } ?: emptyList()
            }
        )
    }

    override fun getFile(
        repoId: String,
        filePath: String,
        ref: String?,
        token: String?
    ): RepoFile {
        return retryFun(
            log = "$repoId get yaml $filePath fail",
            apiErrorCode = ApiErrorCodeEnum.GET_YAML_CONTENT_ERROR,
            action = {
                val content = client.getScm(ServiceGitCiResource::class).getGitCIFileContent(
                    gitProjectId = repoId,
                    filePath = filePath,
                    token = token!!,
                    ref = ref ?: CHART_REPO_BRANCH,
                    useAccessToken = true
                ).data!!
                RepoFile(content = content)
            }
        )
    }

    private fun <T> retryFun(log: String, apiErrorCode: ApiErrorCodeEnum, action: () -> T): T {
        try {
            return RetryUtils.clientRetry {
                action()
            }
        } catch (e: ClientException) {
            logger.warn("retry 5 times $log: ${e.message} ")
            throw ErrorCodeException(
                errorCode = ApiErrorCodeEnum.DEVNET_TIMEOUT_ERROR.errorCode.toString(),
                defaultMessage = ApiErrorCodeEnum.DEVNET_TIMEOUT_ERROR.formatErrorMessage
            )
        } catch (e: RemoteServiceException) {
            logger.warn("GIT_API_ERROR $log: ${e.message} ")
            throw ErrorCodeException(
                statusCode = e.httpStatus,
                errorCode = apiErrorCode.errorCode.toString(),
                defaultMessage = "$log: ${e.errorMessage}"
            )
        } catch (e: Throwable) {
            logger.error("retryFun error $log: ${e.message} ")
            throw ErrorCodeException(
                errorCode = apiErrorCode.errorCode.toString(),
                defaultMessage = if (e.message.isNullOrBlank()) {
                    "$log: ${apiErrorCode.formatErrorMessage}"
                } else {
                    "$log: ${e.message}"
                }
            )
        }
    }
}
