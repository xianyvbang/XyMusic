package cn.xybbz.api.client.subsonic.data

import cn.xybbz.api.enums.subsonic.Status
import com.squareup.moshi.JsonClass

/**
 * 专辑列表
 */
@JsonClass(generateAdapter = true)
data class SubsonicAlbumListResponse(
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
     * 专辑信息
     */
    val albumList2: AlbumList2ID3? = null,
    /**
     * 报错信息
     */
    override val error: SubsonicError? = null
) : SubsonicParentResponse(type, version, status, error)
