package cn.xybbz.api.client.navidrome.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.FavoriteResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NavidromeUserLibraryApi : BaseApi {
    /**
     * 标记最喜欢项目
     * @return [FavoriteResponse]
     */
    @GET("/rest/star")
    suspend fun markFavoriteItem(
        @Query("id") id: List<String>? = null,
        @Query("albumId") albumId: List<String>? = null,
        @Query("artistId") artistId: List<String>? = null
    ): FavoriteResponse

    /**
     * 取消标记喜欢物品
     * @return [FavoriteResponse]
     */
    @GET("/rest/unstar")
    suspend fun unmarkFavoriteItem(
        @Query("id") id: List<String>? = null,
        @Query("albumId") albumId: List<String>? = null,
        @Query("artistId") artistId: List<String>? = null
    ): FavoriteResponse

}