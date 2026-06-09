package cn.xybbz.api.okhttp.proxy

import io.ktor.client.engine.ProxyConfig
import kotlin.concurrent.Volatile

object ProxyManager {


    @Volatile
    var config: ProxyConfig? = null
        private set

    fun proxySelector(): ProxyConfig? = config


    fun update(url: String?) {
        this.config = getProxyConfig(url)
    }

    fun updateProxy(url: String?) {
        update(url)
    }

    fun clearProxy() {
        updateProxy(null)
    }
}
