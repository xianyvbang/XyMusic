package cn.xybbz.config.download.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ForegroundInfo
import cn.xybbz.download.R
import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.download.enums.DownloadStatus
import java.util.Locale

// Android 通知细节全部留在 androidMain，commonMain 只调度状态。
class NotificationController(
    private val context: Context,
) {
    companion object {
        private const val CHANNEL_ID = "downloader_channel"
        private const val CHANNEL_NAME = "Download"
        private const val STATIC_NOTIFICATION_OFFSET = 100000
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Shows download progress"
            setSound(null, null)
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(
        download: XyDownload,
        speedBps: Long,
    ): NotificationCompat.Builder {
        // 这里继续复用原有通知样式和交互按钮，只是从 commonMain 挪到了 Android 实现里。
        val statusText = getStatusText(download.status)
        val speedText =
            if (download.status == DownloadStatus.DOWNLOADING) " - ${formatSpeed(speedBps)}" else ""
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(download.fileName)
            .setContentText("$statusText$speedText")
            .setSmallIcon(R.drawable.svg_notification_download)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setProgress(100, download.progress.toInt(), false)

        when (download.status) {
            DownloadStatus.DOWNLOADING -> {
                builder.addAction(
                    R.drawable.svg_notification_pause,
                    "Pause",
                    createActionIntent(download.id, DownloadNotificationReceiver.ACTION_PAUSE),
                )
                builder.addAction(
                    R.drawable.svg_notification_cancel,
                    "Cancel",
                    createActionIntent(download.id, DownloadNotificationReceiver.ACTION_CANCEL),
                )
            }

            DownloadStatus.PAUSED -> {
                builder.addAction(
                    R.drawable.svg_notification_play,
                    "Resume",
                    createActionIntent(download.id, DownloadNotificationReceiver.ACTION_RESUME),
                )
                builder.addAction(
                    R.drawable.svg_notification_cancel,
                    "Cancel",
                    createActionIntent(download.id, DownloadNotificationReceiver.ACTION_CANCEL),
                )
            }

            else -> Unit
        }
        return builder
    }

    private fun show(notificationId: Int, builder: NotificationCompat.Builder) {
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(notificationId, builder.build())
        }
    }

    suspend fun showProgress(download: XyDownload, speedBps: Long) {
        // 动态进度通知和静态状态通知共存时，先清掉静态那张。
        NotificationManagerCompat.from(context)
            .cancel((download.id + STATIC_NOTIFICATION_OFFSET).toInt())
        show(download.id.toInt(), buildNotification(download, speedBps))
    }

    suspend fun showPaused(download: XyDownload) {
        show((download.id + STATIC_NOTIFICATION_OFFSET).toInt(), buildNotification(download, 0L))
    }

    suspend fun showFinished(download: XyDownload) {
        clear(download.id)
    }

    fun clear(downloadId: Long) {
        NotificationManagerCompat.from(context).cancel(downloadId.toInt())
        NotificationManagerCompat.from(context)
            .cancel((downloadId + STATIC_NOTIFICATION_OFFSET).toInt())
    }

    private fun createActionIntent(taskId: Long, action: String): PendingIntent {
        val intent = Intent(context, DownloadNotificationReceiver::class.java).apply {
            this.action = action
            putExtra(DownloadNotificationReceiver.EXTRA_TASK_ID, taskId)
        }
        return PendingIntent.getBroadcast(
            context,
            (action.hashCode() + taskId).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun getStatusText(status: DownloadStatus): String = when (status) {
        DownloadStatus.QUEUED -> "Queued"
        DownloadStatus.DOWNLOADING -> "Downloading"
        DownloadStatus.PAUSED -> "Paused"
        DownloadStatus.COMPLETED -> "Completed"
        DownloadStatus.FAILED -> "Failed"
        DownloadStatus.CANCEL -> "Canceled"
    }

    private fun formatSpeed(bytesPerSecond: Long): String {
        if (bytesPerSecond < 1024) return "$bytesPerSecond B/s"
        val kbps = bytesPerSecond / 1024
        if (kbps < 1024) return "$kbps KB/s"
        return String.format(Locale.getDefault(), "%.2f MB/s", kbps / 1024.0)
    }

    fun createForegroundInfo(
        notificationId: Int,
        notification: Notification,
    ): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }
}
