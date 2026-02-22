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

package cn.xybbz.localdata.enums

/**
 * 数据加载类型
 * 数据源类型枚举
 * @author 刘梦龙
 * @date 2024/11/04
 * @constructor 创建[DataSourceType]
 * @param [describe] 描述
 * @param [title] 标题
 */
enum class DataSourceType(
    val describe: String,
    val title: String,
    val httpPort: Int,
    val httpsPort: Int? = null,
    //是否显示
    val ifShow: Boolean = true,
    //编码
    val code: String,
    //最低支持版本
    val version: String,
    //是否显示数量
    val ifShowCount: Boolean = true,
    //是否在音乐页面显示筛选和排序菜单
    val ifShowMusicDropdownMenu: Boolean = true,
    //是否音乐页面只能选单年
    val ifMusicSelectOneYear: Boolean,
    //音乐页面是否开始和结束年份筛选
    val ifMusicSelectStartEndYear: Boolean,
    //是否音乐页面能进行排序功能
    val ifMusicSort: Boolean,
    //是否音乐页面能进行收藏筛选
    val ifMusicFavoriteFilter: Boolean,

    //是否专辑页面只能选单年
    val ifAlbumSelectOneYear: Boolean,
    //专辑页面是否开始和结束年份筛选
    val ifAlbumSelectStartEndYear: Boolean = true,
    //是否专辑页面能进行排序功能
    val ifAlbumSort: Boolean,
    //是否专辑页面能进行收藏筛选
    val ifAlbumFavoriteFilter: Boolean,

    //是否专辑详情页面只能选单年
    val ifAlbumInfoSelectOneYear: Boolean,
    //专辑详情页面是否开始和结束年份筛选
    val ifAlbumInfoSelectStartEndYear: Boolean = true,
    //是否专辑详情页面能进行排序功能
    val ifAlbumInfoSort: Boolean,
    //是否专辑详情页面能进行收藏筛选
    val ifAlbumInfoFavoriteFilter: Boolean,

    //是否需要输入需要服务端地址
    val ifInputUrl: Boolean = true,
    //是否转码的时候使用hls
    val ifHls: Boolean = false
) {

    JELLYFIN(
        describe = "jellyfin媒体服务器",
        title = "Jellyfin",
        httpPort = 8096,
        httpsPort = 8920,
        code = "1",
        version = "10.10.7",
        ifMusicSelectOneYear = false,
        ifMusicSelectStartEndYear = true,
        ifMusicSort = true,
        ifMusicFavoriteFilter = true,
        ifAlbumSelectOneYear = false,
        ifAlbumSort = true,
        ifAlbumFavoriteFilter = true,
        ifAlbumInfoSelectOneYear = false,
        ifAlbumInfoSort = true,
        ifAlbumInfoFavoriteFilter = true,
        ifHls = true
    ),

    SUBSONIC(
        describe = "Subsonic媒体服务器",
        title = "Subsonic",
        httpPort = 4040,
        httpsPort = 4040,
        code = "2",
        version = "1.16.0",
        ifShowMusicDropdownMenu = false,
        ifMusicSelectOneYear = false,
        ifMusicSelectStartEndYear = false,
        ifMusicSort = false,
        ifMusicFavoriteFilter = false,
        ifAlbumSelectOneYear = false,
        ifAlbumSort = false,
        ifAlbumFavoriteFilter = true,
        ifAlbumInfoSelectOneYear = false,
        ifAlbumInfoSelectStartEndYear = false,
        ifAlbumInfoSort = false,
        ifAlbumInfoFavoriteFilter = false,
    ),

    NAVIDROME(
        describe = "navidrome媒体服务器",
        title = "Navidrome",
        httpPort = 4533,
        httpsPort = 4533,
        code = "3",
        version = "0.56.0",
        ifMusicSelectOneYear = true,
        ifMusicSelectStartEndYear = false,
        ifMusicSort = true,
        ifMusicFavoriteFilter = true,
        ifAlbumSelectOneYear = true,
        ifAlbumSelectStartEndYear = false,
        ifAlbumSort = true,
        ifAlbumFavoriteFilter = true,
        ifAlbumInfoSelectOneYear = true,
        ifAlbumInfoSelectStartEndYear = false,
        ifAlbumInfoSort = true,
        ifAlbumInfoFavoriteFilter = true
    ),

    EMBY(
        describe = "emby媒体服务器",
        title = "Emby",
        httpPort = 8096,
        httpsPort = 8920,
        code = "4",
        version = "4.1.1.0",
        ifMusicSelectOneYear = false,
        ifMusicSelectStartEndYear = true,
        ifMusicSort = true,
        ifMusicFavoriteFilter = true,
        ifAlbumSelectOneYear = false,
        ifAlbumSort = true,
        ifAlbumFavoriteFilter = true,
        ifAlbumInfoSelectOneYear = false,
        ifAlbumInfoSort = true,
        ifAlbumInfoFavoriteFilter = true,
        ifHls = true
    ),


    PLEX(
        describe = "PLEX媒体服务器",
        title = "Plex",
        httpPort = 32400,
        httpsPort = 32400,
        code = "4",
        version = "4.1.1.0",
        ifInputUrl = false,
        ifMusicSelectOneYear = false,
        ifMusicSelectStartEndYear = true,
        ifMusicSort = true,
        ifMusicFavoriteFilter = true,
        ifAlbumSelectOneYear = false,
        ifAlbumSort = true,
        ifAlbumFavoriteFilter = true,
        ifAlbumInfoSelectOneYear = false,
        ifAlbumInfoSort = true,
        ifAlbumInfoFavoriteFilter = true
    );


    fun getDownloadType(): DownloadTypes {
        return when (this) {
            JELLYFIN -> DownloadTypes.JELLYFIN
            SUBSONIC -> DownloadTypes.SUBSONIC
            NAVIDROME -> DownloadTypes.NAVIDROME
            EMBY -> DownloadTypes.EMBY
            PLEX -> DownloadTypes.PLEX
        }
    }
}