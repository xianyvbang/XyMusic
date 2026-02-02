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
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.DISCONTINUITY_REASON_SEEK
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_SEEK
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.localdata.data.music.XyPlayMusic

class XyPlayerListener(
    private val onGetState: () -> PlayStateEnum,
    private val onUpdateState: (PlayStateEnum) -> Unit,
    private val onsetPicByte: (ByteArray?) -> Unit,
    private val onGetMusicInfo: () -> XyPlayMusic?,
    private val onSetMusicInfo: () -> Unit,
    private val onSeekToNext: () -> Unit,
    private val onEventEmit: (PlayerEvent) -> Unit,
    private val onSetCurOriginIndex: () -> Unit,
    private val onOriginMusicListIsNotEmptyAndIndexEnd: () -> Boolean,
    private val onPageNumber: () -> Int,
    private val onUpdateDuration: () -> Unit,
    private val onMusicStartCache: () -> Unit,
    private val onUpdatePlayerHistory: (XyPlayMusic) -> Unit,
    private val onPlaySessionId: () -> String
) : Player.Listener {

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> {
                //播放器停止时的状态
                Log.i("music", "STATE_IDLE")
            }

            Player.STATE_BUFFERING -> {
                // 正在缓冲数据
                if (onGetState() == PlayStateEnum.Playing)
                    onUpdateState(PlayStateEnum.Loading)

                Log.i("music", "STATE_BUFFERING")
            }

            Player.STATE_READY -> {
                // 可以开始播放 恢复播放
                if (onGetState() == PlayStateEnum.Loading) {
                    onUpdateState(PlayStateEnum.Playing)
                    /*musicInfo?.let {
                        cacheController.cacheMedia(it)
                    }*/
                }
                Log.i("music", "STATE_READY")
            }

            Player.STATE_ENDED -> {
                // 播放结束
                onUpdateState(PlayStateEnum.None)
                Log.i("music", "STATE_ENDED")
            }
        }
    }


    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        Log.i(
            "music",
            "当前索引 --- ${mediaMetadata.title}"
        )

        onsetPicByte(
            if (onGetMusicInfo()?.pic.isNullOrBlank()) {
                mediaMetadata.artworkData
            } else {
                null
            }
        )
    }

    override fun onPlayerError(error: PlaybackException) {
        // 获取播放错误信息
        Log.e("music", "播放报错$error", error)
        if (onGetState() != PlayStateEnum.Pause) {
            onSeekToNext()
        }
    }

    //检测播放何时转换为其他媒体项
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        mediaItem?.localConfiguration?.let { localConfiguration ->

            if (localConfiguration.tag == null) {
                Log.i("music", "诶切换类型 $reason")
                //手动切换
                if (reason == MEDIA_ITEM_TRANSITION_REASON_SEEK || reason == MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED) {
                    onGetMusicInfo()?.let {
                        onEventEmit(PlayerEvent.BeforeChangeMusic)
                    }
                }
                //自动播放
                if (reason == MEDIA_ITEM_TRANSITION_REASON_REPEAT || reason == MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    onGetMusicInfo()?.let {
                        onEventEmit(PlayerEvent.RemovePlaybackProgress(it.itemId))
                    }
                }

                onSetCurOriginIndex()
                if (onOriginMusicListIsNotEmptyAndIndexEnd()) {
                    onEventEmit(PlayerEvent.NextList(onPageNumber()))
                }
                onSetMusicInfo()
                onUpdateDuration()


                //todo 替换mediaitem的位置
                //如果状态是播放的话
                Log.i("music", "播放状态${onGetState()}")
                if (onGetState() != PlayStateEnum.Pause && onGetState() != PlayStateEnum.None)
                    onMusicStartCache()
                onGetMusicInfo()?.let {
                    onEventEmit(
                        PlayerEvent.ChangeMusic(
                            it.itemId,
                            it.artistIds?.get(0),
                            it.artists?.get(0)
                        )
                    )
                    //判断音乐播放进度是否为0,如果为0则不处理,不为0则需要跳转到相应的进度
                    onUpdatePlayerHistory(it)
                }

            }


        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int,
    ) {
        if (reason == DISCONTINUITY_REASON_SEEK) {
            onGetMusicInfo()?.let {
                onEventEmit(
                    PlayerEvent.PositionSeekTo(
                        newPosition.positionMs,
                        it.itemId,
                        onPlaySessionId()
                    )
                )
            }
        }
        super.onPositionDiscontinuity(oldPosition, newPosition, reason)
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        Log.i("music", "播放报错")
        super.onPlayerErrorChanged(error)
    }

}