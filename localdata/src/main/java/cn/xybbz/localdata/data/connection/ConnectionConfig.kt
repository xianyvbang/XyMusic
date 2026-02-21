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

package cn.xybbz.localdata.data.connection

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.xybbz.localdata.enums.DataSourceType
import java.time.Instant

@Entity(tableName = "xy_connection_config")
data class ConnectionConfig(
    /**
     * 链接id
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /**
     * 链接服务id
     */
    val serverId: String = "",
    /**
     * 服务端名称
     */
    val serverName:String = "",
    /**
     * 服务端版本
     */
    val serverVersion: String,

    /**
     * 连接设备标识
     */
    val deviceId:String = "",

    /**
     * 链接名称
     */
    val name: String,
    /**
     * 链接地址 http/https
     */
    val address: String,
    /**
     * 链接类型
     */
    val type: DataSourceType,
    /**
     * 用户id
     */
    val userId: String,
    /**
     * 用户名
     */
    val username: String = "",
    /**
     * 用户token
     */
    val accessToken: String? = "",
    /**
     * 当前密码 加密存储
     */
    val currentPassword: String = "",
    /**
     * AES iv
     */
    val iv: String = "",
    /**
     * AES key
     */
    val key: String = "",

    /**
     * 媒体库Id
     */
    val libraryId: String? = null,

    /**
     * 扩展信息
     */
    val extendInfo: String? = null,
    /**
     * 最后登陆时间
     */
    val lastLoginTime: Long = Instant.now().toEpochMilli(),
    /**
     * 更新数据时间
     */
    val updateTime: Long = System.currentTimeMillis(),
    /**
     * 第一次链接时间
     */
    val createTime: Long = System.currentTimeMillis(),

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
    val machineIdentifier:String? = null,
    /**
     * 是否开启下载功能
     */
    val ifEnabledDownload: Boolean,
    /**
     * 是否开启删除功能
     */
    val ifEnabledDelete: Boolean,
    /**
     * 是否下次登录的时候强制登录
     */
    val ifForceLogin: Boolean,
)
