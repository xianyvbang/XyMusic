package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface EmbyGenreApi : BaseApi {

    /**
     * 获取genre列表
     * @param itemRequest [Map<String, String>]
     * @return [Response<ItemResponse>]
     */
    @GET("/emby/Genres")
    suspend fun getGenres(@QueryMap itemRequest: Map<String, String>): Response<ItemResponse>

    /**
     * 获取genre详情
     * @param userId [String] 用户id
     * @param itemId [String] genreId
     * @return [ItemResponse]
     */
    @GET("/emby/Users/{userId}/Items/{itemId}")
    suspend fun getItem(
        @Path("userId") userId: String,
        @Path("itemId") itemId: String
    ): ItemResponse
}