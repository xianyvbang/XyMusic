package cn.xybbz.entity.data

import androidx.annotation.StringRes

/**
 * 登陆返回状态信息
 */
data class LoginStateData(
    val isError: Boolean = false,
    @param:StringRes val errorHint: Int? = null,
    val errorMessage:String? = null,
    val loading: Boolean = true,
    val isLoginSuccess: Boolean = false
)
