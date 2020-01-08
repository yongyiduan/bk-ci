package com.tencent.devops.store.pojo

import io.swagger.annotations.ApiModelProperty

data class ExtServiceFeatureCreateInfo(
    @ApiModelProperty("扩展服务code")
    val serviceCode: String,
    @ApiModelProperty("是否为公共扩展服务， TRUE：是 FALSE：不是  ")
    val publicFlag: Boolean? = false,
    @ApiModelProperty("是否推荐， TRUE：是 FALSE：不是 ")
    val recommentFlag: Boolean? = false,
    @ApiModelProperty("是否官方认证， TRUE：是 FALSE：不是  ")
    val certificationFlag: Boolean? = false,
    @ApiModelProperty("权重（数值越大代表权重越高）")
    val weight: Int? = 0,
    @ApiModelProperty("扩展服务可见范围 0：私有 10：登录用户开源")
    val visibilityLevel: Int? = 0,
    @ApiModelProperty("代码库hashId")
    val repositoryHashId: String? = "",
    @ApiModelProperty("代码库地址")
    val codeSrc: String? = "",
    @ApiModelProperty("删除标签")
    val deleteFlag: Boolean? = false,
    @ApiModelProperty("添加用户")
    val creatorUser: String,
    @ApiModelProperty("修改用户")
    val modifierUser: String
)