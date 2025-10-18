package cn.xybbz.api.client.navidrome.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.navidrome.data.NavidromeCreatePlaylistResponse
import cn.xybbz.api.client.navidrome.data.PlaylistAddMusicsUpdateRequest
import cn.xybbz.api.client.navidrome.data.PlaylistAddMusicsUpdateResponse
import cn.xybbz.api.client.navidrome.data.PlaylistItemData
import cn.xybbz.api.client.navidrome.data.PlaylistRemoveMusicsUpdateResponse
import cn.xybbz.api.client.navidrome.data.PlaylistUpdateRequest
import cn.xybbz.api.client.navidrome.data.SongItem
import cn.xybbz.api.client.subsonic.data.SubsonicPlaylistResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NavidromePlaylistsApi : BaseApi {

    @POST("/api/playlist")
    suspend fun createPlaylist(
        @Query("name") name: String? = null,
        @Query("public") public: Boolean = false
    ): NavidromeCreatePlaylistResponse?

    @PUT("/api/playlist/{playlistId}")
    suspend fun updatePlaylist(
        @Path("playlistId") playlistId: String,
        @Body playlistUpdateRequest: PlaylistUpdateRequest
    ): PlaylistItemData

    @DELETE("/api/playlist/{playlistId}")
    suspend fun deletePlaylist(
        @Query("playlistId") playlistId: String
    )

    @GET("/api/playlist")
    suspend fun getPlaylists(
        @Query("_order") order: OrderType = OrderType.ASC,
        @Query("_start") start: Int = 0,
        @Query("_end") end: Int = 0,
        @Query("_sort") sort: SortType = SortType.ID,
    ): Response<List<PlaylistItemData>>

    @GET("/rest/getPlaylist")
    suspend fun getPlaylistById(
        @Query("id") id: String
    ): SubsonicResponse<SubsonicPlaylistResponse>

    @POST("/api/playlist/{playlistId}/tracks")
    suspend fun addPlaylistMusics(
        @Path("playlistId") playlistId: String,
        @Body playlistAddMusicsRequest: PlaylistAddMusicsUpdateRequest
    ): PlaylistAddMusicsUpdateResponse

    @DELETE("/api/playlist/{playlistId}/tracks")
    suspend fun removePlaylistMusics(
        @Path("playlistId") playlistId: String,
        @Query("id") id: List<String>
    ): PlaylistRemoveMusicsUpdateResponse

    @GET("/api/playlist/{playlistId}/tracks")
    suspend fun getPlaylistMusicList(
        @Path("playlistId") playlistId: String,
        @Query("_start") start: Int,
        @Query("_end") end: Int,
        @Query("_order") order: OrderType = OrderType.ASC,
        @Query("_sort") sort: SortType = SortType.TITLE,
        @Query("missing") missing: Boolean? = null,
        @Query("starred") starred: Boolean? = null
    ): Response<List<SongItem>>
}