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

package cn.xybbz.api.okhttp.proxy

import cn.xybbz.api.client.data.ProxyConfig
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI

class DynamicProxySelector() : ProxySelector() {

    @Volatile
    var config: ProxyConfig = ProxyConfig()
        private set

    fun update(config: ProxyConfig) {
        this.config = config
    }

    override fun connectFailed(
        uri: URI?,
        sa: SocketAddress?,
        ioe: IOException?
    ) {
        // 可选：失败上报

    }

    override fun select(uri: URI?): List<Proxy> {
        return if (config.enabled) listOf(
            Proxy(
                Proxy.Type.HTTP,
                InetSocketAddress(config.host, config.port!!)
            ),
            Proxy(
                Proxy.Type.SOCKS,
                InetSocketAddress(config.host, config.port!!)
            ),
            Proxy.NO_PROXY
        ) else listOf(Proxy.NO_PROXY)
    }
}