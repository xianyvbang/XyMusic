package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.FavoriteResponse
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface EmbyUserLibraryApi : BaseApi {
    /**
     * 标记最喜欢项目
     * @param [itemId] 项目ID
     * @return [FavoriteResponse]
     */
    @POST("/emby/Users/{userId}/FavoriteItems/{itemId}")
    suspend fun markFavoriteItem(
        @Path("userId") userId: String,
        @Path("itemId") itemId: String
    ): FavoriteResponse

    /**
     * 取消标记喜欢物品
     * @param [itemId] 项目ID
     * @return [FavoriteResponse]
     */
    @DELETE("/emby/Users/{userId}/FavoriteItems/{itemId}")
    suspend fun unmarkFavoriteItem(
        @Path("userId") userId: String,
        @Path("itemId") itemId: String
    ): FavoriteResponse


    @GET("/emby/Users/{userId}/Items/{itemId}")
    suspend fun getItem(
        @Path("userId") userId: String,
        @Path("itemId") itemId: String
    ): ItemResponse

    @GET("/emby/Users/{userId}/Items/Latest")
    suspend fun getLatestMedia(
        @Path("userId") userId: String,
        @QueryMap itemRequest: Map<String, String>
    ): List<ItemResponse>
}