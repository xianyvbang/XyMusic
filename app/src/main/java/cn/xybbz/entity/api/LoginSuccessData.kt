package cn.xybbz.entity.api

/**
 * 登录成功后数据
 * @author 刘梦龙
 * @date 2025/05/09
 * @constructor 创建[LoginSuccessData]
 * @param [userId] 用户身份
 * @param [accessToken] 访问令牌
 */
data class LoginSuccessData(
    val userId: String?,
    val accessToken: String?,
    val serverId: String?,
    val serverName: String? = null,
    val version: String? = null
)
