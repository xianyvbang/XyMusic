package cn.xybbz.entity.data

import org.jetbrains.compose.resources.StringResource


/**
 * 登陆返回状态信息
 */
data class LoginStateData(
    val isError: Boolean = false,
    val errorHint: StringResource? = null,
    val errorMessage: String? = null,
    val loading: Boolean = true,
    val isLoginSuccess: Boolean = false,
    /**
     * 是否需要用户重新登录。
     */
    val needsRelogin: Boolean = false,
    /**
     * 是否因为平台安全存储不可用而失败。
     */
    val isCredentialStoreUnavailable: Boolean = false
)
