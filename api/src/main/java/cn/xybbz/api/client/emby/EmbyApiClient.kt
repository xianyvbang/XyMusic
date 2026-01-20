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

package cn.xybbz.api.client.emby

import android.util.Log
import cn.xybbz.api.TokenServer
import cn.xybbz.api.client.DefaultParentApiClient
import cn.xybbz.api.client.data.ClientLoginInfoReq
import cn.xybbz.api.client.data.LoginSuccessData
import cn.xybbz.api.client.emby.service.EmbyArtistsApi
import cn.xybbz.api.client.emby.service.EmbyGenreApi
import cn.xybbz.api.client.emby.service.EmbyItemApi
import cn.xybbz.api.client.emby.service.EmbyLibraryApi
import cn.xybbz.api.client.emby.service.EmbyLyricsApi
import cn.xybbz.api.client.emby.service.EmbyPlaylistsApi
import cn.xybbz.api.client.emby.service.EmbyUserApi
import cn.xybbz.api.client.emby.service.EmbyUserLibraryApi
import cn.xybbz.api.client.emby.service.EmbyUserViewsApi
import cn.xybbz.api.client.jellyfin.buildParameter
import cn.xybbz.api.client.jellyfin.data.toLogin
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.enums.AudioCodecEnum
import cn.xybbz.api.enums.jellyfin.ImageType
import cn.xybbz.api.exception.ConnectionException
import cn.xybbz.api.exception.UnauthorizedException

/**
 * EMBY API 客户端
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[EmbyApiClient]
 */
class EmbyApiClient : DefaultParentApiClient() {

    /**
     * 客户端名称
     */
    private var clientName: String = ""

    /**
     * 客户端版本
     */
    private var clientVersion: String = ""

    /**
     * 设备id
     */
    var deviceId: String = ""
        private set

    /**
     * 设备名称
     */
    private var deviceName: String = ""

    /**
     * api请求令牌
     */
    private var accessToken: String? = null

    /**
     * 用户id
     */
    private var userId: String? = null

    private lateinit var embyUserApi: EmbyUserApi
    private lateinit var embyPlaylistsApi: EmbyPlaylistsApi
    private lateinit var embyArtistsApi: EmbyArtistsApi
    private lateinit var embyItemApi: EmbyItemApi
    private lateinit var embyGenreApi: EmbyGenreApi
    private lateinit var embyLibraryApi: EmbyLibraryApi
    private lateinit var embyUserLibraryApi: EmbyUserLibraryApi
    private lateinit var embyLyricsApi: EmbyLyricsApi
    private lateinit var embyUserViewsApi: EmbyUserViewsApi

    /**
     * 创建客户端
     * 创建API客户端
     * @param [clientName] 客户端名称
     * @param [clientVersion] 客户端版本
     * @param [deviceId] 设备ID
     * @param [deviceName] 设备名称
     * @return [EmbyApiClient]
     */
    fun createApiClient(
        clientName: String,
        clientVersion: String,
        deviceId: String,
        deviceName: String
    ): EmbyApiClient {
        this.clientName = clientName
        this.clientVersion = clientVersion
        this.deviceId = deviceId
        this.deviceName = deviceName
        return this
    }


    /**
     * 更新访问令牌和用户ID
     * @param [accessToken] 访问令牌
     * @param [userId] 用户ID
     */
    fun updateAccessTokenAndUserId(accessToken: String?, userId: String?) {
        this.accessToken = accessToken
        this.userId = userId
    }


