/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.devops.remotedev.api.remotedev

import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.remotedev.pojo.RemoteDevOauthBack
import com.tencent.devops.remotedev.pojo.WorkspaceProxyDetail
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.HeaderParam
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Api(tags = ["REMOTE_DEV"], description = "remoteDev")
@Path("remotedev/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
interface RemoteDevResource {
    @ApiOperation("获取oauth")
    @GET
    @Path("/oauth")
    fun oauth(
        @ApiParam(value = "secretKey签名(sha256)", required = true)
        @HeaderParam("X-Signature")
        signature: String,
        @ApiParam(value = "public key", required = true)
        @HeaderParam("X-Key")
        key: String,
        @ApiParam(value = "user id", required = true)
        @QueryParam("userId")
        userId: String,
        @ApiParam(value = "workspace name", required = true)
        @QueryParam("workspaceName")
        workspaceName: String,
        @ApiParam(value = "时间戳", required = true)
        @QueryParam("timestamp")
        timestamp: String
    ): Result<RemoteDevOauthBack>

    @ApiOperation("提供给ws-proxy上报工作空间心跳")
    @POST
    @Path("/workspace/heartbeat")
    fun workspaceHeartbeat(
        @ApiParam(value = "secretKey签名(sha256)", required = true)
        @HeaderParam("X-Signature")
        signature: String,
        @ApiParam(value = "工作空间ID", required = true)
        @QueryParam("workspaceName")
        workspaceName: String,
        @ApiParam(value = "时间戳", required = true)
        @QueryParam("timestamp")

        timestamp: String
    ): Result<Boolean>

    @ApiOperation("获取工作空间详情")
    @GET
    @Path("/workspace-proxy/detail")
    fun getWorkspaceDetail(
        @ApiParam(value = "secretKey签名(sha256)", required = true)
        @HeaderParam("X-Signature")
        signature: String,
        @ApiParam(value = "工作空间ID", required = true)
        @QueryParam("workspaceName")
        workspaceName: String,
        @ApiParam(value = "时间戳", required = true)
        @QueryParam("timestamp")
        timestamp: String
    ): Result<WorkspaceProxyDetail>
}