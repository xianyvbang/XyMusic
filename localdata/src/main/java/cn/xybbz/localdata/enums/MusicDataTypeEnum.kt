package cn.xybbz.localdata.enums

import kotlinx.serialization.Serializable

@Serializable
enum class MusicDataTypeEnum(val message: String) {

    /**
     * 专辑/音乐首页
     */
    HOME("专辑/音乐首页"),

    /**
     * 收藏数据
     */
    FAVORITE("收藏数据"),

    /**
     * 专辑页面
     */
    ALBUM("专辑"),

    /**
     * 艺术家
     */
    ARTIST("艺术家"),

    /**
     * 播放历史
     */
    PLAY_HISTORY("播放历史"),

    /**
     * 播放列表
     */
    PLAY_QUEUE("播放列表"),

    /**
     * 最多播放音乐/专辑
     */
    MAXIMUM_PLAY("最多播放音乐/专辑"),

    /**
     * 最新数据
     */
    NEWEST("最新数据"),

    /**
     * 流派
     */
    GENRE("流派"),

    /**
     * 歌单音乐
     */
    PLAYLIST("歌单")
}