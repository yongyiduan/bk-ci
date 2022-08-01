package com.tencent.devops.repository.resources.github

import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.sdk.github.request.CreateCheckRunRequest
import com.tencent.devops.common.sdk.github.request.UpdateCheckRunRequest
import com.tencent.devops.common.sdk.github.response.CheckRunResponse
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.repository.api.github.ServiceGithubCheckResource
import com.tencent.devops.repository.github.service.GithubCheckService
import com.tencent.devops.repository.service.github.GithubTokenService
import org.springframework.beans.factory.annotation.Autowired

@RestResource
class ServiceGithubCheckResourceImpl @Autowired constructor(
    val githubTokenService: GithubTokenService,
    val githubCheckService: GithubCheckService
) : ServiceGithubCheckResource {

    override fun createCheckRun(userId: String, request: CreateCheckRunRequest): Result<CheckRunResponse> {
        return Result(
            githubCheckService.createCheckRun(
                request = request,
                token = githubTokenService.getAccessTokenMustExist(userId).accessToken
            )
        )
    }

    override fun createCheckRunByToken(token: String, request: CreateCheckRunRequest): Result<CheckRunResponse> {
        return Result(
            githubCheckService.createCheckRun(
                request = request,
                token = token
            )
        )
    }

    override fun updateCheckRun(userId: String, request: UpdateCheckRunRequest): Result<CheckRunResponse> {
        return Result(
            githubCheckService.updateCheckRun(
                request = request,
                token = githubTokenService.getAccessTokenMustExist(userId).accessToken
            )
        )
    }
}