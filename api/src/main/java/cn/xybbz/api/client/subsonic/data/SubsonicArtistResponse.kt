package cn.xybbz.api.client.subsonic.data

import cn.xybbz.api.enums.subsonic.Status

/**
 * 艺术家详细信息获取
 */
data class SubsonicArtistResponse(
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
    val artist : ArtistID3? = null,
    /**
     * 报错信息
     */
    override val error: SubsonicError? = null
) : SubsonicParentResponse(type, version, status, error)