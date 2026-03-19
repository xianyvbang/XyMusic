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

package cn.xybbz.api.client.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 用户登录请求实体类
 * @author lml
 * @date 2024/03/17
 * @constructor 创建[ClientLoginInfoReq]
 * @param [username] 用户名
 * @param [password] 密码
 * @param [address] 客户端连接地址
 * @param [appName] 客户端应用名称
 * @param [clientVersion] 服务客户端支持版本
 * @param [serverVersion] 服务端版本
 * @param [serverName] 服务端名称
 * @param [serverId] 服务端ID
 */
@Serializable
data class ClientLoginInfoReq(
    @SerialName(value = "Username")
    val username: String,
    @SerialName(value = "Pw")
    val password: String,
    val address: String,
    val appName: String,
    val clientVersion: String,
    val serverVersion: String? = null,
    val serverName: String? = null,
    val serverId: String? = null
)