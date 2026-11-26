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

import cn.xybbz.api.AuthenticatedRequestData
import cn.xybbz.api.TokenServer
import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.base.IDownLoadApi
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.enums.subsonic.Status
import cn.xybbz.api.events.ReLoginEventBus
import cn.xybbz.api.exception.ServiceException
import cn.xybbz.api.exception.UnauthorizedException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.takeFrom
import kotlinx.coroutines.CancellationException

abstract class DefaultApiClient : ApiFactory, DownloadFactory {

    lateinit var httpClient: HttpClient
        protected set

    /**
     * HttpClient 是否已完成初始化。
     * 供上层判断当前数据源服务能否安全发起请求，避免外部直接访问 lateinit 字段。
     */
    val isHttpClientInitialized: Boolean
        get() = this::httpClient.isInitialized

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
        if (!ifTmp)
            TokenServer.updateBaseUrl(baseUrl)
        updateTokenHeaderName()
        updateTokenOrHeadersOrQuery()
        // 打印实际创建客户端的子类名称，便于排查不同服务端客户端的初始化来源。
        logger.error { "开始创建 createHttpClient，子类: ${this::class.qualifiedName}" }
        val newHttpClient = provideClient().config {
            expectSuccess = true
            followRedirects = true
            install(DefaultRequest) {
                url {
                    if (baseUrl.isNotBlank())
                        takeFrom(baseUrl)
                }
            }
            installXyAuthenticatedRequest(
                authenticatedRequestDataProvider = {
                    // HttpClient 请求发出时一次性读取当前客户端认证参数，避免请求内参数版本混用。
                    AuthenticatedRequestData(
                        token = token,
                        queryMap = queryMap,
                        headerMap = headerMap,
                        tokenHeaderName = tokenHeaderName
                    )
                }
            )
            /*install(HttpRequestRetry) {
//                maxRetries = 1
            }*/
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

                handleResponseExceptionWithRequest { exception, request ->
                    // 请求上下文：即使响应体为空，日志里也能定位是哪个接口、什么状态码
                    val requestInfo = "${request.method.value} ${request.url}"
                    when (exception) {
                        is ServerResponseException -> {
                            val exceptionResponse = exception.response
                            val exceptionResponseText = readResponseTextSafely(exceptionResponse)
                            logger.error {
                                "服务端异常: $requestInfo -> ${exceptionResponse.status.value} ${exceptionResponse.status.description}, 响应体: $exceptionResponseText"
                            }
                            throw ServiceException(exceptionResponseText, exceptionResponse.status.value)
                        }

                        is ClientRequestException -> {
                            val exceptionResponse = exception.response
                            val exceptionResponseText = readResponseTextSafely(exceptionResponse)
                            logger.error {
                                "客户端异常: $requestInfo -> ${exceptionResponse.status.value} ${exceptionResponse.status.description}, 响应体: $exceptionResponseText"
                            }
                            if (exceptionResponse.status == HttpStatusCode.Unauthorized) {
                                throw UnauthorizedException(
                                    exceptionResponseText,
                                    exceptionResponse.status.value,
                                    exceptionResponse.status.description
                                )
                            } else {
                                throw ServiceException(exceptionResponseText, exceptionResponse.status.value)
                            }
                        }

                        else -> {
                            // 连接中断、超时等异常同样记录，避免静默失败（协程取消不算错误）
                            if (exception !is CancellationException) {
                                logger.error(exception) { "请求异常: $requestInfo" }
                            }
                        }
                    }
                }
            }
        }
        // 重建客户端前关闭旧实例，避免切换服务器或重登录时泄漏连接池。
        if (this::httpClient.isInitialized) {
            httpClient.close()
        }
        httpClient = newHttpClient
        restartApiClients()
        downloadApi(true)
    }

    /**
     * 安全读取异常响应的响应体文本。
     * Ktor 默认校验在构造 ResponseException 时已经读取过一次响应体，这里二次读取可能为空或直接抛异常，
     * 因此读取失败或内容为空时回退到状态描述，保证日志与异常信息不会是一片空白。
     */
    private suspend fun readResponseTextSafely(response: HttpResponse): String {
        val text = try {
            response.bodyAsText()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        return text?.takeIf { it.isNotBlank() } ?: response.status.description
    }

    /**
     * 更新token和请求头和请求参数
     */
    open fun updateTokenOrHeadersOrQuery() {
        token = createToken()
        queryMap = getQueryMapData()
        headerMap = getHeadersMapData()
        /*if (this::httpClient.isInitialized)
            httpClient.config {
                defaultRequest {
                    headers {
                        if (token.isNotBlank())
                            append(tokenHeaderName, token)
                        appendAll(headerMap)
                    }
                    url {
                        parameters.appendAll(queryMap)
                    }

                }
            }*/

        //todo 有待观察
        if (!ifTmp) {
            TokenServer.setAuthenticatedRequestData(
                token = token,
                queryMap = queryMap,
                headerMap = headerMap
            )
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
        if (!ifTmp)
            if (TokenServer.tokenHeaderName != tokenHeaderName)
                TokenServer.updateTokenHeaderName(tokenHeaderName)
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
     * 重新绑定依赖 HttpClient 的接口服务。
     */
    protected open fun restartApiClients() {
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
    }

    /**
     * 清空数据
     */
    override fun release() {
        // release 可能早于 createHttpClient 调用，先做初始化保护。
        if (this::httpClient.isInitialized) {
            httpClient.close()
        }
    }
}
