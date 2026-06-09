package cn.xybbz.api.client.subsonic.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.subsonic.data.SubsonicGenresResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class SubsonicGenreApi(private val httpClient: HttpClient) : BaseApi {

    suspend fun getGenres(): SubsonicResponse<SubsonicGenresResponse>{
        return httpClient.get("/rest/getGenres").body()
    }
}