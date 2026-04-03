package cn.xybbz.download.notification

import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.platform.ContextWrapper

// 通知展示能力直接走 expect/actual，commonMain 只调用统一的通知动作。
expect class DownloadNotificationDelegate(contextWrapper: ContextWrapper? = null) {
    suspend fun showProgress(download: XyDownload, speedBps: Long)
    suspend fun showPaused(download: XyDownload)
    suspend fun showFinished(download: XyDownload)
    fun clear(downloadId: Long)
}
