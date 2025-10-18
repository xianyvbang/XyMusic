package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.LyricResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface LyricsApi : BaseApi {

    @GET("/Audio/{itemId}/Lyrics")
    suspend fun getLyrics(@Path("itemId") itemId: String): LyricResponse
}