package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class PlexLyricsApi(private val httpClient: HttpClient) : BaseApi {

    suspend fun getLyrics(lrcId: String): String? {
        return httpClient.get("/library/streams/${lrcId}").body()
    }
}