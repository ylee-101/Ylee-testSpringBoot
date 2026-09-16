package com.example.demo

import com.example.demo.common.AppLogger
import com.example.demo.config.ExternalApiProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class TestClient(
    private val externalApiProperties: ExternalApiProperties
) {
    private val TAG = "TestClient"

    private val sseApiWebClient : WebClient = WebClient.builder()
        .baseUrl(externalApiProperties.ssePort.baseUrl)
        .build()

    fun getStream(): Mono<ResponseEntity<Flux<ServerSentEvent<String>>>> {
        AppLogger.info(TAG, "requesting upstream stream")

        return sseApiWebClient.get()
            .uri { builder ->
                builder.path(externalApiProperties.ssePort.path)
                    .queryParam("scenario", externalApiProperties.ssePort.scenario)
                    .build()
            }
            .retrieve()
            .toEntityFlux(
                object : ParameterizedTypeReference<ServerSentEvent<String>>() {}
            )
    }
}
