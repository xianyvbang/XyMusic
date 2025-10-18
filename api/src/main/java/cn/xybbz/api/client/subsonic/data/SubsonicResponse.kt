package cn.xybbz.api.client.subsonic.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
/**
 * GetArtistsResponse，A subsonic-response element with a nested artists element on success.
 */
@JsonClass(generateAdapter = true)
data class SubsonicResponse<T> (
    @param:Json(name = "subsonic-response")
    val subsonicResponse: T
)