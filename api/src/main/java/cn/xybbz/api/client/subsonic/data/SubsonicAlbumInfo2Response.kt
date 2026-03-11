package cn.xybbz.api.client.subsonic.data

import cn.xybbz.api.enums.subsonic.Status
import kotlinx.serialization.Serializable

@Serializable
data class SubsonicAlbumInfo2Response(
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
    val albumInfo: AlbumInfo2ID3? = null,
    /**
     * 报错信息
     */
    override val error: SubsonicError? = null
):SubsonicParentResponse
