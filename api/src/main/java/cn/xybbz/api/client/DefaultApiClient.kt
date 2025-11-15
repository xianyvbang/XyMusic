package cn.xybbz.api.client

import cn.xybbz.api.TokenServer
import cn.xybbz.api.adapter.LocalDateAdapter
import cn.xybbz.api.adapter.LocalDateTimeAdapter
import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.base.IDownLoadApi
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.constants.ApiConstants.DEFAULT_TIMEOUT_MILLISECONDS
import cn.xybbz.api.okhttp.LoggingInterceptor
import cn.xybbz.api.okhttp.NetWorkInterceptor
import cn.xybbz.api.okhttp.plex.PlexQueryInterceptor
import cn.xybbz.api.okhttp.subsonic.SubsonicNetworkStatusInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

abstract class DefaultApiClient : ApiConfig {

    protected lateinit var baseUrl: String


    private lateinit var retrofit: Retrofit

    /**
     * token的header名称
     */
    protected open val tokenHeaderName = ApiConstants.AUTHORIZATION

    var token: String = ""
        protected set

    var queryMap: Map<String, String> = emptyMap()
        private set

    var headerMap: Map<String, String> = emptyMap()
        private set


    /**
     * 是否为Subsonic
     */
    protected open val ifSubsonic = false

    lateinit var apiOkHttpClient: OkHttpClient

    private lateinit var defaultDownloadApi: IDownLoadApi

    override fun setRetrofitData(baseUrl: String, ifTmp: Boolean) {
        this.baseUrl = baseUrl
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl).client(getOkHttpClient(ifTmp))
//            .addConverterFactory(MyGsonConverterFactory.create()).build()
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder().add(LocalDateTimeAdapter())
                        .add(LocalDateAdapter()).add(KotlinJsonAdapterFactory())
                        .build()
                )
            ).build()
        userApi(true)
        userLibraryApi(true)
        itemApi(true)
        imageApi(true)
        universalAudioApi(true)
        lyricsApi(true)
        userViewsApi(true)
        playlistsApi(true)
        artistsApi(true)
        libraryApi(true)
        genreApi(true)
        downloadApi(true)
    }

    override fun instance(): Retrofit {
        return retrofit
    }

    /**
     * 获得okhttp客户端
     */
    protected open fun getOkHttpClient(ifTmp: Boolean = false): OkHttpClient {
        if (!ifTmp){
            updateTokenHeaderName()
            updateIfSubsonic()
        }
        apiOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(
                NetWorkInterceptor(
                    { if (ifTmp) tokenHeaderName else TokenServer.tokenHeaderName },
                    { if (ifTmp) token else TokenServer.token },
                    { if (ifTmp) queryMap else TokenServer.queryMap },
                    { if (ifTmp) headerMap else TokenServer.headerMap }
                )
            )
            .connectTimeout(DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
            .readTimeout(DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
            .writeTimeout(DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
            .addNetworkInterceptor(PlexQueryInterceptor())
            .addNetworkInterceptor(LoggingInterceptor())
            .addNetworkInterceptor(SubsonicNetworkStatusInterceptor(ifSubsonic = { if (ifTmp) ifSubsonic else TokenServer.ifSubsonic }))
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            // 可以添加其他配置，比如连接超时、读写超时等
            .build()
        return apiOkHttpClient
    }

    /**
     * 更新token和请求头和请求参数
     */
    open fun updateTokenOrHeadersOrQuery() {
        token = createToken()
        TokenServer.setTokenData(token)
        queryMap = getQueryMapData()
        TokenServer.setQueryMapData(queryMap)
        headerMap = getHeadersMapData()
        TokenServer.setHeaderMapData(headerMap)
    }

    /**
     * 获得token
     */
    protected open fun createToken(): String {
        return ""
    }

    /**
     * 更新请求对象
     */
    protected open fun getQueryMapData(): Map<String, String> {
        return emptyMap()
    }

    /**
     * 更新请求头
     */
    protected open fun getHeadersMapData(): Map<String, String> {
        return emptyMap()
    }

    /**
     * 获得用户接口服务
     */
    open fun userApi(restart: Boolean = false): BaseApi = object : BaseApi {

    }

    /**
     *用户资源接口服务
     */
    open fun userLibraryApi(restart: Boolean = false): BaseApi = object : BaseApi {

    }

    /**
     * 音乐,专辑,艺术家相关接口
     */
    open fun itemApi(restart: Boolean = false): BaseApi = object : BaseApi {

    }

    /**
     * 获取文件图片
     */
    open fun imageApi(restart: Boolean = false): BaseApi = object : BaseApi {

    }

    /**
     * 创建音乐流
     */
    open fun universalAudioApi(restart: Boolean = false): BaseApi = object : BaseApi {

    }

    /**
     * 歌词接口
     */
    open fun lyricsApi(restart: Boolean = false): BaseApi = object : BaseApi {

    }

    /**
     * 用户视图信息
     */
    open fun userViewsApi(restart: Boolean = false): BaseApi = object : BaseApi {

    }

    /**
     * 播放列表接口
     */
    open fun playlistsApi(restart: Boolean = false): BaseApi = object : BaseApi {

    }

    /**
     * 艺术家接口
     */
    open fun artistsApi(restart: Boolean = false): BaseApi = object : BaseApi {

    }

    /**
     * 资源接口
     */
    open fun libraryApi(restart: Boolean = false): BaseApi = object : BaseApi {

    }

    /**
     * 流派接口
     */
    open fun genreApi(restart: Boolean = false): BaseApi = object : BaseApi {

    }

    /**
     * 下载相关接口
     */
    override fun downloadApi(restart: Boolean): IDownLoadApi {
        if (!this::defaultDownloadApi.isInitialized || restart) {
            defaultDownloadApi = instance().create(IDownLoadApi::class.java)
        }
        return defaultDownloadApi
    }

    override fun <T> createApiObj(clazz: Class<T>): T {
        return instance().create(clazz)
    }

    open fun updateTokenHeaderName() {
        if (TokenServer.tokenHeaderName != tokenHeaderName)
            TokenServer.updateTokenHeaderName(tokenHeaderName)
    }

    open fun updateIfSubsonic() {
        TokenServer.updateIfSubsonic(ifSubsonic)
    }

    /**
     * 创建下载链接
     */
    abstract fun createDownloadUrl(itemId: String): String
}