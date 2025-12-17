package cn.xybbz.api.okhttp.proxy

import android.util.Log
import cn.xybbz.api.client.data.ProxyConfig
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI

class DynamicProxySelector() : ProxySelector() {

    @Volatile
    var config: ProxyConfig = ProxyConfig()
        private set

    fun update(config: ProxyConfig) {
        this.config = config
    }

    override fun connectFailed(
        uri: URI?,
        sa: SocketAddress?,
        ioe: IOException?
    ) {
        // 可选：失败上报

    }

    override fun select(uri: URI?): List<Proxy> {
        return if (config.enabled) listOf(
            Proxy(
                Proxy.Type.HTTP,
                InetSocketAddress(config.host, config.port!!)
            ),
            Proxy(
                Proxy.Type.SOCKS,
                InetSocketAddress(config.host, config.port!!)
            ),
            Proxy.NO_PROXY
        ) else listOf(Proxy.NO_PROXY)
    }
}