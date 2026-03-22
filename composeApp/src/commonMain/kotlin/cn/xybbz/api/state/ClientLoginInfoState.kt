package cn.xybbz.api.state

sealed class ClientLoginInfoState {

    /**
     * 正在链接
     */
    data class Connected(val address: String) : ClientLoginInfoState()

    /**
     * 服务端超时
     */
    data object ServiceTimeOutState : ClientLoginInfoState()

    /**
     * 登陆成功
     */
    data object UserLoginSuccess : ClientLoginInfoState()

    /**
     * 报错
     */
    data class ErrorState(val error: Throwable) : ClientLoginInfoState()

    /**
     * 权限报错
     */
    data object UnauthorizedErrorState : ClientLoginInfoState()

    /**
     * 网络连接异常
     */
    data object ConnectError : ClientLoginInfoState()

    /**
     * 未选择连接
     */
    data object SelectServer : ClientLoginInfoState()

}
