package cn.xybbz.config.download.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.xybbz.config.download.DownLoadManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * FileName: AppDatabase
 * Author: haosen
 * Date: 10/3/2025 7:02 AM
 * Description:
 **/
@AndroidEntryPoint
class DownloadNotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var downLoadManager: DownLoadManager

    companion object {
        const val ACTION_PAUSE = "downloader.action.PAUSE"
        const val ACTION_RESUME = "downloader.action.RESUME"
        const val ACTION_CANCEL = "downloader.action.CANCEL"
        const val EXTRA_TASK_ID = "downloader.extra.TASK_ID"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        when (intent.action) {
            ACTION_PAUSE -> downLoadManager.pause(taskId)
            ACTION_RESUME -> downLoadManager.resume(taskId)
            ACTION_CANCEL -> downLoadManager.cancel(taskId)
        }
    }
}