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

package com.tencent.devops.rds.resources.user

import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.rds.api.user.UserRdsInitResource
import com.tencent.devops.rds.common.exception.CommonErrorCodeEnum
import com.tencent.devops.rds.service.InitService
import java.io.InputStream
import java.nio.charset.Charset
import org.glassfish.jersey.media.multipart.FormDataContentDisposition
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

@RestResource
class UserRdsInitResourceImpl @Autowired constructor(
    private val initService: InitService,

) : UserRdsInitResource {

    companion object {
        private val logger = LoggerFactory.getLogger(UserRdsInitResourceImpl::class.java)
    }

    override fun init(
        userId: String,
        chartName: String,
        inputStream: InputStream,
        disposition: FormDataContentDisposition
    ): Result<Boolean> {
        // 校验文件 TODO: 增加接收到的文件大小的校验
        val fileName = String(disposition.fileName.toByteArray(Charset.forName("ISO8859-1")), Charset.forName("UTF-8"))
        if (!fileName.endsWith(".zip")) {
            throw ErrorCodeException(
                errorCode = CommonErrorCodeEnum.PARAMS_FORMAT_ERROR.errorCode.toString(),
                defaultMessage = CommonErrorCodeEnum.PARAMS_FORMAT_ERROR.formatErrorMessage,
                params = arrayOf("文件类型错误")
            )
        }

        return Result(initService.init(userId, chartName, inputStream))
    }
}
