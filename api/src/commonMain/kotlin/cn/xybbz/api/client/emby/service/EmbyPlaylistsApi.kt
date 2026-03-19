package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.CreatePlaylistRequest
import cn.xybbz.api.client.jellyfin.data.PlaylistResponse
import cn.xybbz.api.enums.jellyfin.MediaType
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EmbyPlaylistsApi : BaseApi {

    /**
     * 创建歌单
     * @param [name] 歌单名称
     * @param [ids] 音乐ids
     * @param [mediaType] 媒体类型
     * @return [PlaylistResponse]
     */
    @POST("/emby/Playlists")
    suspend fun createPlaylist(
        @Query("Name") name: String,
        @Query("Ids") ids: String? = null,
        @Query("MediaType") mediaType: MediaType? = null
    ): PlaylistResponse

    /**
     * 将音乐添加到歌单
     * @param [playlistId] 歌单id
     * @param [ids] 音乐ids
     * @param [userId] 用户ID
     */
    @POST("/emby/Playlists/{playlistId}/Items")
    suspend fun addItemToPlaylist(
        @Path("playlistId") playlistId: String,
        @Query("ids") ids: String? = null,
        @Query("userId") userId: String? = null,
    )

    /**
     * 更新歌单
     * @param [playlistId] 歌单Id
     * @param [createPlaylistRequest] 歌单信息请求实体类
     */
    @POST("/emby/items/{playlistId}")
    suspend fun updatePlaylist(
        @Path("playlistId") playlistId: String,
        @Body createPlaylistRequest: CreatePlaylistRequest
    )

    /**
     * 删除歌单内音乐
     * @param [playlistId] 歌单id
     * @param [entryIds] 音乐ids
     */
    @DELETE("/emby/Playlists/{playlistId}/Items")
    suspend fun deletePlaylist(
        @Path("playlistId") playlistId: String,
        @Query("entryIds") entryIds: String
    )

}