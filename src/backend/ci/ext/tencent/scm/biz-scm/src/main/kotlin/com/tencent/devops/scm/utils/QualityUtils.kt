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

package com.tencent.devops.scm.utils

object QualityUtils {
    fun getQualityReport(titleData: List<String>, resultData: MutableMap<String, MutableList<List<String>>>): String {
        val sb = StringBuilder()
        val (status, timeCost, triggerType, pipelineName, url) = titleData
        val pipelineNameTitle = if (titleData.size >= 6) {
            titleData[5]
        } else {
            "蓝盾流水线"
        }
        sb.append("$pipelineNameTitle：[$pipelineName]($url) \n")

        val statusLine = if (status == "SUCCEED") {
            "<font color=#4CAF50>执行成功</font>"
        } else {
            "<font color=red>执行成功</font>"
        }
        sb.append("状态：$statusLine \n")

        sb.append("触发方式：$triggerType \n")

        sb.append("任务耗时：$timeCost \n")

        sb.append("| 质量红线产出插件 | 指标 | 结果 | 预期 |  | \n")
        sb.append("| :----:| :----: | :----: | :----: | :----: | \n")
        resultData.forEach { (elementName, result) ->
            result.forEachIndexed { index, list ->
                val prefix = if (index == 0) {
                    "| $elementName "
                } else {
                    "| "
                }
                val (target, outcome, expected, state) = list
                val pattern = if (state == "true") {
                    "<font color=#4CAF50> &nbsp; &radic; &nbsp; </font>"
                } else {
                    "<font color=#F44336> &nbsp; &times; &nbsp; </font>"
                }
                sb.append("$prefix | $target | $outcome | $expected | $pattern | \n")
            }
        }
        return sb.toString()
    }
}
