package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.CreatePlaylistRequest
import cn.xybbz.api.client.jellyfin.data.PlaylistInfoResponse
import cn.xybbz.api.client.jellyfin.data.PlaylistResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PlaylistsApi : BaseApi {

    @POST("/Playlists")
    suspend fun createPlaylist(@Body createPlaylistRequest: CreatePlaylistRequest): PlaylistResponse

    @POST("/Playlists/{playlistId}/Items")
    suspend fun addItemToPlaylist(
        @Path("playlistId") playlistId: String,
        @Query("ids") ids: String? = null,
        @Query("userId") userId: String? = null,
    )

    @POST("/Playlists/{playlistId}")
    suspend fun updatePlaylist(
        @Path("playlistId") playlistId: String,
        @Body createPlaylistRequest: CreatePlaylistRequest
    )

    @DELETE("/Playlists/{playlistId}/Items")
    suspend fun deletePlaylist(
        @Path("playlistId") playlistId: String,
        @Query("entryIds") entryIds: String
    )

    @GET("/Playlists/{playlistId}")
    suspend fun getPlaylistById(
        @Path("playlistId") playlistId: String
    ): PlaylistInfoResponse

}