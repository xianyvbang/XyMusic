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

package cn.xybbz.config.proxy

import cn.xybbz.api.okhttp.proxy.ProxyManager
import cn.xybbz.common.enums.AllDataEnum
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.proxy.XyProxyConfig

class ProxyConfigServer(private val db: DatabaseClient) {

    lateinit var proxyConfig: XyProxyConfig

    suspend fun initConfig() {
        proxyConfig = db.proxyConfigDao.getConfig() ?: XyProxyConfig()
        updateProxyConfig()
    }

    fun get(): XyProxyConfig {
        return proxyConfig
    }

    /**
     * 更新配置
     */
    fun updateProxyConfig() {
        if (proxyConfig.enabled && proxyConfig.address.isNotBlank()) {
            ProxyManager.updateProxy(
                proxyConfig.address
            )
        } else
            ProxyManager.clearProxy()
    }

    suspend fun updateAddressAndEnabled(address: String, enabled: Boolean){
        proxyConfig = get().copy(address = address, enabled = enabled)
        if (proxyConfig.id != AllDataEnum.All.code) {
            db.proxyConfigDao.updateAddressAndEnabled(address, enabled, get().id)
        }else {
            val configId =
                db.proxyConfigDao.save(proxyConfig)
            proxyConfig = get().copy(id = configId)
        }
        updateProxyConfig()
    }

}