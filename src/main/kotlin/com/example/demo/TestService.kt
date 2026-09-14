package com.example.demo

import com.example.demo.common.AppLogger
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
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

    fun getStream(): Flux<ServerSentEvent<String>> {
        AppLogger.info(TAG, "getStream")
        return testClient.getStream()
            .timeout(upstreamTimeout)
    }
}