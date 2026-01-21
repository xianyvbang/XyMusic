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

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.FileTypes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.DISCONTINUITY_REASON_SEEK
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_SEEK
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
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.config.setting.OnSettingsChangeListener
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.entity.data.music.TranscodingAndMusicUrlData
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/**
 * 音乐播放控制器
 */
@OptIn(UnstableApi::class)
class MusicController(
    private val application: Context,
    private val downloadCacheController: DownloadCacheController,
    private val favoriteRepository: FavoriteRepository,
    private val fadeController: AudioFadeController,
    private val settingsManager: SettingsManager,
    private val dataSourceManager: DataSourceManager
) {

    val scope = CoroutineScopeUtils.getIo("MusicController")

    // 原始歌曲列表
    var originMusicList by mutableStateOf(emptyList<XyPlayMusic>())
        private set

    //当前播放歌曲的进度
    var musicCurrentPositionMap = mutableStateMapOf<String, Long>()
        private set

    // 当前播放的歌曲在原始歌曲列表中的索引
    var curOriginIndex by mutableIntStateOf(Constants.MINUS_ONE_INT)
        private set

    //加载的音乐最大页码
    var pageNum by mutableIntStateOf(0)
        private set

    var pageSize by mutableIntStateOf(0)
        private set

    //当前播放音乐信息
    var musicInfo by mutableStateOf<XyPlayMusic?>(null)
        private set

    var picByte: ByteArray? by mutableStateOf(null)
        private set

    //音频总时长
    var duration by mutableLongStateOf(0L)
        private set

    //当前状态
    var state by mutableStateOf(PlayStateEnum.None)
        private set

    //播放进度
    private val _progressStateFlow = MutableStateFlow(0L)
    val progressStateFlow = _progressStateFlow.asStateFlow()

    //当前播放数据类型
    var playDataType by mutableStateOf(MusicPlayTypeEnum.FOUNDATION)
        private set

    //片头跳过时间
    var headTime by mutableLongStateOf(0L)
        private set

    //片尾跳过时间
    var endTime by mutableLongStateOf(0L)
        private set

    //当前播放模式
    var playType by mutableStateOf(PlayerTypeEnum.SEQUENTIAL_PLAYBACK)
        private set

    //事件发送流
    private val _events = MutableSharedFlow<PlayerEvent>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val events = _events.asSharedFlow()

    lateinit var progressTicker: PlayProgressTicker
        private set

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val mediaController: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    init {
        settingsManager.setOnListener(object : OnSettingsChangeListener {
            override fun onCacheMaxBytesChanged(
                cacheUpperLimit: CacheUpperLimitEnum,
                oldCacheUpperLimit: CacheUpperLimitEnum
            ) {
                if (oldCacheUpperLimit == CacheUpperLimitEnum.No && cacheUpperLimit != CacheUpperLimitEnum.No && state == PlayStateEnum.Playing) {
                    musicInfo?.let {
                        startCache(it, settingsManager.getStatic())
                    }
                }
            }
        })
    }


    fun replacePlaylistItemUrl() {
        if (originMusicList.isNotEmpty()) {
            originMusicList = originMusicList.map {
                it.setMusicUrl(
                    getMusicUrl(
                        it.itemId,
                        it.plexPlayKey
                    ).musicUrl
                )
                it
            }
            musicInfo?.let {
                it.setMusicUrl(
                    getMusicUrl(
                        it.itemId,
                        it.plexPlayKey
                    ).musicUrl
                )
                downloadCacheController.cancelAllCache()
                startCache(it, settingsManager.getStatic())
            }

            if (state == PlayStateEnum.Pause) {
                mediaController?.replaceMediaItems(
                    0, originMusicList.size,
                    originMusicList.map {
                        musicSetMediaItem(it)
                    }
                )
            } else {
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

    fun <T> splitListExcludeIndex(list: List<T>, index: Int): Pair<List<T>, List<T>> {
        require(index in list.indices) { "index 越界" }

        val first = list.subList(0, index)
        val second = list.subList(index + 1, list.size)

        return first to second
    }


    //https://developer.android.google.cn/guide/topics/media/exoplayer/listening-to-player-events?hl=zh-cn
    private val playerListener = @UnstableApi object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_IDLE -> {
                    //播放器停止时的状态
                    Log.i("music", "STATE_IDLE")
                }

                Player.STATE_BUFFERING -> {
                    // 正在缓冲数据
                    if (state == PlayStateEnum.Playing)
                        updateState(PlayStateEnum.Loading)
                    Log.i("music", "STATE_BUFFERING")
                }

                Player.STATE_READY -> {
                    // 可以开始播放 恢复播放
                    if (state == PlayStateEnum.Loading) {
                        updateState(PlayStateEnum.Playing)
                        /*musicInfo?.let {
                            cacheController.cacheMedia(it)
                        }*/
                    }
                    Log.i("music", "STATE_READY")
                }

                Player.STATE_ENDED -> {
                    // 播放结束
                    updateState(PlayStateEnum.None)
                    Log.i("music", "STATE_ENDED")
                }
            }
        }


        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            Log.i(
                "music",
                "当前索引${mediaController?.currentMediaItemIndex} --- ${mediaMetadata.title}"
            )

            picByte = if (musicInfo?.pic.isNullOrBlank()) {
                mediaMetadata.artworkData
            } else {
                null
            }
            //获取当前音乐的index
            setCurrentPositionData(mediaController?.currentPosition ?: 0)
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            // 获取播放错误信息
            Log.e("music", "播放报错$error", error)
            if (state != PlayStateEnum.Pause) {
                seekToNext()
            }
        }

        //检测播放何时转换为其他媒体项
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.localConfiguration?.let { localConfiguration ->

                if (localConfiguration.tag == null) {
                    Log.i("music", "诶切换类型 $reason")
                    //手动切换
                    if (reason == MEDIA_ITEM_TRANSITION_REASON_SEEK || reason == MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED) {
                        musicInfo?.let {
                            scope.launch {
                                _events.emit(PlayerEvent.BeforeChangeMusic)
                            }
                        }
                    }
                    //自动播放
                    if (reason == MEDIA_ITEM_TRANSITION_REASON_REPEAT || reason == MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                        musicInfo?.let {
                            scope.launch {
                                _events.emit(PlayerEvent.RemovePlaybackProgress(it.itemId))
                            }
                        }
                    }

                    Log.i("music", "上一个index ${mediaController?.previousMediaItemIndex}")

                    curOriginIndex = mediaController?.currentMediaItemIndex ?: 0
                    if (originMusicList.isNotEmpty() && curOriginIndex >= originMusicList.size - 1) {
                        scope.launch {
                            _events.emit(PlayerEvent.NextList(pageNum))
                        }
                    }
                    musicInfo = originMusicList[curOriginIndex]
                    updateDuration(musicInfo?.runTimeTicks ?: 0)

                    //todo 替换mediaitem的位置
                    //如果状态是播放的话
                    if (state != PlayStateEnum.Pause)
                        startCache(originMusicList[curOriginIndex], settingsManager.getStatic())
                    musicInfo?.let {
                        updateButtonCommend(
                            it.itemId in favoriteRepository.favoriteSet.value
                        )
                        scope.launch {
                            _events.emit(
                                PlayerEvent.ChangeMusic(
                                    it.itemId,
                                    it.artistIds?.get(0),
                                    it.artists?.get(0)
                                )
                            )
                        }
                        //判断音乐播放进度是否为0,如果为0则不处理,不为0则需要跳转到相应的进度
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
                musicInfo?.let {
                    scope.launch {
                        _events.emit(
                            PlayerEvent.PositionSeekTo(
                                newPosition.positionMs,
                                it.itemId,
                                settingsManager.get().playSessionId
                            )
                        )
                    }
                }
            }
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            Log.i("music", "播放报错")
            super.onPlayerErrorChanged(error)
        }

    }

    /**
     * 初始化播放
     */
    fun initController(onRestorePlaylists: (() -> Unit)? = null) {
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

    fun resume() {
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

    /**
     * 开始缓存
     */
    fun startCache(music: XyPlayMusic, ifStatic: Boolean) {
        if (music.filePath.isNullOrBlank())
            downloadCacheController.cacheMedia(music, ifStatic)
    }

    fun pause() {
        mediaController?.pause()
    }

    fun seekTo(millSeconds: Long) {
        Log.i("music", "调用seekTo $millSeconds")
        setCurrentPositionData(millSeconds)
        mediaController?.run {
            seekTo(millSeconds)
            if (state == PlayStateEnum.Pause)
                thisPlay()
//                play()
        }

    }

    fun seekToIndex(index: Int) {
        Log.i("music", "调用seekToIndex")
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
    fun seekToIndex(itemId: String) {
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
    fun seekToPrevious() {
        Log.i("music", "调用seekToPrevious ${mediaController?.hasPreviousMediaItem()}")

        //这里进行缓存数据替换

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
    fun seekToNext() {
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

    fun clear() {
        mediaController?.release()
    }

    fun removeItem(index: Int) {
        //判断要删除的索引和当前索引是否一致
        val tmpList = mutableListOf<XyPlayMusic>()
        tmpList.addAll(originMusicList)
        tmpList.removeAt(index)
        originMusicList = tmpList
        mediaController?.removeMediaItem(index)
        curOriginIndex = mediaController?.currentMediaItemIndex ?: 0
        if (originMusicList.isEmpty()) {
            clearPlayerList()
        }
        //需要重新计算索引
        Log.i("music", "删除索引位置$index")
    }

    /**
     * 设置播放类型
     */
    fun setPlayTypeData(playerTypeEnum: PlayerTypeEnum) {
        playType = playerTypeEnum
        scope.launch {
            _events.emit(PlayerEvent.PlayerTypeChange(playerTypeEnum))
        }
        generateRealMusicList()
    }

    /**
     * 列表中添加数据
     */
    fun addMusicList(
        musicList: List<XyPlayMusic>,
        artistId: String? = null,
        isPlayer: Boolean? = null
    ) {
        var nowIndex = 0
        if (originMusicList.isNotEmpty()) {
            nowIndex = curOriginIndex + 1
            val tmpList = mutableListOf<XyPlayMusic>()
            tmpList.addAll(originMusicList)
            tmpList.addAll(nowIndex, musicList)
            originMusicList = tmpList
        } else {
            val tmpList = mutableListOf<XyPlayMusic>()
            tmpList.addAll(originMusicList)
            tmpList.addAll(musicList)
            originMusicList = tmpList

        }
        mediaController?.run {
            val mediaItemList = musicList.map { item -> musicSetMediaItem(item) }
            addMediaItems(nowIndex, mediaItemList)
            if (isPlayer != null && isPlayer) {
                mediaController?.let { media ->
                    seekToIndex(media.nextMediaItemIndex)
                }
            }
            scope.launch {
                _events.emit(PlayerEvent.AddMusicList(artistId))
            }
        }

    }

    /**
     * 添加音乐到列表
     */
    fun addMusic(
        music: XyPlayMusic,
        isPlayer: Boolean? = null
    ) {

        addNextPlayer(music)
        if (isPlayer == true) {
            seekToNext()
        }

    }


    /**
     * 添加下一首播放功能
     */
    fun addNextPlayer(music: XyPlayMusic) {
        val mediaItem = musicSetMediaItem(music)

        if (originMusicList.isEmpty()) {

            val tmpList = mutableListOf<XyPlayMusic>()
            tmpList.addAll(originMusicList)
            tmpList.add(music)
            originMusicList = tmpList
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
                    originMusicList = tmpList
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
                originMusicList = tmpList
                mediaController?.let { media ->
                    media.addMediaItem(
                        media.nextMediaItemIndex,
                        mediaItem
                    )
                }
            }

        }

        scope.launch {
            _events.emit(PlayerEvent.AddMusicList(music.artistIds?.get(0)))
        }
    }

    /**
     * 设置当前音乐列表
     */
    fun initMusicList(
        musicDataList: List<XyPlayMusic>,
        musicCurrentPositionMapData: Map<String, Long>?,
        originIndex: Int?,
        pageNum: Int,
        pageSize: Int,
        artistId: String?,
        ifInitPlayerList: Boolean = false,
        musicPlayTypeEnum: MusicPlayTypeEnum
    ) {
        playDataType = musicPlayTypeEnum

        Log.i("music", "初始化音乐列表开始播放")

        originMusicList = emptyList()

        if (musicCurrentPositionMapData != null) {
            musicCurrentPositionMap.clear()
            musicCurrentPositionMap.putAll(musicCurrentPositionMapData)
        }
        originIndex?.let { curOriginIndex = originIndex }

        downloadCacheController.cancelAllCache()
        val tmpList = mutableListOf<XyPlayMusic>()
        tmpList.addAll(musicDataList)
        originMusicList = tmpList
        this.pageNum = pageNum
        this.pageSize = pageSize

        if (musicDataList.isNotEmpty()) {
            downloadCacheController.cancelAllCache()
        }
        //设置播放类型
        generateRealMusicList()

        // 停止之前播放
        mediaController?.run {
            clearMediaItems()
            val mediaItemList = musicDataList.map { item -> musicSetMediaItem(item) }
            if (originIndex != null)
                setMediaItems(mediaItemList, originIndex, C.TIME_UNSET)
            else
                setMediaItems(mediaItemList)
            if (!ifInitPlayerList) {
                thisPlay()
            } else {
                stop()
                prepare()
            }
            scope.launch {
                _events.emit(PlayerEvent.AddMusicList(artistId, ifInitPlayerList))
            }
        }
    }

    /**
     * 将MusicArtistExtend转换成MediaItem
     */
    private fun musicSetMediaItem(playMusic: XyPlayMusic): MediaItem {

        //设置单个资源
        val itemId = playMusic.itemId
        var mediaItemBuilder = MediaItem.Builder()
        mediaItemBuilder.setCustomCacheKey(itemId)
        val pic = playMusic.pic

        if (playMusic.filePath.isNullOrBlank()) {
            val transcodingAndMusicUrlInfo =
                getMusicUrl(itemId, playMusic.plexPlayKey)
            playMusic.setMusicUrl(transcodingAndMusicUrlInfo.musicUrl)
            mediaItemBuilder.setUri(transcodingAndMusicUrlInfo.musicUrl)

            val normalizeMimeType =
                MimeTypes.normalizeMimeType(MimeTypes.BASE_TYPE_AUDIO + "/${playMusic.container}")
            //todo 这里的判断临时先用这个判断,后面改成
            mediaItemBuilder.setMimeType(
                if (FileTypes.inferFileTypeFromMimeType(normalizeMimeType) != -1
                    && transcodingAndMusicUrlInfo.static
                ) normalizeMimeType else MimeTypes.APPLICATION_M3U8
            )

            val mediaItem =
                downloadCacheController.downloadManager.downloadIndex.getDownload(itemId)?.request?.toMediaItem()
            if (mediaItem != null) {
                mediaItemBuilder = mediaItem.buildUpon()
            }

            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(playMusic.name)
                .setArtworkUri(pic?.toUri())
                .setDurationMs(playMusic.runTimeTicks)
                .setArtist(playMusic.artists?.joinToString()) // 可以设置其他元数据信息，例如专辑、时长等
//                .setExtras(bundle)
                .build()
            mediaItemBuilder.setMediaMetadata(mediaMetadata)
        } else {
            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(playMusic.name)
                .setArtist(playMusic.artists?.joinToString()) // 可以设置其他元数据信息，例如专辑、时长等
//                .setExtras(bundle)
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
        when (playType) {
            PlayerTypeEnum.RANDOM_PLAY -> {
                mediaController?.shuffleModeEnabled = true
                mediaController?.repeatMode = Player.REPEAT_MODE_ALL
            }

            PlayerTypeEnum.SINGLE_LOOP -> {
                mediaController?.shuffleModeEnabled = false
                mediaController?.repeatMode = Player.REPEAT_MODE_ONE
            }

            else -> {
                mediaController?.shuffleModeEnabled = false
                mediaController?.repeatMode = Player.REPEAT_MODE_ALL
            }
        }
    }

    /**
     * 设置倍速
     */
    fun setDoubleSpeed(value: Float) {
        mediaController?.setPlaybackSpeed(value)
    }

    /**
     * 清空播放列表
     */
    fun clearPlayerList() {
        pause()
        downloadCacheController.cancelAllCache()
        mediaController?.clearMediaItems()
        originMusicList = emptyList()
        musicCurrentPositionMap.clear()
        curOriginIndex = Constants.MINUS_ONE_INT
        musicInfo = null
        updateDuration(Constants.ZERO.toLong())
        setCurrentPositionData(Constants.ZERO.toLong())
        updateState(PlayStateEnum.None)
        headTime = Constants.ZERO.toLong()
        endTime = Constants.ZERO.toLong()
        pageNum = Constants.ZERO
        pageSize = Constants.ZERO
    }

    private fun setOnCurrentPosition() {
        this.mediaController?.let { onControllerReady(it) }
    }

    fun onControllerReady(controller: MediaController) {
        progressTicker = PlayProgressTicker(
            controller = controller,
            intervalMs = MUSIC_POSITION_UPDATE_INTERVAL
        ) { position ->
            setCurrentPositionData(position)
        }
    }

    /**
     * 设置跳过片头片尾时间
     */
    fun setHeadAndEntTime(headTime: Long, endTime: Long) {
        this.headTime = headTime
        this.endTime = endTime
    }

    /**
     * 更新当前音乐的收藏信息->更新UI数据
     */
    fun updateCurrentFavorite(isFavorite: Boolean) {
        Log.i("music", "收藏响应${isFavorite}")
        musicInfo = musicInfo?.copy(ifFavoriteStatus = isFavorite)
        musicInfo?.let {
            updateButtonCommend(isFavorite)
        }
    }

    /**
     * 设置PageNum
     */
    fun setPageNumData(pageNum: Int) {
        this.pageNum = pageNum
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

    /**
     * 设置当前播放进度
     */
    fun setCurrentPositionData(currentPosition: Long) {
        _progressStateFlow.value = currentPosition
    }

    /**
     * 调用onFavorite
     */
    fun invokingOnFavorite(itemId: String) {
        scope.launch {
            _events.emit(PlayerEvent.Favorite(itemId))
        }
    }

    fun updateState(state: PlayStateEnum) {
        Log.i("music", "是否播放中--- ${mediaController?.isPlaying} --- $state")
        this.state = state
    }

    fun updateDuration(duration: Long) {
        this.duration = duration
    }

    fun reportedPlayEvent() {
        musicInfo?.let {
            scope.launch {
                _events.emit(PlayerEvent.Play(it.itemId, settingsManager.get().playSessionId))
            }
        }
    }

    fun reportedPauseEvent() {
        musicInfo?.let {
            scope.launch {
                _events.emit(PlayerEvent.Pause(it.itemId, settingsManager.get().playSessionId))
            }
        }
    }
}