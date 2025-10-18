package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.plex.data.PlexLibrary
import cn.xybbz.api.client.plex.data.PlexResponse
import retrofit2.http.GET

interface PlexUserViewsApi : BaseApi {

    @GET("/library/sections")
    suspend fun getUserViews(): PlexResponse<PlexLibrary>
}