package com.example.demo.common

import org.slf4j.LoggerFactory

object AppLogger {
    private val logger = LoggerFactory.getLogger("app")

    fun info(tag: String, msg: String) {
        logger.info("[$tag] $msg")
    }

    fun warn(tag: String, msg: String) {
        logger.warn("[$tag] $msg")
    }

    fun error(tag: String, msg: String) {
        logger.error("[$tag] $msg")
    }

    fun debug(tag: String, msg: String) {
        logger.debug("[$tag] $msg")
    }
}