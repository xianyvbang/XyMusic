package cn.xybbz.api.client.navidrome

import cn.xybbz.api.client.DefaultApiClient
import cn.xybbz.api.client.DefaultParentApiClient
import cn.xybbz.api.client.navidrome.service.NavidromeArtistsApi
import cn.xybbz.api.client.navidrome.service.NavidromeGenreApi
import cn.xybbz.api.client.navidrome.service.NavidromeItemApi
import cn.xybbz.api.client.navidrome.service.NavidromePlaylistsApi
import cn.xybbz.api.client.navidrome.service.NavidromeUserApi
import cn.xybbz.api.client.navidrome.service.NavidromeUserLibraryApi
import cn.xybbz.api.client.subsonic.SubsonicApiClient
import cn.xybbz.api.constants.ApiConstants
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
        val queryMao = mutableMapOf<String, String>()
        queryMao.put("u", username)
        queryMao.put("t", subsonicToken)
        queryMao.put("s", subsonicSalt)
        queryMao.put("v", subsonicProtocolVersion)
        queryMao.put("c", subsonicClientName)
        queryMao.put("f", subsonicResponseFormat)
        return queryMao
    }

    /**
     * 获得请求头Map
     */
    public override fun getHeadersMapData(): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap.put(ApiConstants.NAVIDROME_HEADER, id)
        return headerMap
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
        return baseUrl + "/rest/download?id=${itemId}"
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
        createSubsonicApiClient("", "", "", "", "", "", "", "")
        token = ""
    }
}