package com.example.demo

import com.example.demo.common.AppLogger
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping
class TestController(
    private val testService: TestService
) {

    private val TAG = "TestController"

    @GetMapping("/hello")
    fun testHello(): String {
        AppLogger.info(TAG, "getHello")
        return testService.getHello()
    }

    @GetMapping(
        "/stream",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun getStream(): Mono<ResponseEntity<Flux<ServerSentEvent<String>>>> {
        AppLogger.info(TAG, "getStream")
        return testService.getStream()
    }

    @GetMapping(
        "/json",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getJson(): Mono<ResponseEntity<MockJsonResponse>> {
        AppLogger.info(TAG, "getJson")
        return testService.getJson()
    }

}
