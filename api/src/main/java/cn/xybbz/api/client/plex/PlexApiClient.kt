package cn.xybbz.api.client.plex

import android.util.Log
import cn.xybbz.api.TokenServer
import cn.xybbz.api.client.DefaultParentApiClient
import cn.xybbz.api.client.data.ClientLoginInfoReq
import cn.xybbz.api.client.data.LoginSuccessData
import cn.xybbz.api.client.jellyfin.encodeUrlParameter
import cn.xybbz.api.client.plex.data.toPlexLogin
import cn.xybbz.api.client.plex.service.PlexItemApi
import cn.xybbz.api.client.plex.service.PlexLibraryApi
import cn.xybbz.api.client.plex.service.PlexLyricsApi
import cn.xybbz.api.client.plex.service.PlexPlaylistsApi
import cn.xybbz.api.client.plex.service.PlexUserApi
import cn.xybbz.api.client.plex.service.PlexUserLibraryApi
import cn.xybbz.api.client.plex.service.PlexUserViewsApi
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.exception.ServiceException

class PlexApiClient : DefaultParentApiClient() {

    /**
     * 客户端编码
     */
    var clientId: String = ""
        private set

    /**
     * 应用名称
     */
    private var clientName: String = ""

    /**
     * 应用版本号
     */
    private var clientVersion: String = ""

    /**
     * 设备名称
     */
    private var deviceName: String = ""

    /**
     * 设备型号
     */
    private var deviceModel: String = ""

    /**
     * 登陆返回token
     */
    private var accessToken: String? = null

    /**
     * 用户id
     */
    var userId: String? = null
        private set

    /**
     * 服务端版本信息
     */
    var serverVersion: String? = null
        private set

    /**
     * 服务编码
     */
    var serverId: String? = null
        private set

    /**
     * 服务端名称
     */
    var serverName: String? = null
        private set


    var musicFavoriteCollectionId: String? = null
        private set
    var musicFavoriteCollectionIndex: Int? = null
        private set

    var albumFavoriteCollectionId: String? = null
        private set
    var albumFavoriteCollectionIndex: Int? = null
        private set

    var artistFavoriteCollectionId: String? = null
        private set
    var artistFavoriteCollectionIndex: Int? = null
        private set

    //服务器信息
    var machineIdentifier: String? = null
        private set

    /**
     * token的header名称
     */
    override val tokenHeaderName: String
        get() = ApiConstants.PLEX_AUTHORIZATION


    private lateinit var plexUserApi: PlexUserApi
    private lateinit var plexUserViewsApi: PlexUserViewsApi
    private lateinit var plexItemApi: PlexItemApi
    private lateinit var plexPlaylistsApi: PlexPlaylistsApi
    private lateinit var plexUserLibraryApi: PlexUserLibraryApi
    private lateinit var plexLibraryApi: PlexLibraryApi
    private lateinit var plexLyricsApi: PlexLyricsApi


    /**
     * 创建 API 客户端
     * @param [clientName] 客户名称
     * @param [clientVersion] 客户端版本
     * @param [deviceName] 设备名称
     * @param [clientId] 客户端 ID
     * @param [deviceModel] 设备型号
     * @return [PlexApiClient]
     */
    fun createApiClient(
        clientName: String,
        clientId: String,
        clientVersion: String,
        deviceName: String,
        deviceModel: String
    ): PlexApiClient {
        this.clientId = clientId
        this.clientName = clientName
        this.clientVersion = clientVersion
        this.deviceName = deviceName
        this.deviceModel = deviceModel
        return this
    }


    /**
     * 获得okhttp客户端
     */
    /*override fun getOkHttpClient(): OkHttpClient {
        val okHttpClient = super.getOkHttpClient()
        okHttpClient.newBuilder().addNetworkInterceptor(PlexQueryInterceptor())
        return okHttpClient
    }*/

    /**
     * 更新token信息
     */
    fun updateAccessToken(accessToken: String?) {
        this.accessToken = accessToken
    }

    /**
     * 更新用户id
     */
    fun updateServerInfo(
        userId: String? = null,
        serverVersion: String? = null,
        serverId: String? = null,
        serverName: String? = null
    ) {
        this.userId = userId
        this.serverVersion = serverVersion
        this.serverId = serverId
        this.serverName = serverName
    }

