/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.api.client.navidrome

import android.util.Log
import cn.xybbz.api.TokenServer
import cn.xybbz.api.TokenServer.baseUrl
import cn.xybbz.api.client.DefaultParentApiClient
import cn.xybbz.api.client.data.ClientLoginInfoReq
import cn.xybbz.api.client.data.LoginSuccessData
import cn.xybbz.api.client.navidrome.data.toNavidromeLogin
import cn.xybbz.api.client.navidrome.service.NavidromeArtistsApi
import cn.xybbz.api.client.navidrome.service.NavidromeGenreApi
import cn.xybbz.api.client.navidrome.service.NavidromeItemApi
import cn.xybbz.api.client.navidrome.service.NavidromePlaylistsApi
import cn.xybbz.api.client.navidrome.service.NavidromeUserApi
import cn.xybbz.api.client.navidrome.service.NavidromeUserLibraryApi
import cn.xybbz.api.client.navidrome.service.NavidromeUserViewsApi
import cn.xybbz.api.client.subsonic.SubsonicApiClient
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.enums.AudioCodecEnum
import cn.xybbz.api.enums.subsonic.ResponseFormatType

class NavidromeApiClient : DefaultParentApiClient() {

    /**
     * subsonic的加密盐
     */
    private var subsonicSalt: String = ""

    /**
     * subsonic的Token
     */
    private var subsonicToken: String = ""

    /**
     * subsonic的版本
     */
    private var subsonicProtocolVersion: String = ""

    /**
     * subsonic的客户端名称
     */
    private var subsonicClientName: String = ""

    /**
     * 用户名
     */
    private var username: String = ""

    /**
     * 用户id
     */
    private var id: String = ""

    /**
     * 登陆返回token
     */
    private var accessToken: String? = null

    /**
     * subsonic的返回格式，可选值: xml, json, jsonp
     */
    private var subsonicResponseFormat: String = ResponseFormatType.JSON.serialName

    override val tokenHeaderName: String = ApiConstants.NAVIDROME_AUTHORIZATION

    private lateinit var navidromeUserApi: NavidromeUserApi
    private lateinit var navidromeItemApi: NavidromeItemApi
    private lateinit var navidromePlaylistsApi: NavidromePlaylistsApi
    private lateinit var navidromeArtistsApi: NavidromeArtistsApi
    private lateinit var navidromeGenreApi: NavidromeGenreApi
    private lateinit var navidromeUserLibraryApi: NavidromeUserLibraryApi
    private lateinit var navidromeUserViewsApi: NavidromeUserViewsApi


    /**
     * 创建API客户端
     * @param [username] 用户名
     * @param [passwordMd5] md5(密码+盐)计算出的身份验证令牌
     * @param [encryptedSalt] 密码加密盐(随机生成,最少6位)
     * @param [protocolVersion] 客户端实现的协议版本
     * @param [clientName] 客户端名称
     * @param [responseFormat] 返回格式，可选值: xml, json, jsonp
     * @param [token] token
     * @return [SubsonicApiClient]
     */
    fun createSubsonicApiClient(
        username: String,
        passwordMd5: String,
        encryptedSalt: String,
        protocolVersion: String,
        clientName: String,
        responseFormat: String = ResponseFormatType.JSON.serialName,
        token: String,
        id: String
    ) {
        this.username = username
        this.subsonicToken = passwordMd5
        this.subsonicSalt = encryptedSalt
        this.subsonicProtocolVersion = protocolVersion
        this.subsonicClientName = clientName
        this.subsonicResponseFormat = responseFormat
        this.accessToken = token
        this.id = id
    }

    /**
     * 获得token
     */
    public override fun createToken(): String {
        return "Bearer " + this.accessToken
    }

    /**
     * 获得校验参数组成的Map
     */
    public override fun getQueryMapData(): Map<String, String> {
        return mapOf(
            "u" to username,
            "t" to subsonicToken,
            "s" to subsonicSalt,
            "v" to subsonicProtocolVersion,
            "c" to subsonicClientName,
            "f" to subsonicResponseFormat,
        )
    }

    /**
     * 获得请求头Map
     */
    public override fun getHeadersMapData(): Map<String, String> {
        return mapOf(ApiConstants.NAVIDROME_HEADER to id)
    }

    /**
     * 获得用户接口服务
     */
    override fun userApi(restart: Boolean): NavidromeUserApi {
        if (!this::navidromeUserApi.isInitialized || restart) {
            navidromeUserApi = instance().create(NavidromeUserApi::class.java)
        }
        return navidromeUserApi
    }

    /**
     * 音乐,专辑,艺术家相关接口
     */
    override fun itemApi(restart: Boolean): NavidromeItemApi {
        if (!this::navidromeItemApi.isInitialized || restart) {
            navidromeItemApi = instance().create(NavidromeItemApi::class.java)
        }
        return navidromeItemApi
    }


