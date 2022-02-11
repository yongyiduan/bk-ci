package com.tencent.devops.rds.pojo

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("rds项目的初始化信息")
data class RdsInitInfo(
    @ApiModelProperty("rds的chart")
    val rdsChartName: String,
    @ApiModelProperty("资源Yaml")
    val resourceYaml: String,
    @ApiModelProperty("设定值Yaml")
    val valuesYaml: String?
)
