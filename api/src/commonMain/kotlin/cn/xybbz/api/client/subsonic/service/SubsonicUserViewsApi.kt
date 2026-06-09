package cn.xybbz.api.client.subsonic.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.subsonic.data.SubsonicMusicFoldersResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class SubsonicUserViewsApi(private val httpClient: HttpClient) : BaseApi {

    suspend fun getMusicFolders(): SubsonicResponse<SubsonicMusicFoldersResponse>{
        return httpClient.get("/rest/getMusicFolders").body()
    }
}