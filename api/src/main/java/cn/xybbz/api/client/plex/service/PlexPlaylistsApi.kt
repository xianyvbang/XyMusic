package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.navidrome.data.PlaylistItemData
import cn.xybbz.api.client.navidrome.data.PlaylistRemoveMusicsUpdateResponse
import cn.xybbz.api.client.plex.data.PlexLibraryItemResponse
import cn.xybbz.api.client.plex.data.PlexPlaylistResponse
import cn.xybbz.api.client.plex.data.PlexResponse
import cn.xybbz.api.enums.plex.PlexPlaylistType
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface PlexPlaylistsApi : BaseApi {

    @POST("/playlists")
    suspend fun createPlaylist(
        @Query("title") title: String,
        @Query("type") type: String = PlexPlaylistType.AUDIO.toString(),
        @Query("public") smart: Int = 1,
    ): PlexResponse<PlexPlaylistResponse>

    @PUT("/playlists/{playlistId}")
    suspend fun updatePlaylist(
        @Path("playlistId") playlistId: String,
        @Query("title") title: String
    ): PlaylistItemData

    @DELETE("/playlists/{playlistId}")
    suspend fun deletePlaylist(
        @Query("playlistId") playlistId: String
    )

    @GET("/playlists")
    suspend fun getPlaylists(
        @Query("plexPlaylistType") order: PlexPlaylistType = PlexPlaylistType.AUDIO,
        @Query("smart") smart: Int? = null,
        @Query("X-Plex-Container-Start") start: Int,
        @Query("X-Plex-Container-Size") pageSize: Int
    ): PlexResponse<PlexPlaylistResponse>

    @GET("/playlists/{playlistId}")
    suspend fun getPlaylistById(
        @Query("playlistId") id: String
    ): PlexResponse<PlexPlaylistResponse>

    @PUT("/playlists/{playlistId}/items")
    suspend fun addPlaylistMusics(
        @Path("playlistId") playlistId: String,
        @Query("uri") uri: String
    ): PlexResponse<PlexPlaylistResponse>

    @DELETE("/playlists/{playlistId}/items/{itemId}")
    suspend fun removePlaylistMusics(
        @Path("playlistId") playlistId: String,
        @Path("itemId") itemId: String
    ): PlaylistRemoveMusicsUpdateResponse

    @GET("/playlists/{playlistId}/items")
    suspend fun getPlaylistMusicList(
        @Path("playlistId") playlistId: String,
        @Query("type") type: Int? = 10,
        @Query("X-Plex-Container-Start") start: Int,
        @Query("X-Plex-Container-Size") pageSize: Int,
        @Query("artist.id") artistId: String? = null,
        @Query("album.id") albumId: String? = null,
        @Query("sort") sort: String? = null,
        @Query("track.collection") trackCollection: Int? = null,
        @Query("album.decade", encoded = false) albumDecade: String? = null,
        @QueryMap params: Map<String, String>? = null
    ): PlexResponse<PlexLibraryItemResponse>
}