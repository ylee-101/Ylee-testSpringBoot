package com.example.demo

import com.example.demo.common.AppLogger
import org.springframework.stereotype.Repository

@Repository
class TestRepository {

    private val TAG = "TestRepository"

    fun getHello(): String {
        AppLogger.info(TAG, "getHello")
        return "test hello hi how are you"
    }
}