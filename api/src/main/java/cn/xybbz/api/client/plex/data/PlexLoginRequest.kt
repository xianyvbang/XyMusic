package cn.xybbz.api.client.plex.data

import cn.xybbz.api.data.auth.ClientLoginInfoReq
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlexLoginRequest(
    val login: String,
    val password: String
)

fun ClientLoginInfoReq.toPlexLogin(): PlexLoginRequest {
    return PlexLoginRequest(this.username, this.password)
}
