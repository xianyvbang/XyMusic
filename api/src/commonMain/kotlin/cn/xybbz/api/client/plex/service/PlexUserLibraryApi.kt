package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.FavoriteResponse
import cn.xybbz.api.client.plex.data.ItemInfoResponse
import cn.xybbz.api.client.plex.data.PlexLibraryItemResponse
import cn.xybbz.api.client.plex.data.PlexResponse
import cn.xybbz.api.enums.plex.PlexSortOrder
import cn.xybbz.api.enums.plex.PlexSortType
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PlexUserLibraryApi : BaseApi {
    /**
     * 增加收藏合集
     * @return [FavoriteResponse]
     */
    @POST("/library/collections")
    suspend fun addCollection(
        @Query("title") title: String,
        @Query("type") type: Int? = 10,
        @Query("smart") smart: String? = "0",
        @Query("sectionId") sectionId: String? = null,
        @Query("uri") uri: String
    ): PlexResponse<ItemInfoResponse>

    /**
     * 获得收藏合集
     */
    @GET("/library/sections/{sectionKey}/collections")
    suspend fun getCollection(
        @Path("sectionKey") sectionKey: String,
        @Query("subtype") subtype: Int? = 10,
        @Query("smart") smart: String? = "0",
        @Query("sectionId") sectionId: String? = null,
        @Query("sort") sort: String = "${PlexSortType.UPDATED_AT}:${PlexSortOrder.DESCENDING}",
        @Query("limit") limit: Int = 1,
        @Query("title") title: String
    ): PlexResponse<ItemInfoResponse>


    /**
     * 标记最喜欢项目
     * @return [FavoriteResponse]
     */
    @PUT("/library/collections/{collectionId}/items")
    suspend fun markFavoriteItem(
        @Path("collectionId") collectionId: String,
        @Query("type") type: Int? = 10,
        @Query("uri") uri: String
    ): PlexResponse<ItemInfoResponse>

    /**
     * 取消标记喜欢物品
     * @return [FavoriteResponse]
     */
    @DELETE("/library/collections/{collectionId}/children/{musicId}")
    suspend fun unmarkFavoriteItem(
        @Path("collectionId") collectionId: String,
        @Path("musicId") musicId: String,
        @Query("excludeAllLeaves") excludeAllLeaves: Int? = 1
    ): PlexResponse<ItemInfoResponse>


    @GET("/library/collections/{sectionKey}/children")
    suspend fun getFavoriteSongs(
        @Path("sectionKey") sectionKey: String,
        @Query("excludeAllLeaves") excludeAllLeaves: Int = 1,
        @Query("includeCollections") includeCollections: Int = 1,
        @Query("includeMeta") includeMeta: Int = 1,
        @Query("includeExternalMedia") includeExternalMedia: Int = 1,
        @Query("X-Plex-Container-Start") start: Int,
        @Query("X-Plex-Container-Size") pageSize: Int
    ): PlexResponse<PlexLibraryItemResponse>

    @GET("/library/metadata/{ids}/similarsimilar")
    suspend fun similarItem(
        @Path("ids") ids: String,
        @Query("count") count: Int
    ): PlexResponse<PlexLibraryItemResponse>

}