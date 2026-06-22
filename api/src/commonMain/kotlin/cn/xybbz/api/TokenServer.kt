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
import kotlinx.coroutines.flow.update

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

/**
 * 公共认证请求参数快照。
 *
 * @property token 当前请求使用的 token。
 * @property queryMap 当前请求追加的公共 query 参数。
 * @property headerMap 当前请求追加的公共 header 参数。
 * @property tokenHeaderName 当前 token 使用的 header 名称。
 * @property baseUrl 当前连接的服务地址。
 */
data class AuthenticatedRequestData(
    val token: String = "",
    val queryMap: Map<String, String> = emptyMap(),
    val headerMap: Map<String, String> = emptyMap(),
    val tokenHeaderName: String = ApiConstants.AUTHORIZATION,
    val baseUrl: String = ""
)

object TokenServer {

    private val requiredQueryKeys = setOf("u", "t", "s", "v", "c", "f")
    private val authenticatedHeaderNames = setOf(
        ApiConstants.AUTHORIZATION,
        ApiConstants.NAVIDROME_AUTHORIZATION,
        ApiConstants.EMBY_AUTHORIZATION,
        ApiConstants.PLEX_AUTHORIZATION
    )

    /**
     * 当前公共认证请求参数快照。
     */
    private val _authenticatedRequestDataFlow = MutableStateFlow(AuthenticatedRequestData())
    val authenticatedRequestDataFlow: StateFlow<AuthenticatedRequestData> =
        _authenticatedRequestDataFlow.asStateFlow()

    val authenticatedRequestData: AuthenticatedRequestData
        get() = _authenticatedRequestDataFlow.value

    val token: String
        get() = authenticatedRequestData.token

    private val baseUrl: String
        get() = authenticatedRequestData.baseUrl

    val queryMap: Map<String, String>
        get() = authenticatedRequestData.queryMap

    val headerMap: Map<String, String>
        get() = authenticatedRequestData.headerMap

    val tokenHeaderName: String
        get() = authenticatedRequestData.tokenHeaderName

    /**
     * 当前公共认证请求状态。
     * 图片加载门禁和认证参数刷新都从这里订阅，避免 ready/version 分散维护。
     */
    private val _authenticatedRequestStateFlow = MutableStateFlow(AuthenticatedRequestState())
    val authenticatedRequestStateFlow: StateFlow<AuthenticatedRequestState> =
        _authenticatedRequestStateFlow.asStateFlow()

    fun setTokenData(token: String) {
        val authenticatedToken = token.takeIf { it.isAuthenticatedTokenValue() } ?: ""
        updateAuthenticatedRequestData { currentData ->
            currentData.copy(token = authenticatedToken)
        }
    }

    fun setQueryMapData(queryMap: Map<String, String>) {
        val validQueryMap = queryMap.filterNotBlankValues()
        updateAuthenticatedRequestData { currentData ->
            currentData.copy(queryMap = validQueryMap)
        }
    }

    fun setHeaderMapData(headerMap: Map<String, String>) {
        val validHeaderMap = headerMap.filterNotBlankValues()
        updateAuthenticatedRequestData { currentData ->
            currentData.copy(headerMap = validHeaderMap)
        }
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
        updateAuthenticatedRequestData { currentData ->
            currentData.copy(
                token = validToken,
                queryMap = validQueryMap,
                headerMap = validHeaderMap
            )
        }
    }

    fun clearAllData() {
        // 清空连接态时先关闭图片加载门禁，防止旧封面请求继续使用过期认证参数。
        updateAuthenticatedRequestData { currentData ->
            currentData.copy(
                token = "",
                queryMap = emptyMap(),
                headerMap = emptyMap()
            )
        }
    }

    fun updateTokenHeaderName(tokenHeaderName: String) {
        updateAuthenticatedRequestData { currentData ->
            currentData.copy(tokenHeaderName = tokenHeaderName)
        }
    }

    fun updateBaseUrl(baseUrl: String) {
        updateAuthenticatedRequestData { currentData ->
            currentData.copy(baseUrl = baseUrl)
        }
    }

    /**
     * 原子更新公共认证参数快照，并同步 ready/version 状态。
     */
    private fun updateAuthenticatedRequestData(
        transform: (AuthenticatedRequestData) -> AuthenticatedRequestData
    ) {
        var ifAuthDataChanged = false
        _authenticatedRequestDataFlow.update { currentData ->
            val nextData = transform(currentData)
            if (nextData != currentData) {
                ifAuthDataChanged = true
            }
            nextData
        }

        val ready = hasAuthenticatedRequestData(authenticatedRequestData)
        val ifReadyChanged = _authenticatedRequestStateFlow.value.ready != ready

        if (!ifAuthDataChanged && !ifReadyChanged) return

        notifyAuthChanged(ready = ready)
    }

    private fun notifyAuthChanged(ready: Boolean = _authenticatedRequestStateFlow.value.ready) {
        // 版本号只作为 Compose remember 的刷新 key 使用，不参与业务请求参数。
        _authenticatedRequestStateFlow.update { currentState ->
            currentState.copy(
                ready = ready,
                version = currentState.version + 1
            )
        }
    }

    private fun hasAuthenticatedRequestData(data: AuthenticatedRequestData): Boolean {
        return hasAuthenticatedRequestData(
            token = data.token,
            queryMap = data.queryMap,
            headerMap = data.headerMap
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
