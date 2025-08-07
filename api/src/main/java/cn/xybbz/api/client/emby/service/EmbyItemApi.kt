package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.PlaybackStartInfo
import cn.xybbz.api.client.jellyfin.data.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

/**
 * 音乐,专辑,艺术家相关接口
 */
interface EmbyItemApi : BaseApi {

    @GET("/emby/Users/{userId}/Items")
    suspend fun getUserItems(
        @Path("userId") userId: String, @QueryMap itemRequest: Map<String, String>
    ): Response<ItemResponse>


}