package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import cn.xybbz.api.client.jellyfin.data.ViewRequest
import cn.xybbz.api.utils.toListMap
import cn.xybbz.api.utils.toStringMap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters
import io.ktor.util.appendAll

class EmbyUserViewsApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 获得媒体库列表
     * @param [userId] 用户ID
     * @param [viewRequest] 请求
     * @return [Response<ItemResponse>]
     */
    suspend fun getUserViews(
        userId: String,
        viewRequest: ViewRequest
    ): Response<ItemResponse> {
        return httpClient.get("/emby/Users/$userId/Views") {
            parameters {
                appendAll(*viewRequest.toListMap())
            }
        }.body()
    }
}