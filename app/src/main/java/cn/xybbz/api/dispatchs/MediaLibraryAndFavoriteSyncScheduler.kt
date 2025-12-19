package cn.xybbz.api.dispatchs

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.localdata.config.DatabaseClient
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class MediaLibraryAndFavoriteSyncScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val db: DatabaseClient,
    private val connectionConfigServer: ConnectionConfigServer
) {

    suspend fun enqueueIfNeeded() {
        if (!shouldSync()) return
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<MediaLibraryAndFavoriteSyncWorker>()
            .setInputData(workDataOf(Constants.CONNECTION_ID to connectionConfigServer.getConnectionId()))
            .setConstraints(constraints)
            .addTag("media_sync")
            .build()

        workManager.enqueueUniqueWork(
            "media_sync",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun cancel() {
        workManager.cancelUniqueWork("media_sync")
    }

    suspend fun shouldSync(): Boolean {
        val state = db.remoteCurrentDao.remoteKeyById(RemoteIdConstants.MEDIA_LIBRARY_AND_FAVORITE)
        return (state == null) ||
                ((System.currentTimeMillis() - state.createTime) > 10.minutes.inWholeMilliseconds)
    }

}