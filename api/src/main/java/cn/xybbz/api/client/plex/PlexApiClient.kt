package cn.xybbz.api.client.plex

import cn.xybbz.api.client.DefaultApiClient
import cn.xybbz.api.client.emby.service.EmbyUserApi
import cn.xybbz.api.client.plex.service.PlexUserApi
import cn.xybbz.api.constants.ApiConstants

class PlexApiClient : DefaultApiClient() {

    /**
     * 客户端编码
     */
    private var clientId:String = ""
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
     * token的header名称
     */
    override val tokenHeaderName: String
        get() = ApiConstants.PLEX_AUTHORIZATION


    private lateinit var plexUserApi: PlexUserApi


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
     * 更新token信息
     */
    fun updateAccessToken(accessToken: String?) {
        this.accessToken = accessToken
    }

    /**
     * 获得token
     */
    public override fun getToken(): String {
        return this.accessToken ?: ""
    }


    /**
     * 获得请求头Map
     */
    fun getHeadersMapData(): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap.put(ApiConstants.PLEX_CLIENT_IDENTIFIER,clientId)
        headerMap.put(ApiConstants.PLEX_PRODUCT,clientName)
        headerMap.put(ApiConstants.PLEX_VERSION,clientVersion)
        headerMap.put(ApiConstants.PLEX_PLATFORM,"Android")
        headerMap.put(ApiConstants.PLEX_PROVIDES,"player")
        headerMap.put(ApiConstants.PLEX_DEVICE_NAME,deviceName)
        headerMap.put(ApiConstants.PLEX_DEVICE,deviceModel)
        return headerMap
    }

    /**
     * 获得用户接口服务
     */
    override fun userApi(restart: Boolean): PlexUserApi {
        if (!this::plexUserApi.isInitialized) {
            plexUserApi = instance().create(PlexUserApi::class.java)
        }
        return plexUserApi
    }


    /**
     * 清空数据
     */
    override fun release() {

    }
}