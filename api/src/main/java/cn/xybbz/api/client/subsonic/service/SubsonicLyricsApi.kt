package cn.xybbz.api.client.subsonic.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.LyricResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SubsonicLyricsApi : BaseApi {

    @GET("/rest/getLyrics")
    suspend fun getLyrics(@Query("artist") artist: String? = null,
                          @Query("title")title:String?? = null): SubsonicResponse<Any>
}