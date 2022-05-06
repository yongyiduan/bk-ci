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

package com.tencent.devops.rds.utils

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.utils.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

const val BUFFER_SIZE = 2048

object TarUtils {
    fun unTarGZ(file: String, destDir: String) {
        val tarFile = File(file)
        unTarGZ(tarFile, destDir)
    }

    private fun unTarGZ(tarFile: File, destDir: String) {
        var dest = destDir
        if (destDir.isBlank()) {
            dest = tarFile.parent
        }
        dest = if (dest.endsWith(File.separator)) {
            dest
        } else {
            dest + File.separator
        }
        unTar(GzipCompressorInputStream(FileInputStream(tarFile)), dest)
    }

    private fun unTar(inputStream: InputStream, destDir: String) {
        val tarIn = TarArchiveInputStream(inputStream, BUFFER_SIZE)
        var entry: TarArchiveEntry?
        try {
            while (tarIn.nextTarEntry.also { entry = it } != null) {
                if (entry!!.isDirectory) { // 是目录
                    createDirectory(destDir, entry!!.name) // 创建空目录
                } else { // 是文件
                    val tmpFile = File(destDir + File.separator + entry!!.name)
                    createDirectory(tmpFile.parent + File.separator, null) // 创建输出目录
                    var out: OutputStream? = null
                    try {
                        out = FileOutputStream(tmpFile)
                        var length = 0
                        val b = ByteArray(2048)
                        while (tarIn.read(b).also { length = it } != -1) {
                            out.write(b, 0, length)
                        }
                    } finally {
                        IOUtils.closeQuietly(out)
                    }
                }
            }
        } catch (e: Exception) {
            throw e
        } finally {
            IOUtils.closeQuietly(tarIn)
        }
    }

    fun createDirectory(outputDir: String, subDir: String?) {
        var file = File(outputDir)
        if (!(subDir == null || subDir.trim { it <= ' ' } == "")) { // 子目录不为空
            file = File(outputDir + File.separator + subDir)
        }
        if (!file.exists()) {
            file.mkdirs()
        }
    }
}
