package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

/**
 * 音乐,专辑,艺术家相关接口
 */
interface ItemApi : BaseApi {

    @GET("/Items")
    suspend fun getItems(@QueryMap itemRequest: Map<String, String>): Response<ItemResponse>


}