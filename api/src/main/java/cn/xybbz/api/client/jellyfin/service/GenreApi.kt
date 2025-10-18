package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface GenreApi : BaseApi {

    @GET("/Genres")
    suspend fun getGenres(@QueryMap itemRequest: Map<String, String>): Response<ItemResponse>
}