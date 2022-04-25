package com.tencent.devops.trigger.source.tapd

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

@Configuration
@ConditionalOnWebApplication
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
class TapdConfig {

    @Value("\${trigger.tapd.apiUrl:}")
    val apiUrl: String = ""

    @Value("\${trigger.tapd.clientId:}")
    val clientId: String = ""

    @Value("\${trigger.tapd.clientSecret:}")
    val clientSecret: String = ""
}
