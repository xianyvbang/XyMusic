package cn.xybbz.api.okhttp.proxy

import cn.xybbz.api.client.data.ProxyConfig
import cn.xybbz.api.enums.ProxyMode
import java.net.ProxySelector

object ProxyManager {

    private val proxySelector = DynamicProxySelector()

    fun proxySelector(): ProxySelector = proxySelector

    fun updateProxy(config: ProxyConfig) {
        proxySelector.update(config)

        when (config.mode) {
            ProxyMode.SOCKS -> SocksAuthenticator.apply(config)
            else -> SocksAuthenticator.clear()
        }
    }

    fun clearProxy() {
        updateProxy(ProxyConfig())
    }
}
