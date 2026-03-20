package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.plex.data.PlexLibrary
import cn.xybbz.api.client.plex.data.PlexResponse
import io.ktor.client.HttpClient
import retrofit2.http.GET

class PlexUserViewsApi(private val httpClient: HttpClient) : BaseApi {

    @GET("/library/sections")
    suspend fun getUserViews(): PlexResponse<PlexLibrary>
}