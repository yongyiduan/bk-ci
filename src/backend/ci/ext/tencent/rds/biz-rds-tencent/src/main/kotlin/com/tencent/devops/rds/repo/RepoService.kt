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

import com.tencent.devops.rds.pojo.RdsRepoFile
import com.tencent.devops.rds.pojo.RdsRepoInfo

/**
 * 操作RDS的chart仓库的接口，具体实现可能为GIT或者BK_REPO或者MYSQL
 */
interface RepoService {

    /**
     * 根据用户的别名获取仓库的信息
     * @param repoName 仓库别名
     */
    fun getRepoInfo(
        repoName: String
    ): RdsRepoInfo

    /**
     * 获取访问仓库需要的凭证，目前只有工蜂的token，看后续需要是否可以干掉
     * @param repoId 具体的仓库ID
     */
    fun getRepoToken(
        repoId: String
    ): String

    /**
     * 获取仓库单个具体文件内容
     * @param repoId 具体的仓库ID
     * @param filePath 当前需要获取的文件路径
     * @param ref 附加参数，在git中可以为分支，commit
     */
    fun getFile(
        repoId: String,
        filePath: String,
        ref: String? = null,
        token: String? = null
    ): RdsRepoFile
}
