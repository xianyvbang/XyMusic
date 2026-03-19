package cn.xybbz.api.client.subsonic.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.subsonic.data.SubsonicMusicFoldersResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import retrofit2.http.GET

interface SubsonicUserViewsApi : BaseApi {

    @GET("/rest/getMusicFolders")
    suspend fun getMusicFolders(): SubsonicResponse<SubsonicMusicFoldersResponse>
}