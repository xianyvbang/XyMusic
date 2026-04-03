package cn.xybbz.download.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

// download module 自己维护一个轻量 IO 作用域，避免再依赖外部模块里的 scoped 基类。
abstract class DownloadIoScoped : AutoCloseable {
    protected lateinit var scope: CoroutineScope

    fun createScope() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    override fun close() {
        if (::scope.isInitialized) {
            scope.cancel()
        }
    }
}
