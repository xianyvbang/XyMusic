package cn.xybbz.api.okhttp.proxy

import io.ktor.client.engine.ProxyConfig
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI

actual fun getProxyConfig(url: String?): ProxyConfig? {
    return url?.trim()?.takeIf { it.isNotBlank() }?.let { proxyUrl ->
        val uri = URI(proxyUrl.withProxyScheme())
        val host = uri.host?.takeIf { it.isNotBlank() } ?: return@let null
        val port = uri.port.takeIf { it > 0 } ?: return@let null
        Proxy(Proxy.Type.HTTP, InetSocketAddress(host, port))
    }
}

private fun String.withProxyScheme(): String {
    return if ("://" in this) this else "http://$this"
}
