package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query

interface EmbyLibraryApi : BaseApi {

    @DELETE("/emby/Items/{itemId}")
    suspend fun deleteItem(@Path("itemId") itemId: String)

    @DELETE("/emby/Items")
    suspend fun deleteItems(@Query("ids") ids: String)
}