    /**
     * 播放列表接口
     */
    override fun playlistsApi(restart: Boolean): NavidromePlaylistsApi {
        if (!this::navidromePlaylistsApi.isInitialized || restart) {
            navidromePlaylistsApi = instance().create(NavidromePlaylistsApi::class.java)
        }
        return navidromePlaylistsApi
    }

    /**
     * 艺术家接口
     */
    override fun artistsApi(restart: Boolean): NavidromeArtistsApi {
        if (!this::navidromeArtistsApi.isInitialized || restart) {
            navidromeArtistsApi = instance().create(NavidromeArtistsApi::class.java)
        }
        return navidromeArtistsApi
    }

    /**
     * 流派接口
     */
    override fun genreApi(restart: Boolean): NavidromeGenreApi {
        if (!this::navidromeGenreApi.isInitialized || restart) {
            navidromeGenreApi = instance().create(NavidromeGenreApi::class.java)
        }
        return navidromeGenreApi
    }

    /**
     * 创建下载链接
     */
    override fun createDownloadUrl(itemId: String): String {
        return baseUrl + "/rest/download?id=${itemId}&format=raw&bitrate=0"
    }

    /**
     * 登陆接口
     */
    override suspend fun login(clientLoginInfoReq: ClientLoginInfoReq): LoginSuccessData {
        val responseData = userApi().login(clientLoginInfoReq.toNavidromeLogin())
        Log.i("=====", "返回响应值: $responseData")
        loginAfter(
            accessToken = responseData.token,
            userId = responseData.id,
            subsonicToken = responseData.subsonicToken,
            subsonicSalt = responseData.subsonicSalt,
            clientLoginInfoReq = clientLoginInfoReq
        )
        val systemInfo = userApi().postPingSystem()
        val user = userApi().getUser(username)
        Log.i("=====", "服务器信息 $systemInfo 用户信息 $user")
        TokenServer.updateLoginRetry(false)
        val serverVersion = systemInfo.subsonicResponse.serverVersion
        return LoginSuccessData(
            userId = responseData.id,
            accessToken = responseData.token,
            serverId = "",
            serverName = systemInfo.subsonicResponse.type,
            version = serverVersion.ifBlank { systemInfo.subsonicResponse.version },
            navidromeExtendToken = responseData.subsonicToken,
            navidromeExtendSalt = responseData.subsonicSalt,
            ifEnabledDownload = user.subsonicResponse.user?.downloadRole ?: false,
            ifEnabledDelete = user.subsonicResponse.user?.adminRole ?: false
        )
    }

    override suspend fun loginAfter(
        accessToken: String?,
        userId: String?,
        subsonicToken: String?,
        subsonicSalt: String?,
        clientLoginInfoReq: ClientLoginInfoReq
    ) {
        if (!subsonicToken.isNullOrBlank() && !subsonicSalt.isNullOrBlank()
            && !accessToken.isNullOrBlank() && !userId.isNullOrBlank()
        )
            createSubsonicApiClient(
                username = clientLoginInfoReq.username,
                passwordMd5 = subsonicToken,
                encryptedSalt = subsonicSalt,
                protocolVersion = clientLoginInfoReq.clientVersion,
                clientName = clientLoginInfoReq.appName,
                token = accessToken,
                id = userId
            )
        updateTokenOrHeadersOrQuery()
    }

    /**
     *用户资源接口服务
     */
    override fun userLibraryApi(restart: Boolean): NavidromeUserLibraryApi {
        if (!this::navidromeUserLibraryApi.isInitialized || restart) {
            navidromeUserLibraryApi = instance().create(NavidromeUserLibraryApi::class.java)
        }
        return navidromeUserLibraryApi
    }

    /**
     * 用户视图信息
     */
    override fun userViewsApi(restart: Boolean): NavidromeUserViewsApi {
        if (!this::navidromeUserViewsApi.isInitialized || restart) {
            navidromeUserViewsApi = instance().create(NavidromeUserViewsApi::class.java)
        }
        return navidromeUserViewsApi
    }

    /**
     * 获取图像URL
     * @param [imageId] 图像ID
     * @param [size] 尺寸
     */
    fun getImageUrl(imageId: String, size: Int? = null): String {
        return if (size == null) {
            "/rest/getCoverArt?id=${imageId}"
        } else {
            "/rest/getCoverArt?id=${imageId}&size=${size}"
        }
    }

    /**
     * 获得音频url
     */
    fun createAudioUrl(
        musicId: String,
        format: AudioCodecEnum? = AudioCodecEnum.ROW,
        maxBitRate: Int? = null
    ): String {
        return "${baseUrl}/rest/stream?id=${musicId}&maxBitRate=${maxBitRate}&format=${format}${if (format != AudioCodecEnum.ROW) "&estimateContentLength=true" else ""}"
    }


    /**
     * 清空数据
     */
    override fun release() {
        createSubsonicApiClient("", "", "", "", "", "", "", "")
        token = ""
    }
}