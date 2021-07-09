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

package com.tencent.devops.common.api.util

import com.fasterxml.jackson.core.type.TypeReference

object ObjectReplaceEnvVarUtil {

    /**
     * 探测[obj]对象类型，并递归解析其中包含有模板占位符字符串 ${xx} 和 ${{ xxx }} ，使用envMap 替换出真正的值
     */
    fun replaceEnvVar(obj: Any?, envMap: Map<String, String>): Any {
        return when {
            obj is Map<*, *> -> {
                val result = mutableMapOf<String, Any>()
                // 递归替换map对象中的变量
                obj.entries.forEach { entry ->
                    val value = entry.value
                    result[entry.key.toString()] =
                        if (!isNormalReplaceEnvVar(value)) {
                            replaceEnvVar(value, envMap)
                        } else {
                            handleNormalEnvVar(value, envMap)
                        }
                }
                result
            }
            obj is List<*> -> {
                val result = mutableListOf<Any>()
                // 递归替换list对象中的变量
                obj.forEach { value ->
                    if (!isNormalReplaceEnvVar(value)) {
                        result.add(replaceEnvVar(value, envMap))
                    } else {
                        result.add(handleNormalEnvVar(value, envMap))
                    }
                }
                result
            }
            obj is Set<*> -> {
                // 递归替换set对象中的变量
                val result = mutableSetOf<Any>()
                obj.forEach { value ->
                    if (!isNormalReplaceEnvVar(value)) {
                        result.add(replaceEnvVar(value, envMap))
                    } else {
                        result.add(handleNormalEnvVar(value, envMap))
                    }
                }
                result
            }
            isNormalReplaceEnvVar(obj) -> {
                // 替换基本类型对象或字符串对象中的变量
                handleNormalEnvVar(obj, envMap)
            }
            else -> {
                try {
                    // 把对象转换成map后进行递归替换变量
                    val dataMap = replaceEnvVar(JsonUtil.toMap(obj!!), envMap)
                    JsonUtil.to(JsonUtil.toJson(dataMap), obj.javaClass)
                } catch (ignored: Exception) {
                    // 转换不了map的对象则直接替换
                    EnvUtils.parseEnv(JsonUtil.toJson(obj!!), envMap)
                }
            }
        }
    }

    private fun handleNormalEnvVar(obj: Any?, envMap: Map<String, String>): Any {
        // 只有字符串参数才需要进行变量替换，其它基本类型参数无需进行变量替换
        if (obj is String) {
            val objStr = obj.trim()
            return if (objStr.startsWith("{") && objStr.endsWith("}") && JsonSchemaUtil.validateJson(objStr)) {
                try {
                    val dataObj = JsonUtil.toMap(objStr)
                    // string能正常转成map，说明是json串，把dataObj进行递归替换变量后再转成json串
                    JsonUtil.toJson(replaceEnvVar(dataObj, envMap))
                } catch (ignore: Exception) {
                    // 转换不了map的字符串对象则直接替换
                    EnvUtils.parseEnv(JsonUtil.toJson(obj), envMap)
                }
            } else if (objStr.startsWith("[") && objStr.endsWith("]") && JsonSchemaUtil.validateJson(objStr)) {
                try {
                    val dataObj = JsonUtil.to(objStr, object : TypeReference<List<*>>() {})
                    // string能正常转成list，说明是json串，把dataObj进行递归替换变量后再转成json串
                    JsonUtil.toJson(replaceEnvVar(dataObj, envMap))
                } catch (ignore: Exception) {
                    // 转换不了list的字符串对象则直接替换
                    EnvUtils.parseEnv(JsonUtil.toJson(obj), envMap)
                }
            } else {
                // 转换不了map或者list的字符串对象则直接替换
                EnvUtils.parseEnv(JsonUtil.toJson(obj), envMap)
            }
        }
        return obj!!
    }

    private fun isNormalReplaceEnvVar(obj: Any?): Boolean {
        return obj == null || ReflectUtil.isNativeType(obj) || obj is String
    }
}
