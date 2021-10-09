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

package com.tencent.devops.image.pojo

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("镜像信息模型")
data class DockerRepo(
    @ApiModelProperty("仓库url")
    var repoUrl: String? = null,
    @ApiModelProperty("仓库")
    var repo: String? = null,
    @ApiModelProperty("类型")
    var type: String? = null,
    @ApiModelProperty("仓库类型")
    var repoType: String? = "",
    @ApiModelProperty("名称")
    var name: String? = null,
    @ApiModelProperty("创建者")
    var createdBy: String? = null,
    @ApiModelProperty("")
    var created: String? = null,
    @ApiModelProperty("")
    var modified: String? = null,
    @ApiModelProperty("修改者")
    var modifiedBy: String? = null,
    @ApiModelProperty("镜像路径")
    var imagePath: String? = null,
    @ApiModelProperty("描述")
    var desc: String? = "",
    @ApiModelProperty("标签")
    var tags: List<DockerTag>? = null,
    @ApiModelProperty("标签数量")
    var tagCount: Int? = null,
    @ApiModelProperty("开始索引")
    var tagStart: Int? = null,
    @ApiModelProperty("页大小")
    var tagLimit: Int? = null,
    @ApiModelProperty("下载次数")
    var downloadCount: Int? = null
)
