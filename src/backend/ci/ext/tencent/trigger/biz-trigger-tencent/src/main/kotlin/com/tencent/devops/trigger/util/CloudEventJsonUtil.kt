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

package com.tencent.devops.trigger.util

import com.fasterxml.jackson.databind.JsonNode
import com.tencent.devops.common.api.util.JsonUtil
import io.cloudevents.CloudEvent
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.jackson.JsonFormat
import java.nio.charset.StandardCharsets

object CloudEventJsonUtil {

    private val eventFormat = EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE)!!
    private val mapper = JsonUtil.getObjectMapper()

    fun deserialize(json: String): CloudEvent {
        return eventFormat.deserialize(json.toByteArray(StandardCharsets.UTF_8))
    }

    fun serialize(event: CloudEvent): ByteArray {
        return eventFormat.serialize(event)
    }

    fun serializeAsString(event: CloudEvent): String {
        return String(eventFormat.serialize(event), StandardCharsets.UTF_8)
    }

    fun serializeAsJsonNode(event: CloudEvent): JsonNode {
        return mapper.readValue(eventFormat.serialize(event), JsonNode::class.java)
    }
}
