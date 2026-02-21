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

import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSourceBitmapLoader
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioOutput
import androidx.media3.exoplayer.audio.AudioOutputProvider
import androidx.media3.exoplayer.audio.AudioTrackAudioOutputProvider
import androidx.media3.exoplayer.audio.ForwardingAudioOutputProvider
import androidx.media3.session.CacheBitmapLoader
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import cn.xybbz.R
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.client.ImageApiClient
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.Constants.REMOVE_FROM_FAVORITES
import cn.xybbz.common.constants.Constants.SAVE_TO_FAVORITES
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.config.media.MediaServer
import cn.xybbz.config.setting.OnSettingsChangeListener
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.config.DatabaseClient
import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@UnstableApi
@AndroidEntryPoint
//class ExamplePlaybackService : MediaSessionService() {
class ExampleLibraryPlaybackService : MediaLibraryService() {

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaLibrarySession? = null
    private val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val myNoisyAudioStreamReceiver = BecomingNoisyReceiver()

    //是否注册BecomingNoisyReceiver true:注册, false:未注册
    private var ifRegister: Boolean = false

    lateinit var audioTrack: AudioTrack

    val scope = CoroutineScopeUtils.getIo("ExamplePlaybackService")


    @Inject
    lateinit var downloadCacheController: DownloadCacheController

    @Inject
    lateinit var musicController: MusicController

    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var db: DatabaseClient

    @Inject
    lateinit var imageApiClient: ImageApiClient

    @Inject
    lateinit var lrcServer: LrcServer

    @Inject
    lateinit var dataSourceManager: DataSourceManager

    @Inject
    lateinit var fadeController: AudioFadeController

    @Inject
    lateinit var mediaServer: MediaServer

    private var exoPlayerListener: ExoPlayerListener? = null

