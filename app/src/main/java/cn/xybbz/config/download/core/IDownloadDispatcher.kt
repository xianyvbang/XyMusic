package cn.xybbz.config.download.core

import cn.xybbz.localdata.enums.DownloadStatus

interface IDownloadDispatcher {

    fun onProgress(
        downloadId: Long,
        progress: Float,
        downloadedBytes: Long,
        totalBytes: Long,
        speedBps: Long,
        isSendNotice: Boolean = true
    )

    fun onStateChanged(
        downloadId: Long,
        status: DownloadStatus,
        finalPath: String? = null,
        error: String? = null,
        isSendNotice: Boolean = true
    )

    /**
     * 监听网络变化,判断是否开启仅wifi下载并且是否处在wifi环境
     */
    fun onEnabledOnlyWifiAndWifiDownload(): Boolean

}