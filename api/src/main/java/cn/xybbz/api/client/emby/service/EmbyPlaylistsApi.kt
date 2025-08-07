package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.CreatePlaylistRequest
import cn.xybbz.api.client.jellyfin.data.PlaylistInfoResponse
import cn.xybbz.api.client.jellyfin.data.PlaylistResponse
import cn.xybbz.api.enums.jellyfin.MediaType
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EmbyPlaylistsApi : BaseApi {

    @POST("/emby/Playlists")
    suspend fun createPlaylist(
        @Query("Name") name: String,
        @Query("Ids") ids: String? = null,
        @Query("MediaType") mediaType: MediaType? = null
    ): PlaylistResponse

    @POST("/emby/Playlists/{playlistId}/Items")
    suspend fun addItemToPlaylist(
        @Path("playlistId") playlistId: String,
        @Query("ids") ids: String? = null,
        @Query("userId") userId: String? = null,
    )

    @POST("/emby/items/{playlistId}")
    suspend fun updatePlaylist(
        @Path("playlistId") playlistId: String,
        @Body createPlaylistRequest: CreatePlaylistRequest
    )

    @DELETE("/emby/Playlists/{playlistId}/Items")
    suspend fun deletePlaylist(
        @Path("playlistId") playlistId: String,
        @Query("entryIds") entryIds: String
    )

    @GET("/Playlists/{playlistId}")
    suspend fun getPlaylistById(
        @Path("playlistId") playlistId: String
    ): PlaylistInfoResponse

}