    /**
     * 获得token
     */
    public override fun createToken(): String {
        return this.accessToken ?: ""
    }


    /**
     * 获得请求头Map
     */
    public override fun getHeadersMapData(): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap.put(ApiConstants.PLEX_CLIENT_IDENTIFIER, clientId.encodeUrlParameter())
        headerMap.put(ApiConstants.PLEX_PRODUCT, clientName.encodeUrlParameter())
        headerMap.put(ApiConstants.PLEX_VERSION, clientVersion.encodeUrlParameter())
        headerMap.put(ApiConstants.PLEX_PLATFORM, "Android")
        headerMap.put(ApiConstants.PLEX_PROVIDES, "player")
        headerMap.put(ApiConstants.PLEX_DEVICE_NAME, deviceName.encodeUrlParameter())
        headerMap.put(ApiConstants.PLEX_DEVICE, deviceModel.encodeUrlParameter())
        return headerMap
    }

    /**
     * 更新musicFavoriteCollectionId
     */
    fun updateMusicFavoriteCollectionId(
        musicFavoriteCollectionId: String?,
        musicFavoriteCollectionIndex: Int?
    ) {
        this.musicFavoriteCollectionId = musicFavoriteCollectionId
        this.musicFavoriteCollectionIndex = musicFavoriteCollectionIndex
    }

    /**
     * 更新albumFavoriteCollectionId
     */
    fun updateAlbumFavoriteCollectionId(
        albumFavoriteCollectionId: String?,
        albumFavoriteCollectionIndex: Int?
    ) {
        this.albumFavoriteCollectionId = albumFavoriteCollectionId
        this.albumFavoriteCollectionIndex = albumFavoriteCollectionIndex
    }

    /**
     * 更新artistFavoriteCollectionId
     */
    fun updateArtistFavoriteCollectionId(
        artistFavoriteCollectionId: String?,
        artistFavoriteCollectionIndex: Int?
    ) {
        this.artistFavoriteCollectionId = artistFavoriteCollectionId
        this.artistFavoriteCollectionIndex = artistFavoriteCollectionIndex
    }

    /**
     * 更新machineIdentifier
     */
    fun updateMachineIdentifier(machineIdentifier: String?) {
        this.machineIdentifier = machineIdentifier
    }

    /**
     * 获得用户接口服务
     */
    override fun userApi(restart: Boolean): PlexUserApi {
        if (!this::plexUserApi.isInitialized || restart) {
            plexUserApi = instance().create(PlexUserApi::class.java)
        }
        return plexUserApi
    }

    /**
     * 用户视图信息
     */
    override fun userViewsApi(restart: Boolean): PlexUserViewsApi {
        if (!this::plexUserViewsApi.isInitialized || restart) {
            plexUserViewsApi = instance().create(PlexUserViewsApi::class.java)
        }
        return plexUserViewsApi
    }

    /**
     * 音乐,专辑,艺术家相关接口
     */
    override fun itemApi(restart: Boolean): PlexItemApi {
        if (!this::plexItemApi.isInitialized || restart) {
            plexItemApi = instance().create(PlexItemApi::class.java)
        }
        return plexItemApi
    }

    /**
     * 播放列表接口
     */
    override fun playlistsApi(restart: Boolean): PlexPlaylistsApi {
        if (!this::plexPlaylistsApi.isInitialized || restart) {
            plexPlaylistsApi = instance().create(PlexPlaylistsApi::class.java)
        }
        return plexPlaylistsApi
    }

    /**
     *用户资源接口服务
     */
    override fun userLibraryApi(restart: Boolean): PlexUserLibraryApi {
        if (!this::plexUserLibraryApi.isInitialized || restart) {
            plexUserLibraryApi = instance().create(PlexUserLibraryApi::class.java)
        }
        return plexUserLibraryApi
    }

    /**
     * 资源接口
     */
    override fun libraryApi(restart: Boolean): PlexLibraryApi {
        if (!this::plexLibraryApi.isInitialized || restart) {
            plexLibraryApi = instance().create(PlexLibraryApi::class.java)
        }
        return plexLibraryApi
    }

    /**
     * 创建下载链接
     */
    override fun createDownloadUrl(itemId: String): String {
        return "$baseUrl$itemId?download=1"
    }

    /**
     * 登陆接口
     */
    override suspend fun login(clientLoginInfoReq: ClientLoginInfoReq): LoginSuccessData {
        var loginSuccessData = LoginSuccessData(
            userId = userId,
            accessToken = createToken(),
            serverId = clientLoginInfoReq.serverId,
            serverName = clientLoginInfoReq.serverName,
            version = clientLoginInfoReq.serverVersion
        )
        if (createToken().isBlank()) {
            loginSuccessData = plexLogin(clientLoginInfoReq)
        }

        try {
            val postPingSystem = userApi().postPingSystem()
            Log.i("=====", postPingSystem.toString())
            //获得machineIdentifier
            pingAfter(postPingSystem.mediaContainer?.machineIdentifier)
            loginSuccessData =
                loginSuccessData.copy(machineIdentifier = postPingSystem.mediaContainer?.machineIdentifier)
        } catch (e: Exception) {
            Log.i("error", "ping服务器失败", e)
            throw ServiceException("ping服务器失败")
        }
        TokenServer.updateLoginRetry(false)
        return loginSuccessData
    }

    override suspend fun loginAfter(
        accessToken: String?,
        userId: String?,
        subsonicToken: String?,
        subsonicSalt: String?,
        clientLoginInfoReq: ClientLoginInfoReq
    ) {
        updateAccessToken(accessToken)
        updateServerInfo(userId = userId)
        updateTokenOrHeadersOrQuery()
    }

    override suspend fun pingAfter(machineIdentifier: String?) {
        updateMachineIdentifier(machineIdentifier)
    }

    suspend fun plexLogin(clientLoginInfoReq: ClientLoginInfoReq): LoginSuccessData {
        val responseData =
            userApi().authenticateByName(
                "https://plex.tv/api/v2/users/signin",
                clientLoginInfoReq.toPlexLogin()
            )
        Log.i("=====", "返回响应值: $responseData")
        loginAfter(
            responseData.authToken, responseData.id,
            clientLoginInfoReq = clientLoginInfoReq
        )
        return LoginSuccessData(
            userId = userId,
            accessToken = createToken(),
            serverId = clientLoginInfoReq.serverId,
            serverName = clientLoginInfoReq.serverName,
            version = clientLoginInfoReq.serverVersion
        )
    }

    /**
     * 歌词接口
     */
    override fun lyricsApi(restart: Boolean): PlexLyricsApi {
        if (!this::plexLyricsApi.isInitialized || restart) {
            plexLyricsApi = instance().create(PlexLyricsApi::class.java)
        }
        return plexLyricsApi
    }

    /**
     * 获取图像URL
     * [plexFileUrl] plex文件路径
     */
    //todo 修改图片获取方式
    fun getImageUrl(plexFileUrl: String, width: Int = 297, height: Int = 297): String {
        val tmpPlexFileUrl = "${plexFileUrl}?X-Plex-Token=${accessToken}".encodeUrlParameter()
        return "${baseUrl}/photo/:/transcode?width=${width}&height=${height}&url=${tmpPlexFileUrl}&minSize=1&upscale=1&X-Plex-Token=${accessToken}"
    }

    fun createAudioUrl(
        trackMediaPartKey: String,
    ): String {
        return getAudioStreamUrl(
            trackMediaPartKey = trackMediaPartKey
        )
    }

    fun createMusicUri(itemId: String): String {
        return "server://${machineIdentifier}/com.plexapp.plugins.library/library/metadata/${itemId}"
    }

    fun createMusicUri(itemIds: List<String>): String {
        return "server://${machineIdentifier}/com.plexapp.plugins.library/library/metadata/${
            itemIds.joinToString(",") { it }
        }"
    }

    private fun getAudioStreamUrl(
        trackMediaPartKey: String,
    ): String {
        return "${baseUrl}${trackMediaPartKey}?X-Plex-Platform=Android&X-Plex-Token=${accessToken}"
    }

    /**
     * 清空数据
     */
    override fun release() {

    }
}