package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.CreatePlaylistRequest
import cn.xybbz.api.client.jellyfin.data.PlaylistResponse
import cn.xybbz.api.enums.jellyfin.MediaType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.parameters

class EmbyPlaylistsApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 创建歌单
     * @param [name] 歌单名称
     * @param [ids] 音乐ids
     * @param [mediaType] 媒体类型
     * @return [PlaylistResponse]
     */
    suspend fun createPlaylist(
        name: String,
        ids: String? = null,
        mediaType: MediaType? = null
    ): PlaylistResponse {
        return httpClient.post("/emby/Playlists") {
            parameters {
                append("Name", name)
                append("Ids", ids)
                mediaType?.let { append("MediaType", it.serialName) }
            }
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
        httpClient.post("/emby/Playlists/$playlistId/Items") {
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
        httpClient.post("/emby/items/$playlistId") {
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
        httpClient.delete("/emby/Playlists/$playlistId/Items") {
            parameters {
                append("entryIds", entryIds)
            }
        }
    }

}