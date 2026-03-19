package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query

interface LibraryApi : BaseApi {

    /**
     * 删除数据
     * @param [itemId] 数据id
     */
    @DELETE("/Items/{itemId}")
    suspend fun deleteItem(@Path("itemId") itemId: String)

    /**
     * 批量删除项目
     * @param [ids] 数据id
     */
    @DELETE("/Items")
    suspend fun deleteItems(@Query("ids") ids: String)
}