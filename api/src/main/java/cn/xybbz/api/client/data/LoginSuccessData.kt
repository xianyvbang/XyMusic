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

/**
 * 登录成功后数据
 * @author 刘梦龙
 * @date 2025/05/09
 * @constructor 创建[LoginSuccessData]
 * @param [userId] 用户身份
 * @param [accessToken] 访问令牌
 */
data class LoginSuccessData(
    val userId: String?,
    val accessToken: String?,
    val serverId: String?,
    val serverName: String? = null,
    val version: String? = null,
    /**
     * 是否开启下载功能
     */
    val ifEnabledDownload: Boolean,
    /**
     * 是否开启删除功能
     */
    val ifEnabledDelete: Boolean,
    /**
     * navidrome扩展SubsonicToken
     */
    val navidromeExtendToken: String? = null,

    /**
     * navidrome扩展扩展SubsonicSalt
     */
    val navidromeExtendSalt: String? = null,

    /**
     * plex的机器标识符
     */
    val machineIdentifier:String? = null
)