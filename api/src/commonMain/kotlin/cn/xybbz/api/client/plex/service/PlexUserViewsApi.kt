package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.plex.data.PlexLibrary
import cn.xybbz.api.client.plex.data.PlexResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class PlexUserViewsApi(private val httpClient: HttpClient) : BaseApi {

    suspend fun getUserViews(): PlexResponse<PlexLibrary>{
        return httpClient.get("/library/sections").body()
    }
}