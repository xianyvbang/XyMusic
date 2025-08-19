package cn.xybbz.common.constants

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
     * 专辑musicList的pageSize
     */
    const val ALBUM_MUSIC_LIST_PAGE_SIZE = 1000

    /**
     * 加载所有数据
     */
    const val PAGE_SIZE_ALL = 1000000

    /**
     * UI页面分页大小
     */
    const val UI_LIST_PAGE = 100

    /**
     * 歌词位置放大
     */
    const val LYRICS_AMPLIFICATION = 10000L

    /**
     * 音乐进度更新间隔
     */
    const val MUSIC_POSITION_UPDATE_INTERVAL = 10L

    /**
     * Paging的专辑分页信息存储id
     */
    const val ALBUM = "album"

    /**
     * Paging的音乐分页信息存储id
     */
    const val MUSIC = "music"

    /**
     * Paging的艺术家分页信息存储id
     */
    const val ARTIST = "artist"

    /**
     * Paging的专辑中音乐列表分页信息存储id
     */
    const val ALBUM_MUSIC = "albumMusic"

    /**
     * Paging的流派列表分页信息存储id
     */
    const val GENRE = "genre"

    /**
     * 收藏
     */
    const val FAVORITE = "Favorite"

    /**
     * 音乐名称为空时显示内容: 未知音乐
     */
    const val UNKNOWN_MUSIC = "未知音乐"

    /**
     * 专辑名称为空时显示内容: 未知专辑
     */
    const val UNKNOWN_ALBUM = "未知专辑"

    /**
     * 艺术家名称为空时显示内容: 未知艺术家
     */
    const val UNKNOWN_ARTIST = "未知艺术家"

    /**
     * 歌单名称为空时显示内容: 未知歌单
     */
    const val UNKNOWN_PLAYLIST = "未知歌单"

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
     * okhttp超时时间
     */
    const val DEFAULT_TIMEOUT_MILLISECONDS = 1000000L

    /**
     * Subsonic的playlist的id后缀
     */
    const val SUBSONIC_PLAYLIST_SUFFIX = "playlist"

    /**
     * 分页失效时间
     */
    const val PAGE_TIME_FAILURE = 20L

    /**
     * log日志前缀
     */
    const val LOG_ERROR_PREFIX = "error"

    /**
     * plex音乐收藏collection的名称
     */
    const val PLEX_MUSIC_COLLECTION_TITLE = "咸鱼音乐音乐收藏"

    /**
     * plex专辑收藏collection的名称
     */
    const val PLEX_ALBUM_COLLECTION_TITLE = "咸鱼音乐专辑收藏"

    /**
     * plex艺术家收藏collection的名称
     */
    const val PLEX_ARTIST_COLLECTION_TITLE = "咸鱼音乐艺术家收藏"
}