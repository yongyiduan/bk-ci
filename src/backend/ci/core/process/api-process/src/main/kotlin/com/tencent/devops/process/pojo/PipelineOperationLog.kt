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

package com.tencent.devops.process.pojo

import com.tencent.devops.common.pipeline.enums.VersionStatus
import com.tencent.devops.process.enums.OperationLogType
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("流水线操作日志")
data class PipelineOperationLog(
    @ApiModelProperty("唯一标识ID", required = true)
    val id: Int,
    @ApiModelProperty("项目ID", required = true)
    val projectId: String,
    @ApiModelProperty("流水线ID", required = true)
    val pipelineId: String,
    @ApiModelProperty("版本ID", required = true)
    val version: Int,
    @ApiModelProperty("操作用户", required = true)
    val operator: String,
    @ApiModelProperty("操作类型", required = true)
    val operationLogType: OperationLogType,
    @ApiModelProperty("操作参数", required = true)
    val params: String,
    @ApiModelProperty("操作时间", required = false)
    val operateTime: Long,
    @ApiModelProperty("操作内容", required = false)
    val description: String?,
    @ApiModelProperty("版本名称", required = false)
    val versionName: String?,
    @ApiModelProperty("版本创建时间", required = false)
    val versionCreateTime: Long?,
    @ApiModelProperty("草稿版本标识", required = false)
    val status: VersionStatus? = null,
    @ApiModelProperty("来源代码库标识（分支名）", required = false)
    val pacRefs: String? = null
)
