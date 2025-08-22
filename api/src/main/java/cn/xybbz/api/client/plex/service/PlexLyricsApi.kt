package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import retrofit2.http.GET
import retrofit2.http.Path

interface PlexLyricsApi : BaseApi {

    @GET("/library/streams/{lrcId}")
    suspend fun getLyrics(@Path("lrcId") lrcId: String): String?
}