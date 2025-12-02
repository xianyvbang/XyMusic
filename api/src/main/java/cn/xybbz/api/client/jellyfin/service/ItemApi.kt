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

    /**
     * 获取音频列表
     * @param [itemRequest] 物品请求
     * @return [Response<ItemResponse>]
     */
    @GET("/Items")
    suspend fun getItems(@QueryMap itemRequest: Map<String, String>): Response<ItemResponse>


}