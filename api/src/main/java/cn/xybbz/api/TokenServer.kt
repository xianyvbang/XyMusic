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

object TokenServer {

    var token: String = ""
        private set

    var baseUrl: String = ""
        private set

    var queryMap: Map<String, String> = emptyMap()
        private set

    var headerMap: Map<String, String> = emptyMap()
        private set

    var tokenHeaderName = ApiConstants.AUTHORIZATION
        private set

    fun setTokenData(token: String) {
        TokenServer.token = token
    }

    fun setQueryMapData(queryMap: Map<String, String>) {
        TokenServer.queryMap = queryMap
    }

    fun setHeaderMapData(headerMap: Map<String, String>) {
        TokenServer.headerMap = headerMap
    }

    fun clearAllData() {
        setTokenData("")
        setQueryMapData(emptyMap())
        setHeaderMapData(emptyMap())
    }

    fun updateTokenHeaderName(tokenHeaderName: String) {
        this.tokenHeaderName = tokenHeaderName
    }

    fun updateBaseUrl(baseUrl: String) {
        this.baseUrl = baseUrl
    }
}
