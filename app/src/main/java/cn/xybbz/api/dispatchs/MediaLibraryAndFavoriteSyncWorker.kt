package cn.xybbz.api.dispatchs

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.remote.RemoteCurrent
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MediaLibraryAndFavoriteSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val dataSourceManager: DataSourceManager,
    private val db: DatabaseClient
) : CoroutineWorker(appContext, params) {

    val remoteId = RemoteIdConstants.MEDIA_LIBRARY_AND_FAVORITE
    override suspend fun doWork(): Result {
        return try {
            db.withTransaction {
                dataSourceManager.selectMediaLibrary()
                dataSourceManager.dataSourceServer.initFavoriteData()
                val connectionId = inputData.getLong(Constants.CONNECTION_ID,0L)
                try {
                    dataSourceManager.getDataInfoCount(connectionId)
                } catch (e: Exception) {
                    Log.i(
                        Constants.LOG_ERROR_PREFIX,
                        "获取音乐/专辑/艺术家/收藏/流派数量异常",
                        e
                    )
                }
                db.remoteCurrentDao.deleteById(remoteId)
                db.remoteCurrentDao.insertOrReplace(
                    RemoteCurrent(
                        id = remoteId,
                        nextKey = 0,
                        total = 0,
                        connectionId = connectionId
                    )
                )
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "同步媒体库,收藏和数量异常", e)
            Result.failure()
        }
    }
}