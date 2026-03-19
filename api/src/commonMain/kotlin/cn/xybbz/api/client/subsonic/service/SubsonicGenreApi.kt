package cn.xybbz.api.client.subsonic.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.subsonic.data.SubsonicGenresResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import retrofit2.http.GET

interface SubsonicGenreApi : BaseApi {

    @GET("/rest/getGenres")
    suspend fun getGenres(): SubsonicResponse<SubsonicGenresResponse>
}