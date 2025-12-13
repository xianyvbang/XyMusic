package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.client.data.ClientLoginInfoReq
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 登录请求
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[LoginRequest]
 * @param [username] 用户名
 * @param [password] 密码
 */
@JsonClass(generateAdapter = true)
data class LoginRequest(
    @param:Json(name = "Username")
    val username: String,
    @param:Json(name = "Pw")
    val password: String
)

/**
 * 转换为Jellyfin和Emby登录请求
 * @author xybbz
 * @date 2025/12/02
 * @return [LoginRequest]
 */
fun ClientLoginInfoReq.toLogin(): LoginRequest {
    return LoginRequest(this.username, this.password)
}