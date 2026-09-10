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

    fun getStream(): Flux<ServerSentEvent<String>> = Flux.defer {
        val hasEmittedEvent = AtomicBoolean(false)

        AppLogger.info(TAG, "getStream")
        testClient.getStream()
            .timeout(upstreamTimeout)
            .doOnNext { hasEmittedEvent.set(true) }
            .doOnError { error ->
                AppLogger.warn(TAG, "upstream stream failed: ${error.javaClass.simpleName}")
            }
            .onErrorResume { error ->
                if (hasEmittedEvent.get()) {
                    Flux.just(errorEvent(error))
                } else {
                    Flux.error(toHttpError(error))
                }
            }
    }

    private fun errorEvent(error: Throwable): ServerSentEvent<String> {
        val code = if (error is TimeoutException) "UPSTREAM_TIMEOUT" else "UPSTREAM_ERROR"
        return ServerSentEvent.builder<String>()
            .event("error")
            .data(code)
            .build()
    }

    private fun toHttpError(error: Throwable): ResponseStatusException = when (error) {
        is ResponseStatusException -> error
        is TimeoutException -> ResponseStatusException(
            HttpStatus.GATEWAY_TIMEOUT,
            "Upstream response timed out",
            error
        )
        is WebClientResponseException -> ResponseStatusException(
            HttpStatus.BAD_GATEWAY,
            "Upstream server returned ${error.statusCode.value()}",
            error
        )
        else -> ResponseStatusException(
            HttpStatus.BAD_GATEWAY,
            "Upstream request failed",
            error
        )
    }
}
