package com.tencent.devops.trigger.util

import com.tencent.devops.common.api.util.DHUtil
import com.tencent.devops.common.client.Client
import com.tencent.devops.ticket.api.ServiceCredentialResource
import java.util.Base64

object CredentialUtil {

    fun getCredential(client: Client, projectId: String, credentialId: String): String {
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
