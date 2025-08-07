package cn.xybbz.api.client.subsonic.data

import cn.xybbz.api.enums.subsonic.Status
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class SubsonicParentResponse(
    /**
     * The server actual name. [Ex: Navidrome or gonic]
     */
    open val type: String? = null,

    /**
     * The server supported Subsonic API version.
     */
    open val version: String,

    /**
     * The command result. `ok`
     *
     * The command result. `failed`
     */
    open val status: Status,

    /**
     * 异常对象
     */
    open val error: SubsonicError?
)
