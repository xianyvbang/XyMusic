package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.FavoriteResponse
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface UserLibraryApi : BaseApi {
    /**
     * 标记最喜欢项目
     * @param [itemId] 项目ID
     * @return [FavoriteResponse]
     */
    @POST("/UserFavoriteItems/{itemId}")
    suspend fun markFavoriteItem(@Path("itemId") itemId: String): FavoriteResponse

    /**
     * 取消标记喜欢物品
     * @param [itemId] 项目ID
     * @return [FavoriteResponse]
     */
    @DELETE("/UserFavoriteItems/{itemId}")
    suspend fun unmarkFavoriteItem(@Path("itemId") itemId: String): FavoriteResponse


    @GET("/Items/{itemId}")
    suspend fun getItem(@Path("itemId") itemId: String): ItemResponse

    @GET("/Items/Latest")
    suspend fun getLatestMedia(@QueryMap itemRequest: Map<String, String>): List<ItemResponse>
}