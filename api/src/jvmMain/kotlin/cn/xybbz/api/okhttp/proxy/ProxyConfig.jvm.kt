package cn.xybbz.api.okhttp.proxy

import io.ktor.client.engine.ProxyConfig
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI

actual fun getProxyConfig(url: String?): ProxyConfig? {
    return url?.let {
        val uri = URI(url)
        Proxy(Proxy.Type.HTTP, InetSocketAddress(uri.host, uri.port))
    }
}