    override fun onCreate() {
        super.onCreate()
        downloadCacheController.createScope(scope.coroutineContext)
        musicController.createScope(scope.coroutineContext)
        fadeController.createScope(scope.coroutineContext)
        lrcServer.init(scope.coroutineContext)
//重试次数和重试时间 https://stackoverflow.com/questions/78042428/how-can-i-increase-exoplayers-buffering-time

        //可以自定义解码
        val renderersFactory = DefaultRenderersFactory(this)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

        val exoPlayerBuilder = ExoPlayer.Builder(this, renderersFactory)
        // 设置逐步加载数据的缓存数据源
        val defaultProvider =
            AudioTrackAudioOutputProvider.Builder(this).build()
        downloadCacheController.getMediaSourceFactory().let {
            exoPlayerBuilder.setMediaSourceFactory(it)
            Log.i("catch", "设置缓存工厂")
        }

//        AudioAttributes.DEFAULT.audioAttributesV21

        settingsManager.setOnListener(object : OnSettingsChangeListener {
            override fun onHandleAudioFocusChanged(ifHandleAudioFocus: Boolean) {
                exoPlayer?.setAudioAttributes(
                    AudioAttributes.DEFAULT,
                    !ifHandleAudioFocus
                )
            }
        })


        // 创建ExoPlayer
        val exoPlayer = exoPlayerBuilder
            .setAudioAttributes(
                AudioAttributes.DEFAULT, /* handleAudioFocus= */
                !settingsManager.get().ifHandleAudioFocus
            )
            .setAudioOutputProvider(object : ForwardingAudioOutputProvider(defaultProvider) {
                override fun getAudioOutput(config: AudioOutputProvider.OutputConfig): AudioOutput {
                    Log.i("music", "创建 AudioTrack")
                    val output = defaultProvider.getAudioOutput(config)
                    audioTrack = output.audioTrack
                    fadeController.attach(audioTrack)
                    return output
                }
            })
            .build()
        //设置音乐分流
        val audioOffloadPreferences =
            TrackSelectionParameters.AudioOffloadPreferences.Builder()
                .setAudioOffloadMode(TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED)
                // Add additional options as needed
                .setIsGaplessSupportRequired(true)
                .build()
        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
            .buildUpon()
            .setAudioOffloadPreferences(audioOffloadPreferences)
            .build()
        this.exoPlayer = exoPlayer

        val exoPlayerListener = ExoPlayerListener(
            musicController,
            downloadCacheController,
            lrcServer,
            exoPlayer
        )
        this.exoPlayerListener = exoPlayerListener

        //这里的可以获得元数据
        exoPlayer.addAnalyticsListener(XyLogger(mediaServer = mediaServer))
        exoPlayer.addListener(exoPlayerListener)

        val sessionCommand = SessionCommand(SAVE_TO_FAVORITES, Bundle.EMPTY)
        val removeFavorites = SessionCommand(REMOVE_FROM_FAVORITES, Bundle.EMPTY)
        val favoriteButton =
            CommandButton.Builder(CommandButton.ICON_UNDEFINED)
                .setEnabled(true)
                .setDisplayName(this.getString(R.string.favorite_added))
                .setCustomIconResId(R.drawable.favorite)
                .setSessionCommand(sessionCommand)
                .build()

        val removeFromFavoritesButton = CommandButton.Builder(CommandButton.ICON_UNDEFINED)
            .setDisplayName(this.getString(R.string.favorite_removed))
            .setCustomIconResId(R.drawable.favorite_empty)
            .setSessionCommand(removeFavorites)
            .build()

        registerReceiver(myNoisyAudioStreamReceiver, intentFilter)

        val forwardingPlayer = object : ForwardingPlayer(exoPlayer) {

            override fun play() {
                Log.i("music", "音乐播放")
                registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
                ifRegister = true
                super.play()
                fadeController.fadeIn()
            }

            override fun pause() {
                Log.i("music", "音乐暂停")
                if (ifRegister)
                    unregisterReceiver(myNoisyAudioStreamReceiver)
                ifRegister = false
                musicController.updateState(PlayStateEnum.Pause)
                fadeController.fadeOut {
                    super.pause()
                }
            }

            override fun seekToNext() {
                Log.i("music", "下一首音乐")
                musicController.seekToNext()
            }

            override fun seekToPrevious() {
                Log.i("music", "上一首音乐")
                musicController.seekToPrevious()
            }

        }
        // 基于已创建的ExoPlayer创建MediaSession

        val mediaSessionBuilder = MediaLibrarySession.Builder(
            this,
            forwardingPlayer,
            object : MediaLibrarySession.Callback {

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: Bundle
                ): ListenableFuture<SessionResult> {
                    val argsValue = args.getString(Constants.MUSIC_PLAY_CUSTOM_COMMAND_TYPE_KEY)
                    //根据args附件参数,进行判断是否是默认行为和主动调用行为
                    if (customCommand.customAction == SAVE_TO_FAVORITES) {
                        Log.i("music", "取消收藏音乐")
                        //更新音乐收藏
                        if (!argsValue.equals(Constants.MUSIC_PLAY_CUSTOM_COMMAND_TYPE)) {
                            musicController.musicInfo?.let {
                                musicController.invokingOnFavorite(
                                    it.itemId
                                )
                            }
                        }
                        session.setCustomLayout(ImmutableList.of(removeFromFavoritesButton))

                        return Futures.immediateFuture(
                            SessionResult(SessionResult.RESULT_SUCCESS)
                        )

                    } else if (customCommand.customAction == REMOVE_FROM_FAVORITES) {
                        //保存收藏
                        if (!argsValue.equals(Constants.MUSIC_PLAY_CUSTOM_COMMAND_TYPE)) {
                            musicController.musicInfo?.let {
                                musicController.invokingOnFavorite(
                                    it.itemId,
                                )
                            }

                        }
                        session.setCustomLayout(ImmutableList.of(favoriteButton))

                        return Futures.immediateFuture(
                            SessionResult(SessionResult.RESULT_SUCCESS)
                        )

                    }

                    return super.onCustomCommand(session, controller, customCommand, args)
                }

                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                        .setAvailableSessionCommands(
                            MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                                .add(sessionCommand)
                                .add(removeFavorites)
                                .build(),
                        )
                        .build()
                }
            })
            .setCustomLayout(ImmutableList.of(favoriteButton))

        mediaSessionBuilder.setBitmapLoader(
            CacheBitmapLoader(
                DataSourceBitmapLoader
                    .Builder(this)
                    .setExecutorService(
                        Preconditions.checkNotNull(
                            DataSourceBitmapLoader.DEFAULT_EXECUTOR_SERVICE.get()
                        )
                    )
                    .setDataSourceFactory(
                        XyDefaultDataSourceFactory(
                            DefaultDataSource.Factory(
                                this,
                                OkHttpDataSource.Factory(imageApiClient.okhttpClientFunction())
                            )
                        )

                    ).build()
            )
        )
        mediaSession = mediaSessionBuilder.build()
    }

    override fun onDestroy() {
        // 释放相关实例
        Log.i("music", "数据释放")
        lrcServer.close()
        exoPlayerListener?.let { exoPlayer?.removeListener(it) }
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
        mediaSession?.release()
        mediaSession = null
        downloadCacheController.close()
        musicController.close()
        clearListener()
        unregisterReceiver(myNoisyAudioStreamReceiver)
        scope.close()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player?.playWhenReady == false || player?.mediaItemCount == 0) {
            stopSelf()
        }
    }
}