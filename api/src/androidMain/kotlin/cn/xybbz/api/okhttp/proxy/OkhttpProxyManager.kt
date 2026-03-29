package cn.xybbz.api.okhttp.proxy

import java.net.ProxySelector

object OkhttpProxyManager {

    private val proxySelector = DynamicProxySelectorAndroid()

    fun proxySelector(): ProxySelector = proxySelector
}
