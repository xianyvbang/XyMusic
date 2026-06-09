package cn.xybbz.api.base

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpStatement

/**
 * 下载方法的基础API
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[IDownLoadApi]
 */
open class IDownLoadApi(private val httpClient: HttpClient) : BaseApi {

    suspend fun downloadFile(
        fileUrl: String,
        range: String, /*= "bytes=0-"*/
    ): HttpStatement {
        return httpClient.prepareGet(fileUrl) {
            header("Range", range)
        }
    }
}