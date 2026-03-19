package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface EmbyUserViewsApi : BaseApi {

    /**
     * 获得媒体库列表
     * @param [userId] 用户ID
     * @param [viewRequest] 请求
     * @return [Response<ItemResponse>]
     */
    @GET("/emby/Users/{userId}/Views")
    suspend fun getUserViews(
        @Path("userId") userId: String,
        @QueryMap viewRequest: Map<String, String>
    ): Response<ItemResponse>
}