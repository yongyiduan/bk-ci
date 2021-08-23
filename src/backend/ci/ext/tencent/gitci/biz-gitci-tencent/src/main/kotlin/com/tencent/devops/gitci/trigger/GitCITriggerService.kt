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

package com.tencent.devops.gitci.trigger

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tencent.devops.common.api.exception.CustomException
import com.tencent.devops.common.api.exception.ErrorCodeException
import com.tencent.devops.common.api.exception.OperationException
import com.tencent.devops.common.api.util.DateTimeUtil
import com.tencent.devops.common.api.util.YamlUtil
import com.tencent.devops.common.api.util.timestampmilli
import com.tencent.devops.common.ci.CiYamlUtils
import com.tencent.devops.common.ci.OBJECT_KIND_MANUAL
import com.tencent.devops.common.ci.OBJECT_KIND_MERGE_REQUEST
import com.tencent.devops.common.ci.OBJECT_KIND_PUSH
import com.tencent.devops.common.ci.OBJECT_KIND_TAG_PUSH
import com.tencent.devops.common.ci.yaml.CIBuildYaml
import com.tencent.devops.common.pipeline.enums.ChannelCode
import com.tencent.devops.gitci.client.ScmClient
import com.tencent.devops.common.ci.v2.utils.ScriptYmlUtils
import com.tencent.devops.common.ci.v2.utils.YamlCommonUtils
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.pipeline.enums.BuildStatus
import com.tencent.devops.common.redis.RedisLock
import com.tencent.devops.common.redis.RedisOperation
import com.tencent.devops.gitci.common.exception.CommitCheck
import com.tencent.devops.gitci.common.exception.ErrorCodeEnum
import com.tencent.devops.gitci.common.exception.TriggerException
import com.tencent.devops.gitci.common.exception.TriggerException.Companion.triggerError
import com.tencent.devops.gitci.common.exception.TriggerThirdException
import com.tencent.devops.gitci.common.exception.Yamls
import com.tencent.devops.gitci.dao.GitCIServicesConfDao
import com.tencent.devops.gitci.dao.GitCISettingDao
import com.tencent.devops.gitci.dao.GitPipelineResourceDao
import com.tencent.devops.gitci.dao.GitRequestEventBuildDao
import com.tencent.devops.gitci.dao.GitRequestEventDao
import com.tencent.devops.gitci.dao.GitRequestEventNotBuildDao
import com.tencent.devops.gitci.listener.GitCIMrConflictCheckDispatcher
import com.tencent.devops.gitci.listener.GitCIMrConflictCheckEvent
import com.tencent.devops.gitci.pojo.EnvironmentVariables
import com.tencent.devops.gitci.pojo.GitProjectPipeline
import com.tencent.devops.gitci.pojo.GitRequestEvent
import com.tencent.devops.gitci.pojo.TriggerBuildReq
import com.tencent.devops.gitci.pojo.enums.GitCICommitCheckState
import com.tencent.devops.gitci.pojo.enums.GitCiMergeStatus
import com.tencent.devops.gitci.pojo.enums.TriggerReason
import com.tencent.devops.gitci.pojo.git.GitCommit
import com.tencent.devops.gitci.pojo.git.GitEvent
import com.tencent.devops.gitci.pojo.git.GitMergeRequestEvent
import com.tencent.devops.gitci.pojo.git.GitPushEvent
import com.tencent.devops.gitci.pojo.git.GitTagPushEvent
import com.tencent.devops.gitci.pojo.v2.GitCIBasicSetting
import com.tencent.devops.gitci.pojo.v2.V2BuildYaml
import com.tencent.devops.gitci.trigger.exception.TriggerExceptionService
import com.tencent.devops.gitci.trigger.v1.YamlBuild
import com.tencent.devops.gitci.trigger.v2.YamlBuildV2
import com.tencent.devops.gitci.v2.dao.GitCIBasicSettingDao
import com.tencent.devops.gitci.v2.service.GitCIBasicSettingService
import com.tencent.devops.gitci.v2.service.OauthService
import com.tencent.devops.gitci.v2.service.ScmService
import com.tencent.devops.gitci.v2.service.GitPipelineBranchService
import com.tencent.devops.process.api.service.ServicePipelineResource
import com.tencent.devops.repository.pojo.oauth.GitToken
import com.tencent.devops.scm.pojo.GitCodeFileInfo
import org.joda.time.DateTime
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.File
import java.io.StringReader
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Base64
import java.util.Date
import javax.ws.rs.core.Response

