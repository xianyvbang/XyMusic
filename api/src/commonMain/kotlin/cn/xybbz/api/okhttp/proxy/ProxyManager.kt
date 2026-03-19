package cn.xybbz.api.okhttp.proxy

import io.ktor.client.engine.ProxyConfig

object ProxyManager {

    private val proxySelector = DynamicProxySelector()

    fun proxySelector(): DynamicProxySelector = proxySelector

    fun updateProxy(url: String?) {
        proxySelector.update(url)
    }

    fun clearProxy() {
        updateProxy(null)
    }
}
