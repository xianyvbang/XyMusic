package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import cn.xybbz.api.client.jellyfin.data.ViewRequest
import cn.xybbz.api.utils.toListMap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters
import io.ktor.util.appendAll

class UserViewsApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 获取媒体库
     * @param [viewRequest] 查看请求
     * @return [Response<ItemResponse>]
     */
    suspend fun getUserViews(
        viewRequest: ViewRequest
    ): Response<ItemResponse>{
        return httpClient.get("/UserViews"){
            parameters {
                appendAll(*viewRequest.toListMap())
            }
        }.body()
    }
}