package cn.xybbz.api.okhttp.proxy

import cn.xybbz.api.client.data.ProxyConfig
import java.net.ProxySelector

object ProxyManager {

    private val proxySelector = DynamicProxySelector()

    fun proxySelector(): ProxySelector = proxySelector

    fun updateProxy(config: ProxyConfig) {
        proxySelector.update(config)
        SocksAuthenticator.apply(config)
    }

    fun clearProxy() {
        updateProxy(ProxyConfig())
    }
}
