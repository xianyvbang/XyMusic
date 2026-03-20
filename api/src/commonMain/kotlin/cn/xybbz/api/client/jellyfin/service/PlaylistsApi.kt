package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.CreatePlaylistRequest
import cn.xybbz.api.client.jellyfin.data.PlaylistResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.parameters

class PlaylistsApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 创建歌单
     * @param [createPlaylistRequest] 创建播放列表请求
     * @return [PlaylistResponse]
     */
    suspend fun createPlaylist(createPlaylistRequest: CreatePlaylistRequest): PlaylistResponse {
        return httpClient.post("/Playlists") {
            postBlock { setBody(createPlaylistRequest) }
        }.body()
    }

    /**
     * 将音乐添加到歌单
     * @param [playlistId] 歌单id
     * @param [ids] 音乐ids
     * @param [userId] 用户ID
     */
    suspend fun addItemToPlaylist(
        playlistId: String,
        ids: String? = null,
        userId: String? = null,
    ) {
        httpClient.post("/Playlists/${playlistId}/Items") {
            parameters {
                append("ids", ids)
                append("userId", userId)
            }
        }
    }

    /**
     * 更新歌单
     * @param [playlistId] 歌单Id
     * @param [createPlaylistRequest] 歌单信息请求实体类
     */
    suspend fun updatePlaylist(
        playlistId: String,
        createPlaylistRequest: CreatePlaylistRequest
    ) {
        httpClient.post("/Playlists/${playlistId}") {
            postBlock { setBody(createPlaylistRequest) }
        }
    }

    /**
     * 删除歌单内音乐
     * @param [playlistId] 歌单id
     * @param [entryIds] 音乐ids
     */
    suspend fun deletePlaylist(
        playlistId: String,
        entryIds: String
    ) {
        httpClient.delete("/Playlists/${playlistId}/Items") {
            parameters {
                append("entryIds", entryIds)
            }
        }
    }

}