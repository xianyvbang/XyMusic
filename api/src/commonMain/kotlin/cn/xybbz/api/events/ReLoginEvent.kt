package cn.xybbz.api.events

sealed interface ReLoginEvent {
    object Unauthorized : ReLoginEvent  // 收到 401
}
