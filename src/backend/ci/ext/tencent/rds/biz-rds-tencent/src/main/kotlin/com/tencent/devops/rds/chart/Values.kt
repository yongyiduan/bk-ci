/*
 *
 *  * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *  *
 *  * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *  *
 *  * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *  *
 *  * A copy of the MIT License is included in this file.
 *  *
 *  *
 *  * Terms of the MIT License:
 *  * ---------------------------------------------------
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 *  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *  * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 *  * the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 *  * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 *  * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.tencent.devops.rds.chart

import com.tencent.devops.rds.common.exception.NotTableException
import com.tencent.devops.rds.common.exception.NotValueException
import com.tencent.devops.rds.common.exception.ValuesException
import com.tencent.devops.rds.utils.Yaml

object Values {

    fun readValues(data: String?): Map<String, Any> {
        if (data.isNullOrBlank()) {
            return mapOf()
        }
        return try {
            Yaml.unMarshal(data)
        } catch (e: Exception) {
            throw ValuesException("解析ValuesYaml失败：${e.message}")
        }
    }

    /**
     * 获取values.yaml 转换为的values中的某个具体的值
     * 如：test:
     *       name: xxx
     * 使用 name 获取到的为 xxx
     */
    fun pathValue(values: Map<String, Any>, path: String): Any {
        if (path.isBlank()) {
            throw ValuesException("Values的Yaml路径不能为空")
        }

        val paths = parsePath(path)
        // 如果只有一项那肯定是顶级key，直接读取
        if (paths.size == 1) {
            val value = values[paths.first()]
            if (value != null && !isTable(value)) {
                return value
            }
            throw NotValueException(paths.first())
        }

        // 不止一项则读取最后的那个key，然后找到最后一个table去读取key
        val valuePath = paths.last()
        val table = try {
            table(values, joinPath(paths.dropLast(1)))
        } catch (ignore: Exception) {
            throw NotValueException(valuePath)
        }

        val value = table[valuePath]
        if (value != null && !isTable(value)) {
            return value
        }
        throw NotValueException(valuePath)
    }

    /**
     * 获取values.yaml 转换为的values中的某一个片段组
     * 如：test:
     *       name: xxx
     * 使用 test 获取到的为 name: xxx
     * @param values values为values.yaml中的某个片段组
     * @param name: 需要寻找的片段组的名字
     */
    fun table(values: Map<String, Any>, name: String): Map<String, Any> {
        var table = values
        parsePath(name).forEach { subPath ->
            table = tableLookUp(table, subPath)
        }
        return table
    }

    private fun tableLookUp(values: Map<String, Any>, subPath: String): Map<String, Any> {
        val newTable = values[subPath].let {
            if (it == null) {
                throw NotTableException(subPath)
            }
        }

        return try {
            newTable as Map<String, Any>
        } catch (ignore: Exception) {
            throw NotTableException(subPath)
        }
    }

    private fun isTable(value: Any) = value is Map<*, *>

    private fun parsePath(path: String) = path.split(".")

    private fun joinPath(path: List<String>) = path.joinToString(".")
}
