package cn.xybbz.api.okhttp.plex

import okhttp3.Interceptor
import okhttp3.Response

class PlexQueryInterceptor() : Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val newUrl = request.url.toString()
            .replace("%3E", ">")
            .replace("%3C", "<")
            .replace("%3D", "=")
            .trimEnd('=')

        request = request.newBuilder()
            .url(newUrl)
            .build()
        return chain.proceed(request)
    }
}