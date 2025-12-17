package cn.xybbz.localdata.enums

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Keep
enum class MusicDataTypeEnum(val message: String) {

    /**
     * 专辑/音乐首页
     */
    @SerialName("home")
    HOME("专辑/音乐首页"),

    /**
     * 收藏数据
     */
    @SerialName("favorite")
    FAVORITE("收藏数据"),

    /**
     * 专辑页面
     */
    @SerialName("album")
    ALBUM("专辑"),

    /**
     * 艺术家
     */
    @SerialName("artist")
    ARTIST("艺术家"),

    /**
     * 播放历史
     */
    @SerialName("play_history")
    PLAY_HISTORY("播放历史"),

    /**
     * 播放列表
     */
    @SerialName("play_queue")
    PLAY_QUEUE("播放列表"),

    /**
     * 最多播放音乐/专辑
     */
    @SerialName("maximum_play")
    MAXIMUM_PLAY("最多播放音乐/专辑"),

    /**
     * 最新数据
     */
    @SerialName("newest")
    NEWEST("最新数据"),

    /**
     * 流派
     */
    @SerialName("genre")
    GENRE("流派"),

    /**
     * 歌单音乐
     */
    @SerialName("playlist")
    PLAYLIST("歌单"),

    /**
     * 推荐音乐
     */
    @SerialName("recommend")
    RECOMMEND("推荐音乐")
}