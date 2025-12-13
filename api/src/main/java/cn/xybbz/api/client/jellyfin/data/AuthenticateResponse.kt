package cn.xybbz.api.client.jellyfin.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 认证响应
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[AuthenticateResponse]
 * @param [accessToken] 访问令牌
 * @param [serverId] 服务器ID
 * @param [user] 用户信息
 */
@JsonClass(generateAdapter = true)
data class AuthenticateResponse(
    @param:Json(name = "AccessToken")
    val accessToken: String?,
    @param:Json(name = "ServerId")
    val serverId: String?,
    @param:Json(name = "User")
    val user: User?
)

/**
 * 用户
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[User]
 * @param [id] 用户id
 */
@JsonClass(generateAdapter = true)
data class User(
    @param:Json(name = "Id")
    val id: String?
)