package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.LyricResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface EmbyLyricsApi : BaseApi {

    @GET("/emby/Audio/{itemId}/Lyrics")
    suspend fun getLyrics(@Path("itemId") itemId: String): LyricResponse
}