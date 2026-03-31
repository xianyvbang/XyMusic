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

class EmbyGenreApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 获取genre列表
     * @param itemRequest [Map<String, String>]
     * @return [Response<ItemResponse>]
     */
    suspend fun getGenres(itemRequest: ItemRequest): Response<ItemResponse> {
        return httpClient.get("/emby/Genres") {
            parametersXy {
                appendAll(*itemRequest.toListMap())
            }
        }.body<Response<ItemResponse>>()
    }

    /**
     * 获取genre详情
     * @param userId [String] 用户id
     * @param itemId [String] genreId
     * @return [ItemResponse]
     */
    suspend fun getItem(
        userId: String,
        itemId: String
    ): ItemResponse {
        return httpClient.get("/emby/Users/${userId}/Items/${itemId}").body()
    }
}