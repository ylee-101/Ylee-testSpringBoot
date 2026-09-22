package com.example.demo

import org.springframework.http.HttpStatusCode

enum class ExternalApiId {
    SSE_API,
    JSON_API
}

abstract class ExternalApiException(
    val apiId: ExternalApiId,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class ExternalApiHttpException(
    apiId: ExternalApiId,
    val status: HttpStatusCode,
    val body: String,
    cause: Throwable
): ExternalApiException(
    apiId = apiId,
    message = "sse error : ${status.value()}",
    cause = cause
)

class ExternalApiConnectException(
    apiId: ExternalApiId,
    cause: Throwable
): ExternalApiException(
    apiId = apiId,
    message = "Connect error : ${cause.message}",
    cause = cause
)

class ExternalApiTimeoutException(
    apiId: ExternalApiId,
    cause: Throwable
): ExternalApiException(
    apiId = apiId,
    message = "Timeout error : ${cause.message}",
    cause = cause
)