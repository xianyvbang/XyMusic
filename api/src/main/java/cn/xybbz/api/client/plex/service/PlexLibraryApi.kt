package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import retrofit2.http.DELETE
import retrofit2.http.Path

interface PlexLibraryApi : BaseApi {

    @DELETE("/library/metadata/{itemId}")
    suspend fun deleteItem(@Path("itemId") itemId: String)
}