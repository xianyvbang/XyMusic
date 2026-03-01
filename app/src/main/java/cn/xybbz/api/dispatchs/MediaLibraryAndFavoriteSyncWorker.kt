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

package cn.xybbz.api.dispatchs

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.events.ReLoginEvent
import cn.xybbz.api.exception.UnauthorizedException
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

    override suspend fun doWork(): Result {
        val connectionId = inputData.getLong(Constants.CONNECTION_ID, 0L)
        val server = dataSourceManager.dataSourceServerFlow.value

        if (connectionId == 0L || server == null) {
            Log.i(Constants.LOG_ERROR_PREFIX, "media sync skipped: invalid connection or server missing")
            return Result.success()
        }

        if (server.getConnectionId() != connectionId) {
            Log.i(
                Constants.LOG_ERROR_PREFIX,
                "media sync skipped: stale worker, expected=$connectionId current=${server.getConnectionId()}"
            )
            return Result.success()
        }

        return try {
            Log.i(Constants.LOG_ERROR_PREFIX, "start syncing media library/favorites/counts")
            db.withTransaction {
                val remoteId = RemoteIdConstants.MEDIA_LIBRARY_AND_FAVORITE + connectionId

                server.initFavoriteData(connectionId = connectionId)
                try {
                    server.getDataInfoCount(connectionId)
                } catch (e: Exception) {
                    Log.e(
                        Constants.LOG_ERROR_PREFIX,
                        "failed to fetch media/album/artist/favorite/genre counts",
                        e
                    )
                }

                try {
                    server.getApiClient().ping()
                } catch (e: Exception) {
                    val activeConnectionId = server.getConnectionId()
                    if (e is UnauthorizedException &&
                        activeConnectionId == connectionId &&
                        server.tryMarkLoginRetry()
                    ) {
                        server.getApiClient().eventBus.notify(ReLoginEvent.Unauthorized)
                    }
                    throw e
                }

                db.remoteCurrentDao.deleteById(remoteId)
                db.remoteCurrentDao.insertOrReplace(
                    RemoteCurrent(
                        id = remoteId,
                        nextKey = 0,
                        total = 0,
                        connectionId = connectionId,
                        refresh = false
                    )
                )
            }
            Result.success()
        } catch (e: Exception) {
            val activeConnectionId = dataSourceManager.dataSourceServerFlow.value?.getConnectionId()
            if (activeConnectionId != connectionId) {
                Log.i(Constants.LOG_ERROR_PREFIX, "media sync ignored after data source switched")
                return Result.success()
            }
            Log.e(Constants.LOG_ERROR_PREFIX, "sync media/favorite/count failed", e)
            Result.failure()
        }
    }
}
