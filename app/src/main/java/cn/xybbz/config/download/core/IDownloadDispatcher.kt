package cn.xybbz.config.download.core

import cn.xybbz.localdata.enums.DownloadStatus

interface IDownloadDispatcher {

    fun onProgress(
        downloadId: Long,
        progress: Float,
        downloadedBytes: Long,
        totalBytes: Long,
        speedBps: Long
    )

    fun onStateChanged(
        downloadId: Long,
        status: DownloadStatus,
        finalPath: String? = null,
        error: String? = null
    )

}