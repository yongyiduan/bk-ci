package com.tencent.devops.common.stream.pulsar.util

import com.tencent.devops.common.stream.annotation.StreamEvent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder

internal class PulsarTopicUtilsTest {

    @Test
    fun validateTopicName() {
        PulsarTopicUtils.validateTopicName("e.build.log.status.event.log-service")
        PulsarTopicUtils.validateTopicName("build_log_status_event")
        try {
            PulsarTopicUtils.validateTopicName("build/log/status/event")
        } catch (e: Exception) {
            Assertions.assertEquals(e.javaClass, IllegalArgumentException::class.java)
        }
    }

    @Test
    fun generateTopic() {
        val topic = PulsarTopicUtils.generateTopic(
            tenant = "tenant1",
            namespace = "namespace1",
            topic = "topic1"
        )
        Assertions.assertEquals(topic, "tenant1/namespace1/topic1")
    }

    @Test
    fun testReflections() {
        val config = ConfigurationBuilder()
        config.addUrls(ClasspathHelper.forPackage("com.tencent.devops"))
        config.setExpandSuperTypes(true)
        config.setScanners(Scanners.Resources)
        val reflections = Reflections(config)
        val re = reflections.getTypesAnnotatedWith(StreamEvent::class.java)
        println(re)
    }
}
