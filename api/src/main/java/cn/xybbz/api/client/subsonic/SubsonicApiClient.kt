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

package cn.xybbz.api.client.subsonic

import android.util.Log
import cn.xybbz.api.TokenServer
import cn.xybbz.api.client.DefaultParentApiClient
import cn.xybbz.api.client.data.ClientLoginInfoReq
import cn.xybbz.api.client.data.LoginSuccessData
import cn.xybbz.api.client.subsonic.data.SubsonicDefaultResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.client.subsonic.service.SubsonicArtistsApi
import cn.xybbz.api.client.subsonic.service.SubsonicGenreApi
import cn.xybbz.api.client.subsonic.service.SubsonicItemApi
import cn.xybbz.api.client.subsonic.service.SubsonicLyricsApi
import cn.xybbz.api.client.subsonic.service.SubsonicPlaylistsApi
import cn.xybbz.api.client.subsonic.service.SubsonicUserApi
import cn.xybbz.api.client.subsonic.service.SubsonicUserLibraryApi
import cn.xybbz.api.client.subsonic.service.SubsonicUserViewsApi
import cn.xybbz.api.enums.AudioCodecEnum
import cn.xybbz.api.enums.subsonic.ResponseFormatType
import cn.xybbz.api.exception.ConnectionException
import cn.xybbz.api.exception.UnauthorizedException

class SubsonicApiClient : DefaultParentApiClient() {

    /**
     * 用户名
     */
    var username: String = ""
        private set

    /**
     * md5(密码+盐)计算出的身份验证令牌
     */
    private var passwordMd5: String = ""

    /**
     * 密码加密盐(随机生成,最少6位)
     */
    private var encryptedSalt: String = ""

    /**
     * 客户端实现的协议版本
     */
    var protocolVersion: String = ""
        private set

    /**
     * 客户端名称
     */
    private var clientName: String = ""

    /**
     * 返回格式，可选值: xml, json, jsonp
     */
    private var responseFormat: String = ResponseFormatType.JSON.serialName

    /**
     * 是否为Subsonic
     */
    override val ifSubsonic: Boolean
        get() = true
    private lateinit var subsonicArtistsApi: SubsonicArtistsApi
    private lateinit var subsonicUserApi: SubsonicUserApi
    private lateinit var subsonicItemApi: SubsonicItemApi
    private lateinit var subsonicPlaylistsApi: SubsonicPlaylistsApi
    private lateinit var subsonicUserViewsApi: SubsonicUserViewsApi
    private lateinit var subsonicGenreApi: SubsonicGenreApi
    private lateinit var subsonicUserLibraryApi: SubsonicUserLibraryApi

    private lateinit var subsonicLyricsApi: SubsonicLyricsApi


    /**
     * 创建API客户端
     * @param [username] 用户名
     * @param [passwordMd5] md5(密码+盐)计算出的身份验证令牌
     * @param [encryptedSalt] 密码加密盐(随机生成,最少6位)
     * @param [protocolVersion] 客户端实现的协议版本
     * @param [clientName] 客户端名称
     * @param [responseFormat] 返回格式，可选值: xml, json, jsonp
     * @return [SubsonicApiClient]
     */
    fun createApiClient(
        username: String,
        passwordMd5: String,
        encryptedSalt: String,
        protocolVersion: String,
        clientName: String,
        responseFormat: String = ResponseFormatType.JSON.serialName
    ): SubsonicApiClient {
        this.username = username
        this.passwordMd5 = passwordMd5
        this.encryptedSalt = encryptedSalt
        this.protocolVersion = protocolVersion
        this.clientName = clientName
        this.responseFormat = responseFormat
        return this
    }

    /**
     * 艺术家接口
     */
    override fun artistsApi(restart: Boolean): SubsonicArtistsApi {
        if (!this::subsonicArtistsApi.isInitialized || restart) {
            subsonicArtistsApi = instance().create(SubsonicArtistsApi::class.java)
        }
        return subsonicArtistsApi
    }

