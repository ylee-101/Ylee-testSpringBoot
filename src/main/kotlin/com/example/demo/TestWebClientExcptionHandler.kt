package com.example.demo

import com.example.demo.common.AppLogger
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.function.client.WebClientResponseException

@RestControllerAdvice
class TestExcptionHandler {

    private val TAG = "TestWebClientExcptionHandler"

    // cliend에서 4xx, 5xx 응답이 retrieve 된 경우 여기로 에러 처리 들어옴
    @ExceptionHandler(ExternalApiHttpException::class)
    fun handleError(e: ExternalApiHttpException): ResponseEntity<String> {
        AppLogger.error(TAG, "${e.apiId.name} error with ${e.cause}")
        return ResponseEntity
            .status(e.status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(e.body)
    }
}