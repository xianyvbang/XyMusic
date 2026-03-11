package cn.xybbz.api.client.custom.data

import kotlinx.serialization.Serializable

/**
 * 自定义歌词接口响应对象
 * 说明：兼容常见字段命名，统一由 data class 承载解析。
 */
@Serializable
data class CustomLyricsResponseData(
    val id: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val lyrics: String? = null
) {
}
