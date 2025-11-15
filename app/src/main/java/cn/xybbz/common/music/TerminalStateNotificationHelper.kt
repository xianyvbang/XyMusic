package cn.xybbz.common.music

import android.app.Notification
import android.content.Context
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import cn.xybbz.R

class TerminalStateNotificationHelper(
    val context: Context,
    val notificationHelper: DownloadNotificationHelper,
    val firstNotificationId: Int
) : DownloadManager.Listener {

    override fun onDownloadChanged(
        downloadManager: DownloadManager,
        download: Download,
        finalException: Exception?
    ) {
        var nextNotificationId = firstNotificationId
        val notification: Notification
        if (download.state == Download.STATE_COMPLETED) {
            notification =
                notificationHelper.buildDownloadCompletedNotification(
                    context,
                    R.drawable.ic_download_done,
                    /* contentIntent= */ null,
                    Util.fromUtf8Bytes(download.request.data)
                );
        } else if (download.state == Download.STATE_FAILED) {
            notification =
                notificationHelper.buildDownloadFailedNotification(
                    context,
                    R.drawable.ic_download_done,
                    /* contentIntent= */ null,
                    Util.fromUtf8Bytes(download.request.data)
                );
        } else {
            return;
        }
        NotificationUtil.setNotification(context, nextNotificationId++, notification);
    }
}