package cn.xybbz.api.client.subsonic.data

import cn.xybbz.api.enums.subsonic.Status

/**
 * 专辑信息包含歌曲列表
 */
data class SubsonicPlaylistResponse(
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
     * 歌单信息
     */
    val playlist : PlaylistID3? = null,
    /**
     * 报错信息
     */
    override val error: SubsonicError? = null
) : SubsonicParentResponse(type, version, status, error)
