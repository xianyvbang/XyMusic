package cn.xybbz.config

/**
 * 当前平台首页刷新是否加载歌单和统计等辅助远程数据。
 */
expect val ifLoadHomeRefreshAuxiliaryData: Boolean

/**
 * 当前平台登录后是否同步刷新服务端统计数量。
 */
expect val ifSyncDataInfoCountAfterLogin: Boolean
