package cn.xybbz.config

/**
 * JVM 首页刷新不加载歌单和统计数据，避免桌面端重复请求辅助接口。
 */
actual val ifLoadHomeRefreshAuxiliaryData: Boolean = false

/**
 * JVM 登录后不刷新服务端统计数量，避免桌面端登录阶段重复拉取辅助数据。
 */
actual val ifSyncDataInfoCountAfterLogin: Boolean = false
