package com.tencent.devops.trigger.source.tapd.api.model

data class TapdResult<out T>(
    val status: Int,
    val info: String? = null,
    val data: T? = null
)
