package cn.xybbz.download.notification

import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.platform.ContextWrapper

// JVM 端通知先留空，后续需要桌面通知时再补实现。
actual class DownloadNotificationDelegate actual constructor(
    contextWrapper: ContextWrapper?,
) {
    actual suspend fun showProgress(download: XyDownload, speedBps: Long) = Unit

    actual suspend fun showPaused(download: XyDownload) = Unit

    actual suspend fun showFinished(download: XyDownload) = Unit

    actual fun clear(downloadId: Long) = Unit
}
