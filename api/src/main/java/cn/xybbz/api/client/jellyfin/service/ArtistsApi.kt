package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface ArtistsApi : BaseApi {

    /**
     * 获得艺术家列表
     * @param [itemRequest] 请求信息
     * @return [Response<ItemResponse>]
     */
    @GET("/Artists")
    suspend fun getArtists(
        @QueryMap itemRequest: Map<String, String>?,
    ): Response<ItemResponse>
}