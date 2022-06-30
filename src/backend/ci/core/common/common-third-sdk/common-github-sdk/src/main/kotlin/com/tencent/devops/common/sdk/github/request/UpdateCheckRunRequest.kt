package com.tencent.devops.common.sdk.github.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.tencent.devops.common.sdk.enums.HttpMethod
import com.tencent.devops.common.sdk.github.GithubRequest
import com.tencent.devops.common.sdk.github.pojo.GithubAction
import com.tencent.devops.common.sdk.github.pojo.GithubOutput
import com.tencent.devops.common.sdk.github.response.CheckRunResponse

data class UpdateCheckRunRequest(
    @JsonIgnore
    val owner: String,
    @JsonIgnore
    val repo: String,
    @JsonIgnore
    val checkRunId: String,
    val name: String? = null,
    @JsonProperty("details_url")
    val detailsUrl: String? = null,
    @JsonProperty("external_id")
    val externalId: String? = null,
    val status: String? = null,
    @JsonProperty("started_at")
    val startedAt: String? = null,
    val conclusion: String? = null,
    @JsonProperty("completed_at")
    val completedAt: String? = null,
    val output: GithubOutput? = null,
    val actions: List<GithubAction>? = null
) : GithubRequest<CheckRunResponse>() {
    override fun getHttpMethod() = HttpMethod.PUT

    override fun getApiPath() = "/repos/$owner/$repo/pulls/check-runs/$checkRunId"

}