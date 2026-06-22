package cn.xybbz.api.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 数据源登录并发协调器。
 */
internal class DataSourceLoginCoordinator {
    /**
     * 登录串行锁，统一保护手动登录、切换连接、启动自动登录和重登录。
     */
    private val loginMutex = Mutex()

    /**
     * 自动重登录抢占锁，避免 onResume 和 401 事件同时拉起多个后台重登录任务。
     */
    private val automaticReloginMutex = Mutex()

    /**
     * 串行执行一次手动登录或连接切换。
     */
    suspend fun runManualLogin(block: suspend () -> Unit) {
        loginMutex.withLock {
            block()
        }
    }

    /**
     * 尝试启动一次自动重登录，已有登录或重登录任务时直接跳过。
     */
    fun tryLaunchAutomaticRelogin(
        scope: CoroutineScope,
        block: suspend () -> Unit
    ): Job? {
        if (!automaticReloginMutex.tryLock()) {
            return null
        }
        var automaticReloginReleased = false

        /**
         * 释放自动重登录抢占锁，兼容协程未真正启动就被取消的情况。
         */
        fun releaseAutomaticReloginLock() {
            if (!automaticReloginReleased) {
                automaticReloginReleased = true
                automaticReloginMutex.unlock()
            }
        }

        val job = scope.launch {
            try {
                if (!loginMutex.tryLock()) {
                    return@launch
                }
                try {
                    block()
                } finally {
                    loginMutex.unlock()
                }
            } finally {
                releaseAutomaticReloginLock()
            }
        }
        job.invokeOnCompletion {
            releaseAutomaticReloginLock()
        }
        return job
    }
}
