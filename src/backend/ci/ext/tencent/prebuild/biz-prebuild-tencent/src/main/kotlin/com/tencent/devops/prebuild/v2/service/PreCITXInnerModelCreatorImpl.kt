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

package com.tencent.devops.prebuild.v2.service

import com.tencent.devops.common.api.exception.CustomException
import com.tencent.devops.common.ci.task.CodeCCScanInContainerTask
import com.tencent.devops.common.ci.task.ServiceJobDevCloudInput
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.pipeline.enums.ChannelCode
import com.tencent.devops.common.pipeline.matrix.DispatchInfo
import com.tencent.devops.common.pipeline.pojo.element.ElementAdditionalOptions
import com.tencent.devops.common.pipeline.pojo.element.market.MarketBuildAtomElement
import com.tencent.devops.common.service.utils.SpringContextUtil
import com.tencent.devops.process.utils.PIPELINE_SETTING_MAX_CON_QUEUE_SIZE_DEFAULT
import com.tencent.devops.process.yaml.modelCreate.inner.ModelCreateEvent
import com.tencent.devops.process.yaml.modelCreate.inner.PreCIData
import com.tencent.devops.process.yaml.modelCreate.inner.TXInnerModelCreator
import com.tencent.devops.process.yaml.modelCreate.pojo.PreCIDispatchInfo
import com.tencent.devops.process.yaml.v2.models.Resources
import com.tencent.devops.process.yaml.v2.models.job.Job
import com.tencent.devops.process.yaml.v2.models.job.JobRunsOnType
import com.tencent.devops.process.yaml.v2.models.step.Step
import com.tencent.devops.store.api.atom.ServiceMarketAtomResource
import com.tencent.devops.store.pojo.atom.InstallAtomReq
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import javax.ws.rs.core.Response

@Primary
@Component
class PreCITXInnerModelCreatorImpl : TXInnerModelCreator {
    companion object {
        private val logger = LoggerFactory.getLogger(PreCITXInnerModelCreatorImpl::class.java)
        private const val REMOTE_SYNC_CODE_PLUGIN_ATOM_CODE = "syncCodeToRemote"
        private const val LOCAL_SYNC_CODE_PLUGIN_ATOM_CODE = "syncLocalCode"
    }

    private val channelCode = ChannelCode.BS

    override val marketRunTask: Boolean
        get() = false

    override val runPlugInAtomCode: String?
        get() = null

    override val runPlugInVersion: String?
        get() = null

    override val defaultImage: String
        get() = "http://mirrors.tencent.com/ci/tlinux3_ci:1.5.0"

    override fun makeCheckoutElement(
        step: Step,
        event: ModelCreateEvent,
        additionalOptions: ElementAdditionalOptions
    ): MarketBuildAtomElement {
        throw CustomException(Response.Status.BAD_REQUEST, "不支持checkout关键字进行代码检出")
    }

    override fun getServiceJobDevCloudInput(
        image: String,
        imageName: String,
        imageTag: String,
        params: String
    ): ServiceJobDevCloudInput? {
        throw CustomException(Response.Status.BAD_REQUEST, "不支持services关键字")
    }

    override fun getDispatchInfo(
        name: String,
        job: Job,
        projectCode: String,
        defaultImage: String,
        resources: Resources?
    ): DispatchInfo {
        return PreCIDispatchInfo(
            name = "dispatchInfo_${job.id}",
            job = job,
            projectCode = projectCode,
            defaultImage = defaultImage,
            resources = resources
        )
    }

    override fun preInstallMarketAtom(client: Client, event: ModelCreateEvent) {
        // not need pre install
    }

    override fun getMaxConRunningQueueSize(projectId: String, pipelineId: String): Int? {
        return PIPELINE_SETTING_MAX_CON_QUEUE_SIZE_DEFAULT
    }

