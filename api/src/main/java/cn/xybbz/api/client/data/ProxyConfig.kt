package cn.xybbz.api.client.data

import cn.xybbz.api.enums.ProxyMode

data class ProxyConfig(
    val mode: ProxyMode = ProxyMode.NONE,
    val host: String? = null,
    val port: Int? = null,
    val username: String? = null,
    val password: String? = null
)