package com.example.demo

import com.example.demo.common.AppLogger
import com.example.demo.config.ExternalApiProperties
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.TimeoutException

@Service
class TestService(
    private val testRepository: TestRepository,
    private val sseApiClient : SseApiClient,
    private val externalApiProperties: ExternalApiProperties
) {

    private val TAG = "TestService"
    private val upstreamTimeout = externalApiProperties.ssePort.responseTimeout

    fun getHello(): String {
        AppLogger.info(TAG, "getHello")
        return testRepository.getHello()
    }

    fun getStream(): Mono<ResponseEntity<Flux<ServerSentEvent<String>>>> {
        AppLogger.info(TAG, "getStream")
        return sseApiClient.getStream()
            .map { upstream ->
                val body = requireNotNull(upstream.body) {
                    "Upstream stream response body is missing"
                }
                ResponseEntity.status(upstream.statusCode)
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(
                        body.timeout(upstreamTimeout)
                            .onErrorResume(TimeoutException::class.java) {
                                Flux.just(
                                    ServerSentEvent.builder<String>()
                                        .event("done with error")
                                        .data("""{"error": "응답 시간이 초과되었습니다"}""")
                                        .build()
                                )
                            }
                    )
            }
    }
}
