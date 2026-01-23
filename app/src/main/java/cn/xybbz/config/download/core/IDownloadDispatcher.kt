package cn.xybbz.config.download.core

import cn.xybbz.localdata.enums.DownloadStatus
import java.lang.AutoCloseable

interface IDownloadDispatcher: AutoCloseable {

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

}