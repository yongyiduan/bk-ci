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

import com.tencent.devops.common.pipeline.Model
import com.tencent.devops.common.service.utils.ZipUtil
import com.tencent.devops.rds.dao.RdsProductInfoDao
import com.tencent.devops.rds.pojo.RdsPipelineCreate
import com.tencent.devops.rds.repo.GitRepoServiceImpl
import com.tencent.devops.rds.utils.DefaultPathUtils
import java.io.File
import java.io.InputStream
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.FileCopyUtils

@Service
class ChartProductService @Autowired constructor(
    private val dslContext: DSLContext,
    private val repoService: GitRepoServiceImpl,
    private val chartPipelineService: ChartPipelineService,
    private val productInfoDao: RdsProductInfoDao
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ChartProductService::class.java)
    }

    @Value("\${rds.volumeData:/data/bkci/public/ci/rds}")
    private val rdsVolumeDataPath: String? = null

    fun initChart(
        userId: String,
        productId: String,
        chartRepoName: String,
        resourceYaml: String,
        valuesYaml: String?
    ) {
        // 先保存项目信息，这里可能出现唯一约束的报错
        productInfoDao.saveProduct(
            dslContext = dslContext,
            productId = productId,
            creator = userId
        )

        // 获取repo的访问信息
        val repoId = repoService.getRepoInfo(chartRepoName).repoId
        val repoToken = repoService.getRepoToken(repoId = repoId)

        // 获取当前chart下的stream流水线
        val streamPaths = repoService.getFileTree(repoId = repoId, path = "STREAM_YAML_DIR", token = repoToken).map {
            it.fileName
        }

        // 读取Values文件为对象
        val values = Values.readValues(valuesYaml)
        val chartPipelines = mutableListOf<Pair<RdsPipelineCreate, Model>>()

        streamPaths.forEach { path ->
            // 获取具体文件内容
            val fileContent = repoService.getFile(repoId, path)

            // 替换values
        }

        // 创建并保存流水线
        chartPipelineService.createChartPipelines(userId, productId, chartPipelines)
    }

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
