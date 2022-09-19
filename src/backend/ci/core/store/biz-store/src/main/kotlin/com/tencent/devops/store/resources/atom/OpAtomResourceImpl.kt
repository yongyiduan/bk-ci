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

package com.tencent.devops.store.resources.atom

import com.tencent.devops.artifactory.api.UserArchiveAtomResource
import com.tencent.devops.artifactory.api.service.ServiceFileResource
import com.tencent.devops.artifactory.pojo.enums.FileChannelTypeEnum
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.api.util.UUIDUtil
import com.tencent.devops.common.service.utils.MessageCodeUtil
import com.tencent.devops.common.service.utils.ZipUtil
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.store.api.atom.OpAtomResource
import com.tencent.devops.store.constant.StoreMessageCode.USER_ATOM_CONF_INVALID
import com.tencent.devops.store.constant.StoreMessageCode.USER_REPOSITORY_TASK_JSON_FIELD_IS_INVALID
import com.tencent.devops.store.pojo.atom.ApproveReq
import com.tencent.devops.store.pojo.atom.Atom
import com.tencent.devops.store.pojo.atom.AtomCreateRequest
import com.tencent.devops.store.pojo.atom.AtomOfflineReq
import com.tencent.devops.store.pojo.atom.AtomResp
import com.tencent.devops.store.pojo.atom.AtomUpdateRequest
import com.tencent.devops.store.pojo.atom.MarketAtomCreateRequest
import com.tencent.devops.store.pojo.atom.ReleaseInfo
import com.tencent.devops.store.pojo.atom.enums.AtomStatusEnum
import com.tencent.devops.store.pojo.atom.enums.AtomTypeEnum
import com.tencent.devops.store.pojo.atom.enums.OpSortTypeEnum
import com.tencent.devops.store.pojo.common.TASK_JSON_NAME
import com.tencent.devops.store.service.atom.AtomReleaseService
import com.tencent.devops.store.service.atom.AtomService
import com.tencent.devops.store.service.atom.MarketAtomService
import com.tencent.devops.store.service.atom.OpAtomService
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.service.utils.CommonUtils
import com.tencent.devops.store.pojo.atom.MarketAtomUpdateRequest
import com.tencent.devops.store.service.common.StoreLogoService
import org.glassfish.jersey.media.multipart.FormDataContentDisposition
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.imageio.ImageIO

