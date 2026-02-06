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

package cn.xybbz.config.update

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.config.setting.SettingsManager
import javax.inject.Inject

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
        return (System.currentTimeMillis() - latestVersionTime) >= Constants.VERSION_INFO_INTERVAL * 60_000
    }
}
