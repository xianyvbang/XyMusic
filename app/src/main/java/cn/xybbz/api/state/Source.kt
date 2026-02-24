package cn.xybbz.api.state

import cn.xybbz.common.enums.LoginStateType

sealed class Source {
    data class Login(val value: LoginStateType) : Source()
    data class Library(val value: String) : Source()
}