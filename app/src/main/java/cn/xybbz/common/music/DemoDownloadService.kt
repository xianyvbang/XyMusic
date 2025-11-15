package cn.xybbz.common.music

import android.app.Notification
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import cn.xybbz.R
import cn.xybbz.common.music.DownloadController.Companion.DOWNLOAD_NOTIFICATION_CHANNEL_ID
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DemoDownloadService(
    val JOB_ID: Int = 1,
    val FOREGROUND_NOTIFICATION_ID: Int = 1
) : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    R.string.exo_download_notification_channel_name, 0
) {

    @Inject
    lateinit var downloadController: DownloadController

    override fun getDownloadManager(): DownloadManager {
        val downloadManager = downloadController.getDownloadManagerData()
        val downloadNotificationHelper =
        downloadController.getDownloadNotificationHelper();
        downloadManager.addListener(
            TerminalStateNotificationHelper (
                    this, downloadNotificationHelper, FOREGROUND_NOTIFICATION_ID + 1
        ));
        return downloadManager;
    }

    override fun getScheduler(): Scheduler {
        return PlatformScheduler(this, JOB_ID);
    }

    override fun getForegroundNotification(
        downloads: List<Download>,
        notMetRequirements: Int
    ): Notification {
        return downloadController.getDownloadNotificationHelper()
            .buildProgressNotification(
                /* context= */ this,
                R.drawable.ic_download,
                /* contentIntent= */ null,
                /* message= */ null,
                downloads,
                notMetRequirements);
    }


}