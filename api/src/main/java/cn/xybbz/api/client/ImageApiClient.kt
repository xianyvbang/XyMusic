package cn.xybbz.api.client

import okhttp3.OkHttpClient

class ImageApiClient : DefaultApiClient() {


    var okhttpClientFunction: (() -> OkHttpClient) = { apiOkHttpClient }

    /**
     * 清空数据
     */
    override fun release() {

    }
    init {
        getOkHttpClient()
    }

    override fun updateIfSubsonic() {

    }

    override fun updateTokenHeaderName() {
    }
}