    override fun makeMarketBuildAtomElement(
        job: Job,
        step: Step,
        event: ModelCreateEvent,
        additionalOptions: ElementAdditionalOptions
    ): MarketBuildAtomElement? {
        val data = mutableMapOf<String, Any>()
        val atomCode = step.uses!!.split('@')[0]
        val preCIData = event.preCIData!!

        // 若是"代码同步"插件标识
        if (atomCode.equals(LOCAL_SYNC_CODE_PLUGIN_ATOM_CODE, ignoreCase = true)) {
            // 若是本地构建机，则无需进行"代码同步"插件
            if (job.runsOn.poolName == JobRunsOnType.LOCAL.type) {
                return null
            }

            // 安装"代码同步"插件
            installMarketAtom(REMOTE_SYNC_CODE_PLUGIN_ATOM_CODE, preCIData)
            val input = step.with?.toMutableMap() ?: mutableMapOf()
            input["agentId"] = input["agentId"] ?: preCIData.agentId
            input["workspace"] = input["workspace"] ?: preCIData.workspace
            input["useDelete"] = input["useDelete"] ?: true
            input["syncGitRepository"] = input["syncGitRepository"] ?: false
            data["input"] = input

            return MarketBuildAtomElement(
                name = step.name ?: "同步本地代码",
                id = null,
                atomCode = "syncAgentCode",
                version = "3.*",
                data = data,
                additionalOptions = additionalOptions
            )
        } else {
            data["input"] = step.with ?: Any()
            setWhitePath(atomCode, data, job, preCIData)

            return MarketBuildAtomElement(
                name = step.name ?: step.uses!!.split('@')[0],
                id = step.id,
                atomCode = step.uses!!.split('@')[0],
                version = step.uses!!.split('@')[1],
                data = data,
                additionalOptions = additionalOptions
            )
        }
    }

    /**
     * 设置白名单
     */
    private fun setWhitePath(
        atomCode: String,
        data: MutableMap<String, Any>,
        job: Job,
        preCIData: PreCIData
    ) {
        if (atomCode == CodeCCScanInContainerTask.atomCode && preCIData.extraParam != null
        ) {
            val input = (data["input"] as Map<*, *>).toMutableMap()
            val isRunOnDocker =
                JobRunsOnType.DEV_CLOUD.type == job.runsOn.poolName || JobRunsOnType.DOCKER.type == job.runsOn.poolName
            input["path"] = getWhitePathList(isRunOnDocker, preCIData)
        }
    }

    /**
     * 获取白名单列表
     */
    private fun getWhitePathList(
        isRunOnDocker: Boolean = false,
        data: PreCIData
    ): List<String> {
        val whitePathList = mutableListOf<String>()

        // idea右键扫描
        if (!(data.extraParam?.codeccScanPath.isNullOrBlank())) {
            whitePathList.add(data.extraParam!!.codeccScanPath!!)
        }

        // push/commit前扫描的文件路径
        if (data.extraParam?.incrementFileList != null &&
                data.extraParam?.incrementFileList!!.isNotEmpty()
        ) {
            whitePathList.addAll(data.extraParam?.incrementFileList!!)
        }

        // 若不是容器中执行的，则无法进行本地路径替换
        if (!isRunOnDocker) {
            return whitePathList
        }

        // 容器文件路径处理
        whitePathList.forEachIndexed { index, path ->
            val filePath = path.removePrefix(data.workspace)
            // 路径开头不匹配则不替换
            if (filePath != path) {
                // 兼容workspace可能带'/'的情况
                if (data.workspace.last() == '/') {
                    whitePathList[index] = "/data/landun/workspace/$filePath"
                } else {
                    whitePathList[index] = "/data/landun/workspace$filePath"
                }
            }
        }

        return whitePathList
    }

    /**
     * 安装插件
     */
    private fun installMarketAtom(
        atomCode: String,
        preCIData: PreCIData
    ) {
        val projectCodes = ArrayList<String>().apply { add(preCIData.projectId) }

        try {
            val request = InstallAtomReq(projectCodes, atomCode)
            val client = SpringContextUtil.getBean(Client::class.java)
            client.get(ServiceMarketAtomResource::class).installAtom(preCIData.userId, channelCode, request)
        } catch (e: Throwable) {
            // 可能之前安装过，继续执行不中断
            logger.error("install atom($atomCode) failed, exception:", e)
        }
    }
}
