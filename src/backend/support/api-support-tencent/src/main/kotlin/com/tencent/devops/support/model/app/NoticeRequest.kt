package com.tencent.devops.support.model.app

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("公告请求报文体")
data class NoticeRequest(
    @ApiModelProperty("公告标题")
    val noticeTitle: String = "",
    @ApiModelProperty("生效日期")
    val effectDate: Long = 0,
    @ApiModelProperty("失效日期")
    val invalidDate: Long = 0,
    @ApiModelProperty("公告内容")
    val noticeContent: String = "",
    @ApiModelProperty("跳转地址")
    val redirectUrl: String = ""
)