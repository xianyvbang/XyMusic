package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.data.auth.AuthenticateResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthenticateResponse(
    @param:Json(name = "AccessToken")
    val accessToken: String?,
    @param:Json(name = "ServerId")
    val serverId: String?,
    @param:Json(name = "User")
    val user: User?
) : AuthenticateResponse

@JsonClass(generateAdapter = true)
data class User(
    @param:Json(name = "Id")
    val id: String?
)