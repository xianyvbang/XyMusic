package cn.xybbz.common.utils

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

object Log {
    fun i(tag: String, msg: String) {
        logger.info { "[$tag] $msg" }
    }

    fun d(tag: String, msg: String) {
        logger.debug { "[$tag] $msg" }
    }

    fun e(tag: String, msg: String?, e: Throwable) {
        logger.error(e) { "[$tag] $msg" }
    }
}