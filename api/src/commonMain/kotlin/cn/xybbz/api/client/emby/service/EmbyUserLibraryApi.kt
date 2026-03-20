package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.FavoriteResponse
import cn.xybbz.api.client.jellyfin.data.ItemRequest
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.utils.toListMap
import cn.xybbz.api.utils.toStringMap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.parameters
import io.ktor.util.appendAll

class EmbyUserLibraryApi(private val httpClient: HttpClient) : BaseApi {
    /**
     * 标记最喜欢项目
     * @param [itemId] 数据id
     * @return [FavoriteResponse]
     */
    suspend fun markFavoriteItem(
        userId: String,
        itemId: String
    ): FavoriteResponse {
        return httpClient.post("/emby/Users/$userId/FavoriteItems/$itemId") {}.body()
    }

    /**
     * 取消标记喜欢物品
     * @param [itemId] 数据id
     * @return [FavoriteResponse]
     */
    suspend fun unmarkFavoriteItem(
        userId: String,
        itemId: String
    ): FavoriteResponse {
        return httpClient.delete("/emby/Users/$userId/FavoriteItems/$itemId") {}.body()
    }


    /**
     * 获取详情
     * @param [userId] 用户ID
     * @param [itemId] 数据id
     * @return [ItemResponse]
     */
    suspend fun getItem(
        userId: String,
        itemId: String
    ): ItemResponse {
        return httpClient.get("/emby/Users/$userId/Items/$itemId") {}.body()
    }

    /**
     * 获取最新专辑
     * @param [userId] 用户ID
     * @param [itemRequest] 物品请求
     * @return [List<ItemResponse>]
     */
    suspend fun getLatestMedia(
        userId: String,
        itemRequest: ItemRequest
    ): List<ItemResponse> {
        return httpClient.get("/emby/Users/$userId/Items/Latest") {
            parameters {
                appendAll(*itemRequest.toListMap())
            }
        }.body()
    }
}