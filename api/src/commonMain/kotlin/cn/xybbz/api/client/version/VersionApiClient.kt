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

package cn.xybbz.api.client.version

import cn.xybbz.api.client.ApiFactory
import cn.xybbz.api.client.DownloadFactory
import cn.xybbz.api.client.provideClient
import cn.xybbz.api.client.version.service.GitHubVersionApi
import cn.xybbz.api.constants.ApiConstants.DEFAULT_TIMEOUT_MILLISECONDS
import cn.xybbz.api.converter.jsonSerializer
import cn.xybbz.api.okhttp.proxy.ProxyManager
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.request
import io.ktor.serialization.kotlinx.json.json

class VersionApiClient : ApiFactory, DownloadFactory {

    private lateinit var httpClient: HttpClient

    private lateinit var gitHubVersionApi: GitHubVersionApi

    init {
        createHttpClient("", false)
    }
    private val logger = KotlinLogging.logger {}

    override fun createHttpClient(baseUrl: String, ifTmp: Boolean) {
        httpClient = provideClient().config {
            engine {
                proxy = ProxyManager.proxySelector()
            }
            install(DefaultRequest) {
                url("https://api.github.com/")
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
                    logger.info{"⬅️ Response code: ${response.status}"}
                    logger.info {"⬅️ Response URL: ${response.request.url}"}
                }
            }
        }
    }


    /**
     * 清空数据
     */
    override fun release() {
        httpClient.close()
    }

    /**
     * 获取版本号信息的Api
     */
    override fun downloadApi(restart: Boolean): GitHubVersionApi {
        if (!this::gitHubVersionApi.isInitialized || restart) {
            gitHubVersionApi = GitHubVersionApi(httpClient)
        }
        return gitHubVersionApi
    }

    /**
     * 获得前缀地址
     */
    override fun getBaseUrl(): String {
        return ""
    }
}