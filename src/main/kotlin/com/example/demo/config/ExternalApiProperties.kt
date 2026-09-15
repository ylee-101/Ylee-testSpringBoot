package com.example.demo.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "external-api")
data class ExternalApiProperties(
    val baseUrl: String,
    val streamPath: String,
    val scenario: String,
    val responseTimeout: Duration
)