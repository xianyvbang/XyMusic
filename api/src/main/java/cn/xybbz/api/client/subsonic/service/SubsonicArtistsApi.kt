package cn.xybbz.api.client.subsonic.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.subsonic.data.SubsonicArtistResponse
import cn.xybbz.api.client.subsonic.data.SubsonicArtistsResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SubsonicArtistsApi : BaseApi{
    @GET("/rest/getArtists")
    suspend fun getArtists(
        @Query("musicFolderId") musicFolderId: String?,
    ): SubsonicResponse<SubsonicArtistsResponse>

    @GET("/rest/getArtist")
    suspend fun getArtist(@Query("id") id: String): SubsonicResponse<SubsonicArtistResponse>
}