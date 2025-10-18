package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.data.auth.AuthenticateRequest
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 用户登录请求实体类
 * @author lml
 * @date 2024/03/17
 * @constructor 创建[ClientLoginInfoReq]
 * @param [username] 用户名
 * @param [password] 密码
 * @param [address] 客户端连接地址
 * @param [connectionId] 连接设置id
 */
@JsonClass(generateAdapter = true)
data class ClientLoginInfoReq(
    @param:Json(name = "Username")
    val username: String,
    @param:Json(name = "Pw")
    val password: String,
    val address: String,
    val connectionId: Long? = null,
    val serverVersion: String? = null,
    val serverName: String? = null,
    val serverId: String? = null,
    val ifEnable: Boolean
) : AuthenticateRequest

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @param:Json(name = "Username")
    val username: String,
    @param:Json(name = "Pw")
    val password: String
)

fun ClientLoginInfoReq.toLogin(): LoginRequest {
    return LoginRequest(this.username, this.password)
}