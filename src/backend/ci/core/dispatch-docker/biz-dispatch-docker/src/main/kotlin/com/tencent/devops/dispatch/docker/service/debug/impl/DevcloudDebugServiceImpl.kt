package com.tencent.devops.dispatch.docker.service.debug.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tencent.devops.common.api.auth.AUTH_HEADER_DEVOPS_USER_ID
import com.tencent.devops.common.api.auth.AUTH_HEADER_GATEWAY_TAG
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.api.util.OkhttpUtils
import com.tencent.devops.common.dispatch.sdk.pojo.docker.DockerRoutingType
import com.tencent.devops.common.service.BkTag
import com.tencent.devops.common.service.config.CommonConfig
import com.tencent.devops.dispatch.docker.common.ErrorCodeEnum
import com.tencent.devops.dispatch.docker.exception.DockerServiceException
import com.tencent.devops.dispatch.docker.service.debug.DebugInterface
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DevcloudDebugServiceImpl @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val commonConfig: CommonConfig,
    private val bkTag: BkTag
) : DebugInterface {

    private val logger = LoggerFactory.getLogger(DevcloudDebugServiceImpl::class.java)

    override fun startDebug(
        userId: String,
        projectId: String,
        pipelineId: String,
        buildId: String?,
        vmSeqId: String,
        dockerRoutingType: DockerRoutingType
    ): String {
        val url = if (buildId == null) {
            "${commonConfig.devopsIdcGateway}/ms/dispatch-devcloud/api/service/dispatchDevcloud/startDebug/" +
                "projects/$projectId/pipeline/$pipelineId/vmSeq/$vmSeqId"
        } else {
            "${commonConfig.devopsIdcGateway}/ms/dispatch-devcloud/api/service/dispatchDevcloud/startDebug/" +
                "projects/$projectId/pipeline/$pipelineId/vmSeq/$vmSeqId?buildId=$buildId"
        }

        val request = Request.Builder().url(url)
            .addHeader("Accept", "application/json; charset=utf-8")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .addHeader(AUTH_HEADER_DEVOPS_USER_ID, userId)
            .addHeader(AUTH_HEADER_GATEWAY_TAG, bkTag.getLocalTag())
            .post(RequestBody.create(MediaType.parse("application/json"), ""))
            .build()

        OkhttpUtils.doHttp(request).use { resp ->
            val responseBody = resp.body()!!.string()
            logger.info("[$pipelineId get devcloud debugUrl responseBody: $responseBody")
            val response: Map<String, Any> = jacksonObjectMapper().readValue(responseBody)
            if (response["code"] == 0) {
                val debugResponse = objectMapper.readValue(
                    JsonUtil.toJson(response["data"] ?: ""),
                    DevCloudDebugResponse::class.java
                )

                return debugResponse.websocketUrl
            } else {
                val msg = response["message"] as String
                logger.error("[$pipelineId] get devcloud debugUrl failed, msg: $msg")
                throw DockerServiceException(
                    errorType = ErrorCodeEnum.START_VM_FAIL.errorType,
                    errorCode = ErrorCodeEnum.START_VM_FAIL.errorCode,
                    errorMsg = "Get devcloud debugUrl failed, msg: $msg"
                )
            }
        }
    }

    override fun stopDebug(
        userId: String,
        projectId: String,
        pipelineId: String,
        vmSeqId: String,
        containerName: String,
        dockerRoutingType: DockerRoutingType
    ): Boolean {
        val url =
            "${commonConfig.devopsIdcGateway}/ms/dispatch-devcloud/api/service/dispatchDevcloud/stopDebug/pipeline" +
                "/$pipelineId/vmSeq/$vmSeqId?containerName=$containerName"

        val request = Request.Builder().url(url)
            .addHeader("Accept", "application/json; charset=utf-8")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .addHeader(AUTH_HEADER_DEVOPS_USER_ID, userId)
            .addHeader(AUTH_HEADER_GATEWAY_TAG, bkTag.getLocalTag())
            .post(RequestBody.create(MediaType.parse("application/json"), ""))
            .build()

        OkhttpUtils.doHttp(request).use { resp ->
            val responseBody = resp.body()!!.string()
            logger.info("[$pipelineId Stop devcloud debug responseBody: $responseBody")
            val response: Map<String, Any> = jacksonObjectMapper().readValue(responseBody)
            if (response["code"] == 0) {
                return true
            } else {
                val msg = response["message"] as String
                logger.error("[$pipelineId] Stop devcloud debug failed, msg: $msg")
                throw DockerServiceException(
                    errorType = ErrorCodeEnum.START_VM_FAIL.errorType,
                    errorCode = ErrorCodeEnum.START_VM_FAIL.errorCode,
                    errorMsg = "Stop devcloud debug failed, msg: $msg"
                )
            }
        }
    }
}

data class DevCloudDebugResponse(
    val websocketUrl: String,
    val containerName: String
)
