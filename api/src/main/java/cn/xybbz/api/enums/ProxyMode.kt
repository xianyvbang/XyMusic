package cn.xybbz.api.enums

enum class ProxyMode {
    NONE,
    HTTP,
    SOCKS;

    companion object{
        fun getProxyMode(modeStr: String): ProxyMode {
            return ProxyMode.entries.find { proxyMode -> proxyMode.name == modeStr } ?: NONE
        }
    }
}