package cn.xybbz.startup

import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.api.state.ClientLoginInfoState
import org.jetbrains.compose.resources.StringResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.empty_info

/**
 * 应用会话的唯一对外状态。
 *
 * 首页只需要订阅这个状态即可判断当前连接是否可用，不再组合多个登录和数据源 Flow。
 */
sealed interface AppSessionState {
    /** 当前状态对应的连接配置。 */
    val connection: ConnectionConfig?

    /** 最近一次底层登录状态，统一替代数据源管理器中的登录状态副本。 */
    val loginState: ClientLoginInfoState?
        get() = null

    /** 没有可恢复的连接。 */
    data object NoConnection : AppSessionState {
        override val connection: ConnectionConfig? = null
    }

    /** 正在恢复本地连接上下文。 */
    data class Restoring(override val connection: ConnectionConfig) : AppSessionState

    /** 已恢复本地连接，可以先显示本地缓存。 */
    data class OfflineReady(override val connection: ConnectionConfig) : AppSessionState

    /** 正在登录远程数据源。 */
    data class LoggingIn(
        override val connection: ConnectionConfig,
        /** 最近一次登录进度，便于状态订阅方展示具体阶段。 */
        override val loginState: ClientLoginInfoState? = null,
    ) : AppSessionState

    /** 远程登录成功，可以触发首页远程刷新。 */
    data class Ready(
        override val connection: ConnectionConfig,
        override val loginState: ClientLoginInfoState = ClientLoginInfoState.UserLoginSuccess,
    ) : AppSessionState

    /** 远程登录失败，页面仍然可以使用本地缓存并提供重试。 */
    data class LoginFailed(
        override val connection: ConnectionConfig,
        override val loginState: ClientLoginInfoState? = null,
        /** 面向用户显示的登录错误标题。 */
        val errorHint: StringResource = Res.string.empty_info,
        /** 登录接口返回的详细错误信息。 */
        val errorMessage: String = "",
    ) : AppSessionState
}
