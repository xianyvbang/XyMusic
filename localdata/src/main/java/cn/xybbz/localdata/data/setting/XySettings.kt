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

package cn.xybbz.localdata.data.setting

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.xybbz.localdata.common.LocalConstants
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.LanguageType
import cn.xybbz.localdata.enums.ThemeTypeEnum
import java.util.UUID

@Entity(tableName = "xy_settings")
data class XySettings(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /**
     * 是否开启边下边播
     */
    val ifEnableEdgeDownload: Boolean = true,
    /**
     * 缓存上限
     */
    val cacheUpperLimit: CacheUpperLimitEnum = CacheUpperLimitEnum.Auto,
    /**
     * 桌面歌词
     */
    val ifDesktopLyrics: Int = LocalConstants.NO,
    /**
     * 倍速播放速度
     *  0.5f -> "0.5倍"
     *  1f -> "正常"
     *  1.5f -> "1.5倍"
     *  2f -> "2倍"
     */
    val doubleSpeed: Float = 1f,
    /**
     * 连接id
     */
    val connectionId: Long? = null,
    /**
     * 链接类型
     */
    val dataSourceType: DataSourceType? = null,
    /**
     * 是否开启所有专辑的播放历史记录
     */
    val ifEnableAlbumHistory: Boolean = false,
    /**
     * 是否允许与其他应用同时播放
     */
    val ifHandleAudioFocus: Boolean = true,
    /**
     * 语言
     */
    val languageType: LanguageType? = null /*LanguageType.ZH_CN*/,
    /**
     * 最新版本获取时间 每次打开如果间隔不超过1小时的话,就不获取新数据
     */
    val latestVersionTime: Long = 0,
    /**
     * 最新版本版本号
     */
    val latestVersion: String = "",
    /**
     * 最新版本下载地址
     */
    val lasestApkUrl: String = "",
    /**
     * 最大同时下载数量
     */
    val maxConcurrentDownloads: Int = 3,
    /**
     * 是否同步播放进度
     */
    val ifEnableSyncPlayProgress: Boolean = true,
    /**
     * 渐入渐出持续时间
     */
    val fadeDurationMs: Long = 300L,
    /**
     * 任意网络是否转码
     */
    val ifTranscoding: Boolean = false,
    /**
     * 转码格式
     */
    val transcodeFormat: String = "mp3",
    /**
     * 移动网络转码比特率
     */
    val mobileNetworkAudioBitRate: Int = 192000,
    /**
     * wifi网络转码比特率
     */
    val wifiNetworkAudioBitRate: Int = 0,

    /**
     * 播放会话ID
     */
    val playSessionId: String = UUID.randomUUID().toString(),
    /**
     *  主题类型
     */
    val themeType: ThemeTypeEnum = ThemeTypeEnum.SYSTEM,
    /**
     * 是否动态颜色
     */
    val isDynamic: Boolean = false,
)
