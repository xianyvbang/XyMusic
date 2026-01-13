package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface EmbyArtistsApi : BaseApi {

    /**
     * 获取专辑列表
     * @param itemRequest [Map<String, String>]
     * @return [Response<ItemResponse>]
     */
    @GET("/emby/Artists")
    suspend fun getArtists(
        @QueryMap itemRequest: Map<String, String>?,
    ): Response<ItemResponse>

    /**
     * 相似歌手
     */
    @GET("/emby/Artists/{artistId}/Similar")
    suspend fun getSimilarArtists(
        @Path("artistId") artistId: String,
        @QueryMap itemRequest: Map<String, String>?
    ): Response<ItemResponse>
}