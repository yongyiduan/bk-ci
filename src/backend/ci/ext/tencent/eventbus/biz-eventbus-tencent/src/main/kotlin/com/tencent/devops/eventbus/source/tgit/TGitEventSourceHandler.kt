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

package com.tencent.devops.eventbus.source.tgit

import com.jayway.jsonpath.JsonPath
import com.tencent.devops.eventbus.constant.SourceType
import com.tencent.devops.eventbus.source.IEventSourceHandler
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import org.springframework.stereotype.Component
import java.net.URI
import java.nio.charset.StandardCharsets

@Component(SourceType.TGIT)
class TGitEventSourceHandler : IEventSourceHandler {

    override fun toCloudEvent(headers: Map<String, String>, payload: String): CloudEvent? {
        val eventType = headers["X-Event"]
        val secretToken = headers["X-Token"]
        val traceId = headers["X-TRACE-ID"]

        val (source, type) = when (eventType) {
            "Push Hook" ->
                Pair(URI.create(JsonPath.parse(payload).read("$.repository.homepage")), "push")
            else ->
                return null
        }
        return CloudEventBuilder.v1()
            .withId(traceId)
            .withType(type)
            .withSource(source)
            .withData("application/json",payload.toByteArray(StandardCharsets.UTF_8))
            .build()
    }
}
