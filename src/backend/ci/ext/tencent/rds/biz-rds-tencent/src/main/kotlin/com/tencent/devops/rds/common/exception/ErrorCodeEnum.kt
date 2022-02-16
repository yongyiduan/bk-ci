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

package com.tencent.devops.rds.common.exception

import com.tencent.devops.common.api.pojo.ErrorType

enum class ErrorCodeEnum(
    val errorType: ErrorType,
    val errorCode: Int,
    val formatErrorMessage: String
) {
    GET_TOKEN_ERROR(ErrorType.THIRD_PARTY, 2130001, "获取工蜂项目的TOKEN %s"),

    DEVNET_TIMEOUT_ERROR(ErrorType.THIRD_PARTY, 2130002, "请求 DEVNET 环境网关超时"),

    GET_GIT_FILE_TREE_ERROR(ErrorType.THIRD_PARTY, 2130003, "获取仓库CI文件列表失败 %s"),

    GET_YAML_CONTENT_ERROR(
        errorType = ErrorType.THIRD_PARTY,
        errorCode = 2130004,
        formatErrorMessage = "获取工蜂仓库文件内容失败 %s"
    ),

    GET_PROJECT_INFO_ERROR(
        errorType = ErrorType.THIRD_PARTY,
        errorCode = 2130005,
        formatErrorMessage = "获取工蜂仓库信息失败 %s"
    ),
}
