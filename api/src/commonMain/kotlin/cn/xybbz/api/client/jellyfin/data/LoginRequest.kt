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

package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.client.data.ClientLoginInfoReq
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 登录请求
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[LoginRequest]
 * @param [username] 用户名
 * @param [password] 密码
 */
@Serializable
data class LoginRequest(
    @SerialName(value = "Username")
    val username: String,
    @SerialName(value = "Pw")
    val password: String
)

/**
 * 转换为Jellyfin和Emby登录请求
 * @author xybbz
 * @date 2025/12/02
 * @return [LoginRequest]
 */
fun ClientLoginInfoReq.toLogin(): LoginRequest {
    return LoginRequest(this.username, this.password)
}