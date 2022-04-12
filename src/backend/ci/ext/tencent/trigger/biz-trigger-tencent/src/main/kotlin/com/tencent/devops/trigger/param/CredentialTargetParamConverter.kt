package com.tencent.devops.trigger.param

import com.fasterxml.jackson.databind.JsonNode
import com.tencent.devops.common.api.util.DHUtil
import com.tencent.devops.common.client.Client
import com.tencent.devops.ticket.api.ServiceCredentialResource
import com.tencent.devops.trigger.constant.TargetFormType
import com.tencent.devops.trigger.pojo.TargetParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.Base64

@Component(TargetFormType.CREDENTIAL)
class CredentialTargetParamConverter @Autowired constructor(
    private val client: Client
) : ITargetParamConverter {

    override fun convert(projectId: String, node: JsonNode, targetParam: TargetParam): Pair<String, Any> {
        if (targetParam.value == null) {
            return Pair(targetParam.resourceKey, "")
        }
        return Pair(
            targetParam.resourceKey,
            getCredential(projectId = projectId, credentialId = targetParam.value.toString())
        )
    }

    private fun getCredential(projectId: String, credentialId: String): String {
        val pair = DHUtil.initKey()
        val encoder = Base64.getEncoder()
        val decoder = Base64.getDecoder()
        val credentialResult = client.get(ServiceCredentialResource::class).get(
            projectId, credentialId,
            encoder.encodeToString(pair.publicKey)
        )
        if (credentialResult.isNotOk() || credentialResult.data == null) {
            return ""
        }
        val credential = credentialResult.data!!
        return String(
            DHUtil.decrypt(
                decoder.decode(credential.v1),
                decoder.decode(credential.publicKey),
                pair.privateKey
            )
        )
    }
}
