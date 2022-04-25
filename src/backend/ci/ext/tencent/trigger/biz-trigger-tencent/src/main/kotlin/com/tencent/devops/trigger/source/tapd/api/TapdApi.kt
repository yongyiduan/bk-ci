package com.tencent.devops.trigger.source.tapd.api

import com.fasterxml.jackson.core.type.TypeReference
import com.tencent.devops.common.api.exception.RemoteServiceException
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.api.util.OkhttpUtils
import com.tencent.devops.trigger.source.tapd.api.model.TapdResult
import org.slf4j.LoggerFactory

class TapdApi {

    companion object {
        private val logger = LoggerFactory.getLogger(TapdApi::class.java)
    }

    /** tapd状态key与值的映射关系
    {
    "status_3": "doing",
    "status_4": "for test",
    "status_5": "tested",
    "status_6": "for gray",
    "status_7": "grayed",
    "status_8": "done",
    "status_9": "refuse"
    }
     */
    fun getWorkflowStatusMap(apiUrl: String, token: String, workspaceId: String, system: String): Map<String, String> {
        val url = "$apiUrl/workflows/status_map?system=$system&workspace_id=$workspaceId"
        val headers = mapOf("Authorization" to token)
        val responseContent = OkhttpUtils.doGet(url = url, headers = headers).use { resp ->
            val responseContent = resp.body()?.string() ?: ""
            if (!resp.isSuccessful) {
                logger.error(
                    "getWorkflowStatusMap|Fail to request with code ${resp.code()} " +
                        "message ${resp.message()} and response $responseContent"
                )
                throw RemoteServiceException("getWorkflowStatusMap", resp.code(), responseContent)
            }
            responseContent
        }
        return JsonUtil.to(
            responseContent,
            object : TypeReference<TapdResult<Map<String, String>>>() {}
        ).data!!
    }
}
