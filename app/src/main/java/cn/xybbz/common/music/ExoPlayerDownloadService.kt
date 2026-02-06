/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.common.music

import android.app.Notification
import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import androidx.media3.exoplayer.workmanager.WorkManagerScheduler
import cn.xybbz.R
import cn.xybbz.common.constants.Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@UnstableApi
@AndroidEntryPoint
class ExoPlayerDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID_NONE,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
) {

    @Inject
    lateinit var downloadCacheController: DownloadCacheController


    private
    var downloadNotificationHelper: DownloadNotificationHelper? = null

    override fun getDownloadManager(): DownloadManager {


        // This will only happen once, because getDownloadManager is guaranteed to be called only once
        // in the life cycle of the process.
        val downloadManager: DownloadManager = downloadCacheController.downloadManager
        /*val downloadNotificationHelper: DownloadNotificationHelper? =
            DemoUtil.getDownloadNotificationHelper( *//* context= *//*this)*/
        /*downloadManager.addListener(
            TerminalStateNotificationHelper(
                this, downloadNotificationHelper, FOREGROUND_NOTIFICATION_ID + 1
            )
        )*/
        downloadManager.addListener(
            object : DownloadManager.Listener { // Override methods of interest here.
            }
        )
        return downloadManager
    }

    override fun getScheduler(): Scheduler {
        return WorkManagerScheduler(this, WORKER_NAME)
    }

    override fun getForegroundNotification(
        downloads: List<Download>,
        notMetRequirements: Int
    ): Notification {
        return getDownloadNotificationHelper(this)
            .buildProgressNotification(
                /* context= */ this,
                R.drawable.ic_download,
                /* contentIntent= */ null,
                /* message= */ null,
                downloads,
                notMetRequirements
            )
    }

    companion object {
        private
        const val WORKER_NAME: String = "media3_worker"

        private
        const val FOREGROUND_NOTIFICATION_ID: Int = 1
    }

    @Synchronized
    fun getDownloadNotificationHelper(context: Context):DownloadNotificationHelper {
    if (downloadNotificationHelper == null) {
      downloadNotificationHelper =
           DownloadNotificationHelper(context, DOWNLOAD_NOTIFICATION_CHANNEL_ID)
    }
    return downloadNotificationHelper!!
  }
}