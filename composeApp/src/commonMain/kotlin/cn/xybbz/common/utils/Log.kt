package cn.xybbz.common.utils

import io.github.oshai.kotlinlogging.KotlinLogging



object Log {
    private val logger = KotlinLogging.logger {}
    fun i(tag: String, msg: String) {
        logger.info { "[$tag] $msg" }
    }

    fun d(tag: String, msg: String) {
        logger.debug { "[$tag] $msg" }
    }

    fun e(tag: String, msg: String?) {
        logger.error { "[$tag] $msg" }
    }

    fun e(tag: String, msg: String?, e: Throwable) {
        logger.error(e) { "[$tag] $msg" }
    }
}
