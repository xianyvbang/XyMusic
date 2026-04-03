package cn.xybbz.download.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.xybbz.download.DownloaderManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// 通知动作只做转发，不在广播接收器里承载业务逻辑。
class DownloadNotificationReceiver : BroadcastReceiver() {

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
