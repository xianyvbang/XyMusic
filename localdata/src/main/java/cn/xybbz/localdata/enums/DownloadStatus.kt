package cn.xybbz.localdata.enums

/**
 * 下载状态枚举
 */
enum class DownloadStatus {
    //未开始
    QUEUED,

    //进行中
    DOWNLOADING,

    //暂停
    PAUSED,

    //完成
    COMPLETED,

    //失败
    FAILED,

    //取消
    CANCEL,
}