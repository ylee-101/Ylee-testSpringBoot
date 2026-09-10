package com.example.demo

import com.example.demo.common.AppLogger
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tools.jackson.databind.JsonNode

@Component
class TestClient {
    private val TAG = "TestClient"

    private val webClient : WebClient = WebClient.builder()
        .baseUrl("http://localhost:9090")
        .build()

    fun getStream(): Flux<ServerSentEvent<String>> {
        AppLogger.info(TAG, "requesting upstream stream")

        return webClient.get()
            .uri("/mock/stream")
            .exchangeToFlux { response ->
                if (response.statusCode().isError) {
                    return@exchangeToFlux response.createError<ServerSentEvent<String>>().flux()
                }

                val contentType = response.headers().contentType().orElse(null)
                when {
                    contentType?.isCompatibleWith(MediaType.TEXT_EVENT_STREAM) == true -> {
                        AppLogger.info(TAG, "upstream response type: SSE")
                        response.bodyToFlux(
                            object : ParameterizedTypeReference<ServerSentEvent<String>>() {}
                        )
                    }

                    contentType?.isCompatibleWith(MediaType.APPLICATION_JSON) == true -> {
                        AppLogger.info(TAG, "upstream response type: JSON")
                        response.bodyToMono(JsonNode::class.java)
                            .switchIfEmpty(
                                Mono.error(IllegalStateException("Upstream JSON response body is empty"))
                            )
                            .map { json ->
                                ServerSentEvent.builder<String>()
                                    .event("json-response")
                                    .data(json.toString())
                                    .build()
                            }
                            .flux()
                    }

                    else -> Mono.error<ServerSentEvent<String>>(
                        IllegalStateException("Unsupported upstream Content-Type: $contentType")
                    ).flux()
                }
            }
    }
}
