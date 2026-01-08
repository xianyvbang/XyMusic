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

package cn.xybbz.router

import androidx.navigation3.runtime.NavKey
import cn.xybbz.common.enums.ConnectionUiType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 路由地址常量
 * @author 刘梦龙
 * @date 2024/03/06
 * @constructor 创建[RouterConstants]
 */
@Serializable
sealed class RouterConstants() : NavKey

/**
 * 首页
 */
@Serializable
@SerialName("Home")
data object Home : RouterConstants()

/**
 * 搜索页面
 */
@Serializable
@SerialName("Search")
data object Search : RouterConstants()

/**
 * 音乐列表页面
 */
@Serializable
@SerialName("Music")
data object Music : RouterConstants()

/**
 * 艺术家
 */
@Serializable
@SerialName("Artist")
data object Artist : RouterConstants()

/**
 * 设置
 */
@Serializable
@SerialName("Setting")
data object Setting : RouterConstants()

/**
 * 专辑详细
 */
@Serializable
@SerialName("AlbumInfo")
data class AlbumInfo(
    /**
     * 专辑id/歌单id
     */
    val itemId: String,
    /**
     * 数据类型0专辑,1歌单
     */
    val dataType: MusicDataTypeEnum
) : RouterConstants()

/**
 * 收藏列表
 */
@Serializable
@SerialName("FavoriteList")
data object FavoriteList : RouterConstants()

/**
 * 专辑推荐页面
 */
@Serializable
@SerialName("Album")
data object Album : RouterConstants()

/**
 * 连接管理
 */
@Serializable
@SerialName("ConnectionManagement")
data object ConnectionManagement : RouterConstants()

/**
 * 艺术家详细
 */
@Serializable
@SerialName("ArtistInfo")
data class ArtistInfo(
    val artistId: String
) : RouterConstants()

/**
 * 连接服务页面
 */
@Serializable
@SerialName("Connection")
data class Connection(
    val connectionUiType: ConnectionUiType? = null
) : RouterConstants()

/**
 * 连接详情
 */
@Serializable
@SerialName("ConnectionInfo")
data class ConnectionInfo(
    val connectionId: Long
) : RouterConstants()

/**
 * 存储管理
 */
@Serializable
@SerialName("MemoryManagement")
data object MemoryManagement : RouterConstants()

/**
 * 界面设置
 */
@Serializable
@SerialName("InterfaceSetting")
data object InterfaceSetting : RouterConstants()

/**
 * 语言设置
 */
@Serializable
@SerialName("LanguageConfig")
data object LanguageConfig : RouterConstants()

/**
 * 流派列表
 */
@Serializable
@SerialName("Genres")
data object Genres : RouterConstants()

/**
 * 流派详情
 */
@Serializable
@SerialName("GenreInfo")
data class GenreInfo(
    val genreId: String
) : RouterConstants()

/**
 * 关于页面
 */
@Serializable
@SerialName("About")
data object About : RouterConstants()

/**
 * 缓存大小设置页面
 */
@Serializable
@SerialName("CacheLimit")
data object CacheLimit : RouterConstants()

/**
 * 选择媒体库页面
 */
@Serializable
@SerialName("SelectLibrary")
data class SelectLibrary(
    val connectionId: Long,
    val libraryId: String?
) : RouterConstants()

/**
 * 每日推荐页面
 */
@Serializable
@SerialName("DailyRecommend")
data object DailyRecommend : RouterConstants()

/**
 * 下载页面
 */
@Serializable
@SerialName("Download")
data object Download : RouterConstants()

/**
 * 本地页面
 */
@Serializable
@SerialName("Local")
data object Local : RouterConstants()

/**
 * 设置背景图片页面
 */
@Serializable
@SerialName("SetBackgroundImage")
data object SetBackgroundImage : RouterConstants()

/**
 * 设置代理页面
 */
@Serializable
@SerialName("ProxyConfig")
data object ProxyConfig : RouterConstants()

/**
 * 在线音乐品质页面
 */
@Serializable
@SerialName("StreamingQuality")
data object StreamingQuality : RouterConstants()
