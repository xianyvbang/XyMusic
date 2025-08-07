package cn.xybbz.api.client.subsonic.data

import cn.xybbz.api.client.data.Request
import com.squareup.moshi.JsonClass

/**
 * 上报播放记录请求实体类
 */
@JsonClass(generateAdapter = true)
data class ScrobbleRequest(
    /**
     * 歌曲id
     */
    val id: String,
    /**
     * 播放时间，自1970以来的毫秒数
     */
    val time: Int? = null,
    /**
     * 是否提交，不提交则表示正在播放
     */
    val submission: Boolean? = false
): Request()
