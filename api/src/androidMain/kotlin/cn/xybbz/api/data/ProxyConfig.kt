package cn.xybbz.api.data

data class ProxyConfig(
    val enabled: Boolean = false,
    val host: String? = null,
    val port: Int? = null,
)