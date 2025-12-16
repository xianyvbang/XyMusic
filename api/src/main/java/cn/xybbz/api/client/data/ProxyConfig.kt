package cn.xybbz.api.client.data

data class ProxyConfig(
    val enabled: Boolean = false,
    val host: String? = null,
    val port: Int? = null,
    val username: String? = null,
    val password: String? = null
)