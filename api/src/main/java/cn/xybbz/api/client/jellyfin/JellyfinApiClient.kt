package cn.xybbz.api.client.jellyfin

import cn.xybbz.api.client.DefaultApiClient
import cn.xybbz.api.client.jellyfin.service.ArtistsApi
import cn.xybbz.api.client.jellyfin.service.GenreApi
import cn.xybbz.api.client.jellyfin.service.ImageApi
import cn.xybbz.api.client.jellyfin.service.ItemApi
import cn.xybbz.api.client.jellyfin.service.LibraryApi
import cn.xybbz.api.client.jellyfin.service.LyricsApi
import cn.xybbz.api.client.jellyfin.service.PlaylistsApi
import cn.xybbz.api.client.jellyfin.service.UniversalAudioApi
import cn.xybbz.api.client.jellyfin.service.UserApi
import cn.xybbz.api.client.jellyfin.service.UserLibraryApi
import cn.xybbz.api.client.jellyfin.service.UserViewsApi
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.enums.jellyfin.ImageType
import cn.xybbz.api.enums.jellyfin.MediaStreamProtocol
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


class JellyfinApiClient : DefaultApiClient() {

    private var clientName: String = ""
    private var clientVersion: String = ""
    var deviceId: String = ""
        private set
    private var deviceName: String = ""
    private var accessToken: String? = null
    private lateinit var jellyfinUserApi: UserApi
    private lateinit var jellyfinUserLibraryApi: UserLibraryApi
    private lateinit var jellyfinItemApi: ItemApi
    private lateinit var jellyfinImageApi: ImageApi
    private lateinit var jellyfinUniversalAudioApi: UniversalAudioApi
    private lateinit var jellyfinLyricsApi: LyricsApi
    private lateinit var jellyfinUserViewsApi: UserViewsApi
    private lateinit var jellyfinPlaylistsApi: PlaylistsApi
    private lateinit var jellyfinArtistsApi: ArtistsApi
    private lateinit var jellyfinLibraryApi: LibraryApi
    private lateinit var jellyfinGenreApi: GenreApi


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
    ): JellyfinApiClient {
        this.clientName = clientName
        this.clientVersion = clientVersion
        this.deviceId = deviceId
        this.deviceName = deviceName
        return this
    }

    /**
     * 更新token信息
     */
    fun updateAccessToken(accessToken: String?) {
        this.accessToken = accessToken
    }

    public override fun createToken(): String {
        val params = arrayOf(
            "Client" to clientName,
            "Version" to clientVersion,
            "DeviceId" to deviceId,
            "Device" to deviceName,
            "Token" to accessToken
        )

        // Format: `MediaBrowser key1="value1", key2="value2"`
        return params
            // Drop null values (token)
            .filterNot { (_, value) -> value == null }
            // Join parts
            .joinToString(
                separator = ", ",
                prefix = "${ApiConstants.AUTHORIZATION_SCHEME} ",
                transform = { (key, value) -> buildParameter(key, value!!) }
            )
    }

    /**
     * 获得用户接口服务
     */
    override fun userApi(restart: Boolean): UserApi {
        if (!this::jellyfinUserApi.isInitialized || restart) {
            jellyfinUserApi = instance().create(UserApi::class.java)
        }
        return jellyfinUserApi
    }

    /**
     *用户资源接口服务
     */
    override fun userLibraryApi(restart: Boolean): UserLibraryApi {
        if (!this::jellyfinUserLibraryApi.isInitialized || restart) {
            jellyfinUserLibraryApi = instance().create(UserLibraryApi::class.java)
        }
        return jellyfinUserLibraryApi
    }

    /**
     * 音乐,专辑,艺术家相关接口
     */
    override fun itemApi(restart: Boolean): ItemApi {
        if (!this::jellyfinItemApi.isInitialized || restart) {
            jellyfinItemApi = instance().create(ItemApi::class.java)
        }
        return jellyfinItemApi
    }

    /**
     * 获取文件图片
     */
    override fun imageApi(restart: Boolean): ImageApi {
        if (!this::jellyfinImageApi.isInitialized || restart) {
            jellyfinImageApi = instance().create(ImageApi::class.java)
        }
        return jellyfinImageApi
    }

    /**
     * 创建音乐流
     */
    override fun universalAudioApi(restart: Boolean): UniversalAudioApi {
        if (!this::jellyfinUniversalAudioApi.isInitialized || restart) {
            jellyfinUniversalAudioApi = instance().create(UniversalAudioApi::class.java)
        }
        return jellyfinUniversalAudioApi
    }

    /**
     * 歌词接口
     */
    override fun lyricsApi(restart: Boolean): LyricsApi {
        if (!this::jellyfinLyricsApi.isInitialized || restart) {
            jellyfinLyricsApi = instance().create(LyricsApi::class.java)
        }
        return jellyfinLyricsApi
    }


    /**
     * 用户视图信息
     */
    override fun userViewsApi(restart: Boolean): UserViewsApi {
        if (!this::jellyfinUserViewsApi.isInitialized || restart) {
            jellyfinUserViewsApi = instance().create(UserViewsApi::class.java)
        }
        return jellyfinUserViewsApi
    }

    /**
     * 播放列表接口
     */
    override fun playlistsApi(restart: Boolean): PlaylistsApi {
        if (!this::jellyfinPlaylistsApi.isInitialized || restart) {
            jellyfinPlaylistsApi = instance().create(PlaylistsApi::class.java)
        }
        return jellyfinPlaylistsApi
    }

    /**
     * 艺术家接口
     */
    override fun artistsApi(restart: Boolean): ArtistsApi {
        if (!this::jellyfinArtistsApi.isInitialized || restart) {
            jellyfinArtistsApi = instance().create(ArtistsApi::class.java)
        }
        return jellyfinArtistsApi
    }

    /**
     * 资源接口
     */
    override fun libraryApi(restart: Boolean): LibraryApi {
        if (!this::jellyfinLibraryApi.isInitialized || restart) {
            jellyfinLibraryApi = instance().create(LibraryApi::class.java)
        }
        return jellyfinLibraryApi
    }

    /**
     * 流派接口
     */
    override fun genreApi(restart: Boolean): GenreApi {
        if (!this::jellyfinGenreApi.isInitialized || restart) {
            jellyfinGenreApi = instance().create(GenreApi::class.java)
        }
        return jellyfinGenreApi
    }

    /**
     * 创建下载链接
     */
    override fun createDownloadUrl(itemId: String): String {
        return baseUrl + "/Items/${itemId}/Download"
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
        playSessionId: String,
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
            playSessionId = playSessionId,
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
        container: String? = null,
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
        return baseUrl + "/Items/${itemId}/Images/${imageType}?fillHeight=${fillHeight}&fillWidth=${fillWidth}&quality=${quality}&tag=${tag}"
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
        return "$baseUrl/Artists/${name}/Images/${imageType}/${imageIndex}?fillHeight=${fillHeight}&fillWidth=${fillWidth}&quality=${quality}&tag=${tag}"
    }

    private fun getUniversalAudioStreamUrl(
        baseUrl: String,
        itemId: String,
        container: Collection<String>? = emptyList(),
        deviceId: String? = null,
        userId: String? = null,
        playSessionId: String? = null,
        maxStreamingBitrate: Int? = null,
        transcodingContainer: String? = null,
        transcodingProtocol: MediaStreamProtocol? = null,
        audioCodec: String? = null,
        startTimeTicks: Long? = null,
        enableRedirection: Boolean? = true,
        enableRemoteMedia: Boolean? = null,
    ): String {
        return "${baseUrl}/Audio/${itemId}/universal?container=${container?.joinToString(",")}" +
                "&deviceId=${deviceId}&userId=${userId}&maxStreamingBitrate=${maxStreamingBitrate}" +
                "&transcodingContainer=${transcodingContainer}&transcodingProtocol=${transcodingProtocol}" +
                "&audioCodec=${audioCodec}&startTimeTicks=${startTimeTicks}&enableRedirection=${enableRedirection}" +
                "&enableRemoteMedia=${enableRemoteMedia}&playSessionId=${playSessionId}"
    }

    private fun getAudioStreamUrl(
        itemId: String,
        container: String? = "hls",
        audioCodec: String? = null,
        static: Boolean = true,
    ): String {
        return "${baseUrl}/Audio/${itemId}/stream?container=${container}" +
                "&deviceId=${deviceId}&static=${static}" +
                "&audioCodec=${audioCodec}"
    }

    /**
     * 清空数据
     */
    override fun release() {
        createApiClient("", "", "", "")
    }

}

fun buildParameter(key: String, value: String): String {
    // Check for bad strings to prevent endless hours debugging why the server throws http 500 errors
    require(!key.contains('=')) {
        "Key $key can not contain the = character in the authorization header"
    }
    require(!key.contains(',')) {
        "Key $key can not contain the , character in the authorization header"
    }
    require(!key.startsWith('"') && !key.endsWith('"')) {
        "Key $key can not start or end with the \" character in the authorization header"
    }

    // key="value"
    return """${key}="${encodeParameterValue(value)}""""
}

private fun encodeParameterValue(raw: String): String = raw
    .trim()
    .replace(Regex("\\n"), " ")
    .encodeUrlParameter()

fun String.encodeUrlParameter(): String {
    return URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
}