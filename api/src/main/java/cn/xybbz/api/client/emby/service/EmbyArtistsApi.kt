package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface EmbyArtistsApi : BaseApi {

    @GET("/emby/Artists")
    suspend fun getArtists(
        @QueryMap itemRequest: Map<String, String>?,
    ): Response<ItemResponse>
}