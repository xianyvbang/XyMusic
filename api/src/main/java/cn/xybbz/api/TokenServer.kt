package cn.xybbz.api

import cn.xybbz.api.constants.ApiConstants

object TokenServer {

    var token: String = ""
        private set

    var baseUrl: String = ""
        private set

    var queryMap: Map<String, String> = emptyMap()
        private set

    var headerMap: Map<String, String> = emptyMap()
        private set

    var tokenHeaderName = ApiConstants.AUTHORIZATION
        private set

    /**
     * 是否完成登陆重试
     */
    var loginRetry = false
        private set

    /**
     * 是否为Subsonic
     */
    var ifSubsonic = false
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

    /**
     * 更新Token的请求头名称
     */
    fun updateTokenHeaderName(tokenHeaderName: String) {
        this.tokenHeaderName = tokenHeaderName
    }

    fun updateIfSubsonic(ifSubsonic: Boolean) {
        this.ifSubsonic = ifSubsonic
    }

    fun updateLoginRetry(loginRetry: Boolean) {
        this.loginRetry = loginRetry
    }

    fun updateBaseUrl(baseUrl: String) {
        this.baseUrl = baseUrl
    }
}