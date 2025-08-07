package cn.xybbz.api.client.navidrome.data

import cn.xybbz.api.client.jellyfin.data.ClientLoginInfoReq
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NavidromeLoginRequest(
    val username: String,
    val password: String
)

fun ClientLoginInfoReq.toNavidromeLogin(): NavidromeLoginRequest {
    return NavidromeLoginRequest(this.username, this.password)
}
