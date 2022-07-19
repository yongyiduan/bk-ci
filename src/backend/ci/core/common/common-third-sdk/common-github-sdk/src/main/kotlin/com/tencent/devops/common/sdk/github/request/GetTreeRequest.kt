package com.tencent.devops.common.sdk.github.request

import com.tencent.devops.common.sdk.enums.HttpMethod
import com.tencent.devops.common.sdk.github.GithubRequest
import com.tencent.devops.common.sdk.github.response.GetTreeResponse

data class GetTreeRequest(
    // val owner: String,
    // val repo: String,
    val id: Long,
    val treeSha: String,
    val recursive: String? = null
) : GithubRequest<GetTreeResponse>() {

    override fun getHttpMethod() = HttpMethod.GET

    override fun getApiPath() = "repositories/$id/git/trees/$treeSha"
}