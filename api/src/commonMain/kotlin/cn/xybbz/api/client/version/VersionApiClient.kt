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
import cn.xybbz.api.constants.ApiConstants
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.statement.request

class VersionApiClient : ApiFactory, DownloadFactory {

    private lateinit var httpClient: HttpClient

    private lateinit var gitHubVersionApi: GitHubVersionApi

    init {
        createHttpClient("", false)
    }
    private val logger = KotlinLogging.logger {}

    override fun createHttpClient(baseUrl: String, ifTmp: Boolean) {
        val newHttpClient = provideClient().config {
            install(DefaultRequest) {
                url("${ApiConstants.HTTPS}api.github.com/")
            }
            install(HttpRequestRetry) {
                maxRetries = 2
            }

            HttpResponseValidator {
                validateResponse { response ->
                    logger.info{"⬅️ Response code: ${response.status}"}
                    logger.info {"⬅️ Response URL: ${response.request.url}"}
                }
            }
        }
        // 重建客户端前关闭旧实例，避免重复初始化时泄漏连接池。
        if (this::httpClient.isInitialized) {
            httpClient.close()
        }
        httpClient = newHttpClient
        // 重建版本接口包装器，避免继续持有已关闭客户端。
        downloadApi(true)
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

    /**
     * 获取版本号信息的Api
     */
    override fun downloadApi(restart: Boolean): GitHubVersionApi {
        if (!this::gitHubVersionApi.isInitialized || restart) {
            gitHubVersionApi = GitHubVersionApi(httpClient)
        }
        return gitHubVersionApi
    }
}
