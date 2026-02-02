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

package cn.xybbz.common.music

import android.os.Handler
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download

@UnstableApi
class DownloadCacheProgressTicker(
    private val downloadCacheController: DownloadCacheController,
    private val intervalMs: Long = 1000L,
    private val onProgress: (Float) -> Unit
) {

    private val downloadCacheHandler =
        Handler(downloadCacheController.downloadManager.applicationLooper)

    private var downloadId: String = ""

    private var periodicUpdatesStarted: Boolean = true

    fun start(downloadId:String) {
        this.downloadId = downloadId
        periodicUpdatesStarted = true
        updateProgress()
    }

    private fun updateProgress() {
        val downloadManager = downloadCacheController.downloadManager
        val download = downloadManager.downloadIndex.getDownload(downloadId)
        Log.i("music","下载状态${download?.state}")
        onProgress(download?.percentDownloaded ?: 0.0f)
        if (download?.state == Download.STATE_COMPLETED) {
            onProgress(100.0f)
        } else if (download?.state == Download.STATE_QUEUED)
            onProgress(0.0f)
        else if (periodicUpdatesStarted) {
            downloadCacheHandler.removeCallbacksAndMessages(null)
            downloadCacheHandler.postDelayed(this::updateProgress, intervalMs)
        }

    }

    fun stop() {
        stopPeriodicUpdates()
    }

    fun stopPeriodicUpdates() {
        periodicUpdatesStarted = false
        downloadCacheHandler.removeCallbacksAndMessages(null)
    }

    fun release() {
        stop()
    }
}
