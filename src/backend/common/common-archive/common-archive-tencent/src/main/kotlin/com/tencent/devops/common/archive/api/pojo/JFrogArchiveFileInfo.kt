package com.tencent.devops.common.archive.api.pojo

class JFrogArchiveFileInfo(
    val children: List<JFrogArchiveFileInfo>?,
    val text: String,
    val repoKey: String,
    val archivePath: String,
    val path: String,
    val type: String,
    val folder: Boolean,
    val tabs: List<JFrogArchiveFileInfoTab>
)