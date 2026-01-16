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

package cn.xybbz.common.constants

import androidx.annotation.StringRes
import cn.xybbz.R

object Constants {

    const val COMPOSITION_LOCAL_ERROR = "没有找到上下文"

    /**
     * 链接用户信息分页长度
     */
    const val MIN_PAGE = 20

    /**
     * 0
     */
    const val ZERO = 0

    /**
     * 加载所有数据
     */
    const val PAGE_SIZE_ALL = 100000

    /**
     * UI页面分页大小
     */
    const val UI_LIST_PAGE = 100

    /**
     * 初始化加载数据大小
     */
    const val UI_INIT_LIST_PAGE = 40

    /**
     * 预加载距离-距离底部多少item加载下一页
     */
    const val UI_PREFETCH_DISTANCE = 5

    /**
     * 专辑音乐的分页大小
     */
    const val ALBUM_MUSIC_LIST_PAGE = 500

    /**
     * 歌词位置放大
     */
    const val LYRICS_AMPLIFICATION = 10000L

    /**
     * 音乐进度更新间隔
     */
    const val MUSIC_POSITION_UPDATE_INTERVAL = 10L


    /**
     * 音乐名称为空时显示内容: 未知音乐
     */
    @StringRes
    val UNKNOWN_MUSIC: Int = R.string.unknown_music

    /**
     * 专辑名称为空时显示内容: 未知专辑
     */
    @StringRes
    val UNKNOWN_ALBUM: Int = R.string.unknown_album

    /**
     * 艺术家名称为空时显示内容: 未知艺术家
     */
    @StringRes
    val UNKNOWN_ARTIST: Int = R.string.unknown_artist

    /**
     * 歌单名称为空时显示内容: 未知歌单
     */
    @StringRes
    val UNKNOWN_PLAYLIST: Int = R.string.unknown_playlist

    @StringRes
    val UNKNOWN: Int = R.string.unknown

    /**
     * MusicPlayer自定义按钮主动调用类型
     */
    const val MUSIC_PLAY_CUSTOM_COMMAND_TYPE_KEY = "MUSIC_PLAY_CUSTOM_COMMAND_TYPE_KEY"
    const val MUSIC_PLAY_CUSTOM_COMMAND_TYPE = "1"

    /**
     * -1
     */
    const val MINUS_ONE_INT: Int = -1

    /**
     * 音乐媒体栏额外收藏按钮key
     */
    const val SAVE_TO_FAVORITES = "favorite"

    /**
     * 音乐媒体栏额外取消收藏按钮key
     */
    const val REMOVE_FROM_FAVORITES = "remove_favorite"

    /**
     * 艺术家名称和id分隔符
     */
    const val ARTIST_DELIMITER_SEMICOLON = ";"

    /**
     * 斜杠分隔符 '/'
     */
    const val SLASH_DELIMITER = "/"

    /**
     * 竖线分隔符 '|'
     */
    const val VERTICAL_DELIMITER = "|"

    /**
     * Subsonic的playlist的id后缀
     */
    const val SUBSONIC_PLAYLIST_SUFFIX = "playlist"

    /**
     * 分页失效时间
     */
    const val PAGE_TIME_FAILURE = 20L

    /**
     * home页面数据失效时间
     */
    const val HOME_PAGE_TIME_FAILURE = 10L

    /**
     * 艺术家分页失效时间
     */
    const val ARTIST_PAGE_TIME_FAILURE = 60L

    /**
     * 相似歌曲分页大小
     */
    const val SIMILAR_MUSIC_LIST_PAGE = 6

    /**
     * 艺术家热门歌曲分页大小
     */
    const val ARTIST_HOT_MUSIC_LIST_PAGE = 4

    /**
     * log日志前缀
     */
    const val LOG_ERROR_PREFIX = "error"

    /**
     * plex音乐收藏collection的名称
     */
    @StringRes
    val PLEX_MUSIC_COLLECTION_TITLE: Int = R.string.plex_music_collection_title

    /**
     * plex专辑收藏collection的名称
     */
    @StringRes
    val PLEX_ALBUM_COLLECTION_TITLE: Int = R.string.plex_album_collection_title

    /**
     * plex艺术家收藏collection的名称
     */
    @StringRes
    val PLEX_ARTIST_COLLECTION_TITLE: Int = R.string.plex_artist_collection_title

    /**
     * 下载唯一id
     */
    const val DOWNLOAD_ID = "download_id"

    /**
     * 登陆后获取数据的worker参数
     */
    const val CONNECTION_ID = "connection_id"

    /**
     * app文件夹名称
     */
    const val APP_NAME = "XyMusic"

    /**
     * 默认代理地址
     */
    const val DEFAULT_PROXY_ADDRESS = "127.0.0.1:7890"
}