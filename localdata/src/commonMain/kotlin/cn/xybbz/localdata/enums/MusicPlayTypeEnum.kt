package cn.xybbz.localdata.enums

/**
 * 数据所处页面数据类型
 */
enum class MusicPlayTypeEnum(val code: Int, val message: String) {

    /**
     * 基础数据
     */
    FOUNDATION(0, "基础数据"),
    /**
     * 专辑/音乐首页
     */
    HOME(1,"专辑/音乐首页"),
    /**
     * 收藏数据
     */
    FAVORITE(1, "收藏数据"),
    /**
     * 专辑页面
     */
    ALBUM(4, "专辑"),
    /**
     * 艺术家
     */
    ARTIST(7,"艺术家"),
    /**
     * 随机列表
     */
    RANDOM(9,"随机列表"),
    /**
     * 播放历史
     */
    RECORD(5,"播放历史"),
    /**
     * 播放列表
     */
    PLAY_QUEUE(6,"播放列表"),
    /**
     * 最多播放音乐
     */
    MAX_PLAY(7,"最多播放音乐"),
    /**
     * 搜索页面
     */
    SEARCH(8,"搜索页面"),
    /**
     * 最新数据
     */
    NEW_DATA(9,"最新数据"),
    /**
     * 流派
     */
    GENRE(10,"流派"),
    /**
     * 歌单音乐
     */
    PLAYLIST(11,"歌单"),
    /**
     * 每日推荐
     */
    RECOMMEND(12,"每日推荐"),
}