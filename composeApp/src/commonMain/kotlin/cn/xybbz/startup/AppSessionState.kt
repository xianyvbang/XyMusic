package cn.xybbz.startup

import cn.xybbz.localdata.data.connection.ConnectionConfig

/**
 * 应用会话的唯一对外状态。
 *
 * 首页只需要订阅这个状态即可判断当前连接是否可用，不再组合多个登录和数据源 Flow。
 */
sealed interface AppSessionState {
    /** 当前状态对应的连接配置。 */
    val connection: ConnectionConfig?

    /** 没有可恢复的连接。 */
    data object NoConnection : AppSessionState {
        override val connection: ConnectionConfig? = null
    }

    /** 正在恢复本地连接上下文。 */
    data class Restoring(override val connection: ConnectionConfig) : AppSessionState

    /** 已恢复本地连接，可以先显示本地缓存。 */
    data class OfflineReady(override val connection: ConnectionConfig) : AppSessionState

    /** 正在登录远程数据源。 */
    data class LoggingIn(override val connection: ConnectionConfig) : AppSessionState

    /** 远程登录成功，可以触发首页远程刷新。 */
    data class Ready(override val connection: ConnectionConfig) : AppSessionState

    /** 远程登录失败，页面仍然可以使用本地缓存并提供重试。 */
    data class LoginFailed(
        override val connection: ConnectionConfig,
        val message: String = "",
    ) : AppSessionState
}
