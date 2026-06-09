package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.AuthenticateResponse
import cn.xybbz.api.client.jellyfin.data.LoginRequest
import cn.xybbz.api.client.jellyfin.data.PlaybackStartInfo
import cn.xybbz.api.client.jellyfin.data.SystemInfoResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class EmbyUserApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 按用户名和密码进行身份验证
     * @param [LoginRequest]
     * @return [AuthenticateResponse]
     */
    suspend fun authenticateByName(loginRequest: LoginRequest): AuthenticateResponse {
        return httpClient.post("/emby/Users/AuthenticateByName") {
            postBlock { setBody(loginRequest) }
        }.body()
    }


    /**
     * POST PING系统
     * @return [String]
     */
    suspend fun postPingSystem(): String {
        return httpClient.post("/emby/System/Ping") {
        }.body()
    }

    /**
     * 获得服务端信息
     */
    suspend fun getSystemInfo(): SystemInfoResponse {
        return httpClient.get("/emby/System/Info").body()
    }

    /**
     * 上报正在播放音乐
     * @param [data] 数据
     */
    suspend fun playing(data: PlaybackStartInfo) {
        httpClient.post("/emby/Sessions/Playing") {
            postBlock { setBody(data) }
        }
    }

    /**
     * 上报播放进度
     * @param [data] 数据
     */
    suspend fun progress(data: PlaybackStartInfo) {
        httpClient.post("/emby/Sessions/Playing/Progress") {
            postBlock { setBody(data) }
        }
    }

    /**
     * 上报停止播放
     */
    suspend fun stopped(data: PlaybackStartInfo) {
        httpClient.post("/emby/Sessions/Playing/Stopped") {
            postBlock { setBody(data) }
        }
    }
}