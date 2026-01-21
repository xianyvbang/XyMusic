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
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import cn.xybbz.common.utils.CoroutineScopeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayProgressTicker(
    private val controller: MediaController,
    private val intervalMs: Long = 1000L,
    private val onProgress: (Long) -> Unit
) {

    private val scope: CoroutineScope = CoroutineScopeUtils.getIo("PlaybackProgressTicker")
    private var job: Job? = null

    private val controllerHandler =
        Handler(controller.applicationLooper)

    fun start() {
        if (job != null) return

        job = scope.launch {
            withContext(Dispatchers.IO){
                while (coroutineContext.isActive) {
                    controllerHandler.removeCallbacksAndMessages(null)
                    controllerHandler.post {
                        if (
                            controller.isPlaying &&
                            controller.playbackState == Player.STATE_READY
                        ) {
                            onProgress(controller.currentPosition)
                        }
                    }
                    delay(intervalMs)
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun release() {
        stop()
    }
}
