package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.LyricResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class LyricsApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 获取歌词
     * @param [itemId] 音乐id
     * @return [LyricResponse]
     */
    suspend fun getLyrics(itemId: String): LyricResponse{
        return httpClient.get("/Audio/${itemId}/Lyrics").body()
    }
}