package cn.xybbz.common.music

import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSourceBitmapLoader
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import cn.xybbz.R
import cn.xybbz.api.client.ImageApiClient
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.Constants.REMOVE_FROM_FAVORITES
import cn.xybbz.common.constants.Constants.SAVE_TO_FAVORITES
import cn.xybbz.config.SettingsConfig
import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.localdata.config.DatabaseClient
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
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

    @Inject
    lateinit var cacheController: CacheController

    @Inject
    lateinit var musicController: MusicController

    @Inject
    lateinit var settingsConfig: SettingsConfig

    @Inject
    lateinit var db: DatabaseClient

    @Inject
    lateinit var imageApiClient: ImageApiClient
    @Inject
    lateinit var lrcServer: LrcServer

    override fun onCreate() {
        super.onCreate()
//        setMediaNotificationProvider(ExampleMediaNotificationProvider(this))
//重试次数和重试时间 https://stackoverflow.com/questions/78042428/how-can-i-increase-exoplayers-buffering-time

        //可以自定义解码
        /*val renderersFactory: RenderersFactory = DefaultRenderersFactory(this)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)*/

        val renderersFactory = DefaultRenderersFactory(this)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

        val exoPlayerBuilder = ExoPlayer.Builder(this,renderersFactory)
        // 设置逐步加载数据的缓存数据源

        cacheController.getMediaSourceFactory()?.let {
            exoPlayerBuilder.setMediaSourceFactory(it)
            Log.i("catch", "设置缓存工厂")
        }

//        AudioAttributes.DEFAULT.audioAttributesV21
        // 创建ExoPlayer
        exoPlayer = exoPlayerBuilder
            .setAudioAttributes(
                AudioAttributes.DEFAULT, /* handleAudioFocus= */
                settingsConfig.get().ifHandleAudioFocus
            )
            .build()
        //这里的可以获得元数据
        exoPlayer?.addAnalyticsListener(XyLogger(lrcServer = lrcServer))


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

        val forwardingPlayer = object : ForwardingPlayer(exoPlayer!!) {

            override fun play() {
                Log.i("music", "音乐播放")
                registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
                ifRegister = true
                super.play()
            }

            override fun pause() {
                Log.i("music", "音乐暂停")
                if (ifRegister)
                    unregisterReceiver(myNoisyAudioStreamReceiver)
                ifRegister = false
                super.pause()
            }

            override fun seekToNext() {
                Log.i("music", "下一首音乐")
                musicController.seekToNext()
            }

        }
        // 基于已创建的ExoPlayer创建MediaSession

        val mediaSessionBuilder = MediaLibrarySession.Builder(
            this,
            forwardingPlayer,
            object : MediaLibrarySession.Callback {

//                    override fun onAddMediaItems(
//                        mediaSession: MediaSession,
//                        controller: MediaSession.ControllerInfo,
//                        mediaItems: MutableList<MediaItem>
//                    ): ListenableFuture<MutableList<MediaItem>> {
//                        Log.i("=====","重新填充数据")
//                        // NOTE: You can use the id from the mediaItems to look up missing
//                        // information (e.g. get URI from a database) and return a Future with
//                        // a list of playable MediaItems.
//
//                        // If your use case is really simple and the security/privacy reasons
//                        // mentioned earlier don't apply to you, you can add the URI to the
//                        // MediaItem request metadata in your activity/fragment and use it
//                        // to rebuild the playable MediaItem.
//                        val updatedMediaItems = mediaItems.map { mediaItem ->
//                            mediaItem.buildUpon()
//                                .build()
//                        }
//                        val mutableListOf = mutableListOf<MediaItem>()
//                        mutableListOf.addAll(updatedMediaItems)
//                        return Futures.immediateFuture<MutableList<MediaItem>>(mutableListOf);
//                    }


                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: Bundle
                ): ListenableFuture<SessionResult> {
                    val argsValue = args.getString(Constants.MUSIC_PLAY_CUSTOM_COMMAND_TYPE_KEY)
                    //根据args附件参数,进行判断是否是默认行为和主动调用行为
                    Log.i(
                        "=====",
                        "点击按钮数据${customCommand.customAction}----附加参数${args}"
                    )
                    customCommand.customExtras
                    if (customCommand.customAction == SAVE_TO_FAVORITES) {
                        // Do custom logic here
//                            saveToFavorites(session.player.currentMediaItem)
                        Log.i("=====", "取消收藏音乐")
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

//                            return super.onConnect(session, controller)
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
            DataSourceBitmapLoader(
                Assertions.checkStateNotNull<ListeningExecutorService>(
                    DataSourceBitmapLoader.DEFAULT_EXECUTOR_SERVICE.get()
                ), DefaultDataSource.Factory(
                    this,
                    OkHttpDataSource.Factory(imageApiClient.okhttpClientFunction())
                )
            )
        )

        mediaSession = mediaSessionBuilder.build()
    }

    override fun onDestroy() {
        // 释放相关实例
        Log.i("=====", "数据释放")
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
        mediaSession?.release()
        mediaSession = null
        cacheController.release()
        musicController.clear()
        clearListener()
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