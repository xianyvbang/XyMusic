package cn.xybbz.download.core

import io.ktor.client.HttpClient

interface HttpClientFactory {
    fun createHttpClient(): HttpClient

    /**
     * 释放 createHttpClient 返回的客户端。
     *
     * 默认不关闭，因为当前下载器借用的是数据源层共享 HttpClient；
     * 如果以后某个实现为每个下载创建独立客户端，需要在实现里覆盖并 close。
     */
    fun releaseHttpClient(client: HttpClient) = Unit
}
