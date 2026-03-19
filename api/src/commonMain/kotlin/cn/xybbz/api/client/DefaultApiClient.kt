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
import cn.xybbz.api.base.IDownLoadApi
import cn.xybbz.api.client.subsonic.data.SubsonicDefaultResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.constants.ApiConstants.DEFAULT_TIMEOUT_MILLISECONDS
import cn.xybbz.api.converter.json
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
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.util.appendAll
import io.ktor.util.appendIfNameAndValueAbsent

abstract class DefaultApiClient : ApiFactory, DownloadFactory {

    private lateinit var httpClient: HttpClient

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

    val eventBus = ReLoginEventBus()

    private lateinit var defaultDownloadApi: IDownLoadApi
    private val logger = KotlinLogging.logger {}
    override fun createHttpClient(baseUrl: String, ifTmp: Boolean) {
        TokenServer.updateBaseUrl(baseUrl)
        updateTokenHeaderName()
        //todo 注意关闭
        httpClient = provideClient().config {
            engine {
                proxy = ProxyManager.proxySelector().config
            }
            install(DefaultRequest) {
                url(baseUrl)
                headers {
                    append(tokenHeaderName, createToken())
                    val bool = headers.contains(ApiConstants.CUSTOM_IMAGE_HEADER_NAME)
                    if (bool) {
                        append(
                            ApiConstants.AUTHORIZATION,
                            headers.get(ApiConstants.AUTHORIZATION) ?: ""
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
                json
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
                    val body: SubsonicResponse<SubsonicDefaultResponse> = response.body()
                    if (body.subsonicResponse.status == Status.Failed){
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

        downloadApi(true)
    }

    /**
     * 更新token和请求头和请求参数
     */
    open fun updateTokenOrHeadersOrQuery() {
        token = createToken()
        queryMap = getQueryMapData()
        headerMap = getHeadersMapData()
        httpClient.config {
            defaultRequest {
                headers {
                    append(tokenHeaderName, token)
                    appendAll(headerMap)
                }
                parameters {
                    appendAll(queryMap)
                }
            }
        }

        //todo 有待观察
        TokenServer.clearAllData()
        TokenServer.setTokenData(token)
        TokenServer.setQueryMapData(queryMap)
        TokenServer.setHeaderMapData(headerMap)
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
            defaultDownloadApi = instance().create(IDownLoadApi::class.java)
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
     * 清空数据
     */
    override fun release() {
        httpClient.close()
    }
}
