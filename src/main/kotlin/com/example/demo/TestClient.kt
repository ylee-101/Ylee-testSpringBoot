package com.example.demo

import com.example.demo.common.AppLogger
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import javax.xml.crypto.Data

@Component
class TestClient(
    private val webClient : WebClient = WebClient.builder().build()
) {
    private val TAG = "TestClient"

    fun getStream(): Flux<DataBuffer> {
        val baseUrl = "http://localhost:9090"
        AppLogger.info(TAG, "getStream")
        return webClient.get()
            .uri(baseUrl+"/mock/stream?scenario=slow-response")
            .exchangeToFlux{ response ->
                response.bodyToFlux(DataBuffer::class.java)
            }
    }
}