    /**
     * 获得用户接口服务
     */
    override fun userApi(restart: Boolean): SubsonicUserApi {
        if (!this::subsonicUserApi.isInitialized || restart) {
            subsonicUserApi = instance().create(SubsonicUserApi::class.java)
        }
        return subsonicUserApi
    }

    /**
     * 音乐,专辑,艺术家相关接口
     */
    override fun itemApi(restart: Boolean): SubsonicItemApi {
        if (!this::subsonicItemApi.isInitialized || restart) {
            subsonicItemApi = instance().create(SubsonicItemApi::class.java)
        }
        return subsonicItemApi
    }

    /**
     * 播放列表接口
     */
    override fun playlistsApi(restart: Boolean): SubsonicPlaylistsApi {
        if (!this::subsonicPlaylistsApi.isInitialized || restart) {
            subsonicPlaylistsApi = instance().create(SubsonicPlaylistsApi::class.java)
        }
        return subsonicPlaylistsApi
    }

    /**
     * 用户视图信息
     */
    override fun userViewsApi(restart: Boolean): SubsonicUserViewsApi {
        if (!this::subsonicUserViewsApi.isInitialized || restart) {
            subsonicUserViewsApi = instance().create(SubsonicUserViewsApi::class.java)
        }
        return subsonicUserViewsApi
    }

    /**
     * 流派接口
     */
    override fun genreApi(restart: Boolean): SubsonicGenreApi {
        if (!this::subsonicGenreApi.isInitialized || restart) {
            subsonicGenreApi = instance().create(SubsonicGenreApi::class.java)
        }
        return subsonicGenreApi
    }

    /**
     *用户资源接口服务
     */
    override fun userLibraryApi(restart: Boolean): SubsonicUserLibraryApi {
        if (!this::subsonicUserLibraryApi.isInitialized || restart) {
            subsonicUserLibraryApi = instance().create(SubsonicUserLibraryApi::class.java)
        }
        return subsonicUserLibraryApi
    }

    /**
     * 歌词接口
     */
    override fun lyricsApi(restart: Boolean): SubsonicLyricsApi {
        if (!this::subsonicLyricsApi.isInitialized || restart) {
            subsonicLyricsApi = instance().create(SubsonicLyricsApi::class.java)
        }
        return subsonicLyricsApi
    }


    /**
     * 获得校验参数组成的Map
     */
    public override fun getQueryMapData(): Map<String, String> {
        return mapOf(
            "u" to username,
            "t" to passwordMd5,
            "s" to encryptedSalt,
            "v" to protocolVersion,
            "c" to clientName,
            "f" to responseFormat,
        )
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
        return "/rest/stream?id=${musicId}&maxBitRate=${maxBitRate}&format=${format}${if (format != AudioCodecEnum.ROW) "&estimateContentLength=true" else ""}"
    }

    /**
     * 清空数据
     */
    override fun release() {

    }

    /**
     * 创建下载链接
     */
    override fun createDownloadUrl(itemId: String): String {
        return "/rest/download?id=${itemId}"
    }

    /**
     * 登陆接口
     */
    override suspend fun login(clientLoginInfoReq: ClientLoginInfoReq): LoginSuccessData {
        val systemInfo = try {
            ping()
        }catch (e: Exception){
            e.printStackTrace()
            when (e) {
                !is UnauthorizedException -> {
                    throw ConnectionException()
                }
                else -> throw e
            }
        }
        val user = userApi().getUser(username)
        Log.i("=====", "服务器信息 $systemInfo 用户信息 $user")
        TokenServer.updateLoginRetry(false)
        return LoginSuccessData(
            userId = clientLoginInfoReq.username,
            accessToken = clientLoginInfoReq.username,
            serverId = "",
            serverName = systemInfo.subsonicResponse.type,
            version = systemInfo.subsonicResponse.version,
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

    }

    override suspend fun ping(): SubsonicResponse<SubsonicDefaultResponse> {
        return userApi().postPingSystem()
    }
}