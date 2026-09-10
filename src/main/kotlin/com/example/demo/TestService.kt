package com.example.demo

import com.example.demo.common.AppLogger
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.concurrent.TimeoutException

@Service
class TestService(
    private val testRepository: TestRepository,
    private val testClient : TestClient
) {

    private val TAG = "TestService"

    fun getHello(): String {
        AppLogger.info(TAG, "getHello")
        return testRepository.getHello()
    }

    fun getStream() : Flux<ServerSentEvent<String>> {
        AppLogger.info(TAG, "getStream")
        return testClient.getStream()
            .timeout(Duration.ofSeconds(15L))
            .onErrorResume(TimeoutException::class.java) {
                Flux.just(
                    ServerSentEvent.builder<String>()
                        .event("error")
                        .data("15초동안 데이터가 들어오지않음")
                        .build()
                )
            }
    }
}