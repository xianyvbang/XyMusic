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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 认证响应
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[AuthenticateResponse]
 * @param [accessToken] 访问令牌
 * @param [serverId] 服务器ID
 * @param [user] 用户信息
 */
@Serializable
data class AuthenticateResponse(
    @SerialName(value = "AccessToken")
    val accessToken: String?,
    @SerialName(value = "ServerId")
    val serverId: String?,
    @SerialName(value = "User")
    val user: User?
)

/**
 * 用户
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[User]
 * @param [id] 用户id
 */
@Serializable
data class User(
    @SerialName(value = "Id")
    val id: String?,
    @SerialName(value = "Policy")
    val policy:Policy?
)

@Serializable
data class Policy(
    /**
     * 是否开启内容下载
     */
    @SerialName(value = "EnableContentDownloading")
    val enableContentDownloading: Boolean?,
    /**
     * 是否开启内容删除
     */
    @SerialName(value = "EnableContentDeletion")
    val enableContentDeletion:Boolean?
)