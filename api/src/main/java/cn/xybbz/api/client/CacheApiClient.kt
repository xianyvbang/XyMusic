package cn.xybbz.api.client

import okhttp3.OkHttpClient

class CacheApiClient : DefaultApiClient() {


    var okhttpClientFunction: (() -> OkHttpClient) = { apiOkHttpClient }

    /**
     * 清空数据
     */
    override fun release() {

    }

    init {
        getOkHttpClient()
    }

    override fun updateTokenHeaderName() {
    }

    override fun updateIfSubsonic() {
    }
}