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

import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.localdata.config.DatabaseClient
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class MediaLibraryAndFavoriteSyncScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val db: DatabaseClient
) {

    val tag = "media_sync"

    suspend fun enqueueIfNeeded(connectionId: Long) {
        val shouldSync = shouldSync()
        Log.i(
            Constants.LOG_ERROR_PREFIX,
            "开始启动获取音乐/专辑/艺术家/收藏/流派数量 $shouldSync"
        )
        if (!shouldSync) return
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<MediaLibraryAndFavoriteSyncWorker>()
            .setInputData(workDataOf(Constants.CONNECTION_ID to connectionId))
            .setConstraints(constraints)
            .addTag(tag)
            .build()

        workManager.enqueueUniqueWork(
            tag,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel() {
        workManager.cancelUniqueWork(tag)
    }

    suspend fun shouldSync(): Boolean {
        val state = db.remoteCurrentDao.remoteKeyById(RemoteIdConstants.MEDIA_LIBRARY_AND_FAVORITE)
        return (state == null) ||
                ((System.currentTimeMillis() - state.createTime) > 10.minutes.inWholeMilliseconds)
    }

}