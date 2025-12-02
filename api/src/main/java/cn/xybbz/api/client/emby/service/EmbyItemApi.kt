package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

/**
 * 音乐,专辑,艺术家相关接口
 */
interface EmbyItemApi : BaseApi {

    /**
     * 获得音乐列表
     * @param userId 用户id
     * @param itemRequest 请求参数
     * @return [Response<ItemResponse>]
     */
    @GET("/emby/Users/{userId}/Items")
    suspend fun getUserItems(
        @Path("userId") userId: String, @QueryMap itemRequest: Map<String, String>
    ): Response<ItemResponse>


}