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
import com.tencent.devops.common.api.pojo.Page
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.rds.api.user.UserRdsProductResource
import com.tencent.devops.rds.constants.Constants
import com.tencent.devops.rds.exception.CommonErrorCodeEnum
import com.tencent.devops.rds.pojo.ProductDetailReq
import com.tencent.devops.rds.pojo.ProductDetailResp
import com.tencent.devops.rds.pojo.ProductListReq
import com.tencent.devops.rds.pojo.RdsProductStatusResult
import com.tencent.devops.rds.service.ProductInfoService
import com.tencent.devops.rds.service.ProductInitService
import org.glassfish.jersey.media.multipart.FormDataContentDisposition
import org.springframework.beans.factory.annotation.Autowired
import java.io.InputStream
import java.nio.charset.Charset

@RestResource
class UserRdsProductResourceImpl @Autowired constructor(
    private val productInitService: ProductInitService,
    private val productInfoService: ProductInfoService
) : UserRdsProductResource {
    override fun init(
        userId: String,
        chartName: String,
        inputStream: InputStream,
        disposition: FormDataContentDisposition
    ): Result<RdsProductStatusResult> {
        // 校验文件 TODO: 增加接收到的文件大小的校验
        val fileName = String(
            disposition.fileName.toByteArray(
                Charset.forName("ISO8859-1")
            ),
            Charset.forName("UTF-8")
        )
        if (!fileName.endsWith(Constants.CHART_PACKAGE_FORMAT)) {
            throw ErrorCodeException(
                errorCode = CommonErrorCodeEnum.PARAMS_FORMAT_ERROR.errorCode,
                defaultMessage = CommonErrorCodeEnum.PARAMS_FORMAT_ERROR.formatErrorMessage,
                params = arrayOf("文件类型错误")
            )
        }

        return Result(productInitService.initProduct(userId, chartName, inputStream))
    }

    override fun upgrade(
        userId: String,
        chartName: String,
        inputStream: InputStream,
        disposition: FormDataContentDisposition
    ): Result<RdsProductStatusResult> {
        // TODO: 目前为止 upgrade 和 init 的功能完全相同只是产品设计区分，所以代码保持完全一致即可
        // 校验文件 TODO: 增加接收到的文件大小的校验
        val fileName = String(
            disposition.fileName.toByteArray(
                Charset.forName("ISO8859-1")
            ),
            Charset.forName("UTF-8")
        )
        if (!fileName.endsWith(Constants.CHART_PACKAGE_FORMAT)) {
            throw ErrorCodeException(
                errorCode = CommonErrorCodeEnum.PARAMS_FORMAT_ERROR.errorCode,
                defaultMessage = CommonErrorCodeEnum.PARAMS_FORMAT_ERROR.formatErrorMessage,
                params = arrayOf("文件类型错误")
            )
        }

        return Result(productInitService.upgradeProduct(userId, chartName, inputStream))
    }

    override fun list(userId: String, req: ProductListReq): Result<Page<RdsProductStatusResult>> {
        return Result(productInfoService.listStatus(userId, req))
    }

    override fun get(userId: String, productCode: String, detail: ProductDetailReq): Result<ProductDetailResp> {
        return Result(productInfoService.detail(productCode, detail))
    }

    override fun status(userId: String, productCode: String): Result<RdsProductStatusResult?> {
        return Result(productInfoService.status(productCode))
    }
}
