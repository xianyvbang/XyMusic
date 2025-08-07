package cn.xybbz.api.client.subsonic.data

import cn.xybbz.api.enums.subsonic.Status
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 艺术家响应类
 */
@JsonClass(generateAdapter = true)
data class SubsonicArtistsResponse(
    override val type: String? = null,
    /**
     * 版本号
     */
    override val version: String,
    /**
     * 状态
     */
    override val status: Status,
    /**
     * 艺术家信息
     */
    @param:Json(name = "artists")
    val artists: ArtistsID3? = null,
    /**
     * 报错信息
     */
    override val error: SubsonicError? = null
) : SubsonicParentResponse(type, version, status, error)