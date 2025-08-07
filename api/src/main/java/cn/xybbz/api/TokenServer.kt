package cn.xybbz.api

object TokenServer {

    var token: String = ""
        private set

    var queryMap: Map<String, String> = emptyMap()
        private set

    var headerMap: Map<String, String> = emptyMap()
        private set

    var baseUrl: String = ""
        private set

    fun setTokenData(token: String) {
        TokenServer.token = token
    }

    /**
     * 设置通用额外参数
     */
    fun setQueryMapData(queryMap: Map<String, String>) {
        TokenServer.queryMap = queryMap
    }

    /**
     * 设置通用额外请求头
     */
    fun setHeaderMapData(headerMap: Map<String, String>) {
        TokenServer.headerMap = headerMap
    }

    /**
     * 清空所有数据
     */
    fun clearAllData() {
        setTokenData("")
        setQueryMapData(emptyMap())
        setHeaderMapData(emptyMap())
    }

    fun setBaseUrlData(baseUrl: String) {
        TokenServer.baseUrl = baseUrl
    }
}