    /**
     * 获得请求头Map
     * @return [Map<String, String>]
     */
    public override fun getHeadersMapData(): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        accessToken?.let {
            headerMap.put(ApiConstants.EMBY_AUTHORIZATION, it)
        }
        return headerMap
    }


    /**
     * 创建令牌
     * @return [String]
     */
    public override fun createToken(): String {
        val params = arrayOf(
            "UserId" to userId,
            "Client" to clientName,
            "Device" to deviceName,
            "DeviceId" to deviceId,
            "Version" to clientVersion,
            "Token" to accessToken
        )

        // Format: `MediaBrowser key1="value1", key2="value2"`
        return params
            // Drop null values (token)
            .filterNot { (_, value) -> value.isNullOrBlank() }
            // Join parts
            .joinToString(
                separator = ", ",
                prefix = "${ApiConstants.EMBY_AUTHORIZATION_SCHEME} ",
                transform = { (key, value) -> buildParameter(key, value!!) }
            )
    }

    /**
     * 获得用户接口服务
     */
    override fun userApi(restart: Boolean): EmbyUserApi {
        if (!this::embyUserApi.isInitialized || restart) {
            embyUserApi = instance().create(EmbyUserApi::class.java)
        }
        return embyUserApi
    }

    /**
     * 播放列表接口
     */
    override fun playlistsApi(restart: Boolean): EmbyPlaylistsApi {
        if (!this::embyPlaylistsApi.isInitialized || restart) {
            embyPlaylistsApi = instance().create(EmbyPlaylistsApi::class.java)
        }
        return embyPlaylistsApi
    }

    /**
     * 艺术家接口
     */
    override fun artistsApi(restart: Boolean): EmbyArtistsApi {
        if (!this::embyArtistsApi.isInitialized || restart) {
            embyArtistsApi = instance().create(EmbyArtistsApi::class.java)
        }
        return embyArtistsApi
    }


    /**
     * 音乐,专辑,艺术家相关接口
     */
    override fun itemApi(restart: Boolean): EmbyItemApi {
        if (!this::embyItemApi.isInitialized || restart) {
            embyItemApi = instance().create(EmbyItemApi::class.java)
        }
        return embyItemApi
    }

    /**
     * 流派接口
     */
    override fun genreApi(restart: Boolean): EmbyGenreApi {
        if (!this::embyGenreApi.isInitialized || restart) {
            embyGenreApi = instance().create(EmbyGenreApi::class.java)
        }
        return embyGenreApi
    }

    /**
     * 创建下载链接
     */
    override fun createDownloadUrl(itemId: String): String {
        return baseUrl + "/Items/${itemId}/Download"
    }

    /**
     * 登陆接口
     */
    override suspend fun login(clientLoginInfoReq: ClientLoginInfoReq): LoginSuccessData {

        try {
            val pingData = userApi().postPingSystem()
            Log.i("=====", "是否连通: $pingData")
            if (pingData.isSuccessful) {
                val raw = pingData.body()?.string()
                Log.i("=====", "ping数据返回: $raw")// "Ping"
            } else {
                throw ConnectionException()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            when (e) {
                !is UnauthorizedException -> {
                    throw ConnectionException()
                }
            }
        }
        val responseData =
            userApi().authenticateByName(clientLoginInfoReq.toLogin())
        Log.i("=====", "返回响应值: $responseData")
        loginAfter(
            responseData.accessToken,
            responseData.user?.id,
            clientLoginInfoReq = clientLoginInfoReq
        )
        val systemInfo = userApi().getSystemInfo()
        Log.i("=====", "服务器信息 $systemInfo")
        TokenServer.updateLoginRetry(false)
        return LoginSuccessData(
            userId = responseData.user?.id,
            accessToken = responseData.accessToken,
            serverId = responseData.serverId,
            serverName = systemInfo.serverName,
            version = systemInfo.version
        )
    }

    override suspend fun loginAfter(
        accessToken: String?,
        userId: String?,
        subsonicToken: String?,
        subsonicSalt: String?,
        clientLoginInfoReq: ClientLoginInfoReq
    ) {
        updateAccessTokenAndUserId(accessToken, userId)
        updateTokenOrHeadersOrQuery()
    }

    /**
     * 资源接口
     */
    override fun libraryApi(restart: Boolean): EmbyLibraryApi {
        if (!this::embyLibraryApi.isInitialized || restart) {
            embyLibraryApi = instance().create(EmbyLibraryApi::class.java)
        }
        return embyLibraryApi
    }

    /**
     *用户资源接口服务
     */
    override fun userLibraryApi(restart: Boolean): EmbyUserLibraryApi {
        if (!this::embyUserLibraryApi.isInitialized || restart) {
            embyUserLibraryApi = instance().create(EmbyUserLibraryApi::class.java)
        }
        return embyUserLibraryApi
    }

    /**
     * 歌词接口
     */
    override fun lyricsApi(restart: Boolean): EmbyLyricsApi {
        if (!this::embyLyricsApi.isInitialized || restart) {
            embyLyricsApi = instance().create(EmbyLyricsApi::class.java)
        }
        return embyLyricsApi
    }

    /**
     * 用户视图信息
     */
    override fun userViewsApi(restart: Boolean): EmbyUserViewsApi {
        if (!this::embyUserViewsApi.isInitialized || restart) {
            embyUserViewsApi = instance().create(EmbyUserViewsApi::class.java)
        }
        return embyUserViewsApi
    }

    /**
     * 创建图像URL
     * @param [itemId] 项目ID
     * @param [imageType] 图像类型
     * @param [fillWidth] 填充宽度
     * @param [fillHeight] 填充高度
     * @param [quality] 质量
     * @param [tag] 标签
     * @return [String]
     */
    fun createImageUrl(
        itemId: String,
        imageType: ImageType,
        fillWidth: Int? = null,
        fillHeight: Int? = null,
        quality: Int? = null,
        tag: String? = null
    ): String {
        return getItemImageUrl(
            baseUrl = baseUrl,
            itemId = itemId,
            imageType = imageType,
            fillWidth = fillWidth,
            fillHeight = fillHeight,
            quality = quality,
            tag = tag
        )
    }

    /**
     * 获得艺术家图片链接
     * @param [name] 姓名
     * @param [imageType] 图像类型
     * @param [imageIndex] 图像索引
     * @param [tag] 标签
     * @param [quality] 质量
     * @param [fillWidth] 填充宽度
     * @param [fillHeight] 填充高度
     * @return [String]
     */
    fun createArtistImageUrl(
        name: String,
        imageType: ImageType,
        imageIndex: Int,
        tag: String? = null,
        quality: Int? = null,
        fillWidth: Int? = null,
        fillHeight: Int? = null,
    ): String {
        return getArtistImageUrl(
            baseUrl = baseUrl,
            name = name,
            imageType = imageType,
            fillWidth = fillWidth,
            fillHeight = fillHeight,
            quality = quality,
            tag = tag,
            imageIndex = imageIndex
        )
    }


    /**
     * 创建音频URL
     * @param [itemId] 项目编号
     * @param [container] 容器
     * @param [audioCodec] 音频编解码器
     * @param [static] 是否是静态不转码的
     * @return [String]
     */
    fun createAudioUrl(
        itemId: String,
        audioCodec: AudioCodecEnum? = null,
        static: Boolean = true,
        audioBitRate: Int? = null
    ): String {
        return getAudioStreamUrl(
            itemId = itemId,
            audioCodec = audioCodec,
            static = static,
            audioBitRate = audioBitRate
        )
    }


    /**
     * 获取项目图像URL
     * @param [baseUrl] 基础网址
     * @param [itemId] 项目ID
     * @param [imageType] 图像类型
     * @param [fillWidth] 填充宽度
     * @param [fillHeight] 填充高度
     * @param [quality] 质量
     * @param [tag] 标签
     * @return [String]
     */
    fun getItemImageUrl(
        baseUrl: String,
        itemId: String,
        imageType: ImageType,
        fillWidth: Int? = null,
        fillHeight: Int? = null,
        quality: Int? = null,
        tag: String? = null,
    ): String {
        return baseUrl + "/emby/Items/${itemId}/Images/${imageType}?fillHeight=${fillHeight}&fillWidth=${fillWidth}&quality=${quality}&tag=${tag}"
    }

    /**
     * 获取艺术家图像URL
     * @param [baseUrl] 基础网址
     * @param [name] 姓名
     * @param [imageType] 图像类型
     * @param [imageIndex] 图像索引
     * @param [tag] 标签
     * @param [quality] 质量
     * @param [fillWidth] 填充宽度
     * @param [fillHeight] 填充高度
     * @return [String]
     */
    fun getArtistImageUrl(
        baseUrl: String,
        name: String,
        imageType: ImageType,
        imageIndex: Int,
        tag: String? = null,
        quality: Int? = null,
        fillWidth: Int? = null,
        fillHeight: Int? = null,
    ): String {
        return "$baseUrl/emby/Artists/${name}/Images/${imageType}/${imageIndex}?fillHeight=${fillHeight}&fillWidth=${fillWidth}&quality=${quality}&tag=${tag}"
    }

    /**
     * 创建音频URL
     * @param [itemId] 项目编号
     * @param [container] 容器
     * @param [audioCodec] 音频编解码器
     * @param [static] 是否是静态不转码的
     * @return [String]
     */
    private fun getAudioStreamUrl(
        itemId: String,
        audioCodec: AudioCodecEnum? = null,
        static: Boolean = true,
        audioBitRate: Int? = null
    ): String {
        return if (audioBitRate == null){
            "${baseUrl}/emby/Audio/${itemId}/stream?" +
                    "deviceId=${deviceId}&userId=${userId}&static=${static}"
        }else {
            "${baseUrl}/emby/Audio/${itemId}/universal?" +
                    "deviceId=${deviceId}" +
                    "&AudioCodec=${audioCodec}&MaxStreamingBitrate=${audioBitRate}" +
                    "&Container=opus%2Cwebm%7Copus%2Cts%7Cmp3%2Cmp3%2Caac%2Cm4a%7Caac%2Cm4b%7Caac%2Cflac%2Cwebma%2Cwebm%7Cwebma%2Cwav%2Cogg" +
                    "&EnableRedirection=true&EnableRemoteMedia=false&EnableAudioVbrEncoding=true" +
                    "&transcodingProtocol=hls"
        }
    }

    /**
     * 清空数据
     */
    override fun release() {
        clientName = ""
        clientVersion = ""
        deviceId = ""
        deviceName = ""
        accessToken = ""
        userId = null
    }
}