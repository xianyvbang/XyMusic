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

package cn.xybbz.api.client

import cn.xybbz.api.TokenServer
import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.base.IDownLoadApi
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.constants.ApiConstants.DEFAULT_TIMEOUT_MILLISECONDS
import cn.xybbz.api.converter.jsonSerializer
import cn.xybbz.api.enums.subsonic.Status
import cn.xybbz.api.events.ReLoginEventBus
import cn.xybbz.api.exception.ServiceException
import cn.xybbz.api.exception.UnauthorizedException
import cn.xybbz.api.okhttp.proxy.ProxyManager
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.appendAll
import kotlinx.serialization.json.Json

abstract class DefaultApiClient : ApiFactory, DownloadFactory {

    lateinit var httpClient: HttpClient
        protected set

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

    //是否临时使用
    var ifTmp = false

    val eventBus = ReLoginEventBus()

    private lateinit var defaultDownloadApi: IDownLoadApi
    protected val logger = KotlinLogging.logger {}
    override fun createHttpClient(baseUrl: String, ifTmp: Boolean) {
        this.ifTmp = ifTmp
        TokenServer.updateBaseUrl(baseUrl)
        if (!ifTmp)
            updateTokenHeaderName()

        //todo 注意关闭
        httpClient = provideClient().config {
            engine {
                proxy = ProxyManager.proxySelector()
            }
            install(DefaultRequest) {
                if (baseUrl.isNotBlank())
                    url(baseUrl)
                headers {
                    append(tokenHeaderName, createToken())
                    val bool = headers.contains(ApiConstants.CUSTOM_IMAGE_HEADER_NAME)
                    if (bool) {
                        append(
                            ApiConstants.AUTHORIZATION,
                            headers[ApiConstants.AUTHORIZATION] ?: ""
                        )
                    }
                }
            }

            install(Logging) {
                logger = object : Logger {
                    private val logger = KotlinLogging.logger {}
                    override fun log(message: String) {
                        logger.info { message }
                    }
                }
                level = LogLevel.HEADERS
            }
            install(ContentNegotiation) {
                json(jsonSerializer)
            }
            install(HttpRequestRetry) {
                maxRetries = 2
            }
            install(HttpTimeout) {
                requestTimeoutMillis = DEFAULT_TIMEOUT_MILLISECONDS
                connectTimeoutMillis = DEFAULT_TIMEOUT_MILLISECONDS
                socketTimeoutMillis = DEFAULT_TIMEOUT_MILLISECONDS
            }

            HttpResponseValidator {
                validateResponse { response ->
                    val any = response.body<Any>()
                    if (any is SubsonicResponse<*>) {
                        val body: SubsonicResponse<*> = any
                        if (body.subsonicResponse.status == Status.Failed) {
                            val error = body.subsonicResponse.error
                            val code = error?.code
                            if (code == 40) {
                                logger.error { "接口报错响应:${body.subsonicResponse}" }
                                throw UnauthorizedException(
                                    msg = "${error.message}",
                                    statusCode = code,
                                    responsePhrase = error.message
                                )
                            } else {
                                throw ServiceException(
                                    message = error?.message ?: "",
                                    code = code
                                )
                            }
                        }
                    }
                }
            }
        }
        updateTokenOrHeadersOrQuery()
        downloadApi(true)
    }

    /**
     * 更新token和请求头和请求参数
     */
    open fun updateTokenOrHeadersOrQuery() {
        token = createToken()
        queryMap = getQueryMapData()
        headerMap = getHeadersMapData()
        if (this::httpClient.isInitialized)
            httpClient.config {
                defaultRequest {
                    headers {
                        if (token.isNotBlank())
                            append(tokenHeaderName, token)
                        appendAll(headerMap)
                    }
                    parameters {
                        appendAll(queryMap)
                    }
                }
            }

        //todo 有待观察
        if (!ifTmp) {
            TokenServer.clearAllData()
            TokenServer.setTokenData(token)
            TokenServer.setQueryMapData(queryMap)
            TokenServer.setHeaderMapData(headerMap)
        }
    }

    fun updateTokenHeaderValue() {
        token = createToken()
        if (this::httpClient.isInitialized)
            httpClient.config {
                defaultRequest {
                    headers {
                        append(tokenHeaderName, token)
                    }
                }
            }
        if (!ifTmp)
            TokenServer.setTokenData(token)
    }

    /**
     * 创建令牌
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
     * 下载相关接口
     */
    override fun downloadApi(restart: Boolean): IDownLoadApi {
        if (!this::defaultDownloadApi.isInitialized || restart) {
            defaultDownloadApi = IDownLoadApi(httpClient)
        }
        return defaultDownloadApi
    }

    open fun updateTokenHeaderName() {
        if (TokenServer.tokenHeaderName != tokenHeaderName)
            TokenServer.updateTokenHeaderName(tokenHeaderName)
    }

    /**
     * 获得前缀地址
     */
    override fun getBaseUrl(): String {
        return TokenServer.baseUrl
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
     * 清空数据
     */
    override fun release() {
        httpClient.close()
    }
}
