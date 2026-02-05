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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 认证响应
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[AuthenticateResponse]
 * @param [accessToken] 访问令牌
 * @param [serverId] 服务器ID
 * @param [user] 用户信息
 */
@JsonClass(generateAdapter = true)
data class AuthenticateResponse(
    @param:Json(name = "AccessToken")
    val accessToken: String?,
    @param:Json(name = "ServerId")
    val serverId: String?,
    @param:Json(name = "User")
    val user: User?
)

/**
 * 用户
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[User]
 * @param [id] 用户id
 */
@JsonClass(generateAdapter = true)
data class User(
    @param:Json(name = "Id")
    val id: String?,
    @param:Json(name = "Policy")
    val policy:Policy?
)

@JsonClass(generateAdapter = true)
data class Policy(
    /**
     * 是否开启内容下载
     */
    @param:Json(name = "EnableContentDownloading")
    val enableContentDownloading: Boolean?,
    /**
     * 是否开启内容删除
     */
    @param:Json(name = "EnableContentDeletion")
    val enableContentDeletion:Boolean?
)