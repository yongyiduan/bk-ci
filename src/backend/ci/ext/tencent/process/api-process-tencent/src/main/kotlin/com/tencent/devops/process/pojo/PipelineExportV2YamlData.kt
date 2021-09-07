package com.tencent.devops.process.pojo

/**
 * 目前仅为导出Yaml函数使用
 * 封装原本的model对象，在Yaml中为其添加相应位置的注释
 */
data class PipelineExportV2YamlData(
    val yamlStr: String,
    val conflictMap: Map<String, List<PipelineExportV2YamlConflictMapItem>>
)

data class PipelineExportV2YamlConflictMapItem(
    val conflictForStage: PipelineExportV2YamlConflictMapBaseItem? = null,
    var conflictForJob: PipelineExportV2YamlConflictMapBaseItem? = null,
    var conflictForStep: PipelineExportV2YamlConflictMapBaseItem? = null
)

data class PipelineExportV2YamlConflictMapBaseItem(
    val id: String?,
    val name: String?
)
