package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface EmbyGenreApi : BaseApi {

    @GET("/emby/Genres")
    suspend fun getGenres(@QueryMap itemRequest: Map<String, String>): Response<ItemResponse>

    @GET("/emby/Users/{userId}/Items/{itemId}")
    suspend fun getItem(@Path("userId")userId: String, @Path("itemId") itemId: String): ItemResponse
}