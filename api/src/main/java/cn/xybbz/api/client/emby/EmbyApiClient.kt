package cn.xybbz.api.client.emby

import cn.xybbz.api.client.DefaultApiClient
import cn.xybbz.api.client.emby.service.EmbyArtistsApi
import cn.xybbz.api.client.emby.service.EmbyGenreApi
import cn.xybbz.api.client.emby.service.EmbyItemApi
import cn.xybbz.api.client.emby.service.EmbyLibraryApi
import cn.xybbz.api.client.emby.service.EmbyLyricsApi
import cn.xybbz.api.client.emby.service.EmbyPlaylistsApi
import cn.xybbz.api.client.emby.service.EmbyUserApi
import cn.xybbz.api.client.emby.service.EmbyUserLibraryApi
import cn.xybbz.api.client.emby.service.EmbyUserViewsApi
import cn.xybbz.api.client.jellyfin.JellyfinApiClient
import cn.xybbz.api.client.jellyfin.buildParameter
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.enums.jellyfin.ImageType
import cn.xybbz.api.enums.jellyfin.MediaStreamProtocol

class EmbyApiClient : DefaultApiClient() {

    private var clientName: String = ""
    private var clientVersion: String = ""
    var deviceId: String = ""
        private set
    private var deviceName: String = ""
    private var accessToken: String? = null
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
     * @return [JellyfinApiClient]
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
     * 更新token信息
     */
    fun updateAccessTokenAndUserId(accessToken: String?, userId: String?) {
        this.accessToken = accessToken
        this.userId = userId
    }


    /**
     * 获得请求头Map
     */
    public override fun getHeadersMapData(): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        accessToken?.let {
            headerMap.put(ApiConstants.EMBY_AUTHORIZATION, it)
        }
        return headerMap
    }


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
     * @param [itemId] 项目ID
     * @param [container] 容器
     * @param [deviceId] 设备ID
     * @param [userId] 用户id
     * @param [maxStreamingBitrate] 最大流率比特率
     * @param [transcodingContainer] 转码容器
     * @param [transcodingProtocol] 转码协议
     * @param [audioCodec] 音频编解码器
     * @param [startTimeTicks] 开始时间
     * @param [enableRedirection] 启用重定向
     * @param [enableRemoteMedia] 启用远程媒体
     * @return [String]
     */
    fun createAudioUrl(
        itemId: String,
        container: Collection<String>? = emptyList(),
        deviceId: String? = null,
        userId: String? = null,
        maxStreamingBitrate: Int? = null,
        transcodingContainer: String? = null,
        transcodingProtocol: MediaStreamProtocol? = null,
        audioCodec: String? = null,
        startTimeTicks: Long? = null,
        enableRedirection: Boolean? = true,
        enableRemoteMedia: Boolean? = null,
    ): String {
        return getUniversalAudioStreamUrl(
            baseUrl = baseUrl,
            itemId = itemId,
            container = container,
            deviceId = deviceId,
            userId = userId,
            maxStreamingBitrate = maxStreamingBitrate,
            transcodingContainer = transcodingContainer,
            transcodingProtocol = transcodingProtocol,
            audioCodec = audioCodec,
            startTimeTicks = startTimeTicks,
            enableRedirection = enableRedirection,
            enableRemoteMedia = enableRemoteMedia
        )
    }


    fun createAudioUrl(
        itemId: String,
        container: String? = "hls",
        audioCodec: String? = null,
        static: Boolean = true,
    ): String {
        return getAudioStreamUrl(
            itemId = itemId,
            container = container,
            audioCodec = audioCodec,
            static = static
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

    private fun getUniversalAudioStreamUrl(
        baseUrl: String,
        itemId: String,
        container: Collection<String>? = emptyList(),
        deviceId: String? = null,
        userId: String? = null,
        maxStreamingBitrate: Int? = null,
        transcodingContainer: String? = null,
        transcodingProtocol: MediaStreamProtocol? = null,
        audioCodec: String? = null,
        startTimeTicks: Long? = null,
        enableRedirection: Boolean? = true,
        enableRemoteMedia: Boolean? = null,
    ): String {
        return "${baseUrl}/emby/Audio/${itemId}/universal?container=${container?.joinToString(",")}" +
                "&deviceId=${deviceId}&userId=${userId}&maxStreamingBitrate=${maxStreamingBitrate}" +
                "&transcodingContainer=${transcodingContainer}&transcodingProtocol=${transcodingProtocol}" +
                "&audioCodec=${audioCodec}&startTimeTicks=${startTimeTicks}&enableRedirection=${enableRedirection}" +
                "&enableRemoteMedia=${enableRemoteMedia}"
    }

    private fun getAudioStreamUrl(
        itemId: String,
        container: String? = "hls",
        audioCodec: String? = null,
        static: Boolean = true,
    ): String {
        return "${baseUrl}/emby/Audio/${itemId}/stream?container=${container}" +
                "&deviceId=${deviceId}&userId=${userId}&static=${static}" +
                "&audioCodec=${audioCodec}"
    }

    /**
     * 清空数据
     */
    override fun release() {

    }
}