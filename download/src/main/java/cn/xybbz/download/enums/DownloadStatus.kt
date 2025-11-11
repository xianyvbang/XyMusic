package cn.xybbz.download.enums

/**
 * 下载状态
 * @author Administrator
 * @date 2025/11/11
 * @constructor 创建[DownloadStatus]
 */
enum class DownloadStatus {
    //排队中
    QUEUED,
    //下载中
    DOWNLOADING,
    //已暂停
    PAUSED,
    //已完成
    COMPLETED,
    //取消
    CANCEL,
    //失败
    FAIL
}