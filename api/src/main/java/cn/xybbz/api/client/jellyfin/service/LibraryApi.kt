package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query

interface LibraryApi : BaseApi {

    @DELETE("/Items/{itemId}")
    suspend fun deleteItem(@Path("itemId") itemId: String)

    @DELETE("/Items")
    suspend fun deleteItems(@Query("ids") ids: String)
}