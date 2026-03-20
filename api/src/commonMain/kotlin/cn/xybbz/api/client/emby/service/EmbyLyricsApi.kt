package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.LyricResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class EmbyLyricsApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 获取歌词
     * @param [itemId] 音乐id
     * @return [LyricResponse]
     */
    suspend fun getLyrics(itemId: String): LyricResponse{
        return httpClient.get("/emby/Audio/$itemId/Lyrics").body()
    }
}