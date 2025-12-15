package cn.xybbz.api.okhttp.proxy

import cn.xybbz.api.client.data.ProxyConfig
import cn.xybbz.api.enums.ProxyMode
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI

class DynamicProxySelector : ProxySelector() {

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
        return when (config.mode) {
            ProxyMode.HTTP -> {
                listOf(
                    Proxy(
                        Proxy.Type.HTTP,
                        InetSocketAddress(config.host, config.port!!)
                    )
                )
            }

            ProxyMode.SOCKS -> {
                listOf(
                    Proxy(
                        Proxy.Type.SOCKS,
                        InetSocketAddress(config.host, config.port!!)
                    )
                )
            }

            ProxyMode.NONE -> listOf(Proxy.NO_PROXY)
        }
    }
}