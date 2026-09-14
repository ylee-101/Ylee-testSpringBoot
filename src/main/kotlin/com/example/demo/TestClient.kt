package com.example.demo

import com.example.demo.common.AppLogger
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class TestClient {
    private val TAG = "TestClient"

    private val testWebClient : WebClient = WebClient.builder()
        .baseUrl("http://localhost:9090")
        .build()

    fun getStream(): Mono<ResponseEntity<Flux<ServerSentEvent<String>>>> {
        AppLogger.info(TAG, "requesting upstream stream")

        return testWebClient.get()
            .uri("/mock/stream?scenario=accepted-queue")
            .retrieve()
            .toEntityFlux(
                object : ParameterizedTypeReference<ServerSentEvent<String>>() {}
            )
    }
}
