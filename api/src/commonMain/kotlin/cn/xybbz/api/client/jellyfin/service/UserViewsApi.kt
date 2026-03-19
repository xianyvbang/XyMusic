package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface UserViewsApi : BaseApi {

    /**
     * 获取媒体库
     * @param [viewRequest] 查看请求
     * @return [Response<ItemResponse>]
     */
    @GET("/UserViews")
    suspend fun getUserViews(
        @QueryMap viewRequest: Map<String, String>
    ): Response<ItemResponse>
}