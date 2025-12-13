package cn.xybbz.api.okhttp

import android.util.Log
import cn.xybbz.api.events.ReLoginEvent
import cn.xybbz.api.events.ReLoginEventBus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class DefaultAuthenticator(
    private val eventBus: ReLoginEventBus,
    private val onLoginRetry: () -> Boolean,
    private val onSetLoginRetry: (Boolean) -> Unit
) : Authenticator {

    // 防止并发重复触发
    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        runBlocking {
            mutex.withLock {
                Log.i("认证失败","认证失败失败")
                if (!onLoginRetry()) {
                    onSetLoginRetry(true)
                    eventBus.notify(ReLoginEvent.Unauthorized)
                }
            }
        }
        // 返回 null 表示不重试请求
        return null
    }
}