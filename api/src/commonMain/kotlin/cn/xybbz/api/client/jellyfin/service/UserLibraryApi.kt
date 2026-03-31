package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.FavoriteResponse
import cn.xybbz.api.client.jellyfin.data.ItemRequest
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.utils.toListMap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.http.parameters
import io.ktor.util.appendAll

class UserLibraryApi(private val httpClient: HttpClient) : BaseApi {
    /**
     * 标记最喜欢项目
     * @param [itemId] 项目ID
     * @return [FavoriteResponse]
     */
    suspend fun markFavoriteItem(itemId: String): FavoriteResponse{
        return httpClient.put("/UserFavoriteItems/${itemId}").body()
    }

    /**
     * 取消标记喜欢物品
     * @param [itemId] 项目ID
     * @return [FavoriteResponse]
     */
    suspend fun unmarkFavoriteItem(itemId: String): FavoriteResponse{
        return httpClient.delete("/UserFavoriteItems/${itemId}").body()
    }


    /**
     * 获取详细信息
     * @param [itemId] 商品编号
     * @return [ItemResponse]
     */
    suspend fun getItem(itemId: String): ItemResponse{
        return httpClient.get("/Items/${itemId}").body()
    }

    /**
     * 获取最新音乐列表
     * @param [itemRequest] 物品请求
     * @return [List<ItemResponse>]
     */
    suspend fun getLatestMedia(itemRequest: ItemRequest): List<ItemResponse>{
        return httpClient.get("/Items/Latest"){
            parametersXy {
                appendAll(*itemRequest.toListMap())
            }
        }.body()
    }
}