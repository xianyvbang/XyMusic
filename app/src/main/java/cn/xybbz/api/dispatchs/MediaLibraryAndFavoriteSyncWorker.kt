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
        return try {
            db.withTransaction {
                val connectionId = inputData.getLong(Constants.CONNECTION_ID,0L)
                val remoteId = RemoteIdConstants.MEDIA_LIBRARY_AND_FAVORITE + connectionId
                dataSourceManager.selectMediaLibrary(connectionId = connectionId)
                dataSourceManager.initFavoriteData(connectionId = connectionId)
                try {
                    dataSourceManager.getDataInfoCount(connectionId)
                } catch (e: Exception) {
                    Log.e(
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