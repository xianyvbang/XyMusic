package cn.xybbz.config.update

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cn.xybbz.common.constants.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class VersionCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val apkUpdateManager: ApkUpdateManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val initLatestVersion = apkUpdateManager.initLatestVersion(false)
            if (initLatestVersion)
                Result.success()
            else Result.failure()
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取App版本异常: ", e)
            Result.failure()
        }
    }
}
