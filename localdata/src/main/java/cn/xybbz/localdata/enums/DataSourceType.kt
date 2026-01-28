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
 * @param [img] 图片网址
 * @param [port] 端口号
 * @param [ifScan] 是否需要扫描
 * @param [ifUpdatePassword] 是否可以更改密码
 * @param [ifSelectProtocolType] 是否可以选择协议类型
 * @param [options] 协议列表
 */
enum class DataSourceType(
    val describe: String,
    val title: String,
    val httpPort: Int,
    val httpsPort:Int? = null,
    val ifSelectProtocolType: Boolean = false,
    val ifShow: Boolean = true,
    val options: List<String> = emptyList(),
    val code: String,
    val version: String,
    //是否显示数量
    val ifShowCount: Boolean,

    //是否音乐页面只能选单年
    val ifMusicSelectOneYear: Boolean,
    //是否音乐页面能进行排序功能
    val ifMusicSort: Boolean,
    //是否音乐页面能进行收藏筛选
    val ifMusicFavoriteFilter: Boolean,

    //是否专辑页面只能选单年
    val ifAlbumSelectOneYear: Boolean,
    //是否专辑页面能进行排序功能
    val ifAlbumSort: Boolean,
    //是否专辑页面能进行收藏筛选
    val ifAlbumFavoriteFilter: Boolean,

    //是否专辑详情页面只能选单年
    val ifAlbumInfoSelectOneYear: Boolean,
    //是否专辑详情页面能进行排序功能
    val ifAlbumInfoSort: Boolean,
    //是否专辑详情页面能进行收藏筛选
    val ifAlbumInfoFavoriteFilter: Boolean,

    //是否需要选择开始和结束年进行筛选
    val ifStartEndYear: Boolean,
    //是否能进行年筛选
    val ifYearFilter: Boolean = true,
    //是否支持删除功能
    val ifDelete: Boolean,
    //是否艺术家可以进行收藏筛选
    val ifArtistFavorite: Boolean,
    //是否需要输入需要服务端地址
    val ifInputUrl: Boolean = true,
    //是否转码的时候使用hls
    val ifHls: Boolean = false,
) {

    JELLYFIN(
        describe = "jellyfin媒体服务器",
        title = "Jellyfin",
        httpPort = 8096,
        httpsPort = 8920,
        ifSelectProtocolType = true,
        options = listOf("http://", "https://"),
        code = "1",
        version = "10.10.7",
        ifShowCount = true,

        ifMusicSelectOneYear = false,
        ifMusicSort = true,
        ifMusicFavoriteFilter = true,
        ifAlbumSelectOneYear = false,
        ifAlbumSort = true,
        ifAlbumFavoriteFilter = true,
        ifAlbumInfoSelectOneYear = false,
        ifAlbumInfoSort = true,
        ifAlbumInfoFavoriteFilter = true,
        ifStartEndYear = false,
        ifDelete = true,
        ifArtistFavorite = true,
        ifHls = true
    ),

    SUBSONIC(
        describe = "Subsonic媒体服务器",
        title = "Subsonic",
        httpPort = 4040,
        httpsPort = 4040,
        ifSelectProtocolType = true,
        options = listOf("http://", "https://"),
        code = "2",
        version = "1.16.0",
        ifShowCount = true,
        ifDelete = false,
        ifMusicSelectOneYear = false,
        ifMusicSort = false,
        ifMusicFavoriteFilter = false,
        ifAlbumSelectOneYear = true,
        ifAlbumSort = false,
        ifAlbumFavoriteFilter = true,
        ifAlbumInfoSelectOneYear = false,
        ifAlbumInfoSort = false,
        ifAlbumInfoFavoriteFilter = false,
        ifStartEndYear = true,
        ifArtistFavorite = true,
    ),

    NAVIDROME(
        describe = "navidrome媒体服务器",
        title = "Navidrome",
        httpPort = 4533,
        httpsPort = 4533,
        ifSelectProtocolType = true,
        options = listOf("http://", "https://"),
        code = "3",
        version = "0.56.0",
        ifShowCount = true,
        ifDelete = false,
        ifMusicSelectOneYear = true,
        ifMusicSort = true,
        ifMusicFavoriteFilter = true,
        ifAlbumSelectOneYear = true,
        ifAlbumSort = true,
        ifAlbumFavoriteFilter = true,
        ifAlbumInfoSelectOneYear = true,
        ifAlbumInfoSort = true,
        ifAlbumInfoFavoriteFilter = true,
        ifStartEndYear = false,
        ifArtistFavorite = true,
    ),

    EMBY(
        describe = "emby媒体服务器",
        title = "Emby",
        httpPort = 8096,
        httpsPort = 8920,
        ifSelectProtocolType = true,
        options = listOf("http://", "https://"),
        code = "4",
        version = "4.1.1.0",
        ifShowCount = true,
        ifDelete = true,
        ifMusicSelectOneYear = false,
        ifMusicSort = true,
        ifMusicFavoriteFilter = true,
        ifAlbumSelectOneYear = false,
        ifAlbumSort = true,
        ifAlbumFavoriteFilter = true,
        ifAlbumInfoSelectOneYear = false,
        ifAlbumInfoSort = true,
        ifAlbumInfoFavoriteFilter = true,
        ifStartEndYear = false,
        ifYearFilter = false,
        ifArtistFavorite = true,
        ifHls =  true
    ),


    PLEX(
        describe = "PLEX媒体服务器",
        title = "Plex",
        httpPort = 32400,
        httpsPort = 32400,
        ifSelectProtocolType = true,
        options = listOf("http://", "https://"),
        code = "4",
        version = "4.1.1.0",
        ifShowCount = true,
        ifDelete = true,
        ifInputUrl = false,
        ifMusicSelectOneYear = true,
        ifMusicSort = true,
        ifMusicFavoriteFilter = true,
        ifAlbumSelectOneYear = true,
        ifAlbumSort = true,
        ifAlbumFavoriteFilter = true,
        ifAlbumInfoSelectOneYear = true,
        ifAlbumInfoSort = true,
        ifAlbumInfoFavoriteFilter = true,
        ifStartEndYear = true,
        ifArtistFavorite = true,
    );



    fun getDownloadType(): DownloadTypes{
        return when(this){
            JELLYFIN -> DownloadTypes.JELLYFIN
            SUBSONIC -> DownloadTypes.SUBSONIC
            NAVIDROME -> DownloadTypes.NAVIDROME
            EMBY -> DownloadTypes.EMBY
            PLEX -> DownloadTypes.PLEX
        }
    }
}