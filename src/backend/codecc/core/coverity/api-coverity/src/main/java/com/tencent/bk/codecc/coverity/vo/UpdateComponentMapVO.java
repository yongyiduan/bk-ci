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

<<<<<<< HEAD:src/backend/ci/ext/blueking/common/common-auth/common-auth-blueking/src/main/kotlin/com/tencent/devops/common/auth/api/pojo/BkDeleteResourceAuthResponse.kt
package com.tencent.devops.common.auth.api.pojo

import com.fasterxml.jackson.annotation.JsonProperty
import com.tencent.devops.common.api.util.JsonUtil

data class BkDeleteResourceAuthResponse(
    val code: Int, // 0
    val data: Data?,
    val message: String?,
    @JsonProperty("request_id")
    val requestId: String?, // 32655832876747a5b5edc55d83ac6fdf
    val result: Boolean // true
) {
    data class Data(
        @get:JsonProperty("is_deleted")
        val deleted: Boolean? // true
    )
}

fun main(array: Array<String>) {
    val d = BkDeleteResourceAuthResponse.Data(true)
    println(JsonUtil.toJson(d))
}
=======
package com.tencent.bk.codecc.coverity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新coverity platform ComponentMap的请求体
 *
 * @version V1.0
 * @date 2019/4/26
 */
@Data
@ApiModel("更新coverity platform ComponentMap的请求体")
public class UpdateComponentMapVO
{
    @NotNull(message = "流名称不能为空")
    @ApiModelProperty(value = "流名称", required = true)
    private String streamName;

    @NotNull(message = "platformIp不能为空")
    @ApiModelProperty(value = "接口传入的platform ip")
    private String platformIp;

    @ApiModelProperty(value = "屏蔽路径列表")
    private List<String> pathList;
}
>>>>>>> github/master:src/backend/codecc/core/coverity/api-coverity/src/main/java/com/tencent/bk/codecc/coverity/vo/UpdateComponentMapVO.java