@Service
class GitCITriggerService @Autowired constructor(
    private val client: Client,
    private val scmClient: ScmClient,
    private val objectMapper: ObjectMapper,
    private val dslContext: DSLContext,
    private val redisOperation: RedisOperation,
    private val gitRequestEventDao: GitRequestEventDao,
    private val gitRequestEventBuildDao: GitRequestEventBuildDao,
    private val gitRequestEventNotBuildDao: GitRequestEventNotBuildDao,
    private val gitCISettingDao: GitCIBasicSettingDao,
    private val gitCIBasicSettingService: GitCIBasicSettingService,
    private val gitCIV1SettingDao: GitCISettingDao,
    private val gitPipelineResourceDao: GitPipelineResourceDao,
    private val gitServicesConfDao: GitCIServicesConfDao,
    private val rabbitTemplate: RabbitTemplate,
    private val yamlTriggerFactory: YamlTriggerFactory,
    private val oauthService: OauthService,
    private val gitCIEventSaveService: GitCIEventService,
    private val gitPipelineBranchService: GitPipelineBranchService,
    private val scmService: ScmService,
    private val yamlBuild: YamlBuild,
    private val yamlBuildV2: YamlBuildV2,
    private val triggerExceptionService: TriggerExceptionService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(GitCITriggerService::class.java)
        private val channelCode = ChannelCode.GIT
        private val ciFileExtensions = listOf(".yml", ".yaml")
        private const val ciFileExtensionYml = ".yml"
        private const val ciFileExtensionYaml = ".yaml"
        private const val ciFileName = ".ci.yml"
        private const val ciFileDirectoryName = ".ci"
        const val noPipelineBuildEvent = "MR held, waiting until pipeline validation finish."
    }

    fun triggerBuild(userId: String, pipelineId: String, triggerBuildReq: TriggerBuildReq): Boolean {
        logger.info("Trigger build, userId: $userId, pipeline: $pipelineId, triggerBuildReq: $triggerBuildReq")

        val gitRequestEvent = createGitRequestEvent(userId, triggerBuildReq)
        val id = gitRequestEventDao.saveGitRequest(dslContext, gitRequestEvent)
        gitRequestEvent.id = id

        val existsPipeline =
            gitPipelineResourceDao.getPipelineById(dslContext, triggerBuildReq.gitProjectId, pipelineId)
                ?: throw OperationException("git ci pipelineId not exist")
        // 如果该流水线已保存过，则继续使用
        val buildPipeline = GitProjectPipeline(
            gitProjectId = existsPipeline.gitProjectId,
            pipelineId = existsPipeline.pipelineId,
            filePath = existsPipeline.filePath,
            displayName = existsPipeline.displayName,
            enabled = existsPipeline.enabled,
            creator = existsPipeline.creator,
            latestBuildInfo = null
        )

        // 流水线未启用在手动触发处直接报错
        if (!buildPipeline.enabled) {
            throw CustomException(
                status = Response.Status.METHOD_NOT_ALLOWED,
                message = "${TriggerReason.PIPELINE_DISABLE.name}(${TriggerReason.PIPELINE_DISABLE.detail})"
            )
        }

        val originYaml = triggerBuildReq.yaml
        // 如果当前文件没有内容直接不触发
        if (originYaml.isNullOrBlank()) {
            logger.warn("Matcher is false, event: ${gitRequestEvent.id} yaml is null")
            gitCIEventSaveService.saveBuildNotBuildEvent(
                userId = gitRequestEvent.userId,
                eventId = gitRequestEvent.id!!,
                pipelineId = buildPipeline.pipelineId.ifBlank { null },
                pipelineName = buildPipeline.displayName,
                filePath = buildPipeline.filePath,
                originYaml = originYaml,
                normalizedYaml = null,
                reason = TriggerReason.CI_YAML_CONTENT_NULL.name,
                reasonDetail = TriggerReason.CI_YAML_CONTENT_NULL.detail,
                gitProjectId = gitRequestEvent.gitProjectId,
                sendCommitCheck = false,
                commitCheckBlock = false,
                version = null
            )
        }

        if (!ScriptYmlUtils.isV2Version(originYaml)) {
            val (yamlObject, normalizedYaml) =
                prepareCIBuildYaml(
                    gitRequestEvent = gitRequestEvent,
                    originYaml = originYaml,
                    filePath = existsPipeline.filePath,
                    pipelineId = existsPipeline.pipelineId,
                    pipelineName = existsPipeline.displayName
                ) ?: return false

            val gitBuildId = gitRequestEventBuildDao.save(
                dslContext = dslContext,
                eventId = gitRequestEvent.id!!,
                originYaml = originYaml!!,
                parsedYaml = originYaml,
                normalizedYaml = normalizedYaml,
                gitProjectId = gitRequestEvent.gitProjectId,
                branch = gitRequestEvent.branch,
                objectKind = gitRequestEvent.objectKind,
                commitMsg = triggerBuildReq.customCommitMsg,
                triggerUser = gitRequestEvent.userId,
                sourceGitProjectId = gitRequestEvent.sourceGitProjectId,
                buildStatus = BuildStatus.RUNNING,
                version = null
            )
            yamlBuild.gitStartBuild(
                pipeline = buildPipeline,
                event = gitRequestEvent,
                yaml = yamlObject,
                gitBuildId = gitBuildId
            )
            return true
        } else {
            // v2 先做OAuth校验
            val token = oauthService.getAndCheckOauthToken(userId)
            val objects = yamlTriggerFactory.requestTriggerV2.prepareCIBuildYaml(
                gitToken = token,
                forkGitToken = null,
                gitRequestEvent = gitRequestEvent,
                isMr = false,
                originYaml = originYaml,
                filePath = existsPipeline.filePath,
                pipelineId = existsPipeline.pipelineId,
                pipelineName = existsPipeline.displayName
            ) ?: return false
            val parsedYaml = YamlCommonUtils.toYamlNotNull(objects.preYaml)
            val gitBuildId = gitRequestEventBuildDao.save(
                dslContext = dslContext,
                eventId = gitRequestEvent.id!!,
                originYaml = originYaml!!,
                parsedYaml = parsedYaml,
                normalizedYaml = YamlUtil.toYaml(objects.normalYaml),
                gitProjectId = gitRequestEvent.gitProjectId,
                branch = gitRequestEvent.branch,
                objectKind = gitRequestEvent.objectKind,
                commitMsg = triggerBuildReq.customCommitMsg,
                triggerUser = gitRequestEvent.userId,
                sourceGitProjectId = gitRequestEvent.sourceGitProjectId,
                buildStatus = BuildStatus.RUNNING,
                version = "v2.0"
            )
            // 拼接插件时会需要传入GIT仓库信息需要提前刷新下状态
            gitCIBasicSettingService.refreshSetting(gitRequestEvent.gitProjectId)
            yamlBuildV2.gitStartBuild(
                pipeline = buildPipeline,
                event = gitRequestEvent,
                yaml = objects.normalYaml,
                parsedYaml = parsedYaml,
                originYaml = originYaml,
                normalizedYaml = YamlUtil.toYaml(objects.normalYaml),
                gitBuildId = gitBuildId
            )
            return true
        }
    }

    fun externalCodeGitBuild(event: String): Boolean? {
        logger.info("Trigger code git build($event)")

        val eventObject = try {
            objectMapper.readValue<GitEvent>(event)
        } catch (e: Exception) {
            logger.warn("Fail to parse the git web hook commit event, errMsg: ${e.message}")
            return false
        }

        val gitRequestEvent = saveGitRequestEvent(eventObject, event) ?: return true

        return triggerExceptionService.handle { checkRequest(gitRequestEvent, eventObject) }
    }

    private fun checkRequest(gitRequestEvent: GitRequestEvent, event: GitEvent): Boolean {
        val start = LocalDateTime.now().timestampmilli()
        val gitProjectConf = checkGitProjectConf(gitRequestEvent, event) ?: return false

        triggerExceptionService.requestEvent = gitRequestEvent
        triggerExceptionService.gitEvent = event
        triggerExceptionService.basicSetting = gitProjectConf

        val path2PipelineExists = gitPipelineResourceDao.getAllByGitProjectId(dslContext, gitProjectConf.gitProjectId)
            .associate {
                it.filePath to GitProjectPipeline(
                    gitProjectId = it.gitProjectId,
                    pipelineId = it.pipelineId,
                    filePath = it.filePath,
                    displayName = it.displayName,
                    enabled = it.enabled,
                    creator = it.creator,
                    latestBuildInfo = null
                )
            }

        // 校验mr请求是否产生冲突
        if (event is GitMergeRequestEvent) {
            return checkMrConflict(gitRequestEvent, event, path2PipelineExists, gitProjectConf)
        }
        logger.info("It takes ${LocalDateTime.now().timestampmilli() - start}ms to match trigger pipeline")

        return matchAndTriggerPipeline(gitRequestEvent, event, path2PipelineExists, gitProjectConf)
    }

    @Suppress("ALL")
    private fun matchAndTriggerPipeline(
        gitRequestEvent: GitRequestEvent,
        event: GitEvent,
        path2PipelineExists: Map<String, GitProjectPipeline>,
        gitProjectConf: GitCIBasicSetting
    ): Boolean {
        val mrEvent = event is GitMergeRequestEvent
        val hookStartTime = LocalDateTime.now()
        val gitToken = handleGetToken(gitRequestEvent)!!
        logger.info("get token for gitProject[${gitRequestEvent.gitProjectId}] form scm, token: $gitToken")

        // fork项目库的projectId与原项目不同
        val isFork = isFork(mrEvent, gitRequestEvent)
        var forkGitToken: GitToken? = null
        if (isFork) {
            forkGitToken = handleGetToken(gitRequestEvent, true)!!
            logger.info(
                "get fork token for gitProject[${
                    getProjectId(
                        mrEvent,
                        gitRequestEvent
                    )
                }] form scm, token: $forkGitToken"
            )
        }

        // 判断本次push提交是否需要删除流水线
        if (event is GitPushEvent) {
            checkAndDeletePipeline(gitRequestEvent, event, path2PipelineExists, gitProjectConf)
        }

        // 获取指定目录下所有yml文件
        val yamlPathList = if (isFork) {
            getCIYamlList(forkGitToken!!, gitRequestEvent, mrEvent)
        } else {
            getCIYamlList(gitToken, gitRequestEvent, mrEvent)
        }.toMutableList()
        // 兼容旧的根目录yml文件
        val isCIYamlExist = if (isFork) {
            isCIYamlExist(forkGitToken!!, gitRequestEvent, mrEvent)
        } else {
            isCIYamlExist(gitToken, gitRequestEvent, mrEvent)
        }
        if (isCIYamlExist) {
            yamlPathList.add(ciFileName)
        }

        logger.info("matchAndTriggerPipeline in gitProjectId:${gitProjectConf.gitProjectId}, yamlPathList: " +
            "$yamlPathList, path2PipelineExists: $path2PipelineExists, " +
            "commitTime:${gitRequestEvent.commitTimeStamp}, " +
            "hookStartTime:${DateTimeUtil.toDateTime(hookStartTime)}, " +
            "yamlCheckedTime:${DateTimeUtil.toDateTime(LocalDateTime.now())}")
        // 如果没有Yaml文件则直接不触发
        if (yamlPathList.isEmpty()) {
            logger.warn("event: ${gitRequestEvent.id} cannot found ci yaml from git")
            triggerError(
                request = gitRequestEvent,
                reason = TriggerReason.CI_YAML_NOT_FOUND
            )
        }

        // mr提交锁定,这时还没有流水线，所以提交的是无流水线锁
        blockCommitCheck(
            mrEvent = mrEvent,
            event = gitRequestEvent,
            gitProjectConf = gitProjectConf,
            context = noPipelineBuildEvent,
            block = true,
            state = GitCICommitCheckState.PENDING
        )

        // 获取mr请求的变更文件列表，用来给后面判断
        val changeSet = if (mrEvent) {
            // 由于前面提交无流水线锁，所以这个出错需要解锁
            triggerExceptionService.handleErrorCode(
                request = gitRequestEvent,
                commitCheck = CommitCheck(
                    isNoPipelineCheck = true,
                    block = false,
                    state = GitCICommitCheckState.FAILURE
                ),
                action = {
                    scmService.getMergeRequestChangeInfo(
                        userId = null,
                        token = gitToken.accessToken,
                        gitProjectId = gitRequestEvent.gitProjectId,
                        mrId = (event as GitMergeRequestEvent).object_attributes.id
                    )
                }
            )?.files?.filter { !it.deletedFile }?.map { it.newPath }?.toSet() ?: emptySet()
        } else {
            emptySet()
        }

        yamlPathList.forEach { filePath ->

            // 因为要为 GIT_CI_YAML_INVALID 这个异常添加文件信息，所以先创建流水线，后面再根据Yaml修改流水线名称即可
            var displayName = filePath
            ciFileExtensions.forEach {
                displayName = filePath.removeSuffix(it)
            }
            val existsPipeline = path2PipelineExists[filePath]
            // 如果该流水线已保存过，则继续使用
            val buildPipeline = if (existsPipeline != null) {
                // mr请求不涉及删除操作
                if (!mrEvent) {
                    // 触发时新增流水线-分支记录
                    gitPipelineBranchService.save(
                        gitProjectId = gitProjectConf.gitProjectId,
                        pipelineId = existsPipeline.pipelineId,
                        branch = gitRequestEvent.branch
                    )
                }
                existsPipeline
            } else {
                // 对于来自fork库的mr新建的流水线，当前库不维护其状态
                GitProjectPipeline(
                    gitProjectId = gitProjectConf.gitProjectId,
                    displayName = displayName,
                    pipelineId = "", // 留空用于是否创建判断
                    filePath = filePath,
                    enabled = true,
                    creator = gitRequestEvent.userId,
                    latestBuildInfo = null
                )
            }
            // 针对每个流水线处理异常
            triggerExceptionService.handle {
                // ErrorCode都是系统错误，在最外面统一处理,都要发送无锁的commitCheck
                triggerExceptionService.handleErrorCode(
                    request = gitRequestEvent,
                    event = event,
                    pipeline = buildPipeline,
                    action = {
                        checkAndTrigger(
                            buildPipeline = buildPipeline,
                            gitRequestEvent = gitRequestEvent,
                            event = event,
                            forkGitToken = forkGitToken,
                            gitToken = gitToken,
                            changeSet = changeSet,
                            displayName = displayName,
                            gitProjectConf = gitProjectConf
                        )
                    },
                    commitCheck = CommitCheck(
                        block = false,
                        state = GitCICommitCheckState.FAILURE
                    )
                )
            }
        }
        // yml校验全部结束后，解除锁定
        blockCommitCheck(
            mrEvent = mrEvent,
            event = gitRequestEvent,
            gitProjectConf = gitProjectConf,
            context = noPipelineBuildEvent,
            block = false,
            state = GitCICommitCheckState.SUCCESS
        )
        return true
    }

    @Throws(ErrorCodeException::class)
    private fun checkAndTrigger(
        buildPipeline: GitProjectPipeline,
        gitRequestEvent: GitRequestEvent,
        event: GitEvent,
        forkGitToken: GitToken?,
        gitToken: GitToken,
        changeSet: Set<String>,
        displayName: String,
        gitProjectConf: GitCIBasicSetting
    ) {
        val mrEvent = event is GitMergeRequestEvent
        val filePath = buildPipeline.filePath
        // 流水线未启用则跳过
        if (!buildPipeline.enabled) {
            logger.warn(
                "Pipeline is not enabled, gitProjectId: ${gitRequestEvent.gitProjectId}, eventId: ${gitRequestEvent.id}"
            )
            triggerError(
                request = gitRequestEvent,
                event = event,
                pipeline = buildPipeline,
                reason = TriggerReason.PIPELINE_DISABLE
            )
        }

        // 检查版本落后信息和真正要触发的文件
        val originYaml = if (mrEvent) {
            // todo: 将超级token根据项目ID塞到Map里，每次取一下，没有了就重新拿
            val (result, orgYaml) =
                checkYmlVersion(
                    mrEvent = event as GitMergeRequestEvent,
                    sourceGitToken = forkGitToken,
                    targetGitToken = gitToken,
                    filePath = filePath,
                    changeSet = changeSet
                )

            logger.info("origin yamlStr: $orgYaml")
            if (!result) {
                triggerError(
                    request = gitRequestEvent,
                    event = event,
                    pipeline = buildPipeline,
                    reason = TriggerReason.CI_YAML_NEED_MERGE_OR_REBASE,
                    commitCheck = CommitCheck(
                        block = mrEvent,
                        state = GitCICommitCheckState.FAILURE
                    )
                )
            }
            orgYaml
        } else {
            scmService.getYamlFromGit(
                token = forkGitToken?.accessToken ?: gitToken.accessToken,
                ref = gitRequestEvent.branch,
                fileName = filePath,
                gitProjectId = getProjectId(mrEvent, gitRequestEvent).toString(),
                useAccessToken = true
            )
        }

        // 为已存在的流水线设置名称
        buildPipeline.displayName = displayName

        // 如果当前文件没有内容直接不触发
        if (originYaml.isBlank()) {
            logger.warn(
                "Matcher is false,gitProjectId: ${gitRequestEvent.gitProjectId}, eventId: ${gitRequestEvent.id}"
            )
            triggerError(
                request = gitRequestEvent,
                event = event,
                pipeline = buildPipeline,
                reason = TriggerReason.CI_YAML_CONTENT_NULL,
                yamls = Yamls(
                    originYaml = originYaml,
                    parsedYaml = null,
                    normalYaml = null
                ),
                commitCheck = CommitCheck(
                    block = mrEvent,
                    state = GitCICommitCheckState.FAILURE
                )
            )
        }

        // 检查yml版本，根据yml版本选择不同的实现
        val ymlVersion = ScriptYmlUtils.parseVersion(originYaml)
        val triggerInterface = yamlTriggerFactory.getGitCIRequestTrigger(ymlVersion)
        triggerInterface.triggerBuild(
            gitToken = gitToken,
            forkGitToken = forkGitToken,
            gitRequestEvent = gitRequestEvent,
            gitProjectPipeline = buildPipeline,
            event = event,
            originYaml = originYaml,
            filePath = filePath
        )
    }

    fun validateCIBuildYaml(yamlStr: String) = CiYamlUtils.validateYaml(yamlStr)

    fun getCIBuildYamlSchema() = CiYamlUtils.getCIBuildYamlSchema()

    fun createCIBuildYaml(yamlStr: String, gitProjectId: Long? = null): CIBuildYaml {
        logger.info("input yamlStr: $yamlStr")

        var yaml = CiYamlUtils.formatYaml(yamlStr)
        yaml = replaceEnv(yaml, gitProjectId)
        val yamlObject = YamlUtil.getObjectMapper().readValue(yaml, CIBuildYaml::class.java)

        // 检测services镜像
        if (yamlObject.services != null) {
            yamlObject.services!!.forEachIndexed { index, it ->
                // 判断镜像格式是否合法
                val (imageName, imageTag) = it.parseImage()
                val record = gitServicesConfDao.get(dslContext, imageName, imageTag)
                    ?: throw CustomException(Response.Status.INTERNAL_SERVER_ERROR,
                        "Git CI没有此镜像版本记录. ${it.image}")
                if (!record.enable) {
                    throw CustomException(Response.Status.INTERNAL_SERVER_ERROR, "镜像版本不可用. ${it.image}")
                }
            }
        }

        return CiYamlUtils.normalizeGitCiYaml(yamlObject)
    }

    private fun prepareCIBuildYaml(
        gitRequestEvent: GitRequestEvent,
        originYaml: String?,
        filePath: String,
        pipelineId: String?,
        pipelineName: String?
    ): Pair<CIBuildYaml, String>? {

        if (originYaml.isNullOrBlank()) {
            return null
        }

        val yamlObject = try {
            createCIBuildYaml(originYaml!!, gitRequestEvent.gitProjectId)
        } catch (e: Throwable) {
            logger.warn("v1 git ci yaml is invalid", e)
            // 手动触发不发送commitCheck
            gitCIEventSaveService.saveBuildNotBuildEvent(
                userId = gitRequestEvent.userId,
                eventId = gitRequestEvent.id!!,
                pipelineId = pipelineId,
                pipelineName = pipelineName,
                filePath = filePath,
                originYaml = originYaml,
                normalizedYaml = null,
                reason = TriggerReason.CI_YAML_INVALID.name,
                reasonDetail = TriggerReason.CI_YAML_INVALID.detail.format(e.message),
                gitProjectId = gitRequestEvent.gitProjectId,
                sendCommitCheck = false,
                commitCheckBlock = false,
                version = null
            )
            return null
        }

        val normalizedYaml = YamlUtil.toYaml(yamlObject)
        logger.info("normalize yaml: $normalizedYaml")
        return Pair(yamlObject, normalizedYaml)
    }

    @Throws(TriggerException::class)
    private fun checkGitProjectConf(gitRequestEvent: GitRequestEvent, event: GitEvent): GitCIBasicSetting? {
        val gitProjectSetting = gitCISettingDao.getSetting(dslContext, gitRequestEvent.gitProjectId)
        // 完全没创建过得项目不存记录
        if (null == gitProjectSetting) {
            logger.info("git ci is not enabled, git project id: ${gitRequestEvent.gitProjectId}")
            return null
        }
        if (!gitProjectSetting.enableCi) {
            logger.warn("git ci is disabled, git project id: ${gitRequestEvent.gitProjectId}, " +
                "name: ${gitProjectSetting.name}")
            triggerError(
                request = gitRequestEvent,
                reason = TriggerReason.CI_DISABLED
            )
        }
        when (event) {
            is GitPushEvent -> {
                if (!gitProjectSetting.buildPushedBranches) {
                    logger.warn("git ci conf buildPushedBranches is false, git project id: " +
                        "${gitRequestEvent.gitProjectId}, name: ${gitProjectSetting.name}")
                    triggerError(
                        request = gitRequestEvent,
                        reason = TriggerReason.BUILD_PUSHED_BRANCHES_DISABLED
                    )
                }
            }
            is GitTagPushEvent -> {
                if (!gitProjectSetting.buildPushedBranches) {
                    logger.warn("git ci conf buildPushedBranches is false, git project id: " +
                        "${gitRequestEvent.gitProjectId}, name: ${gitProjectSetting.name}")
                    triggerError(
                        request = gitRequestEvent,
                        reason = TriggerReason.BUILD_PUSHED_BRANCHES_DISABLED
                    )
                }
            }
            is GitMergeRequestEvent -> {
                if (!gitProjectSetting.buildPushedPullRequest) {
                    logger.warn("git ci conf buildMergePullRequest is false, git project id: " +
                        "${gitRequestEvent.gitProjectId}, name: ${gitProjectSetting.name}")
                    triggerError(
                        request = gitRequestEvent,
                        reason = TriggerReason.BUILD_MERGE_REQUEST_DISABLED
                    )
                }
            }
        }
        return gitProjectSetting
    }

    /**
     * 检查请求中是否有冲突
     * - 冲突通过请求详情获取，冲突检查为异步，需要通过延时队列轮训冲突检查结果
     * - 有冲突，不触发
     * - 没有冲突，进行后续操作
     */
    @Throws(TriggerException::class, TriggerThirdException::class)
    private fun checkMrConflict(
        gitRequestEvent: GitRequestEvent,
        event: GitEvent,
        path2PipelineExists: Map<String, GitProjectPipeline>,
        gitProjectConf: GitCIBasicSetting
    ): Boolean {
        val gitToken = handleGetToken(gitRequestEvent)!!
        logger.info("get token form scm, token: $gitToken")

        val projectId = gitRequestEvent.gitProjectId
        val mrRequestId = (event as GitMergeRequestEvent).object_attributes.id

        val mrInfo = triggerExceptionService.handleErrorCode(
            request = gitRequestEvent,
            action = {
                scmService.getMergeInfo(
                    gitProjectId = projectId,
                    mergeRequestId = mrRequestId,
                    token = gitToken.accessToken
                )
            }
        )!!
        // 通过查询当前merge请求的状态，unchecked说明未检查完，进入延迟队列
        when (mrInfo.mergeStatus) {
            GitCiMergeStatus.MERGE_STATUS_UNCHECKED.value -> {
                // 第一次未检查完则改变状态为正在检查供用户查看
                val recordId = gitCIEventSaveService.saveTriggerNotBuildEvent(
                    userId = gitRequestEvent.userId,
                    eventId = gitRequestEvent.id!!,
                    reason = TriggerReason.CI_MERGE_CHECKING.name,
                    reasonDetail = TriggerReason.CI_MERGE_CHECKING.detail,
                    gitProjectId = gitRequestEvent.gitProjectId
                )

                dispatchMrConflictCheck(
                    GitCIMrConflictCheckEvent(
                        token = gitToken.accessToken,
                        gitRequestEvent = gitRequestEvent,
                        event = event,
                        path2PipelineExists = path2PipelineExists,
                        gitProjectConf = gitProjectConf,
                        notBuildRecordId = recordId
                    )
                )
                return true
            }
            GitCiMergeStatus.MERGE_STATUS_CAN_NOT_BE_MERGED.value -> {
                logger.warn("git ci mr request has conflict , git project id: $projectId, mr request id: $mrRequestId")
                triggerError(
                    request = gitRequestEvent,
                    reason = TriggerReason.CI_MERGE_CONFLICT
                )
            }
            // 没有冲突则触发流水线
            else -> return matchAndTriggerPipeline(
                gitRequestEvent = gitRequestEvent,
                event = event,
                path2PipelineExists = path2PipelineExists,
                gitProjectConf = gitProjectConf
            )
        }
    }

    // 检查是否存在冲突，供Rabbit Listener使用
    // todo: 由于是update所以先不用handle做异常统一处理，后续优化
    fun checkMrConflictByListener(
        token: String,
        gitRequestEvent: GitRequestEvent,
        event: GitEvent,
        path2PipelineExists: Map<String, GitProjectPipeline>,
        gitProjectConf: GitCIBasicSetting,
        // 是否是最后一次的检查
        isEndCheck: Boolean = false,
        notBuildRecordId: Long
    ): Boolean {
        val projectId = gitRequestEvent.gitProjectId
        val mrRequestId = (event as GitMergeRequestEvent).object_attributes.id
        val mrInfo = try {
            scmService.getMergeInfo(
                gitProjectId = projectId,
                mergeRequestId = mrRequestId,
                token = token
            )
        } catch (e: ErrorCodeException) {
            gitRequestEventNotBuildDao.updateNoBuildReasonByRecordId(
                dslContext = dslContext,
                recordId = notBuildRecordId,
                reason = ErrorCodeEnum.GET_GIT_MERGE_INFO.name,
                reasonDetail = if (e.defaultMessage.isNullOrBlank()) {
                    ErrorCodeEnum.GET_GIT_MERGE_INFO.formatErrorMessage
                } else {
                    e.defaultMessage!!
                }
            )
            return false
        }
        when (mrInfo.mergeStatus) {
            GitCiMergeStatus.MERGE_STATUS_UNCHECKED.value -> {
                // 如果最后一次检查还未检查完就是检查超时
                if (isEndCheck) {
                    // 第一次之后已经在not build中有数据了，修改构建原因
                    gitRequestEventNotBuildDao.updateNoBuildReasonByRecordId(
                        dslContext = dslContext,
                        recordId = notBuildRecordId,
                        reason = TriggerReason.CI_MERGE_CHECK_TIMEOUT.name,
                        reasonDetail = TriggerReason.CI_MERGE_CHECK_TIMEOUT.detail
                    )
                }
                return false
            }
            GitCiMergeStatus.MERGE_STATUS_CAN_NOT_BE_MERGED.value -> {
                logger.warn("git ci mr request has conflict , git project id: $projectId, mr request id: $mrRequestId")
                gitRequestEventNotBuildDao.updateNoBuildReasonByRecordId(
                    dslContext = dslContext,
                    recordId = notBuildRecordId,
                    reason = TriggerReason.CI_MERGE_CONFLICT.name,
                    reasonDetail = TriggerReason.CI_MERGE_CONFLICT.detail
                )
                return true
            }
            else -> {
                gitRequestEventNotBuildDao.deleteNoBuildsById(
                    dslContext = dslContext,
                    recordId = notBuildRecordId
                )
                triggerExceptionService.handle {
                    matchAndTriggerPipeline(
                        gitRequestEvent = gitRequestEvent,
                        event = event,
                        path2PipelineExists = path2PipelineExists,
                        gitProjectConf = gitProjectConf
                    )
                }
                return true
            }
        }
    }

    /**
     * MR触发时，yml以谁为准：
     * - 当前MR变更中不存在yml文件，取目标分支（默认为未改动时目标分支永远是最新的）
     * - 当前MR变更中存在yml文件，通过对比两个文件的blobId：
     *   - blobId一样/目标分支文件不存，取源分支文件
     *   - blobId不一样，判断当前文件的根提交的blobID是否相同
     *      - 如果相同取源分支的(更新过了)
     *      - 如果不同，报错提示用户yml文件版本落后需要更新
     * 注：注意存在fork库不同projectID的提交
     */
    @Throws(ErrorCodeException::class)
    private fun checkYmlVersion(
        mrEvent: GitMergeRequestEvent,
        targetGitToken: GitToken,
        sourceGitToken: GitToken?,
        filePath: String,
        changeSet: Set<String>
    ): Pair<Boolean, String> {
        val targetFile = getFileInfo(
            token = targetGitToken.accessToken,
            ref = mrEvent.object_attributes.target_branch,
            filePath = filePath,
            gitProjectId = mrEvent.object_attributes.target_project_id.toString(),
            useAccessToken = true
        )
        if (!changeSet.contains(filePath)) {
            return if (targetFile?.content.isNullOrBlank()) {
                Pair(true, "")
            } else {
                Pair(true, String(Base64.getDecoder().decode(targetFile!!.content)))
            }
        }

        val sourceFile = getFileInfo(
            token = sourceGitToken?.accessToken ?: targetGitToken.accessToken,
            ref = mrEvent.object_attributes.source_branch,
            filePath = filePath,
            gitProjectId = mrEvent.object_attributes.source_project_id.toString(),
            useAccessToken = true
        )
        val sourceContent = if (sourceFile?.content.isNullOrBlank()) {
            ""
        } else {
            String(Base64.getDecoder().decode(sourceFile!!.content))
        }

        if (targetFile?.blobId.isNullOrBlank()) {
            return Pair(true, sourceContent)
        }

        if (targetFile?.blobId == sourceFile?.blobId) {
            return Pair(true, sourceContent)
        }

        val mergeRequest = scmService.getMergeInfo(
            gitProjectId = mrEvent.object_attributes.target_project_id,
            mergeRequestId = mrEvent.object_attributes.id,
            token = targetGitToken.accessToken
        )
        val baseTargetFile = getFileInfo(
            token = targetGitToken.accessToken,
            ref = mergeRequest!!.baseCommit,
            filePath = filePath,
            gitProjectId = mrEvent.object_attributes.target_project_id.toString(),
            useAccessToken = true
        )
        if (targetFile?.blobId == baseTargetFile?.blobId) {
            return Pair(true, sourceContent)
        }

        return Pair(false, "")
    }

    private fun getFileInfo(
        token: String,
        gitProjectId: String,
        filePath: String?,
        ref: String?,
        useAccessToken: Boolean
    ): GitCodeFileInfo? {
        return try {
            scmService.getFileInfo(
                token = token,
                ref = ref,
                filePath = filePath,
                gitProjectId = gitProjectId,
                useAccessToken = useAccessToken
            )
        } catch (e: ErrorCodeException) {
            if (e.statusCode == 404) {
                return null
            }
            throw e
        }
    }

    private fun isFork(isMrEvent: Boolean, gitRequestEvent: GitRequestEvent): Boolean {
        return isMrEvent && gitRequestEvent.sourceGitProjectId != null && gitRequestEvent.sourceGitProjectId !=
            gitRequestEvent.gitProjectId
    }

    // mr锁定提交
    private fun blockCommitCheck(
        mrEvent: Boolean,
        event: GitRequestEvent,
        gitProjectConf: GitCIBasicSetting,
        block: Boolean,
        state: GitCICommitCheckState,
        context: String
    ) {
        logger.info("CommitCheck with block, gitProjectId:${event.gitProjectId}, mrEvent:$mrEvent, " +
            "block:$block, state:$state, context:$context, enableMrBlock:${gitProjectConf.enableMrBlock}")
//        if (!isMrEvent) {
//            // push事件也发送commitCheck不加锁
//            scmClient.pushCommitCheckWithBlock(
//                commitId = event.commitId,
//                mergeRequestId = event.mergeRequestId ?: 0L,
//                userId = event.userId,
//                block = false,
//                state = state,
//                context = context,
//                gitCIBasicSetting = gitProjectConf
//            )
//        }
        if (gitProjectConf.enableMrBlock && mrEvent) {
            scmClient.pushCommitCheckWithBlock(
                commitId = event.commitId,
                mergeRequestId = event.mergeRequestId ?: 0L,
                userId = event.userId,
                block = block,
                state = state,
                context = context,
                gitCIBasicSetting = gitProjectConf,
                jumpRequest = false,
                description = null
            )
        }
    }

    /**
     * push请求时涉及到删除yml文件的操作
     * 所有向远程库的请求最后都会为push，所以针对push删除即可
     * push请求  - 检索当前流水线的存在分支，如果源分支分支在流水线存在分支中唯一，删除流水线
     * 因为源分支已经删除文件，所以后面执行时不会触发构建
     */
    private fun checkAndDeletePipeline(
        gitRequestEvent: GitRequestEvent,
        event: GitEvent,
        path2PipelineExists: Map<String, GitProjectPipeline>,
        gitProjectConf: GitCIBasicSetting
    ) {
        val deleteYamlFiles = (event as GitPushEvent).commits.flatMap {
            if (it.removed != null) {
                it.removed!!.asIterable()
            } else {
                emptyList()
            }
        }.filter { isCiFile(it) }

        if (deleteYamlFiles.isEmpty()) {
            return
        }

        val processClient = client.get(ServicePipelineResource::class)
        deleteYamlFiles.forEach { filePath ->
            val existPipeline = path2PipelineExists[filePath] ?: return@forEach
            val pipelineId = existPipeline.pipelineId
            // 先删除后查询的过程需要加锁
            val redisLock = RedisLock(
                redisOperation,
                "GIT_CI_DELETE_PIPELINE_$pipelineId",
                60L
            )
            try {
                redisLock.lock()
                gitPipelineBranchService.deleteBranch(pipelineId, gitRequestEvent.branch)
                if (!gitPipelineBranchService.hasBranchExist(pipelineId)) {
                    logger.info("event: ${gitRequestEvent.id} delete file: $filePath with pipeline: $pipelineId ")
                    gitPipelineResourceDao.deleteByPipelineId(dslContext, pipelineId)
                    processClient.delete(gitRequestEvent.userId, gitProjectConf.projectCode!!, pipelineId, channelCode)
                    // 删除相关的构建记录
                    gitCIEventSaveService.deletePipelineBuildHistory(setOf(pipelineId))
                }
            } finally {
                redisLock.unlock()
            }
        }
    }

    private fun handleGetToken(gitRequestEvent: GitRequestEvent, isMrEvent: Boolean = false): GitToken? {
        return triggerExceptionService.handleErrorCode(
            request = gitRequestEvent,
            action = { scmService.getToken(getProjectId(isMrEvent, gitRequestEvent).toString()) }
        )
    }

    private fun isCiFile(name: String): Boolean {
        if (name == ciFileName) {
            return true
        }
        if (name.startsWith(ciFileDirectoryName) &&
            (name.endsWith(ciFileExtensionYml) || name.endsWith(ciFileExtensionYaml))) {
            return true
        }
        return false
    }

    private fun replaceEnv(yaml: String, gitProjectId: Long?): String {
        if (gitProjectId == null) {
            return yaml
        }
        val gitProjectConf = gitCIV1SettingDao.getSetting(dslContext, gitProjectId) ?: return yaml
        logger.info("gitProjectConf: $gitProjectConf")
        if (null == gitProjectConf.env) {
            return yaml
        }

        val sb = StringBuilder()
        val br = BufferedReader(StringReader(yaml))
        val envRegex = Regex("\\\$env:\\w+")
        var line: String? = br.readLine()
        while (line != null) {
            val envMatches = envRegex.find(line)
            envMatches?.groupValues?.forEach {
                logger.info("envKeyPrefix: $it")
                val envKeyPrefix = it
                val envKey = envKeyPrefix.removePrefix("\$env:")
                val envValue = getEnvValue(gitProjectConf.env!!, envKey)
                logger.info("envKey: $envKey, envValue: $envValue")
                line = if (null != envValue) {
                    envRegex.replace(line!!, envValue)
                } else {
                    envRegex.replace(line!!, "null")
                }
                logger.info("line: $line")
            }

            sb.append(line).append("\n")
            line = br.readLine()
        }

        return sb.toString()
    }

    private fun getEnvValue(env: List<EnvironmentVariables>, key: String): String? {
        env.forEach {
            if (it.name == key) {
                return it.value
            }
        }
        return null
    }

    private fun dispatchMrConflictCheck(event: GitCIMrConflictCheckEvent) {
        GitCIMrConflictCheckDispatcher.dispatch(rabbitTemplate, event)
    }

    @Throws(TriggerThirdException::class)
    private fun getCIYamlList(
        gitToken: GitToken,
        gitRequestEvent: GitRequestEvent,
        isMrEvent: Boolean = false
    ): List<String> {
        val ciFileList =
            triggerExceptionService.handleErrorCode(request = gitRequestEvent,
                action = { scmService.getFileTreeFromGit(gitToken, gitRequestEvent, ciFileDirectoryName, isMrEvent) }
            )?.filter { it.name.endsWith(ciFileExtensionYml) || it.name.endsWith(ciFileExtensionYaml) }
        return ciFileList?.map { ciFileDirectoryName + File.separator + it.name }?.toList() ?: emptyList()
    }

    @Throws(TriggerThirdException::class)
    private fun isCIYamlExist(
        gitToken: GitToken,
        gitRequestEvent: GitRequestEvent,
        isMrEvent: Boolean = false
    ): Boolean {
        val ciFileList =
            triggerExceptionService.handleErrorCode(request = gitRequestEvent,
                action = { scmService.getFileTreeFromGit(gitToken, gitRequestEvent, "", isMrEvent) }
            )?.filter { it.name == ciFileName } ?: emptyList()
        return ciFileList.isNotEmpty()
    }

    // 获取项目ID，兼容没有source字段的旧数据，和fork库中源项目id不同的情况
    private fun getProjectId(isMrEvent: Boolean = false, gitRequestEvent: GitRequestEvent): Long {
        with(gitRequestEvent) {
            return if (isMrEvent && sourceGitProjectId != null && sourceGitProjectId != gitProjectId) {
                sourceGitProjectId!!
            } else {
                gitProjectId
            }
        }
    }

    private fun saveGitRequestEvent(event: GitEvent, e: String): GitRequestEvent? {
        when (event) {
            is GitPushEvent -> {
                if (event.total_commits_count <= 0) {
                    logger.info("Git web hook no commit(${event.total_commits_count})")
                    return null
                }
                val gitRequestEvent = createGitRequestEvent(event, e)
                val id = gitRequestEventDao.saveGitRequest(dslContext, gitRequestEvent)
                gitRequestEvent.id = id
                return gitRequestEvent
            }
            is GitTagPushEvent -> {
                if (event.total_commits_count <= 0) {
                    logger.info("Git web hook no commit(${event.total_commits_count})")
                    return null
                }
                val gitRequestEvent = createGitRequestEvent(event, e)
                val id = gitRequestEventDao.saveGitRequest(dslContext, gitRequestEvent)
                gitRequestEvent.id = id
                return gitRequestEvent
            }
            is GitMergeRequestEvent -> {
                if (event.object_attributes.action == "close" || event.object_attributes.action == "merge" ||
                    (event.object_attributes.action == "update" &&
                        event.object_attributes.extension_action != "push-update")
                ) {
                    logger.info("Git web hook is ${event.object_attributes.action} merge request")
                    return null
                }
                val gitRequestEvent = createGitRequestEvent(event, e)
                val id = gitRequestEventDao.saveGitRequest(dslContext, gitRequestEvent)
                gitRequestEvent.id = id
                return gitRequestEvent
            }
        }
        logger.info("event invalid: $event")
        return null
    }

    private fun createGitRequestEvent(gitPushEvent: GitPushEvent, e: String): GitRequestEvent {
        val latestCommit = getLatestCommit(gitPushEvent.after, gitPushEvent.commits)
        return GitRequestEvent(
            id = null,
            objectKind = OBJECT_KIND_PUSH,
            operationKind = gitPushEvent.operation_kind,
            extensionAction = null,
            gitProjectId = gitPushEvent.project_id,
            sourceGitProjectId = null,
            branch = gitPushEvent.ref.removePrefix("refs/heads/"),
            targetBranch = null,
            commitId = gitPushEvent.after,
            commitMsg = latestCommit?.message,
            commitTimeStamp = getCommitTimeStamp(latestCommit?.timestamp),
            userId = gitPushEvent.user_name,
            totalCommitCount = gitPushEvent.total_commits_count.toLong(),
            mergeRequestId = null,
            event = e,
            description = "",
            mrTitle = null
        )
    }

    private fun createGitRequestEvent(gitTagPushEvent: GitTagPushEvent, e: String): GitRequestEvent {
        val latestCommit = getLatestCommit(gitTagPushEvent.after, gitTagPushEvent.commits)
        return GitRequestEvent(
            id = null,
            objectKind = OBJECT_KIND_TAG_PUSH,
            operationKind = gitTagPushEvent.operation_kind,
            extensionAction = null,
            gitProjectId = gitTagPushEvent.project_id,
            sourceGitProjectId = null,
            branch = gitTagPushEvent.ref.removePrefix("refs/tags/"),
            targetBranch = null,
            commitId = gitTagPushEvent.after,
            commitMsg = latestCommit?.message,
            commitTimeStamp = getCommitTimeStamp(latestCommit?.timestamp),
            userId = gitTagPushEvent.user_name,
            totalCommitCount = gitTagPushEvent.total_commits_count.toLong(),
            mergeRequestId = null,
            event = e,
            description = "",
            mrTitle = null
        )
    }

    private fun createGitRequestEvent(gitMrEvent: GitMergeRequestEvent, e: String): GitRequestEvent {
        val latestCommit = gitMrEvent.object_attributes.last_commit
        return GitRequestEvent(
            id = null,
            objectKind = OBJECT_KIND_MERGE_REQUEST,
            operationKind = null,
            extensionAction = gitMrEvent.object_attributes.extension_action,
            gitProjectId = gitMrEvent.object_attributes.target_project_id,
            sourceGitProjectId = gitMrEvent.object_attributes.source_project_id,
            branch = gitMrEvent.object_attributes.source_branch,
            targetBranch = gitMrEvent.object_attributes.target_branch,
            commitId = latestCommit.id,
            commitMsg = latestCommit.message,
            commitTimeStamp = getCommitTimeStamp(latestCommit.timestamp),
            userId = gitMrEvent.user.username,
            totalCommitCount = 0,
            mergeRequestId = gitMrEvent.object_attributes.iid,
            event = e,
            description = "",
            mrTitle = gitMrEvent.object_attributes.title
        )
    }

    private fun createGitRequestEvent(userId: String, triggerBuildReq: TriggerBuildReq): GitRequestEvent {
        return GitRequestEvent(
            id = null,
            objectKind = OBJECT_KIND_MANUAL,
            operationKind = "",
            extensionAction = null,
            gitProjectId = triggerBuildReq.gitProjectId,
            sourceGitProjectId = null,
            branch = getBranchName(triggerBuildReq.branch),
            targetBranch = null,
            commitId = triggerBuildReq.commitId ?: "",
            commitMsg = triggerBuildReq.customCommitMsg,
            commitTimeStamp = getCommitTimeStamp(null),
            userId = userId,
            totalCommitCount = 0,
            mergeRequestId = null,
            event = "",
            description = triggerBuildReq.description,
            mrTitle = ""
        )
    }

    private fun getLatestCommit(commitId: String, commits: List<GitCommit>): GitCommit? {
        commits.forEach {
            if (it.id == commitId) {
                return it
            }
        }
        return null
    }

    private fun getCommitTimeStamp(commitTimeStamp: String?): String {
        return if (commitTimeStamp.isNullOrBlank()) {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            formatter.format(Date())
        } else {
            val time = DateTime.parse(commitTimeStamp)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.format(time.toDate())
        }
    }

    private fun getBranchName(ref: String): String {
        return when {
            ref.startsWith("refs/heads/") ->
                ref.removePrefix("refs/heads/")
            ref.startsWith("refs/tags/") ->
                ref.removePrefix("refs/tags/")
            else -> ref
        }
    }

    fun getYaml(gitProjectId: Long, buildId: String): String {
        logger.info("get yaml by buildId:($buildId), gitProjectId: $gitProjectId")
        gitCISettingDao.getSetting(dslContext, gitProjectId) ?: throw CustomException(
            Response.Status.FORBIDDEN,
            "项目未开启工蜂CI，无法查询"
        )
        val eventBuild = gitRequestEventBuildDao.getByBuildId(dslContext, buildId)
        return (eventBuild?.originYaml) ?: ""
    }

    fun getYamlV2(gitProjectId: Long, buildId: String): V2BuildYaml? {
        logger.info("get yaml by buildId:($buildId), gitProjectId: $gitProjectId")
        gitCISettingDao.getSetting(dslContext, gitProjectId) ?: throw CustomException(
            Response.Status.FORBIDDEN,
            "项目未开启工蜂CI，无法查询"
        )
        val eventBuild = gitRequestEventBuildDao.getByBuildId(dslContext, buildId) ?: return null
        // 针对V2版本做替换
        val parsed = eventBuild.parsedYaml.replaceFirst("triggerOn:", "on:")
        return V2BuildYaml(parsedYaml = parsed, originYaml = eventBuild.originYaml)
    }
}
