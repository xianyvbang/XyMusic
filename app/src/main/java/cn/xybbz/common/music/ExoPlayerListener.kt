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

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.PlayWhenReadyChangeReason
import androidx.media3.exoplayer.ExoPlayer
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.config.lrc.LrcServer

class ExoPlayerListener(
    private val musicController: MusicController,
    private val downloadCacheController: DownloadCacheController,
    private val lrcServer: LrcServer,
    private val exoPlayer: ExoPlayer?
) : Player.Listener {

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Log.i(
            "music",
            "当前播放状态$isPlaying -- ${exoPlayer?.currentMediaItem?.mediaMetadata?.title}"
        )
        if (isPlaying) {
            musicController.updateState(PlayStateEnum.Playing)
            musicController.reportedPlayEvent()
        } else {
            musicController.reportedPauseEvent()
        }
    }

    override fun onPlayWhenReadyChanged(
        playWhenReady: Boolean,
        @PlayWhenReadyChangeReason reason: Int
    ) {
        Log.i(
            "music",
            "播放状态改变$playWhenReady --- ${reason} -- ${exoPlayer?.isPlaying} -- ${musicController.state}"
        )
        if (playWhenReady) {
            musicController.progressTicker.start()
        } else {
            musicController.progressTicker.stop()
            musicController.musicInfo?.let {
                downloadCacheController.pauseCache()
            }
        }
        //todo 将loading状态变成播放中状态的就是这个
        if (musicController.musicInfo != null) {

            val playState =
                if (!playWhenReady) PlayStateEnum.Pause
                else if (musicController.state == PlayStateEnum.Loading) PlayStateEnum.Loading
                else PlayStateEnum.Playing
            musicController.updateState(playState)

        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        if (!mediaItem?.mediaMetadata?.title.isNullOrBlank()) {
            lrcServer.clear()
        }
    }

}