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

package cn.xybbz.api

import cn.xybbz.api.constants.ApiConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 公共认证请求状态。
 *
 * @property ready 图片等依赖公共认证参数的请求是否可以开始。
 * @property version token/query/header/baseUrl 变化后的版本号，用于触发封面 URL 重新计算。
 */
data class AuthenticatedRequestState(
    val ready: Boolean = false,
    val version: Int = 0
)

object TokenServer {

    private val requiredQueryKeys = setOf("u", "t", "s", "v", "c", "f")
    private val authenticatedHeaderNames = setOf(
        ApiConstants.AUTHORIZATION,
        ApiConstants.NAVIDROME_AUTHORIZATION,
        ApiConstants.EMBY_AUTHORIZATION,
        ApiConstants.PLEX_AUTHORIZATION
    )

    var token: String = ""
        private set

    private var baseUrl: String = ""
//        private set

    var queryMap: Map<String, String> = emptyMap()
        private set

    var headerMap: Map<String, String> = emptyMap()
        private set

    var tokenHeaderName = ApiConstants.AUTHORIZATION
        private set

    /**
     * 当前公共认证请求状态。
     * 图片加载门禁和认证参数刷新都从这里订阅，避免 ready/version 分散维护。
     */
    private val _authenticatedRequestStateFlow = MutableStateFlow(AuthenticatedRequestState())
    val authenticatedRequestStateFlow: StateFlow<AuthenticatedRequestState> =
        _authenticatedRequestStateFlow.asStateFlow()

    fun setTokenData(token: String) {
        val authenticatedToken = token.takeIf { it.isAuthenticatedTokenValue() } ?: ""
        val ready = hasAuthenticatedRequestData(authenticatedToken, queryMap, headerMap)
        val ifAuthDataChanged = TokenServer.token != authenticatedToken
        val ifReadyChanged = _authenticatedRequestStateFlow.value.ready != ready

        if (!ifAuthDataChanged && !ifReadyChanged) return

        if (ifAuthDataChanged) {
            TokenServer.token = authenticatedToken
        }
        notifyAuthChanged(ready = ready)
    }

    fun setQueryMapData(queryMap: Map<String, String>) {
        val validQueryMap = queryMap.filterNotBlankValues()
        val ready = hasAuthenticatedRequestData(token, validQueryMap, headerMap)
        val ifAuthDataChanged = TokenServer.queryMap != validQueryMap
        val ifReadyChanged = _authenticatedRequestStateFlow.value.ready != ready

        if (!ifAuthDataChanged && !ifReadyChanged) return

        if (ifAuthDataChanged) {
            TokenServer.queryMap = validQueryMap
        }
        notifyAuthChanged(ready = ready)
    }

    fun setHeaderMapData(headerMap: Map<String, String>) {
        val validHeaderMap = headerMap.filterNotBlankValues()
        val ready = hasAuthenticatedRequestData(token, queryMap, validHeaderMap)
        val ifAuthDataChanged = TokenServer.headerMap != validHeaderMap
        val ifReadyChanged = _authenticatedRequestStateFlow.value.ready != ready

        if (!ifAuthDataChanged && !ifReadyChanged) return

        if (ifAuthDataChanged) {
            TokenServer.headerMap = validHeaderMap
        }
        notifyAuthChanged(ready = ready)
    }

    /**
     * 一次性写入当前连接的公共认证参数。
     * 这里批量更新，避免先清空再逐项写入期间触发图片提前加载。
     */
    fun setAuthenticatedRequestData(
        token: String,
        queryMap: Map<String, String>,
        headerMap: Map<String, String>
    ) {
        val validToken = token.takeIf { it.isAuthenticatedTokenValue() } ?: ""
        val validQueryMap = queryMap.filterNotBlankValues()
        val validHeaderMap = headerMap.filterNotBlankValues()
        val ready = hasAuthenticatedRequestData(validToken, validQueryMap, validHeaderMap)
        val ifAuthDataChanged =
            TokenServer.token != validToken ||
                    TokenServer.queryMap != validQueryMap ||
                    TokenServer.headerMap != validHeaderMap
        val ifReadyChanged = _authenticatedRequestStateFlow.value.ready != ready

        if (!ifAuthDataChanged && !ifReadyChanged) {
            return
        }

        if (ifAuthDataChanged) {
            TokenServer.token = validToken
            TokenServer.queryMap = validQueryMap
            TokenServer.headerMap = validHeaderMap
        }
        notifyAuthChanged(ready = ready)
    }

    fun clearAllData() {
        // 清空连接态时先关闭图片加载门禁，防止旧封面请求继续使用过期认证参数。
        val ifAuthDataChanged =
            token.isNotBlank() ||
                    queryMap.isNotEmpty() ||
                    headerMap.isNotEmpty()
        val ifReadyChanged = _authenticatedRequestStateFlow.value.ready

        if (!ifAuthDataChanged && !ifReadyChanged) {
            return
        }

        token = ""
        queryMap = emptyMap()
        headerMap = emptyMap()
        notifyAuthChanged(ready = false)
    }

    fun updateTokenHeaderName(tokenHeaderName: String) {
        if (this.tokenHeaderName == tokenHeaderName) return
        this.tokenHeaderName = tokenHeaderName
        notifyAuthChanged()
    }

    fun updateBaseUrl(baseUrl: String) {
        if (this.baseUrl == baseUrl) return
        this.baseUrl = baseUrl
        notifyAuthChanged()
    }

    private fun notifyAuthChanged(ready: Boolean = _authenticatedRequestStateFlow.value.ready) {
        // 版本号只作为 Compose remember 的刷新 key 使用，不参与业务请求参数。
        val currentState = _authenticatedRequestStateFlow.value
        _authenticatedRequestStateFlow.value = currentState.copy(
            ready = ready,
            version = currentState.version + 1
        )
    }

    private fun hasAuthenticatedRequestData(
        token: String,
        queryMap: Map<String, String>,
        headerMap: Map<String, String>
    ): Boolean {
        return token.isNotBlank() ||
                hasCompleteSubsonicQuery(queryMap) ||
                hasAuthenticatedHeader(headerMap)
    }

    private fun hasCompleteSubsonicQuery(queryMap: Map<String, String>): Boolean {
        return requiredQueryKeys.all { key -> queryMap[key]?.isNotBlank() == true }
    }

    private fun hasAuthenticatedHeader(headerMap: Map<String, String>): Boolean {
        return headerMap.any { (key, value) ->
            key in authenticatedHeaderNames && value.isAuthenticatedTokenValue()
        }
    }

    private fun Map<String, String>.filterNotBlankValues(): Map<String, String> {
        return filterValues { it.isUsefulRequestValue() }
    }

    private fun String.isUsefulRequestValue(): Boolean {
        val value = trim()
        return value.isNotBlank() && value != "null" && value != "Bearer null"
    }

    private fun String.isAuthenticatedTokenValue(): Boolean {
        val value = trim()
        if (!value.isUsefulRequestValue()) {
            return false
        }
        if (value.startsWith("Bearer", ignoreCase = true)) {
            return value.substringAfter("Bearer", "").trim().isUsefulRequestValue()
        }
        if (
            value.startsWith(ApiConstants.AUTHORIZATION_SCHEME, ignoreCase = true) ||
            value.startsWith(ApiConstants.EMBY_AUTHORIZATION_SCHEME, ignoreCase = true)
        ) {
            return Regex("""\bToken="[^"]+"""").containsMatchIn(value)
        }
        return true
    }
}
