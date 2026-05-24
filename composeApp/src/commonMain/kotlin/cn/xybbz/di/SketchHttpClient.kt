package cn.xybbz.di

import cn.xybbz.api.client.DataSourceManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRedirect

internal fun createSketchHttpClient(dataSourceManager: DataSourceManager): HttpClient {
    return dataSourceManager.getHttpClient().config {
        expectSuccess = false
        followRedirects = true
        install(HttpRedirect) {
            // Some cover endpoints redirect from an HTTPS API URL to a plain HTTP image URL.
            allowHttpsDowngrade = true
        }
    }
}
