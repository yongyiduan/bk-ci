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

package com.tencent.devops.gitci.pojo.v2.message

import io.swagger.annotations.ApiModelProperty

data class UserMessage(
    @ApiModelProperty("ID")
    val id: Int,
    @ApiModelProperty("用户ID")
    val userId: String,
    @ApiModelProperty("消息类型")
    val messageType: UserMessageType,
    @ApiModelProperty("消息标题")
    val messageTitle: String,
    @ApiModelProperty("消息ID")
    val messageId: String,
    @ApiModelProperty("是否已读")
    val haveRead: Boolean,
    @ApiModelProperty("创建时间")
    val createTime: Long?,
    @ApiModelProperty("修改时间")
    val updateTime: Long?,
    @ApiModelProperty("消息内容")
    val content: List<RequestMessageContent>,
    @ApiModelProperty("消息内容属性")
    val contentAttr: ContentAttr?
)

data class ContentAttr(
    @ApiModelProperty("内容总数")
    val total: Int?,
    @ApiModelProperty("request为触发构建总数")
    val failedNum: Int?
)

data class UserMessageRecord(
    val time: String,
    val records: List<UserMessage>
)
