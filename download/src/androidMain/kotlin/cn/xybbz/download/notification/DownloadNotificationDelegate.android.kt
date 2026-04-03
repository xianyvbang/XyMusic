package cn.xybbz.download.notification

import cn.xybbz.config.download.notification.NotificationController
import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.platform.ContextWrapper

// Android 端直接复用现有通知控制器，commonMain 不再依赖接口实现类。
actual class DownloadNotificationDelegate actual constructor(
    contextWrapper: ContextWrapper?,
) {
    private val notificationController = NotificationController(
        contextWrapper?.context
            ?: error("Android DownloadNotificationDelegate requires ContextWrapper."),
    )

    actual suspend fun showProgress(download: XyDownload, speedBps: Long) {
        notificationController.showProgress(download, speedBps)
    }

    actual suspend fun showPaused(download: XyDownload) {
        notificationController.showPaused(download)
    }

    actual suspend fun showFinished(download: XyDownload) {
        notificationController.showFinished(download)
    }

    actual fun clear(downloadId: Long) {
        notificationController.clear(downloadId)
    }
}
