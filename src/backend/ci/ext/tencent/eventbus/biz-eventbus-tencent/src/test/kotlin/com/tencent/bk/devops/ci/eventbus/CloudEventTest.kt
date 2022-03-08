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

package com.tencent.bk.devops.ci.eventbus

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.tencent.devops.common.api.util.JsonUtil
import io.appform.jsonrules.ExpressionEvaluationContext
import io.appform.jsonrules.expressions.string.EmptyExpression
import io.cloudevents.core.builder.CloudEventBuilder
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.jackson.JsonFormat
import org.junit.Test
import java.net.URI
import java.nio.charset.StandardCharsets


class CloudEventTest {

    private val mapper = ObjectMapper()

    @Test
    fun cloudEventToJson() {
        val content: MutableMap<String, String> = HashMap()
        content["content"] = "testAsyncMessage"
        val cloudEvent = CloudEventBuilder.v1()
            .withId("000")
            .withType("example.demo")
            .withSource(URI.create("http://example.com"))
            .withData("application/json", JsonUtil.toJson(content).toByteArray(StandardCharsets.UTF_8))
            .build()
        val bodyByte = EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE)!!
            .serialize(cloudEvent)

        val node = mapper.readValue<JsonNode>(bodyByte, JsonNode::class.java)
        val context = ExpressionEvaluationContext.builder().node(node).build()

        val isMatch = EmptyExpression.builder()
            .path("$.data.content")
            .defaultResult(true)
            .build()
            .evaluate(context)
        println(isMatch )
    }
}
