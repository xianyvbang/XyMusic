package cn.xybbz.api.client.subsonic

import cn.xybbz.api.client.DefaultParentApiClient
import cn.xybbz.api.client.subsonic.service.SubsonicArtistsApi
import cn.xybbz.api.client.subsonic.service.SubsonicGenreApi
import cn.xybbz.api.client.subsonic.service.SubsonicItemApi
import cn.xybbz.api.client.subsonic.service.SubsonicLyricsApi
import cn.xybbz.api.client.subsonic.service.SubsonicPlaylistsApi
import cn.xybbz.api.client.subsonic.service.SubsonicUserApi
import cn.xybbz.api.client.subsonic.service.SubsonicUserLibraryApi
import cn.xybbz.api.client.subsonic.service.SubsonicUserViewsApi
import cn.xybbz.api.enums.subsonic.ResponseFormatType

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

    fun updateVersion(protocolVersion: String) {
        this.protocolVersion = protocolVersion
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
        val queryMap = mutableMapOf<String, String>()
        queryMap.put("u", username)
        queryMap.put("t", passwordMd5)
        queryMap.put("s", encryptedSalt)
        queryMap.put("v", protocolVersion)
        queryMap.put("c", clientName)
        queryMap.put("f", responseFormat)
        return queryMap
    }

    /**
     * 获取图像URL
     * @param [imageId] 图像ID
     * @param [size] 尺寸
     */
    fun getImageUrl(imageId: String, size: Int? = null): String {
        return if (size == null) {
            "${baseUrl}/rest/getCoverArt?id=${imageId}"
        } else {
            "${baseUrl}/rest/getCoverArt?id=${imageId}&size=${size}"
        }
    }

    /**
     * 获得音频url
     */
    fun createAudioUrl(musicId: String): String {
        return "${baseUrl}/rest/stream?id=${musicId}&maxBitRate=0"
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
        return baseUrl + "/rest/download?id=${itemId}"
    }

}