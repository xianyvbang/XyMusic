package cn.xybbz.api.okhttp.proxy

import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.http

actual fun getProxyConfig(url: String?): ProxyConfig? {
    return url?.let { ProxyBuilder.http(it) }
}