package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemRequest
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import cn.xybbz.api.utils.toListMap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters
import io.ktor.util.appendAll

class EmbyArtistsApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 获取专辑列表
     * @param itemRequest [Map<String, String>]
     * @return [Response<ItemResponse>]
     */

    suspend fun getArtists(itemRequest: ItemRequest): Response<ItemResponse> {
        return httpClient.get("/emby/Artists") {
            parameters {
                appendAll(*itemRequest.toListMap())
            }
        }.body<Response<ItemResponse>>()
    }

    /**
     * 相似歌手
     */
    suspend fun getSimilarArtists(
        artistId: String,
        itemRequest: ItemRequest?
    ): Response<ItemResponse> {
        return httpClient.get("/emby/Artists/${artistId}/Similar") {
            parameters {
                appendAll(*itemRequest.toListMap())
            }
        }.body<Response<ItemResponse>>()
    }
}