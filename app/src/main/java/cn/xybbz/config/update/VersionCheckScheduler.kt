package cn.xybbz.config.update

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import cn.xybbz.config.setting.SettingsManager
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

class VersionCheckScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val settingsManager: SettingsManager,
) {

    val tag = "version_check"
    fun enqueueIfNeeded() {
        if (!shouldCheck()) return
        val request = OneTimeWorkRequestBuilder<VersionCheckWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(tag)
            .build()

        workManager.enqueueUniqueWork(
            tag,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun shouldCheck(): Boolean {
        val latestVersionTime = settingsManager.get().latestVersionTime
        return System.currentTimeMillis() - latestVersionTime >= 1.hours.inWholeMilliseconds
    }
}
