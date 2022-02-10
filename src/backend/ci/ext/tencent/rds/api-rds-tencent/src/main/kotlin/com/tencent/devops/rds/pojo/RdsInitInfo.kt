package com.tencent.devops.rds.pojo

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("rds项目的初始化信息")
data class RdsInitInfo(
    @ApiModelProperty("资源yaml")
    val resourceYaml: String
)
