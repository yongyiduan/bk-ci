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

package com.tencent.devops.rds.constants

object Constants {
    // 保存chart模板文件的目录
    const val CHART_TEMPLATE_DIR = "templates"
    // 指定的main入口文件
    const val CHART_MAIN_YAML_FILE = "main.yaml"
    // 指定的resource声明文件
    const val CHART_RESOURCE_YAML_FILE = "resource.yaml"
    // 用户自己的配置文件
    const val CHART_CLIENT_CONFIG_YAML_FILE = "client_config.yaml"
    // rds会保存的产品负责人的GIT TOKEN
    const val RDS_PRODUCT_USER_GIT_PRIVATE_TOKEN = "RDS_PERSONAL_ACCESS_TOKEN"
    // rds的chart包的格式
    const val CHART_PACKAGE_FORMAT = ".tgz"
    // rds接收引擎构建启动和结束事件 TODO 改为SCS模式的广播队列
    const val QUEUE_PIPELINE_BUILD_FINISH_RDS = "q.engine.pipeline.build.finish.rds"
    const val QUEUE_PIPELINE_BUILD_START_RDS = "q.engine.pipeline.build.start.rds"
}
