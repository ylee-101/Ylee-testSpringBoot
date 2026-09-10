package com.example.demo

import com.example.demo.common.AppLogger
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@Component
class TestClient {
    private val TAG = "TestClient"

    private val webClient : WebClient = WebClient.builder()
        .baseUrl("http://localhost:9090")
        .build()

    fun getStream(): Flux<ServerSentEvent<String>> {
        AppLogger.info(TAG, "getStream")
        return webClient.get()
            .uri("/mock/stream?scenario=slow-response")
            .retrieve()
            .bodyToFlux(
                object : ParameterizedTypeReference<ServerSentEvent<String>>() {}
            )
    }
}