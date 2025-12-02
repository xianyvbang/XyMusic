package cn.xybbz.api.client.jellyfin.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * 创建歌单响应
 * @author Administrator
 * @date 2025/12/02
 * @constructor 创建[PlaylistResponse]
 * @param [id] 歌单id
 */
@JsonClass(generateAdapter = true)
data class PlaylistResponse(
    @param:Json(name = "Id")
    val id: String,
)
