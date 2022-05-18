package com.tencent.devops.trigger.param

import com.fasterxml.jackson.databind.JsonNode
import com.tencent.devops.common.client.Client
import com.tencent.devops.trigger.constant.TargetFormType
import com.tencent.devops.trigger.pojo.TargetParam
import com.tencent.devops.trigger.util.CredentialUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

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
            CredentialUtil.getCredential(
                client = client,
                projectId = projectId,
                credentialId = targetParam.value.toString()
            )
        )
    }
}
