package cn.xybbz.api.enums.subsonic

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * The command result. `ok`
 *
 * The command result. `failed`
 */
@JsonClass(generateAdapter = false)
enum class Status(val value: String) {
    @Json(name = "failed")
    Failed("failed"),
    @Json(name = "ok")
    Ok("ok");

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = value
}