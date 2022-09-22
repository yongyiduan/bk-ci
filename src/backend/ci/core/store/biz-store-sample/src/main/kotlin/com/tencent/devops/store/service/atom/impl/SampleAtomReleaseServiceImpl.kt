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

package com.tencent.devops.store.service.atom.impl

import com.tencent.devops.artifactory.api.ServiceArchiveAtomResource
import com.tencent.devops.artifactory.api.service.ServiceFileResource
import com.tencent.devops.artifactory.pojo.enums.FileChannelTypeEnum
import com.tencent.devops.common.api.constant.BEGIN
import com.tencent.devops.common.api.constant.COMMIT
import com.tencent.devops.common.api.constant.CommonMessageCode
import com.tencent.devops.common.api.constant.DOING
import com.tencent.devops.common.api.constant.END
import com.tencent.devops.common.api.constant.NUM_FOUR
import com.tencent.devops.common.api.constant.NUM_ONE
import com.tencent.devops.common.api.constant.NUM_THREE
import com.tencent.devops.common.api.constant.NUM_TWO
import com.tencent.devops.common.api.constant.SUCCESS
import com.tencent.devops.common.api.constant.TEST
import com.tencent.devops.common.api.constant.UNDO
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.api.util.UUIDUtil
import com.tencent.devops.common.service.utils.CommonUtils
import com.tencent.devops.common.service.utils.MessageCodeUtil
import com.tencent.devops.common.service.utils.ZipUtil
import com.tencent.devops.model.store.tables.records.TAtomRecord
import com.tencent.devops.store.api.common.OpStoreLogoResource
import com.tencent.devops.store.constant.StoreMessageCode
import com.tencent.devops.store.pojo.atom.AtomReleaseRequest
import com.tencent.devops.store.pojo.atom.MarketAtomCreateRequest
import com.tencent.devops.store.pojo.atom.MarketAtomUpdateRequest
import com.tencent.devops.store.pojo.atom.ReleaseInfo
import com.tencent.devops.store.pojo.atom.enums.AtomPackageSourceTypeEnum
import com.tencent.devops.store.pojo.atom.enums.AtomStatusEnum
import com.tencent.devops.store.pojo.common.ReleaseProcessItem
import com.tencent.devops.store.pojo.common.TASK_JSON_NAME
import com.tencent.devops.store.pojo.common.enums.StoreTypeEnum
import com.tencent.devops.store.service.atom.SampleAtomReleaseService
import org.glassfish.jersey.media.multipart.FormDataContentDisposition
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class SampleAtomReleaseServiceImpl : SampleAtomReleaseService, AtomReleaseServiceImpl() {

    override fun handleAtomPackage(
        marketAtomCreateRequest: MarketAtomCreateRequest,
        userId: String,
        atomCode: String
    ): Result<Map<String, String>?> {
        return Result(data = null)
    }

    override fun getAtomPackageSourceType(): AtomPackageSourceTypeEnum {
        return AtomPackageSourceTypeEnum.UPLOAD
    }

    override fun getFileStr(
        projectCode: String,
        atomCode: String,
        atomVersion: String,
        fileName: String,
        repositoryHashId: String?,
        branch: String?
    ): String? {
        logger.info("getFileStr $projectCode|$atomCode|$atomVersion|$fileName|$repositoryHashId|$branch")
        val fileStr = marketAtomArchiveService.getFileStr(projectCode, atomCode, atomVersion, fileName)
        logger.info("getFileStr fileStr is:$fileStr")
        return fileStr
    }

    override fun asyncHandleUpdateAtom(context: DSLContext, atomId: String, userId: String, branch: String?) = Unit

    override fun validateUpdateMarketAtomReq(
        userId: String,
        marketAtomUpdateRequest: MarketAtomUpdateRequest,
        atomRecord: TAtomRecord
    ): Result<Boolean> {
        // 开源版升级插件暂无特殊参数需要校验
        return Result(true)
    }

    override fun handleProcessInfo(
        userId: String,
        atomId: String,
        isNormalUpgrade: Boolean,
        status: Int
    ): List<ReleaseProcessItem> {
        val processInfo = initProcessInfo()
        val totalStep = NUM_FOUR
        when (status) {
            AtomStatusEnum.INIT.status, AtomStatusEnum.COMMITTING.status -> {
                storeCommonService.setProcessInfo(processInfo, totalStep, NUM_TWO, DOING)
            }
            AtomStatusEnum.TESTING.status -> {
                storeCommonService.setProcessInfo(processInfo, totalStep, NUM_THREE, DOING)
            }
            AtomStatusEnum.RELEASED.status -> {
                storeCommonService.setProcessInfo(processInfo, totalStep, NUM_FOUR, SUCCESS)
            }
        }
        return processInfo
    }

    override fun doCancelReleaseBus(userId: String, atomId: String) = Unit

    override fun getPreValidatePassTestStatus(atomCode: String, atomId: String, atomStatus: Byte): Byte {
        return AtomStatusEnum.RELEASED.status.toByte()
    }

    override fun doAtomReleaseBus(userId: String, atomReleaseRequest: AtomReleaseRequest) = Unit

    /**
     * 初始化插件版本进度
     */
    private fun initProcessInfo(): List<ReleaseProcessItem> {
        val processInfo = mutableListOf<ReleaseProcessItem>()
        processInfo.add(ReleaseProcessItem(MessageCodeUtil.getCodeLanMessage(BEGIN), BEGIN, NUM_ONE, SUCCESS))
        processInfo.add(ReleaseProcessItem(MessageCodeUtil.getCodeLanMessage(COMMIT), COMMIT, NUM_TWO, UNDO))
        processInfo.add(ReleaseProcessItem(MessageCodeUtil.getCodeLanMessage(TEST), TEST, NUM_THREE, UNDO))
        processInfo.add(ReleaseProcessItem(MessageCodeUtil.getCodeLanMessage(END), END, NUM_FOUR, UNDO))
        return processInfo
    }

    /**
     * 检查版本发布过程中的操作权限
     */
    @Suppress("ALL")
    override fun checkAtomVersionOptRight(
        userId: String,
        atomId: String,
        status: Byte,
        isNormalUpgrade: Boolean?
    ): Pair<Boolean, String> {
        val record =
            marketAtomDao.getAtomRecordById(dslContext, atomId) ?: return Pair(
                false,
                CommonMessageCode.PARAMETER_IS_INVALID
            )
        val atomCode = record.atomCode
        val creator = record.creator
        val recordStatus = record.atomStatus

        // 判断用户是否有权限(当前版本的创建者和管理员可以操作)
        if (!(storeMemberDao.isStoreAdmin(
                dslContext = dslContext,
                userId = userId,
                storeCode = atomCode,
                storeType = StoreTypeEnum.ATOM.type.toByte()
            ) || creator == userId)
        ) {
            return Pair(false, CommonMessageCode.PERMISSION_DENIED)
        }

        logger.info("record status=$recordStatus, status=$status")
        var validateFlag = true
        if (status == AtomStatusEnum.COMMITTING.status.toByte() &&
            recordStatus != AtomStatusEnum.INIT.status.toByte()
        ) {
            validateFlag = false
        } else if (status == AtomStatusEnum.TESTING.status.toByte() &&
            recordStatus != AtomStatusEnum.COMMITTING.status.toByte()
        ) {
            validateFlag = false
        } else if (status == AtomStatusEnum.RELEASED.status.toByte() &&
            recordStatus != AtomStatusEnum.TESTING.status.toByte()
        ) {
            validateFlag = false
        } else if (status == AtomStatusEnum.GROUNDING_SUSPENSION.status.toByte() &&
            recordStatus == AtomStatusEnum.RELEASED.status.toByte()
        ) {
            validateFlag = false
        } else if (status == AtomStatusEnum.UNDERCARRIAGING.status.toByte() &&
            recordStatus != AtomStatusEnum.RELEASED.status.toByte()
        ) {
            validateFlag = false
        } else if (status == AtomStatusEnum.UNDERCARRIAGED.status.toByte() &&
            recordStatus !in (
                listOf(
                    AtomStatusEnum.UNDERCARRIAGING.status.toByte(),
                    AtomStatusEnum.RELEASED.status.toByte()
                ))
        ) {
            validateFlag = false
        }

        return if (validateFlag) Pair(true, "") else Pair(false, StoreMessageCode.USER_ATOM_RELEASE_STEPS_ERROR)
    }

    override fun releaseAtom(
        userId: String,
        atomCode: String,
        inputStream: InputStream,
        disposition: FormDataContentDisposition
    ): Result<Boolean> {
        // 解压插件包到临时目录
        val atomPath = unzipFile(
            userId = userId,
            atomCode = atomCode,
            inputStream = inputStream,
            disposition = disposition
        )
        val taskJsonFile = File("$atomPath", TASK_JSON_NAME)
        if (!taskJsonFile.exists()) {
            return MessageCodeUtil.generateResponseDataObject(
                StoreMessageCode.USER_ATOM_CONF_INVALID,
                arrayOf(TASK_JSON_NAME)
            )
        }
        val taskJsonMap: Map<String, Any>
        val releaseInfo: ReleaseInfo
        // 解析task.json文件
        try {
            val taskJsonStr = taskJsonFile.readText(Charset.forName("UTF-8"))
            taskJsonMap = JsonUtil.toMap(taskJsonStr).toMutableMap()
            val releaseInfoMap = taskJsonMap["releaseInfo"]
            releaseInfo = JsonUtil.mapTo(releaseInfoMap as Map<String, Any>, ReleaseInfo::class.java)
        } catch (e: Exception) {
            return MessageCodeUtil.generateResponseDataObject(
                StoreMessageCode.USER_REPOSITORY_TASK_JSON_FIELD_IS_INVALID
            )
        } finally {
            taskJsonFile.delete()
        }
        // 新增插件
        val addMarketAtomResult = addMarketAtom(
            userId,
            MarketAtomCreateRequest(
                projectCode = releaseInfo.projectId,
                atomCode = releaseInfo.atomCode,
                name = releaseInfo.name,
                language = releaseInfo.language
            )
        )
        if (addMarketAtomResult.isNotOk()) {
            return Result(data = false, message = addMarketAtomResult.message)
        }
        val atomId = addMarketAtomResult.data!!
        // 解析logoUrl
        val logoUrlAnalysisResult = logoUrlAnalysis(userId, releaseInfo.logoUrl, atomPath)
        if (logoUrlAnalysisResult.isNotOk()) {
            return Result(data = false, message = logoUrlAnalysisResult.message)
        }
        releaseInfo.logoUrl = logoUrlAnalysisResult.data!!
        // 归档插件包
        try {
            val zipFile = File(zipFiles(userId, atomCode, atomPath))
            if (zipFile.exists()) {
                val archiveAtomResult = client.get(ServiceArchiveAtomResource::class).archiveAtomFile(
                    userId = userId,
                    projectCode = releaseInfo.projectId,
                    atomId = atomId,
                    atomCode = releaseInfo.atomCode,
                    version = releaseInfo.versionInfo.version,
                    releaseType = releaseInfo.versionInfo.releaseType,
                    inputStream = zipFile.inputStream(),
                    disposition = disposition,
                    os = JsonUtil.toJson(releaseInfo.os)
                )
                if (archiveAtomResult.isNotOk()) {
                    return Result(
                        data = false,
                        status = archiveAtomResult.status,
                        message = archiveAtomResult.message
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("archiveAtomResult is fail ${e.message}")
        }
        // 解析description
        val description = descriptionAnalysis(releaseInfo.description, atomCode, atomPath)
        // 升级插件
        val updateMarketAtomResult = updateMarketAtom(
            userId,
            releaseInfo.projectId,
            MarketAtomUpdateRequest(
                atomCode = releaseInfo.atomCode,
                name = releaseInfo.name,
                category = releaseInfo.category,
                jobType = releaseInfo.jobType,
                os = releaseInfo.os,
                summary = releaseInfo.summary,
                description = description,
                version = releaseInfo.versionInfo.version,
                releaseType = releaseInfo.versionInfo.releaseType,
                versionContent = releaseInfo.versionInfo.versionContent,
                publisher = releaseInfo.versionInfo.publisher,
                labelIdList = releaseInfo.labelIdList,
                frontendType = releaseInfo.frontendType,
                logoUrl = releaseInfo.logoUrl,
                classifyCode = releaseInfo.classifyCode
            )
        )
        if (updateMarketAtomResult.isNotOk()) {
            return Result(
                data = false,
                status = updateMarketAtomResult.status,
                message = updateMarketAtomResult.message
            )
        }
        // 确认测试通过
        return passTest(userId, atomId)
    }

    private fun zipFiles(userId: String, atomCode: String, atomPath: String): String {
        val zipPath = "${buildAtomArchivePath(userId, atomCode)}.zip"
        val zipOutputStream = ZipOutputStream(FileOutputStream(zipPath))
                val files = File(atomPath).listFiles()
        try {
            files?.forEach { file ->
                if (!file.isDirectory) {
                    zipOutputStream.putNextEntry(ZipEntry(file.name))
                    try {
                        val input = FileInputStream(file)
                        val byteArray = ByteArray(1024)
                        var len: Int
                        len = input.read(byteArray)
                        println(len)
                        while (len != -1) {
                            while (len != -1) {
                                zipOutputStream.write(byteArray, 0, len)
                                len = input.read(byteArray)
                            }
                        }
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                    zipOutputStream.closeEntry()
                }
            }
            return zipPath
        } finally {
            files?.clone()
        }
    }

    private fun logoUrlAnalysis(userId: String, logoUrl: String, atomPath: String): Result<String> {
        val separator = File.separator
        var result = logoUrl
        if (!logoUrl.startsWith("http")) {
            val pattern: Pattern = Pattern.compile(BK_CI_PATH_REGEX)
            val matcher: Matcher = pattern.matcher(logoUrl)
            val relativePath = if (matcher.find()) {
                matcher.group(2)
            } else null
            if (relativePath.isNullOrBlank()) {
                return MessageCodeUtil.generateResponseDataObject(
                    StoreMessageCode.USER_REPOSITORY_TASK_JSON_FIELD_IS_INVALID,
                    arrayOf("releaseInfo.logoUrl")
                )
            }
            val logoFile = File("$atomPath${separator}file${separator}${relativePath.removePrefix(separator)}")
            try {
                if (logoFile.exists()) {
                    val uploadStoreLogoResult = client.get(OpStoreLogoResource::class).uploadStoreLogo(
                        userId = userId,
                        contentLength = logoFile.length(),
                        inputStream = logoFile.inputStream(),
                        disposition = FormDataContentDisposition(
                            "form-data; name=\"logo\"; filename=\"${logoFile.name}\""
                        )
                    )
                    if (uploadStoreLogoResult.isOk()) {
                        result = uploadStoreLogoResult.data!!.logoUrl!!
                    } else {
                        return Result(data = logoUrl, message = uploadStoreLogoResult.message)
                    }
                }
            } finally {
                logoFile.delete()
            }
        }
        return Result(result)
    }

    private fun descriptionAnalysis(userId: String, description: String, atomPath: String): String {
        val separator = File.separator
        var descriptionText =
            if (description.startsWith("http") && description.endsWith(".md")) {
                val inputStream = URL(description).openStream()
                val file = File("$atomPath${separator}file${separator}description.md")
                FileOutputStream(file).use { outputStream ->
                    var read: Int
                    val bytes = ByteArray(1024)
                    while (inputStream.read(bytes).also { read = it } != -1) {
                        outputStream.write(bytes, 0, read)
                    }
                }
                file.readText()
            } else {
                description
            }
        val analysisResult = regexAnalysis(
            userId = userId,
            input = descriptionText,
            atomPath
        )
        analysisResult.forEach {
            val pattern: Pattern = Pattern.compile("(\\$\\{\\{indexFile\\()\\\"${it.key}\\\"\\)}}")
            val matcher: Matcher = pattern.matcher(descriptionText)
            if (matcher.find()) {
                descriptionText = matcher.replaceFirst("![](${it.value})")
            }
        }
        return descriptionText
    }

    private fun getAtomBasePath(): String {
        return System.getProperty("java.io.tmpdir").removeSuffix(File.separator)
    }

    private fun regexAnalysis(userId: String, input: String, atomPath: String): Map<String, String> {
        val separator = File.separator
        val pattern: Pattern = Pattern.compile(BK_CI_PATH_REGEX)
        val matcher: Matcher = pattern.matcher(input)
        val pathList = mutableListOf<String>()
        val result = mutableMapOf<String, String>()
        while (matcher.find()) {
            val path = matcher.group(2).removePrefix("$separator")
            if (path.endsWith(".md")) {
                val file = File("$atomPath${separator}file${separator}$path")
                if (file.exists()) {
                    return regexAnalysis(userId, file.readText(), atomPath)
                }
            }
            pathList.add(matcher.group(2).removePrefix(separator))
        }
        pathList.forEach {
            val file = File("$atomPath${separator}file${separator}$it")
            val serviceUrlPrefix = client.getServiceUrl(ServiceFileResource::class)
            try {
                if (file.exists()) {
                    val uploadFileResult = CommonUtils.serviceUploadFile(
                        userId = userId,
                        serviceUrlPrefix = serviceUrlPrefix,
                        file = file,
                        fileChannelType = FileChannelTypeEnum.WEB_SHOW.name,
                        logo = false
                    )
                    if (uploadFileResult.isOk()) {
                        result[it] = uploadFileResult.data!!
                    } else {
                        logger.error("uploadFileResult is fail, file path:$it")
                    }
                }
            } finally {
                file.delete()
            }
        }
        return result
    }

    private fun buildAtomArchivePath(userId: String, atomCode: String) =
        "${getAtomBasePath()}${File.separator}$BK_CI_ATOM_DIR${File.separator}$userId${File.separator}$atomCode"

    private fun unzipFile(
        disposition: FormDataContentDisposition,
        inputStream: InputStream,
        userId: String,
        atomCode: String
    ): String {
        val fileName = disposition.fileName
        val index = fileName.lastIndexOf(".")
        val fileType = fileName.substring(index + 1)
        // 解压到指定目录
        val atomPath = buildAtomArchivePath(userId, atomCode)
        if (!File(atomPath).exists()) {
            val file = Files.createTempFile(UUIDUtil.generate(), ".$fileType").toFile()
            file.outputStream().use {
                inputStream.copyTo(it)
            }
            try {
                ZipUtil.unZipFile(file, atomPath, false)
            } finally {
                file.delete() // 删除临时文件
            }
        }
        return atomPath
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SampleAtomReleaseServiceImpl::class.java)
        private const val BK_CI_ATOM_DIR = "bk-atom"
        private const val BK_CI_PATH_REGEX = "(\\$\\{\\{indexFile\\()\\\"([^\\\"]*)\\\""
    }
}
