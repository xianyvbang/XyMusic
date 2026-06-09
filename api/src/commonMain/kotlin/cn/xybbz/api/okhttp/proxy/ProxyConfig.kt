package cn.xybbz.api.okhttp.proxy

import io.ktor.client.engine.ProxyConfig

expect fun getProxyConfig(url: String?): ProxyConfig?