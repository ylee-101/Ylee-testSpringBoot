package com.example.demo

import com.example.demo.common.AppLogger
import com.example.demo.config.ExternalApiProperties
import com.example.demo.config.JsonPortProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tools.jackson.databind.JsonNode

@Component
class JsonAPiClient(
    private val externalApiProperties: ExternalApiProperties
) {
    private val TAG = "JsonAPiClient"

    private val jsonApiWebClient : WebClient = WebClient.builder()
        .baseUrl(externalApiProperties.jsonPort.baseUrl)
        .build()

    fun getJson(): Mono<ResponseEntity<MockJsonResponse>>{
        AppLogger.info(TAG, "requesting json api")

        return jsonApiWebClient.get()
            .uri { builder ->
                builder.path(externalApiProperties.jsonPort.path)
                    .queryParam("scenario", externalApiProperties.jsonPort.scenario)
                    .build()
            }
            .retrieve()
            .toEntity(MockJsonResponse::class.java)
            .onErrorMap(WebClientResponseException::class.java) { exception ->
                ExternalApiHttpException(
                    apiId = ExternalApiId.JSON_API,
                    status = exception.statusCode,
                    body = exception.responseBodyAsString,
                    cause = exception
                )
            }
    }
}


data class MockJsonResponse(
    val source: String,
    val scenario: String,
    val success: Boolean,
    val message: String,
)
