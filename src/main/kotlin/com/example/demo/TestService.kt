package com.example.demo

import com.example.demo.common.AppLogger
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

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

    fun getStream() : Flux<DataBuffer> {
        AppLogger.info(TAG, "getStream")
        return testClient.getStream()
    }

}