@RestResource
class OpAtomResourceImpl @Autowired constructor(
    private val atomService: AtomService,
    private val opAtomService: OpAtomService,
    private val marketAtomService: MarketAtomService,
    private val atomReleaseService: AtomReleaseService,
    private val storeLogoService: StoreLogoService,
    private val client: Client
) : OpAtomResource {

    override fun add(userId: String, atomCreateRequest: AtomCreateRequest): Result<Boolean> {
        return atomService.savePipelineAtom(userId, atomCreateRequest)
    }

    override fun update(userId: String, id: String, atomUpdateRequest: AtomUpdateRequest): Result<Boolean> {
        return atomService.updatePipelineAtom(userId, id, atomUpdateRequest)
    }

    override fun listAllPipelineAtoms(
        atomName: String?,
        atomType: AtomTypeEnum?,
        serviceScope: String?,
        os: String?,
        category: String?,
        classifyId: String?,
        atomStatus: AtomStatusEnum?,
        sortType: OpSortTypeEnum?,
        desc: Boolean?,
        page: Int?,
        pageSize: Int?
    ): Result<AtomResp<Atom>?> {
        return opAtomService.getOpPipelineAtoms(
            atomName = atomName,
            atomType = atomType,
            serviceScope = serviceScope,
            os = os,
            category = category,
            classifyId = classifyId,
            atomStatus = atomStatus,
            sortType = sortType,
            desc = desc,
            page = page,
            pageSize = pageSize
        )
    }

    override fun getPipelineAtomById(id: String): Result<Atom?> {
        return opAtomService.getPipelineAtom(id)
    }

    override fun deletePipelineAtomById(id: String): Result<Boolean> {
        return atomService.deletePipelineAtom(id)
    }

    override fun approveAtom(userId: String, atomId: String, approveReq: ApproveReq): Result<Boolean> {
        return opAtomService.approveAtom(userId, atomId, approveReq)
    }

    override fun generateCiYaml(
        atomCode: String?,
        os: String?,
        classType: String?,
        defaultShowFlag: Boolean?
    ): Result<String> {
        return Result(marketAtomService.generateCiYaml(atomCode, os, classType, defaultShowFlag))
    }

    override fun offlineAtom(userId: String, atomCode: String, atomOfflineReq: AtomOfflineReq): Result<Boolean> {
        return atomReleaseService.offlineAtom(
            userId = userId,
            atomCode = atomCode,
            atomOfflineReq = atomOfflineReq,
            checkPermissionFlag = false
        )
    }

    override fun releaseAtom(
        userId: String,
        atomCode: String,
        inputStream: InputStream,
        disposition: FormDataContentDisposition
    ): Result<Boolean> {
        val atomPath = unzipFile(
            userId = userId,
            atomCode = atomCode,
            inputStream = inputStream,
            disposition = disposition
        )
        val taskJsonFile = File("$atomPath", TASK_JSON_NAME)
        if (!taskJsonFile.exists()) {
            return MessageCodeUtil.generateResponseDataObject(
                USER_ATOM_CONF_INVALID,
                arrayOf(TASK_JSON_NAME)
            )
        }
        var taskJsonMap: Map<String, Any>
        var releaseInfo: ReleaseInfo
        try {
            val taskJsonStr = taskJsonFile.readText(Charset.forName("UTF-8"))
            taskJsonMap = JsonUtil.toMap(taskJsonStr).toMutableMap()
            releaseInfo = JsonUtil.to(taskJsonMap["releaseInfo"] as String, ReleaseInfo::class.java)
        } catch (e: Exception) {
            return MessageCodeUtil.generateResponseDataObject(
                USER_REPOSITORY_TASK_JSON_FIELD_IS_INVALID
            )
        } finally {
            taskJsonFile.delete()
        }
        // 新增插件
        val addMarketAtomResult = atomReleaseService.addMarketAtom(
            userId,
            MarketAtomCreateRequest(
                projectCode = releaseInfo.projectCode,
                atomCode = releaseInfo.atomCode,
                name = releaseInfo.name,
                language = releaseInfo.language
            )
        )
        if (addMarketAtomResult.isNotOk()) {
            return Result(data = false, message = addMarketAtomResult.message)
        }
        val atomId = addMarketAtomResult.data!!
        var logoUrl = releaseInfo.logoUrl
        if (!logoUrl.startsWith("http")) {
            val pattern: Pattern = Pattern.compile(BK_CI_PATH_REGEX)
            val matcher: Matcher = pattern.matcher(logoUrl)
            val relativePath = if (matcher.find()) {
                matcher.group(2)
            } else null
            if (relativePath.isNullOrBlank()) {
                return MessageCodeUtil.generateResponseDataObject(
                    USER_REPOSITORY_TASK_JSON_FIELD_IS_INVALID,
                    arrayOf("releaseInfo.logoUrl")
                )
            }
            val logoFile = File("$atomPath/${relativePath.removePrefix("/")}")
            try {
                if (logoFile.exists()) {
                    val uploadStoreLogoResult = storeLogoService.uploadStoreLogo(
                        userId = userId,
                        contentLength = logoFile.length(),
                        inputStream = logoFile.inputStream(),
                        fileName = logoFile.name
                    )
                    if (uploadStoreLogoResult.isOk()) {
                        releaseInfo.logoUrl = uploadStoreLogoResult.data!!.logoUrl!!
                    } else {
                        return Result(data = false, message = uploadStoreLogoResult.message)
                    }
                }
            } finally {
                logoFile.delete()
            }
        }
        val archiveAtomResult = client.get(UserArchiveAtomResource::class).archiveAtom(
            userId = userId,
            projectCode = releaseInfo.projectCode,
            atomId = atomId,
            atomCode = releaseInfo.atomCode,
            version = releaseInfo.version,
            releaseType = releaseInfo.releaseType,
            inputStream = inputStream,
            disposition = disposition,
            os = JsonUtil.toJson(releaseInfo.os)
        )
        if (archiveAtomResult.isNotOk()) {
            return Result(data = false, message = archiveAtomResult.message)
        }

        descriptionAnalysis(releaseInfo.description, atomCode, atomPath)


        atomReleaseService.updateMarketAtom(
            userId,
            releaseInfo.projectCode,
            MarketAtomUpdateRequest(
                atomCode = releaseInfo.atomCode,
                name = releaseInfo.name,
                category = releaseInfo.category,
                jobType = releaseInfo.jobType,
                os = releaseInfo.os,
                summary = releaseInfo.summary,
                description =
            )
        )


    }

    fun descriptionAnalysis(userId: String,description: String, atomPath: String) {
        if (description.startsWith("http") && description.endsWith(".md")) {
            val inputStream = URL(description).openStream()
            val file = File("$atomPath/file/description.md")
            FileOutputStream(file).use { outputStream ->
                var read: Int
                val bytes = ByteArray(4096)
                while (inputStream.read(bytes).also { read = it } != -1) {
                    outputStream.write(bytes, 0, read)
                }
            }
            var descriptionText = StringBuffer(file.readText())
            val analysisResult = regexAnalysis(
                userId = userId,
                input = descriptionText.toString(),
                atomPath
            )
            //替换资源路径 ![](https://0705737.png)
        }
    }

    private fun getAtomBasePath(): String {
        return System.getProperty("java.io.tmpdir").removeSuffix("/")
    }

    private fun regexAnalysis(userId: String, input: String, atomPath: String): Map<String, String> {
        val pattern: Pattern = Pattern.compile(BK_CI_PATH_REGEX)
        val matcher: Matcher = pattern.matcher(input)
        val pathList = mutableListOf<String>()
        val result = mutableMapOf<String, String>()
        while (matcher.find()) {
            pathList.add(matcher.group(2).removePrefix("/"))
        }
        pathList.forEach {
            val file = File("$atomPath/file/$it")
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
        "${getAtomBasePath()}/$BK_CI_ATOM_DIR/$userId/$atomCode"

    private fun unzipFile(
        disposition: FormDataContentDisposition,
        inputStream: InputStream,
        userId: String,
        atomCode: String
    ): String {
        val fileName = disposition.fileName
        val index = fileName.lastIndexOf(".")
        val fileType = fileName.substring(index + 1)
        val file = Files.createTempFile(UUIDUtil.generate(), ".$fileType").toFile()
        file.outputStream().use {
            inputStream.copyTo(it)
        }
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
        private val logger = LoggerFactory.getLogger(OpAtomResourceImpl::class.java)
        private const val BK_CI_ATOM_DIR = "bk-atom"
        private const val BK_CI_PATH_REGEX = "(\\$\\{\\{indexFile\\()\\\"([^\\\"]*)\\\""
    }
}