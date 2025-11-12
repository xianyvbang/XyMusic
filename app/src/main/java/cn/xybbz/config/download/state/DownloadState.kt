package cn.xybbz.download.state

sealed class DownloadState {

    /**
     * 进行中
     * @author xybbz
     * @date 2025/11/12
     * @constructor 创建[InProgress]
     * @param [progress] 进度
     * @param [downloadedBytes] 下载字节数
     * @param [totalBytes] 总字节数
     * @param [speedBps] 速度 BPS
     */
    data class InProgress(
        val progress: Int,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val speedBps: Long
    ) : DownloadState()

    data object Success : DownloadState()

    data class Error(val message: String, val cause: Throwable? = null) : DownloadState()

    data object Paused : DownloadState()
}