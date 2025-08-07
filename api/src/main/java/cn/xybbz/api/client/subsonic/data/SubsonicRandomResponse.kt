package cn.xybbz.api.client.subsonic.data

import cn.xybbz.api.enums.subsonic.Status

data class SubsonicRandomResponse(
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
    val randomSongs: RandomSongs? = null,
    /**
     * 报错信息
     */
    override val error: SubsonicError? = null
): SubsonicParentResponse(type, version, status, error)
