package cn.xybbz.api.client.subsonic.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.subsonic.data.SubsonicParentResponse
import cn.xybbz.api.client.subsonic.data.SubsonicPlaylistResponse
import cn.xybbz.api.client.subsonic.data.SubsonicPlaylistsResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SubsonicPlaylistsApi : BaseApi {

    @GET("/rest/createPlaylist")
    suspend fun createPlaylist(
        @Query("playlistId") playlistId: String? = null,
        @Query("name") name: String? = null,
        @Query("songId") songId: List<String>? = null
    ): SubsonicResponse<SubsonicPlaylistResponse>

    @GET("/rest/updatePlaylist")
    suspend fun updatePlaylist(
        @Query("playlistId") playlistId: String,
        @Query("name") name: String? = null,
        @Query("comment") comment: String? = null,
        @Query("public") public: Boolean? = false,
        @Query("songIdToAdd") songIdToAdd: List<String>? = null,
        @Query("songIndexToRemove") songIndexToRemove: List<String>? = null
    ): SubsonicResponse<SubsonicParentResponse>

    @GET("/rest/deletePlaylist")
    suspend fun deletePlaylist(
        @Query("id") id: String
    ): SubsonicResponse<SubsonicParentResponse>

    @GET("/rest/getPlaylists")
    suspend fun getPlaylists(@Query("username") username: String): SubsonicResponse<SubsonicPlaylistsResponse>

    @GET("/rest/getPlaylist")
    suspend fun getPlaylistById(
        @Query("id") id: String
    ): SubsonicResponse<SubsonicPlaylistResponse>

}