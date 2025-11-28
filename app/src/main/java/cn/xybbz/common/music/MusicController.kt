package cn.xybbz.common.music

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
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
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.Constants.MUSIC_PLAY_CUSTOM_COMMAND_TYPE
import cn.xybbz.common.constants.Constants.MUSIC_PLAY_CUSTOM_COMMAND_TYPE_KEY
import cn.xybbz.common.constants.Constants.MUSIC_POSITION_UPDATE_INTERVAL
import cn.xybbz.common.constants.Constants.REMOVE_FROM_FAVORITES
import cn.xybbz.common.constants.Constants.SAVE_TO_FAVORITES
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File


/**
 * 音乐播放控制器
 */
@OptIn(UnstableApi::class)
class MusicController(
    private val application: Context,
    private val cacheController: CacheController,
    private val favoriteRepository: FavoriteRepository
) {

    // 原始歌曲列表
    var originMusicList by mutableStateOf(emptyList<XyMusicExtend>())
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
    var musicInfo by mutableStateOf<XyMusicExtend?>(null)
        private set

    //音频总时长
    var duration by mutableLongStateOf(0L)
        private set

    //当前播放进度
    var currentPosition by mutableLongStateOf(0L)
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

    //设置删除播放历史进度
    private var removePlaybackProgress: ((String) -> Unit)? = null

    //暂停
    private var onPause: ((String, String, String) -> Unit)? = null

    //播放
    private var onPlay: ((String, String) -> Unit)? = null

    //进度跳转
    private var onSeekTo: ((Long, String, String) -> Unit)? = null

    //手动切换音频之前
    private var onManualChangeMusic: (() -> Unit)? = null

    //音频切换
    private var onChangeMusic: ((String) -> Unit)? = null

    //收藏/取消收藏
    var onFavorite: ((String) -> Unit)? = null

    //加载下一页数据,参数是页码
    var onNextList: ((Int) -> Unit)? = null

    //播放列表增加数据,传值未艺术家id
    private var onAddMusicList: ((String?) -> Unit)? = null

    //设置播放模式变化监听方法
    private var onPlayerTypeChange: ((PlayerTypeEnum) -> Unit)? = null

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val mediaController: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    //https://developer.android.google.cn/guide/topics/media/exoplayer/listening-to-player-events?hl=zh-cn
    private val playerListener = @UnstableApi object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            // 播放状态变化回调
            Log.i("=====", "当前播放状态$isPlaying")
            if (isPlaying) {
                state = PlayStateEnum.Playing
                duration = mediaController?.duration ?: 0
                musicInfo?.let {
                    onPlay?.invoke(it.itemId, it.playSessionId)
                }
            } else if (state != PlayStateEnum.Loading) {
                state = PlayStateEnum.Pause
                musicInfo?.let {
                    onPause?.invoke(it.itemId, it.playSessionId, it.musicUrl)
                }
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_IDLE -> {
                    //播放器停止时的状态
                    Log.i("=====", "STATE_IDLE")
                }

                Player.STATE_BUFFERING -> {
                    // 正在缓冲数据
                    state = PlayStateEnum.Loading
                    Log.i("=====", "STATE_BUFFERING")
                }

                Player.STATE_READY -> {
                    duration = mediaController?.duration!!
                    // 可以开始播放 恢复播放
//                    state = PlayStateEnum.Pause

                    Log.i("=====", "STATE_READY")
                }

                Player.STATE_ENDED -> {
                    // 播放结束
                    state = PlayStateEnum.Pause
                    Log.i("=====", "STATE_ENDED")
                }
            }
        }


        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)
            Log.i("=====", "获得当前播放信息$mediaMetadata")
            Log.i(
                "=====",
                "当前索引${mediaController?.currentMediaItemIndex} --- ${mediaMetadata.title}"
            )
            //获取当前音乐的index
            setCurrentPositionData(mediaController?.currentPosition ?: 0)
            duration = mediaController?.duration ?: 0
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            // 获取播放错误信息
            Log.e("=====", "播放报错$error", error)
            seekToNext()
        }

        //检测播放何时转换为其他媒体项
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.localConfiguration?.let { localConfiguration ->
                if (localConfiguration.tag == null) {
                    //手动切换
                    if (reason == MEDIA_ITEM_TRANSITION_REASON_SEEK || reason == MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED) {
                        musicInfo?.let {
                            onManualChangeMusic?.invoke()
                        }
                    }
                    //自动播放
                    if (reason == MEDIA_ITEM_TRANSITION_REASON_REPEAT || reason == MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                        musicInfo?.let {
                            removePlaybackProgress?.invoke(it.itemId)
                        }
                    }
                    curOriginIndex = mediaController?.currentMediaItemIndex ?: 0
                    if (originMusicList.isNotEmpty() && curOriginIndex >= originMusicList.size - 1) {
                        onNextList?.invoke(pageNum)
                    }
                    musicInfo = originMusicList[curOriginIndex]
                    //如果状态是播放的话
                    if (state != PlayStateEnum.Pause)
                        startCache(originMusicList[curOriginIndex])
                    musicInfo?.let {
                        updateButtonCommend(
                            it.itemId in favoriteRepository.favoriteSet.value
                        )
                        onChangeMusic?.invoke(it.itemId)
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
                            Log.i("=====", "音乐 ${it.name}没有播放进度")
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
                    onSeekTo?.invoke(newPosition.positionMs, it.itemId, it.playSessionId)
                }
            }
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            Log.i("=====", "播放报错")
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
        Log.i("=====", "恢复播放 ${mediaController?.mediaItemCount}")
        mediaController?.let {
            if (it.mediaItemCount > 0) {
                // 恢复播放
                Log.i("=====", "恢复播放")
                mediaController?.play()
                musicInfo?.let { music ->
                    startCache(music)
                }

            }
        }
    }

    /**
     * 开始缓存
     */
    fun startCache(music: XyMusicExtend) {
        cacheController.cacheMedia(music)
    }

    fun pause() {
        Log.i("=====", "点击暂停")
        mediaController?.pause()
        state = PlayStateEnum.Pause

    }

    fun seekTo(millSeconds: Long) {
        Log.i("=====", "调用seekTo")
        setCurrentPositionData(millSeconds)
        mediaController?.seekTo(millSeconds)
        mediaController?.play()

    }

    fun seekToIndex(index: Int) {
        Log.i("=====", "调用seekToIndex")
        setCurrentPositionData(Constants.ZERO.toLong())
        mediaController?.seekToDefaultPosition(index)
        mediaController?.play()
    }

    /**
     * 根据音乐id跳转
     */
    fun seekToIndex(itemId: String) {
        Log.i("=====", "调用seekToIndex(id)")
        setCurrentPositionData(Constants.ZERO.toLong())
        val indexOfFirst = originMusicList.indexOfFirst { it.itemId == itemId }
        if (indexOfFirst != -1) {
            mediaController?.seekToDefaultPosition(indexOfFirst)
            mediaController?.play()
        }
    }

    fun clear() {
        mediaController?.release()
    }

    fun removeItem(index: Int) {
        //判断要删除的索引和当前索引是否一致
        val tmpList = mutableListOf<XyMusicExtend>()
        tmpList.addAll(originMusicList)
        tmpList.removeAt(index)
        originMusicList = tmpList
        mediaController?.removeMediaItem(index)
        curOriginIndex = mediaController?.currentMediaItemIndex ?: 0
        if (originMusicList.isEmpty()) {
            clearPlayerList()
        }
        //需要重新计算索引
        Log.i("=====", "删除索引位置$index")
    }

    /**
     * 设置播放类型
     */
    fun setPlayTypeData(playerTypeEnum: PlayerTypeEnum) {
        playType = playerTypeEnum
        onPlayerTypeChange?.invoke(playerTypeEnum)
        generateRealMusicList()
    }

    /**
     * 列表中添加数据
     */
    fun addMusicList(
        musicList: List<XyMusicExtend>,
        artistId: String? = null,
        isPlayer: Boolean? = null
    ) {
        var nowIndex = 0
        if (originMusicList.isNotEmpty()) {
            nowIndex = curOriginIndex + 1
            val tmpList = mutableListOf<XyMusicExtend>()
            tmpList.addAll(originMusicList)
            tmpList.addAll(nowIndex, musicList)
            originMusicList = tmpList
        } else {
            val tmpList = mutableListOf<XyMusicExtend>()
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
            onAddMusicList?.invoke(artistId)
//            prepare()
        }

    }

    /**
     * 添加音乐到列表
     */
    fun addMusic(
        music: XyMusicExtend,
        artistId: String = "",
        isPlayer: Boolean? = null
    ) {
        val indexOfLast = originMusicList.indexOfLast { it.itemId == music.itemId }
        if (indexOfLast != -1) {
            if (isPlayer == true) {
                seekToIndex(indexOfLast)
            }
        } else {
            val nowIndex = curOriginIndex + 1
            val tmpList = mutableListOf<XyMusicExtend>()
            tmpList.addAll(originMusicList)
            val isListEmpty = tmpList.isEmpty()
            tmpList.add(nowIndex, music)
            originMusicList = tmpList

            mediaController?.run {
                addMediaItem(
                    if (isListEmpty) 0 else this.nextMediaItemIndex,
                    musicSetMediaItem(music)
                )
            }
            mediaController?.let { media ->
                if (isPlayer != null && isPlayer) {
                    seekToIndex(media.nextMediaItemIndex)
                }
            }

        }
        onAddMusicList?.invoke(artistId)
    }


    /**
     * 添加下一首播放功能
     */
    fun addNextPlayer(music: XyMusicExtend) {
        val mediaItem = musicSetMediaItem(music)

        if (originMusicList.isEmpty()) {

            val tmpList = mutableListOf<XyMusicExtend>()
            tmpList.addAll(originMusicList)
            tmpList.add(music)
            originMusicList = tmpList
            mediaController?.addMediaItem(mediaItem)
            mediaController?.run {
                play()
                prepare()
            }
        } else {
            //判断是否存在
            val indexOfFirst =
                originMusicList.indexOfFirst { it.itemId == music.itemId }
            if (indexOfFirst != -1) {
                val tmpList = mutableListOf<XyMusicExtend>()
                tmpList.addAll(originMusicList)
                tmpList.removeAt(indexOfFirst)
                originMusicList = tmpList
            }
            if (indexOfFirst != curOriginIndex + 1) {
                val tmpList = mutableListOf<XyMusicExtend>()
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
    }

    /**
     * 设置当前音乐列表
     */
    fun initMusicList(
        musicDataList: List<XyMusicExtend>,
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

        cacheController.cancelAllCache()
        val tmpList = mutableListOf<XyMusicExtend>()
        tmpList.addAll(musicDataList)
        originMusicList = tmpList
        this.pageNum = pageNum
        this.pageSize = pageSize

        if (musicDataList.isNotEmpty()) {
            cacheController.cancelAllCache()
        }
        //设置播放类型
        generateRealMusicList()

        // 停止之前播放
        mediaController?.run {
            stop()
            clearMediaItems()
            val mediaItemList = musicDataList.map { item -> musicSetMediaItem(item) }
            if (originIndex != null)
                setMediaItems(mediaItemList, originIndex, C.TIME_UNSET)
            else
                setMediaItems(mediaItemList)
            Log.i("=====", "当前播放状态${state}")
            if (!ifInitPlayerList) {
                Log.i("=====", "重新播放")
                play()
                prepare()

            }
            if (ifInitPlayerList) {
                this@MusicController.pause()
                state = PlayStateEnum.Pause
            }
            onAddMusicList?.invoke(artistId)

        }
    }

    /**
     * 将MusicArtistExtend转换成MediaItem
     */
    private fun musicSetMediaItem(musicExtend: XyMusicExtend): MediaItem {

        //设置单个资源
        val bundle = Bundle()
        bundle.putString("id", musicExtend.itemId)
        val mediaItemBuilder = MediaItem.Builder()

        var musicUrl = musicExtend.musicUrl
        if (musicExtend.filePath.isNullOrBlank()) {
            musicUrl += "&playSessionId=${musicExtend.playSessionId}"
            mediaItemBuilder.setUri(musicUrl)
        } else {
            mediaItemBuilder.setUri(Uri.fromFile(File(musicUrl)))
        }

        val pic = musicExtend.pic ?: ""
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(musicExtend.name)
            .setArtworkUri(pic.toUri())
            //可以存储文件的byte[]
//            .setArtworkData()
            .setArtist(musicExtend.artists) // 可以设置其他元数据信息，例如专辑、时长等
            .setExtras(bundle)
            .build()
        val normalizeMimeType =
            MimeTypes.normalizeMimeType(MimeTypes.BASE_TYPE_AUDIO + "/${musicExtend.container}")

        return mediaItemBuilder.setMediaId(musicExtend.itemId)
            .setMediaMetadata(mediaMetadata)
            //todo 这里的判断临时先用这个判断,后面改成
            .setMimeType(
                if (FileTypes.inferFileTypeFromMimeType(normalizeMimeType) != -1) normalizeMimeType else MimeTypes.APPLICATION_M3U8
            )
            .setTag(musicExtend)
            .build()
    }

    /**
     * 生成当前播放模式下的歌曲列表
     */
    private fun generateRealMusicList() {
        Log.i("=====", "设置播放模式${playType}")
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
     * 获取当前播放模式下的上一首歌曲
     */
    fun seekToPrevious() {
        mediaController?.seekToPreviousMediaItem()
        Log.i("=====", "调用seekToPrevious")
        resume()
    }

    /**
     * 获取当前播放模式下的下一首歌曲
     */
    fun seekToNext() {
        mediaController?.seekToNextMediaItem()
        Log.i("=====", "调用seekToNext")
        resume()
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
        cacheController.cancelAllCache()
        mediaController?.clearMediaItems()
        originMusicList = emptyList()
        musicCurrentPositionMap.clear()
        curOriginIndex = Constants.MINUS_ONE_INT
        musicInfo = null
        duration = Constants.ZERO.toLong()
        setCurrentPositionData(Constants.ZERO.toLong())
        state = PlayStateEnum.None
        headTime = Constants.ZERO.toLong()
        endTime = Constants.ZERO.toLong()
        pageNum = Constants.ZERO
        pageSize = Constants.ZERO
    }

    private fun setOnCurrentPosition(): Handler {
        val handler = object : Handler(mediaController?.applicationLooper!!) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (mediaController?.isPlaying == true) {
                    setCurrentPositionData(mediaController?.currentPosition!!)
                    if (endTime >= MUSIC_POSITION_UPDATE_INTERVAL) {
                        if (duration - currentPosition <= endTime) {
                            seekTo(endTime)
                        }
                    }

                }
                sendEmptyMessageDelayed(0, MUSIC_POSITION_UPDATE_INTERVAL) // 每秒更新一次进度
            }
        }
        handler.sendEmptyMessage(0)
        return handler
    }

    /**
     * 设置跳过片头片尾时间
     */
    fun setHeadAndEntTime(headTime: Long, endTime: Long) {
        this.headTime = headTime
        this.endTime = endTime
    }

    /**
     * 设置删除播放进度历史方法
     */
    fun setRemovePlaybackProgressFun(onRemoveProgress: (String) -> Unit) {
        removePlaybackProgress = onRemoveProgress
    }

    /**
     * 设置播放方法
     */
    fun setOnPlay(onPlayFun: (String, String) -> Unit) {
        onPlay = onPlayFun
    }

    /**
     * 设置暂停方法
     */
    fun setOnPauseFun(onPauseFun: (String, String, String) -> Unit) {
        onPause = onPauseFun
    }

    /**
     * 设置进度跳转方法
     */
    fun setOnSeekTo(onSeekToFun: (Long, String, String) -> Unit) {
        onSeekTo = onSeekToFun
    }

    /**
     * 设置手动音频切换方法
     */
    fun setOnManualChangeMusic(onManualChangeMusic: () -> Unit) {
        this.onManualChangeMusic = onManualChangeMusic
    }

    /**
     * 音频切换调用方法
     */
    fun setOnChangeMusic(onChangeMusic: (String) -> Unit) {
        this.onChangeMusic = onChangeMusic
    }


    /**
     * 收藏/取消收藏方法
     */
    fun setFavoriteMusic(onFavorite: (String) -> Unit) {
        this.onFavorite = onFavorite
    }

    /**
     * 设置读取下一个列表的方法
     */
    fun setOnNextListFun(onNextList: ((Int) -> Unit)? = null) {
        this.onNextList = onNextList
    }

    /**
     * 更新当前音乐的收藏信息->更新UI数据
     */
    fun updateCurrentFavorite(isFavorite: Boolean) {
        Log.i("=====", "收藏响应${isFavorite}")
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
     * 设置音乐列表变化时调用的方法
     */
    fun setOnAddMusicListFun(onAddMusicList: (String?) -> Unit) {
        this.onAddMusicList = onAddMusicList
    }

    /**
     * 播放模式变化方法
     */
    fun setOnPlayerTypeChangeFun(onPlayerTypeChange: (PlayerTypeEnum) -> Unit) {
        this.onPlayerTypeChange = onPlayerTypeChange
    }


    /**
     * 获得下一个的index
     */
    fun getNextIndexData(): Int? {
        return mediaController?.nextMediaItemIndex
    }

    /**
     * 设置当前播放进度
     */
    fun setCurrentPositionData(currentPosition: Long) {
        this.currentPosition = currentPosition
        _progressStateFlow.value = currentPosition
    }

    /**
     * 调用onFavorite
     */
    fun invokingOnFavorite(itemId: String) {
        this.onFavorite?.invoke(itemId)
//        favoriteRepository.toggle(id = itemId)
    }
}