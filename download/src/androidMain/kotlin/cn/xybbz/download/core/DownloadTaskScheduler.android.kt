package cn.xybbz.download.core

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import cn.xybbz.download.work.DownloadWork
import cn.xybbz.platform.ContextWrapper

// Android 端直接在 actual 里接 WorkManager，其它公共层无需再感知平台调度细节。
actual class DownloadTaskScheduler actual constructor(contextWrapper:ContextWrapper) {
    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(contextWrapper.context)
    }

    actual fun enqueue(downloadId: Long) {
        val workRequest = OneTimeWorkRequestBuilder<DownloadWork>()
            .setInputData(workDataOf(DownloadConstants.WORK_INPUT_DOWNLOAD_ID to downloadId))
            .addTag(downloadId.toString())
            .build()

        workManager.enqueueUniqueWork(
            downloadId.toString(),
            ExistingWorkPolicy.KEEP,
            workRequest,
        )
    }

    actual fun cancel(downloadId: Long) {
        workManager.cancelAllWorkByTag(downloadId.toString())
    }
}
