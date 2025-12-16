package cn.xybbz.api.okhttp.proxy

import cn.xybbz.api.client.data.ProxyConfig
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI

class DynamicProxySelector(private var ifTemp: Boolean = false) : ProxySelector() {

    @Volatile
    var config: ProxyConfig = ProxyConfig()
        private set

    fun update(config: ProxyConfig) {
        this.config = config
    }

    fun updateTemp(ifTemp: Boolean) {
        this.ifTemp = ifTemp
    }


    override fun connectFailed(
        uri: URI?,
        sa: SocketAddress?,
        ioe: IOException?
    ) {
        // 可选：失败上报

    }

    override fun select(uri: URI?): List<Proxy> {
        return if (config.enabled || ifTemp) listOf(
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