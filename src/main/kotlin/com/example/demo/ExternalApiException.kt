package com.example.demo

import org.springframework.http.HttpStatusCode

enum class ExternalApiId {
    SSE_API,
    JSON_API
}

abstract class ExternalApiException(
    val apiId: ExternalApiId,
    val status : HttpStatusCode,
    val body : String,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class ExternalApiHttpException(
    apiId: ExternalApiId,
    status: HttpStatusCode,
    body: String,
    cause: Throwable
): ExternalApiException(
    apiId = apiId,
    status = status,
    body = body,
    message = "sse error : ${status.value()}",
    cause = cause
)