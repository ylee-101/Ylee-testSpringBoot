package com.example.demo

import com.example.demo.common.AppLogger
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean

@Service
class TestService(
    private val testRepository: TestRepository,
    private val testClient : TestClient
) {

    private val TAG = "TestService"
    private val upstreamTimeout = Duration.ofSeconds(15)

    fun getHello(): String {
        AppLogger.info(TAG, "getHello")
        return testRepository.getHello()
    }

    fun getStream(): Mono<ResponseEntity<Flux<ServerSentEvent<String>>>> {
        AppLogger.info(TAG, "getStream")
        return testClient.getStream()
            .map { upstream ->
                val body = requireNotNull(upstream.body) {
                    "Upstream stream response body is missing"
                }
                ResponseEntity.status(upstream.statusCode)
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(body.timeout(upstreamTimeout))
            }
    }
}
