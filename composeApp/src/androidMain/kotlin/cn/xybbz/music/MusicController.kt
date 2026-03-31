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

package cn.xybbz.music

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.FileTypes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.enums.AudioCodecEnum
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.Constants.MUSIC_PLAY_CUSTOM_COMMAND_TYPE
import cn.xybbz.common.constants.Constants.MUSIC_PLAY_CUSTOM_COMMAND_TYPE_KEY
import cn.xybbz.common.constants.Constants.MUSIC_POSITION_UPDATE_INTERVAL
import cn.xybbz.common.constants.Constants.REMOVE_FROM_FAVORITES
import cn.xybbz.common.constants.Constants.SAVE_TO_FAVORITES
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.config.image.CoverImageResolver
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.PlayerEvent
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.entity.data.music.TranscodingAndMusicUrlData
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch
import org.koin.core.component.get


/**
 * 音乐播放控制器
 */
@OptIn(UnstableApi::class)
class MusicController(
    private val application: Context,
    private val fadeController: AudioFadeAndroidController,
    private val dataSourceManager: DataSourceManager
) : MusicCommonController() {

    private val coverImageResolver: CoverImageResolver = get()

    lateinit var progressTicker: PlayProgressTicker
        private set

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val mediaController: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    private fun withMediaControllerOnApplicationThread(block: MediaController.() -> Unit) {
        val controller = mediaController ?: return
        if (Looper.myLooper() == controller.applicationLooper) {
            controller.block()
            return
        }
        Handler(controller.applicationLooper).post {
            val currentController = mediaController ?: return@post
            currentController.block()
        }
    }


    override fun replacePlaylistItemUrl() {
        if (originMusicList.isNotEmpty()) {

            updateOriginMusicList(originMusicList.map {
                it.setMusicUrl(
                    getMusicUrl(
                        it.itemId,
                        it.plexPlayKey
                    ).musicUrl
                )
                it
            })

            if (state == PlayStateEnum.Pause) {
                mediaController?.stop()
                mediaController?.replaceMediaItems(
                    0, originMusicList.size,
                    originMusicList.map {
                        musicSetMediaItem(it)
                    }
                )
                mediaController?.prepare()
                mediaController?.pause()
            } else {

                musicInfo?.let {
                    it.setMusicUrl(
                        getMusicUrl(
                            it.itemId,
                            it.plexPlayKey
                        ).musicUrl
                    )
                }

                val (left, right) = splitListExcludeIndex(originMusicList, curOriginIndex)
                if (left.isNotEmpty()) {
                    val leftItem = left.map { item ->
                        musicSetMediaItem(item)
                    }
                    mediaController?.replaceMediaItems(0, curOriginIndex, leftItem)
                }

                if (right.isNotEmpty()) {
                    val rightItem = right.map { item ->
                        musicSetMediaItem(item)
                    }
                    mediaController?.replaceMediaItems(
                        curOriginIndex + 1,
                        originMusicList.size,
                        rightItem
                    )
                }
            }

        }
    }

    override fun refreshPlaylistCoverMetadata() {
        if (originMusicList.isEmpty()) {
            return
        }
        val controller = mediaController ?: return
        val mediaItemList = originMusicList.map { item -> musicSetMediaItem(item) }
        val playlistSize = mediaItemList.size
        if (playlistSize == 0) {
            return
        }
        val currentIndex = when {
            controller.currentMediaItemIndex in 0 until playlistSize -> controller.currentMediaItemIndex
            curOriginIndex in 0 until playlistSize -> curOriginIndex
            else -> 0
        }
        val currentPosition = controller.currentPosition.coerceAtLeast(0L)
        val shouldResumePlay = controller.isPlaying || state == PlayStateEnum.Playing
        val shouldPause = !shouldResumePlay && state == PlayStateEnum.Pause

        controller.replaceMediaItems(0, controller.mediaItemCount, mediaItemList)
        controller.seekTo(currentIndex, currentPosition)

        if (shouldResumePlay) {
            thisPlay()
        } else if (shouldPause) {
            controller.prepare()
            controller.pause()
        }

        updateCoverRefreshVersion(1)
    }

    fun <T> splitListExcludeIndex(list: List<T>, index: Int): Pair<List<T>, List<T>> {
        require(index in list.indices) { "index 越界" }

        val first = list.subList(0, index)
        val second = list.subList(index + 1, list.size)

        return first to second
    }

    private val playerListener = XyPlayerListener(
        onGetState = { state },
        onUpdateState = { updateState(it) },
        onsetPicByte = { updatePicBytes(it) },
        onGetMusicInfo = { musicInfo },
        onSetMusicInfo = { updateCurrentMusic(originMusicList[curOriginIndex]) },
        onSeekToNext = { seekToNext() },
        onEventEmit = {
            updateEvent(it)
            updateCurrentFavorite(musicInfo?.ifFavoriteStatus ?: false)
        },
        onSetCurOriginIndex = {
            setCurrentPositionData(0)
            updateOriginIndex(mediaController?.currentMediaItemIndex ?: 0)
        },
        onOriginMusicListIsNotEmptyAndIndexEnd = {
            originMusicList.isNotEmpty() && curOriginIndex >= originMusicList.size - 1 && ifNextPage
        },
        onPageNumber = { pageNum },
        onUpdateDuration = {
            updateDuration(musicInfo?.runTimeTicks ?: 0)
        },
        onMusicStartCache = {
            startCache(originMusicList[curOriginIndex], settingsManager.getStatic())
        },
        onUpdatePlayerHistory = {
            if (musicCurrentPositionMap.containsKey(it.itemId)) {
                musicCurrentPositionMap[it.itemId]?.let { position ->
                    if (position > 0 && position > mediaController?.currentPosition!!) {
                        seekTo(position)
                    } else if (headTime > 0 && headTime > mediaController?.currentPosition!!) {
                        seekTo(headTime)
                    }
                }

            } else {
                Log.i("music", "音乐 ${it.name}没有播放进度")
            }
        },
        onPlaySessionId = { settingsManager.get().playSessionId }
    )

    //https://developer.android.google.cn/guide/topics/media/exoplayer/listening-to-player-events?hl=zh-cn

    /**
     * 初始化播放
     */
    override fun initController(onRestorePlaylists: (() -> Unit)?) {
        controllerFuture = MediaController.Builder(
            application,
            SessionToken(
                application,
                ComponentName(application, ExampleLibraryPlaybackService::class.java)
            )
        ).buildAsync()

        controllerFuture.addListener({
            mediaController?.let {
                it.apply {
                    setOnCurrentPosition()
                    // 设置播放监听
                    addListener(playerListener)

                    // 设置重复模式
                    // Player.REPEAT_MODE_ALL 无限重复
                    // Player.REPEAT_MODE_ONE 重复一次
                    // Player.REPEAT_MODE_OFF 不重复
                    repeatMode = Player.REPEAT_MODE_ALL
                    //单曲循环
//                    repeatMode = Player.REPEAT_MODE_ONE
                    // 设置当缓冲完毕后直接播放视频
                    playWhenReady = true
                }
            }
            onRestorePlaylists?.invoke()
        }, ContextCompat.getMainExecutor(application))
    }

    override fun resume() {
        mediaController?.let {
            if (it.mediaItemCount > 0) {
                updateState(PlayStateEnum.Loading)
                // 恢复播放
                Log.i("music", "恢复播放")
                mediaController?.run {
                    prepare()
                    play()
                }
                musicInfo?.let { music ->
                    Log.i("music", "回复播放开始缓存")
                    startCache(music, settingsManager.getStatic())
                }

            }
        }
    }

    fun thisPlay() {
        mediaController?.run {
            prepare()
            play()
        }
    }

    override fun pause() {
        mediaController?.pause()
    }

    override fun seekTo(millSeconds: Long) {
        Log.i("music", "调用seekTo $millSeconds")
        setCurrentPositionData(millSeconds)
        mediaController?.run {
            seekTo(millSeconds)
            if (state == PlayStateEnum.Pause)
                thisPlay()
//                play()
        }

    }

    override fun seekToIndex(index: Int) {
        Log.i("music", "调用seekToIndex")
        updateState(PlayStateEnum.Loading)
        setCurrentPositionData(Constants.ZERO.toLong())
        fadeController.fadeOut {
            mediaController?.seekToDefaultPosition(index)
            thisPlay()
            fadeController.fadeIn()
        }

    }

    /**
     * 根据音乐id跳转
     */
    override fun seekToIndex(itemId: String) {
        Log.i("music", "调用seekToIndex(id)")
        setCurrentPositionData(Constants.ZERO.toLong())
        val indexOfFirst = originMusicList.indexOfFirst { it.itemId == itemId }
        if (indexOfFirst != -1) {
            mediaController?.run {
                seekToDefaultPosition(indexOfFirst)
                thisPlay()
            }
        }
    }


    /**
     * 获取当前播放模式下的上一首歌曲
     */
    override fun seekToPrevious() {
        Log.i("music", "调用seekToPrevious ${mediaController?.hasPreviousMediaItem()}")

        //这里进行缓存数据替换
        updateState(PlayStateEnum.Loading)
        if (playType == PlayerTypeEnum.SINGLE_LOOP && mediaController?.hasPreviousMediaItem() != true) {
            seekToIndex((mediaController?.mediaItemCount ?: 1) - 1)
        } else {
            fadeController.fadeOut {
                mediaController?.seekToPreviousMediaItem()
                Log.i("music", "调用seekToPrevious")
                thisPlay()
                fadeController.fadeIn()
            }
        }
    }

    /**
     * 获取当前播放模式下的下一首歌曲
     */
    override fun seekToNext() {
        updateState(PlayStateEnum.Loading)
        if (playType == PlayerTypeEnum.SINGLE_LOOP && mediaController?.hasNextMediaItem() != true) {
            seekToIndex(0)
        } else {
            fadeController.fadeOut {
                mediaController?.seekToNextMediaItem()
                Log.i("music", "调用seekToNext")
                thisPlay()
                fadeController.fadeIn()
            }
        }

    }

    override fun getNextPlayableIndex(): Int? {
        return mediaController?.nextMediaItemIndex?.takeIf { it in originMusicList.indices }
    }

    override fun getPreviousPlayableIndex(): Int? {
        return mediaController?.previousMediaItemIndex?.takeIf { it in originMusicList.indices }
    }


    override fun removeItem(index: Int) {
        //判断要删除的索引和当前索引是否一致
        val tmpList = mutableListOf<XyPlayMusic>()
        tmpList.addAll(originMusicList)
        tmpList.removeAt(index)
        updateOriginMusicList(tmpList)
        mediaController?.removeMediaItem(index)
        updateOriginIndex(mediaController?.currentMediaItemIndex ?: 0)
        if (originMusicList.isEmpty()) {
            clearPlayerList()
        }
        //需要重新计算索引
        Log.i("music", "删除索引位置$index")
    }

    /**
     * 设置播放类型
     */
    override fun setPlayTypeData(playerTypeEnum: PlayerTypeEnum) {
        playType = playerTypeEnum
        scope.launch {
            updateEvent(PlayerEvent.PlayerTypeChange(playerTypeEnum))
        }
        generateRealMusicList()
    }

    /**
     * 列表中添加数据
     */
    override fun addMusicList(
        musicList: List<XyPlayMusic>,
        artistId: String?,
        isPlayer: Boolean?
    ) {
        var nowIndex = 0
        val tmpList = mutableListOf<XyPlayMusic>()
        if (originMusicList.isNotEmpty()) {
            nowIndex = curOriginIndex + 1
            val tmpList = mutableListOf<XyPlayMusic>()
            tmpList.addAll(originMusicList)
            tmpList.addAll(nowIndex, musicList)
        } else {
            tmpList.addAll(originMusicList)
            tmpList.addAll(musicList)
        }
        updateOriginMusicList(tmpList)
        mediaController?.run {
            val mediaItemList = musicList.map { item -> musicSetMediaItem(item) }
            addMediaItems(nowIndex, mediaItemList)
            if (isPlayer != null && isPlayer) {
                mediaController?.let { media ->
                    seekToIndex(media.nextMediaItemIndex)
                }
            }
            updateEvent(PlayerEvent.AddMusicList(artistId))
        }

    }


    /**
     * 添加下一首播放功能
     */
    override fun addNextPlayer(music: XyPlayMusic) {
        val mediaItem = musicSetMediaItem(music)

        if (originMusicList.isEmpty()) {
            val tmpList = mutableListOf<XyPlayMusic>()
            tmpList.addAll(originMusicList)
            tmpList.add(music)
            updateOriginMusicList(tmpList)
            mediaController?.addMediaItem(mediaItem)
            thisPlay()
        } else {
            //判断是否存在
            val indexOfFirst =
                originMusicList.indexOfFirst { it.itemId == music.itemId }
            if (indexOfFirst != -1) {
                if (indexOfFirst != curOriginIndex + 1) {
                    val tmpList = mutableListOf<XyPlayMusic>()
                    tmpList.addAll(originMusicList)
                    tmpList.add(curOriginIndex + 1, music)
                    tmpList.removeAt(indexOfFirst)
                    updateOriginMusicList(tmpList)
                    mediaController?.let { controller ->
                        controller.addMediaItem(
                            controller.nextMediaItemIndex,
                            mediaItem
                        )
                        controller.removeMediaItem(indexOfFirst)
                    }
                }
            } else {
                val tmpList = mutableListOf<XyPlayMusic>()
                tmpList.addAll(originMusicList)
                tmpList.add(curOriginIndex + 1, music)
                updateOriginMusicList(tmpList)
                mediaController?.let { media ->
                    media.addMediaItem(
                        media.nextMediaItemIndex,
                        mediaItem
                    )
                }
            }

        }
        updateEvent(PlayerEvent.AddMusicList(music.artistIds?.get(0)))
    }

    /**
     * 设置当前音乐列表
     */
    override fun initMusicList(
        musicDataList: List<XyPlayMusic>,
        musicCurrentPositionMapData: Map<String, Long>?,
        originIndex: Int?,
        pageNum: Int,
        pageSize: Int,
        artistId: String?,
        ifInitPlayerList: Boolean,
        musicPlayTypeEnum: MusicPlayTypeEnum
    ) {
        playDataType = musicPlayTypeEnum

        Log.i("music", "初始化音乐列表开始播放")
        updateRestartCount()
        updateOriginMusicList(emptyList())

        if (musicCurrentPositionMapData != null) {
            musicCurrentPositionMap.clear()
            musicCurrentPositionMap.putAll(musicCurrentPositionMapData)
        }
        updateOriginIndex(originIndex ?: 0)

        downloadCacheController.cancelAllCache()
        val tmpList = mutableListOf<XyPlayMusic>()
        tmpList.addAll(musicDataList)
        updateOriginMusicList(tmpList)
        setPageNumData(pageNum)
        updatePageSize(pageSize)

        if (musicDataList.isNotEmpty()) {
            downloadCacheController.cancelAllCache()
        }

        // MediaController methods must run on the controller application thread.
        withMediaControllerOnApplicationThread {
            clearMediaItems()
            if (!ifInitPlayerList) {
                updateState(PlayStateEnum.Loading)
                musicListSetMediaItems(musicDataList, originIndex)
                thisPlay()
            } else {
                updateState(PlayStateEnum.Pause)
                musicListSetMediaItems(musicDataList, originIndex)
                prepare()
                pause()
            }

            updateEvent(PlayerEvent.AddMusicList(artistId, ifInitPlayerList))
        }
    }

    /**
     * XyPlayMusic转换成MediaItem,并且加入到播放列表
     */
    private fun musicListSetMediaItems(playMusicList: List<XyPlayMusic>, originIndex: Int?) {
        val mediaItemList = playMusicList.map { item -> musicSetMediaItem(item) }
        if (originIndex != null)
            mediaController?.setMediaItems(mediaItemList, originIndex, C.TIME_UNSET)
        else
            mediaController?.setMediaItems(mediaItemList)
    }

    /**
     * 将MusicArtistExtend转换成MediaItem
     */
    private fun musicSetMediaItem(playMusic: XyPlayMusic): MediaItem {

        //设置单个资源
        val itemId = playMusic.itemId
        var mediaItemBuilder = MediaItem.Builder()
        val customCacheKey = downloadCacheController.getCacheKey(itemId)
        mediaItemBuilder.setCustomCacheKey(customCacheKey)
        val pic = coverImageResolver.resolveMusic(playMusic).primaryUrl

        if (playMusic.filePath.isNullOrBlank()) {
            val transcodingAndMusicUrlInfo =
                getMusicUrl(itemId, playMusic.plexPlayKey)
            playMusic.setMusicUrl(transcodingAndMusicUrlInfo.musicUrl)
            mediaItemBuilder.setUri(transcodingAndMusicUrlInfo.musicUrl)
            val normalizeMimeType =
                MimeTypes.normalizeMimeType(MimeTypes.BASE_TYPE_AUDIO + "/${playMusic.container}")
            val mimeType =
                if (dataSourceManager.dataSourceType?.ifHls == true && !transcodingAndMusicUrlInfo.static) {
                    MimeTypes.APPLICATION_M3U8
                } else if (dataSourceManager.dataSourceType?.ifHls == false && !transcodingAndMusicUrlInfo.static) {
                    normalizeMimeType
                } else {
                    val inferFileTypeFromMimeType =
                        FileTypes.inferFileTypeFromMimeType(normalizeMimeType)
                    if (inferFileTypeFromMimeType == -1) {
                        MimeTypes.APPLICATION_M3U8
                    } else {
                        normalizeMimeType
                    }
                }
            mediaItemBuilder.setMimeType(
                mimeType
            )

            val mediaItem =
                downloadCacheController.getMediaItem(customCacheKey)
            if (mediaItem != null && mediaItem is MediaItem) {
                mediaItemBuilder = mediaItem.buildUpon()
            }

            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(playMusic.name)
                .setArtworkUri(pic?.toUri())
                .setDurationMs(playMusic.runTimeTicks)
                .setArtist(playMusic.artists?.joinToString()) // 可以设置其他元数据信息，例如专辑、时长等
                .build()
            mediaItemBuilder.setMediaMetadata(mediaMetadata)
        } else {
            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(playMusic.name)
                .setArtworkUri(pic?.toUri())
                .setArtist(playMusic.artists?.joinToString()) // 可以设置其他元数据信息，例如专辑、时长等
                .build()
            mediaItemBuilder.setUri(playMusic.filePath?.toUri())
                .setMediaMetadata(mediaMetadata)
        }
        return mediaItemBuilder.setMediaId(itemId)
            .build()
    }

    private fun getMusicUrl(musicId: String, plexPlayKey: String?): TranscodingAndMusicUrlData {
        val audioBitRate = settingsManager.getAudioBitRate()

        val static: Boolean =
            settingsManager.getStatic()

        val musicUrl = dataSourceManager.getMusicPlayUrl(
            if (static) musicId else plexPlayKey ?: musicId,
            static,
            AudioCodecEnum.getAudioCodec(settingsManager.get().transcodeFormat),
            audioBitRate,
            settingsManager.get().playSessionId
        )

        return TranscodingAndMusicUrlData(audioBitRate, static, musicUrl)
    }

    /**
     * 生成当前播放模式下的歌曲列表
     */
    private fun generateRealMusicList() {
        Log.i("music", "设置播放模式${playType}")
        // MediaController 的状态变更必须切回它自己的 application thread 执行，
        // 否则在恢复播放列表或后台协程里调用时会触发线程校验异常。
        withMediaControllerOnApplicationThread {
            when (playType) {
                PlayerTypeEnum.RANDOM_PLAY -> {
                    shuffleModeEnabled = true
                    repeatMode = Player.REPEAT_MODE_ALL
                }

                PlayerTypeEnum.SINGLE_LOOP -> {
                    shuffleModeEnabled = false
                    repeatMode = Player.REPEAT_MODE_ONE
                }

                else -> {
                    shuffleModeEnabled = false
                    repeatMode = Player.REPEAT_MODE_ALL
                }
            }
        }
    }

    /**
     * 设置倍速
     */
    override fun setDoubleSpeed(value: Float) {
        mediaController?.setPlaybackSpeed(value)
    }

    /**
     * 清空播放列表
     */
    override fun clearPlayerList() {
        progressTicker.stop()
        withMediaControllerOnApplicationThread {
            clearMediaItems()
        }
        fadeController.release()
        super.clearPlayerList()
    }

    private fun setOnCurrentPosition() {
        this.mediaController?.let { onControllerReady(it) }
    }

    fun onControllerReady(controller: MediaController) {
        progressTicker = PlayProgressTicker(
            controller = controller,
            intervalMs = MUSIC_POSITION_UPDATE_INTERVAL,
            scope.coroutineContext
        ) { position ->
            setCurrentPositionData(position)
        }
    }


    /**
     * 更新当前音乐的收藏信息->更新UI数据
     */
    override fun updateCurrentFavorite(isFavorite: Boolean) {
        Log.i("music", "收藏响应${isFavorite}")
        updateCurrentMusic(musicInfo?.copy(ifFavoriteStatus = isFavorite))
        musicInfo?.let {
            updateButtonCommend(isFavorite)
        }
    }


    /**
     * 更新自定义按钮状态
     */
    fun updateButtonCommend(isFavorite: Boolean) {
        val args = Bundle()
        args.putString(MUSIC_PLAY_CUSTOM_COMMAND_TYPE_KEY, MUSIC_PLAY_CUSTOM_COMMAND_TYPE)
        //根据不同传值进行判断是否要调用session里的收藏方法
        if (isFavorite) {
            val removeFavorites = SessionCommand(REMOVE_FROM_FAVORITES, Bundle.EMPTY)
            mediaController?.sendCustomCommand(removeFavorites, args)
        } else {
            val sessionCommand = SessionCommand(SAVE_TO_FAVORITES, Bundle.EMPTY)
            mediaController?.sendCustomCommand(sessionCommand, args)
        }
    }


    override fun close() {
        withMediaControllerOnApplicationThread {
            clearMediaItems()
            removeListener(playerListener)
            release()
        }
        fadeController.close()
        progressTicker.close()
        super.close()
    }
}
