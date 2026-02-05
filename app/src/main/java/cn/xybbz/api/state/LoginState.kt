package cn.xybbz.api.state

import cn.xybbz.api.client.IDataSourceParentServer

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val server: IDataSourceParentServer) : LoginState()
    data class Error(val throwable: Throwable? = null) : LoginState()
}
