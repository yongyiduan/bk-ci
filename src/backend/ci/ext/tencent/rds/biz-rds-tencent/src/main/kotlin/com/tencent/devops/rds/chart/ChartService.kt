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

import com.tencent.devops.common.service.utils.ZipUtil
import com.tencent.devops.rds.common.Constants
import com.tencent.devops.rds.repo.GitRepoServiceImpl
import com.tencent.devops.rds.utils.CommonUtils
import com.tencent.devops.rds.utils.DefaultPathUtils
import java.io.File
import java.io.InputStream
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.FileCopyUtils

@Service
class ChartService @Autowired constructor(
    private val repoService: GitRepoServiceImpl
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ChartService::class.java)
    }

    @Value("\${rds.volumeData:/data/bkci/public/ci/rds}")
    private val rdsVolumeDataPath: String? = null

    fun cacheChartDisk(
        chartName: String,
        inputStream: InputStream
    ): String {
        // 创建临时文件
        val file = DefaultPathUtils.randomFile("zip")
        file.outputStream().use { inputStream.copyTo(it) }

        // 将文件写入磁盘
        val cacheDir = "$rdsVolumeDataPath/$chartName${System.currentTimeMillis()}"
        val zipFilePath = "$cacheDir/$chartName.zip"
        uploadFileToRepo(zipFilePath, file)

        // 解压文件
        val destDir = "$cacheDir/chart"
        ZipUtil.unZipFile(File(zipFilePath), destDir, false)

        return destDir
    }

    // 获取缓存中的chart的流水线文件
    fun getCacheChartPipelineFiles(
        cachePath: String
    ): List<File> {
        val dir = File("$cachePath/${Constants.CHART_TEMPLATE_DIR}")
        return dir.listFiles()?.toList()?.filter { it.isFile && CommonUtils.ciFile(it.name) } ?: emptyList()
    }

    private fun uploadFileToRepo(destPath: String, file: File) {
        logger.info("uploadFileToRepo: destPath: $destPath, file: ${file.absolutePath}")
        val targetFile = File(destPath)
        val parentFile = targetFile.parentFile
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        FileCopyUtils.copy(file, targetFile)
    }
}
