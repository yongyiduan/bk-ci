package com.tencent.devops.dispatch.devcloud.pojo

data class EnvironmentSpec(
    val volumes: List<Volume> = emptyList(),
    val initContainers: List<Container> = emptyList(),
    val containers: List<Container>,
    val restartPolicy: String = "",
    val terminationGracePeriodSeconds: Long = 60,
    val activeDeadlineSeconds: Long = 230400, // 默认存活8天
    val dnsPolicy: String = "",
    val securityContext: EnvironmentSecurityContext? = null,
    val imagePullCertificate: List<ImagePullCertificate>? = emptyList(),
)
