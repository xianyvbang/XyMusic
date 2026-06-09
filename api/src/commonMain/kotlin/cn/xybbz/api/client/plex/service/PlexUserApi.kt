package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.AuthenticateResponse
import cn.xybbz.api.client.plex.data.PlexLoginRequest
import cn.xybbz.api.client.plex.data.PlexLoginResponse
import cn.xybbz.api.client.plex.data.PlexPingSystemResponse
import cn.xybbz.api.client.plex.data.PlexSystemInfoResponse
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.enums.plex.PlayState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.parameters

class PlexUserApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 按名称进行身份验证
     * @param [PlexLoginRequest]
     * @return [AuthenticateResponse]
     */
    suspend fun authenticateByName(
        fullUrl: String,
        loginRequest: PlexLoginRequest
    ): PlexLoginResponse {
        return httpClient.post(fullUrl) {
            postBlock { setBody(loginRequest) }
        }.body()
    }


    /**
     * POST PING系统
     * @return [String]
     */
    suspend fun postPingSystem(): PlexPingSystemResponse {
        return httpClient.get("/").body()
    }

    /**
     * 获得服务端信息
     */
    suspend fun getSystemInfo(fullUrl: String): List<PlexSystemInfoResponse> {
        return httpClient.get(fullUrl).body()
    }


    /**
     * 上报正在播放音乐
     */
    suspend fun playing(
        ratingKey: String,
        time: Long,
        state: PlayState
    ) {
        httpClient.get("/:/timeline") {
            parametersXy {
                append(name = "ratingKey", value = ratingKey)
                append(name = "time", value = time.toString())
                append(name = "state", value = state.serialName)
            }
        }
    }

    /**
     * 标记已播放
     */
    suspend fun markAsPlayed(
        key: String,
        identifier: String = ApiConstants.PLEX_PRODUCT_NAME
    ) {
        httpClient.get("/:/scrobble") {
            parametersXy {
                append(name = "key", value = key)
                append(name = "identifier", value = identifier)
            }
        }
    }
}