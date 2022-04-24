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

package com.tencent.devops.stream.api.op

import com.tencent.devops.common.api.pojo.Result
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Api(tags = ["OP_STREAM_CHECK"], description = "Stream校验op系统")
@Path("/op/stream/check")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
interface OpStreamCheckResource {

    @ApiOperation("校验并删除在工蜂中不存在的分支")
    @POST
    @Path("/branches")
    fun checkBranches(
        @ApiParam(value = "工蜂项目ID", required = true)
        @QueryParam("gitProjectId")
        gitProjectId: Long?,
        @ApiParam(value = "流水线ID", required = true)
        @QueryParam("pipelineId")
        pipelineId: String?
    ): Result<Boolean>

    @ApiOperation("校验并删除在工蜂中不存在的分支")
    @GET
    @Path("/conflictJobs")
    fun conflictJobs(
        @ApiParam("构建数据失效时间", required = false)
        @QueryParam("buildDays")
        buildDays: Long?
    ): Result<String>

    @ApiOperation("能检索yml内容中包含有某个关键字的yml文件及其负责人")
    @GET
    @Path("/includeKeyJobs")
    fun getTriggerAndYmlFileByYmlContent(
        @ApiParam("最近N天的数据", required = false)
        @QueryParam("buildDays")
        buildDays: Long?,
        @ApiParam("关键词", required = true)
        @QueryParam("key")
        key: String
    ): String
}
