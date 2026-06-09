package cn.xybbz.config

/**
 * iOS 首页刷新需要同步加载歌单和统计数据。
 */
actual val ifLoadHomeRefreshAuxiliaryData: Boolean = true

/**
 * iOS 登录后需要同步刷新服务端统计数量。
 */
actual val ifSyncDataInfoCountAfterLogin: Boolean = true
