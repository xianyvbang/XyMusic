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
    const val PAGE_SIZE_ALL = 1000000

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
    const val ALBUM_MUSIC_LIST_PAGE = 1000

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
     * 本地服务端口号
     */
    const val LOCALHOST_PORT = 5000

    /**
     * 艺术家名称和id分隔符
     */
    const val ARTIST_DELIMITER = ","

    /**
     * 斜杠分隔符 '/'
     */
    const val SLASH_DELIMITER = "/"

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
}