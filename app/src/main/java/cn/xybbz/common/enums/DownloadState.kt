package cn.xybbz.common.enums

/**
 * 下载状态枚举
 */
enum class DownloadState {
    //未开始
    NOT_STARTED,

    //进行中
    DOWNLOADING,

    //暂停
    PAUSED,

    //完成
    COMPLETED,

    //失败
    FAILED
}