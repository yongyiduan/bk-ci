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

package com.tencent.devops.trigger.param

import com.fasterxml.jackson.databind.JsonNode
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.trigger.constant.TargetFormType
import com.tencent.devops.trigger.pojo.TargetParam
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.springframework.core.io.ClassPathResource
import java.nio.charset.Charset

class TargetParamConverterTest {
    private var node: JsonNode? = null

    @Before
    fun setUp() {
        val classPathResource = ClassPathResource("TargetParamConvert.json")
        node = JsonUtil.getObjectMapper()
            .readTree(classPathResource.inputStream.readBytes().toString(Charset.defaultCharset()))
    }

    @Test
    fun originTest() {
        val targetParam = TargetParam(
            resourceKey = "body",
            form = TargetFormType.ORIGINAL
        )
        Assert.assertEquals(
            OriginalTargetParamConverter().convert(node = node!!, targetParam),
            Pair(targetParam.resourceKey, node.toString())
        )
    }

    @Test
    fun jsonPathTest() {
        val targetParam = TargetParam(
            resourceKey = "branch",
            form = TargetFormType.JSON_PATH,
            value = "$.data.ref"
        )
        Assert.assertEquals(
            JsonPathTargetParamConverter().convert(node = node!!, targetParam),
            Pair(targetParam.resourceKey, "refs/heads/merge_base_test")
        )
    }

    @Test
    fun templateTest() {
        val value = mapOf(
            "branch" to "$.data.ref",
            "repoUrl" to "$.data.repository.git_http_url"
        )
        val template = mapOf(
            "BK_CI_BRANCH" to "\${branch}",
            "BK_CI_REPO_URL" to "\${repoUrl}"
        )
        val targetParam = TargetParam(
            resourceKey = "params",
            form = TargetFormType.TEMPLATE,
            value = value,
            template = template
        )
        Assert.assertEquals(
            TemplateTargetParamConverter().convert(node!!, targetParam),
            Pair(targetParam.resourceKey, mapOf(
                "BK_CI_BRANCH" to "refs/heads/merge_base_test",
                "BK_CI_REPO_URL" to "http://git.code.tencent.com/mingshewhe/webhook_test.git"
            ))
        )
    }
}
