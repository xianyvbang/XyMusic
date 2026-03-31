package cn.xybbz.api.client.subsonic.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.subsonic.data.ScrobbleRequest
import cn.xybbz.api.client.subsonic.data.SubsonicDefaultResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.client.subsonic.data.SubsonicUserResponse
import cn.xybbz.api.utils.toListMap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.parameters
import io.ktor.util.appendAll

/**
 * 用户相关请求实体类
 */
class SubsonicUserApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * POST PING系统
     * @return [String]
     */
    suspend fun postPingSystem(): SubsonicResponse<SubsonicDefaultResponse> {
        return httpClient.post("/rest/ping").body()
    }

    /**
     * 获取用户信息
     */
    suspend fun getUser(username: String): SubsonicResponse<SubsonicUserResponse> {
        return httpClient.get("/rest/getUser") {
            parametersXy {
                append("username", username)
            }
        }.body()
    }

    /**
     * 上报播放记录
     */
    suspend fun scrobble(scrobbleRequest: ScrobbleRequest): SubsonicResponse<SubsonicDefaultResponse> {
        return httpClient.get("/rest/scrobble") {
            parametersXy {
                appendAll(*scrobbleRequest.toListMap())
            }
        }.body()
    }

}