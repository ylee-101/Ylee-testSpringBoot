package com.example.demo.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "external-api")
data class ExternalApiProperties(
    val ssePort: SsePortProperties,
    val jsonPort: JsonPortProperties
)

data class SsePortProperties(
    val baseUrl: String,
    val path: String,
    val scenario: String,
    val responseTimeout: Duration
)

data class JsonPortProperties(
    val baseUrl: String,
    val path: String,
    val scenario: String
)