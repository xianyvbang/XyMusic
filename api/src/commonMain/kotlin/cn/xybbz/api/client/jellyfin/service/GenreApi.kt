package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemRequest
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import cn.xybbz.api.utils.toListMap
import cn.xybbz.api.utils.toStringMap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters
import io.ktor.util.appendAll

class GenreApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 获取流派
     * @param [itemRequest] 请求信息
     * @return [Response<ItemResponse>]
     */
    suspend fun getGenres(itemRequest: ItemRequest): Response<ItemResponse>{
        return httpClient.get("Genres"){
            parametersXy {
                appendAll(*itemRequest.toListMap())
            }
        }.body()
    }
}