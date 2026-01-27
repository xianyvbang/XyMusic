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

import android.media.AudioTrack
import android.media.VolumeShaper
import cn.xybbz.config.scope.IoScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AudioFadeController() : IoScoped() {

    private var currentTrack: AudioTrack? = null
    private var volumeShaper: VolumeShaper? = null
    private var pauseToken = 0
    private var released = false  // 新增标志
    private var fadeDurationMs: Long = 300L

    fun updateFadeDurationMs(fadeDurationMs: Long) {
        this.fadeDurationMs = fadeDurationMs
    }

    fun attach(track: AudioTrack) {
        if (currentTrack === track) return
        release()
        currentTrack = track
        released = false
    }

    fun fadeIn() {
        val track = currentTrack ?: return
        if (released) return
        pauseToken++
        if (fadeDurationMs != 0L) {
            try {
                volumeShaper?.close()
                volumeShaper = track.createVolumeShaper(
                    VolumeShaper.Configuration.Builder()
                        .setDuration(fadeDurationMs)
                        .setCurve(
                            floatArrayOf(0f, 1f),
                            floatArrayOf(0f, 1f)
                        )
                        .build()
                )

                volumeShaper?.apply(VolumeShaper.Operation.PLAY)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private var fadeJob: Job? = null

    fun fadeOut(onEnd: () -> Unit) {
        val track = currentTrack ?: run {
            onEnd()
            return
        }
        if (released) {
            onEnd()
            return
        }
        val token = ++pauseToken
        if (fadeDurationMs != 0L)
            try {
                try {
                    volumeShaper?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                volumeShaper = track.createVolumeShaper(
                    VolumeShaper.Configuration.Builder()
                        .setDuration(fadeDurationMs)
                        .setCurve(
                            floatArrayOf(0f, 1f),
                            floatArrayOf(1f, 0f)
                        )
                        .build()
                )
                volumeShaper?.apply(VolumeShaper.Operation.PLAY)
                fadeJob?.cancel()
                fadeJob = scope.launch(Dispatchers.Main) {
                    delay(fadeDurationMs)
                    if (token == pauseToken) {
                        onEnd()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onEnd()
            }
    }

    fun release() {
        currentTrack?.release()
        volumeShaper?.close()
        volumeShaper = null
        currentTrack = null
        released = true
    }

    override fun close() {
        super.close()
        release()
    }
}

