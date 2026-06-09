package cn.xybbz.music

import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.PlayerEvent
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.localdata.enums.PlayerModeEnum
import cn.xybbz.proxy.JvmReverseProxyServer
import cn.xybbz.startup.startJvmLyrics
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.log.LogEventListener
import uk.co.caprica.vlcj.log.LogLevel
import uk.co.caprica.vlcj.log.NativeLog
import uk.co.caprica.vlcj.media.Media
import uk.co.caprica.vlcj.media.MediaParsedStatus
import uk.co.caprica.vlcj.media.MediaRef
import uk.co.caprica.vlcj.media.Meta
import uk.co.caprica.vlcj.media.ParseFlag
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64
import uk.co.caprica.vlcj.media.MediaEventAdapter as VlcMediaEventAdapter

class JvmMusicController : MusicCommonController() {

    private var mediaPlayerFactory: MediaPlayerFactory? = null
    private var mediaPlayer: MediaPlayer? = null
    private var nativeLog: NativeLog? = null
    private var mediaPlayerListenerRegistered = false
    private var ignoreNextStoppedEvent = false
    // VLC loading is asynchronous, so keep the latest user play/pause intent separately.
    @Volatile
    private var playWhenReady = false

    private val nativeLogListener = LogEventListener { level, module, file, line, name, header, id, message ->
        if (level == LogLevel.ERROR || level == LogLevel.WARNING) {
            Log.e(
                "vlc-native",
                formatNativeLogMessage(
                    level = level,
                    module = module,
                    file = file,
                    line = line,
                    name = name,
                    header = header,
                    id = id,
                    message = message
                )
            )
        }
    }

    private val playerListener = object : MediaPlayerEventAdapter() {
        /**
         * 播放开始时同步当前歌曲、进度与播放状态。
         */
        override fun playing(mediaPlayer: MediaPlayer?) {
            Log.i("vlc", "播放开始")
            clearIgnoredStoppedEvent()
            submitMediaPlayerTask(mediaPlayer) { player ->
                if (!playWhenReady) {
                    player.controls().setPause(true)
                    updateState(PlayStateEnum.Pause)
                    return@submitMediaPlayerTask
                }
                if (state != PlayStateEnum.Playing) {
                    reportedPlayEvent()
                }
//                downloadCacheController.updateCacheSchedule(1f)
                updateState(PlayStateEnum.Playing)
            }
        }

        /**
         * JVM 缓存进度只由 JvmDownloadCacheController 写入，避免 VLC 网络缓冲值覆盖磁盘缓存进度。
         */
        override fun buffering(mediaPlayer: MediaPlayer?, newCache: Float) {
        }

        /**
         * 播放暂停时上报暂停事件并刷新状态。
         */
        override fun paused(mediaPlayer: MediaPlayer?) {
            clearIgnoredStoppedEvent()
            playWhenReady = false
            if (state == PlayStateEnum.Playing) {
                reportedPauseEvent()
            }
            updateState(PlayStateEnum.Pause)
        }

        /**
         * 播放停止时重置进度和播放状态。
         */
        override fun stopped(mediaPlayer: MediaPlayer?) {
            if (ignoreNextStoppedEvent) {
                clearIgnoredStoppedEvent()
                return
            }
            Log.i("vlc", "stopped")
            playWhenReady = false
            setCurrentPositionData(0L)
            downloadCacheController.updateCacheSchedule(0f)
            updateState(PlayStateEnum.None)
        }

        /**
         * 播放自然结束时回退为空闲状态。
         */
        override fun finished(mediaPlayer: MediaPlayer?) {
            Log.i("vlc", "finished")
            val shouldContinuePlayback = playWhenReady
            musicInfo?.let {
                updateEvent(PlayerEvent.RemovePlaybackProgress(it.itemId))
            }
            setCurrentPositionData(0L)
            submitMediaPlayerTask(mediaPlayer) {
                handlePlaybackFinished(shouldContinuePlayback)
            }
        }


        override fun mediaChanged(
            mediaPlayer: MediaPlayer?,
            media: MediaRef?
        ) {
            Log.i("vlc", "播放变化: $media")
            clearIgnoredStoppedEvent()

            val retainedMedia = media?.newMedia() ?: return
            val player = mediaPlayer ?: run {
                retainedMedia.release()
                return
            }
            submitMediaPlayerTask(mediaPlayer) {
                try {
                    refreshArtworkBytes(retainedMedia)
                    val appliedPendingStartPosition = applyPendingStartPosition()
                    if (!appliedPendingStartPosition) {
                        // 没有显式恢复历史进度时，也主动拉一次底层真实时间，
                        // 避免 VLC 已经跳到某个位置而 UI 还停留在 0。
                        syncCurrentPositionFromPlayer(player)
                    }
                } finally {
                    retainedMedia.release()
                }

            }
        }


        /**
         * 播放异常时回退为空闲状态。
         */
        override fun error(mediaPlayer: MediaPlayer?) {
            val appState = state
            val wasPlayWhenReady = playWhenReady
            val currentMusic = musicInfo
            submitMediaPlayerTask(mediaPlayer) { player ->
                logPlaybackError(player, appState, wasPlayWhenReady, currentMusic)
            }
            if (mediaPlayer == null) {
                logPlaybackError(null, appState, wasPlayWhenReady, currentMusic)
            }
            clearIgnoredStoppedEvent()
            playWhenReady = false
            downloadCacheController.updateCacheSchedule(0f)
            updateState(PlayStateEnum.None)
        }

        /**
         * 优先使用 VLC 主动推送的时间事件更新进度。
         */
        override fun timeChanged(mediaPlayer: MediaPlayer?, newTime: Long) {
            if (newTime >= 0L) {
                setCurrentPositionData(newTime)
            }
        }

        /**
         * VLC 有时会先更新内部 position，再迟一点才分发 timeChanged。
         * 这里补一层基于真实播放器状态的同步，避免听感已跳播但 UI 进度还停在旧值。
         */
        override fun positionChanged(mediaPlayer: MediaPlayer?, newPosition: Float) {
            if (newPosition < 0f) {
                return
            }
            submitMediaPlayerTask(mediaPlayer) { player ->
                syncCurrentPositionFromPlayer(player, newPosition)
            }
        }

        /**
         * 仅依赖 VLC 主动推送的时长事件更新总时长，避免触发底层 timer 断言。
         */
        override fun lengthChanged(mediaPlayer: MediaPlayer?, newLength: Long) {
            updateDurationFromEvent(newLength)
        }
    }

    private val listener: VlcMediaEventAdapter = object : VlcMediaEventAdapter() {
        override fun mediaParsedChanged(media: Media, newStatus: MediaParsedStatus) {
            submitMediaPlayerTask(mediaPlayer) {
                updatePicBytes(null)
                try {
                    if (newStatus == MediaParsedStatus.DONE) {
                        val bytes = readArtworkBytesFromMedia(media)
                        updatePicBytes(bytes)
                    }
                } catch (e: Exception) {
                    Log.e("vlc", "读取专辑图片异常", e)
                }
            }
        }
    }


    /**
     * 是否能操作playMusicList
     */
    override val isPlayMusicListMutable: Boolean
        get() = true

    /**
     * 初始化 JVM 播放器监听器与进度轮询任务。
     */
    override fun initController(onRestorePlaylists: (MusicCommonController.() -> Unit)?) {
        ensureMediaPlayer()
        // JVM 没有 Android MediaServer 的 metadata 回调，这里启动桌面专属歌词监听链路。
        startJvmLyrics(this)
        onRestorePlaylists?.invoke(this)
    }

    /**
     * 将歌曲列表插入当前播放位置之后，并按需立即播放。
     */
    override fun addMusicList(
        musicList: List<XyPlayMusic>,
        artistId: String?,
        isPlayer: Boolean?
    ) {
        if (musicList.isEmpty()) {
            return
        }
        addMusicList(musicList)
        val originIndex = originMusicList.indexOfFirst { it.itemId == musicList.first().itemId }
        ensurePlaylistPrepared(musicList)
        if (isPlayer == true && originIndex != Constants.MINUS_ONE_INT) {
            seekToIndex(originIndex)
        }
        updateEvent(PlayerEvent.AddMusicList(artistId))
    }

    /**
     * 暂停当前 vlcj 播放器。
     */
    override fun pause() {
        val previousState = state
        playWhenReady = false
        if (previousState == PlayStateEnum.Loading || previousState == PlayStateEnum.Playing) {
            reportedPauseEvent()
            updateState(PlayStateEnum.Pause)
        }
        currentMediaPlayer()?.controls()?.setPause(true)
    }

    /**
     * 恢复播放；若当前为空闲状态则重新播放当前索引歌曲。
     */
    override fun resume() {
        Log.i("music", "恢复播放")
        playWhenReady = true
        updateState(PlayStateEnum.Loading)
        val mrl = currentMediaPlayer()?.media()?.info()?.mrl()
        if (mrl.isNullOrBlank()) {
            seekToIndex(curOriginIndex)
        } else
            currentMediaPlayer()?.controls()?.play()
    }

    /**
     * 跳转到指定进度。
     */
    override fun seekTo(millSeconds: Long) {
        val player = currentMediaPlayer() ?: return
        if (millSeconds > 0) {
            player.controls().setTime(millSeconds)
            setCurrentPositionData(millSeconds)
            musicInfo?.let {
                updateEvent(
                    PlayerEvent.PositionSeekTo(
                        millSeconds,
                        it.itemId
                    )
                )
            }
            if (state == PlayStateEnum.Pause) {
                playWhenReady = true
                updateState(PlayStateEnum.Loading)
                player.controls().play()
            }
        }
    }

    /**
     * 播放列表内跳转到下一首歌曲。
     */
    override fun seekToNext() {
        val targetRealIndex = getNextPlayableIndex()
            .takeIf { it != Constants.MINUS_ONE_INT }
            ?: return
        playMusicAtRealIndex(targetRealIndex)
    }

    /**
     * 播放列表内跳转到上一首歌曲。
     */
    override fun seekToPrevious() {
        val targetRealIndex = getPreviousPlayableIndex() ?: return
        playMusicAtRealIndex(targetRealIndex)
    }

    /**
     * 根据业务层列表索引切换歌曲，并直接交给单曲 MediaPlayer 播放。
     */
    override fun seekToIndex(index: Int) {
        val targetRealIndex = realIndexForOriginIndex(index) ?: return
        playMusicAtRealIndex(targetRealIndex)
    }

    /**
     * 根据歌曲 id 查找索引并切换播放。
     */
    override fun seekToItemId(itemId: String) {
        val index = originMusicList.indexOfFirst { it.itemId == itemId }
        if (index != Constants.MINUS_ONE_INT) {
            seekToIndex(index)
        }
    }

    /**
     * 从播放列表移除指定歌曲，并在必要时修正当前索引。
     */
    override fun removeItem(index: Int) {
        if (index !in originMusicList.indices) {
            return
        }
        val removingCurrent = index == curOriginIndex
        val currentRealIndexBeforeRemove = curRealIndex
        val previousState = state
        removeMusic(index)
        when {
            originMusicList.isEmpty() -> {
                clearPlayerList()
            }

            index < curOriginIndex -> {
                updateOriginIndex(curOriginIndex - 1)
            }

            removingCurrent -> {
                stopCurrentPlayback()
                if (currentRealIndexBeforeRemove != Constants.MINUS_ONE_INT) {
                    val targetRealIndex =
                        minOf(currentRealIndexBeforeRemove, playMusicList.lastIndex)
                    if (targetRealIndex in playMusicList.indices) {
                        playMusicAtRealIndex(targetRealIndex)
                        if (previousState == PlayStateEnum.Pause) {
                            pause()
                        }
                    }
                }
            }

            else -> {
            }
        }
    }

    /**
     * 设置当前播放器倍速。
     */
    override fun setDoubleSpeed(value: Float) {
        currentMediaPlayer()?.controls()?.setRate(value)
    }

    /**
     * 设置当前 vlcj 播放器音量。
     */
    override fun setVolume(volume: Int) {
        currentMediaPlayer()?.audio()?.setVolume(volume.coerceIn(0, 100))
    }

    /**
     * 将歌曲插入到“下一首播放”位置。
     */
    override fun addNextPlayer(music: XyPlayMusic) {
        playbackSourceOf(music)
        if (originMusicList.isEmpty()) {
            addMusic(music)
            playMusicAtRealIndex(0)
            return
        }

        val existingOriginIndex = originMusicList.indexOfFirst { it.itemId == music.itemId }
        if (existingOriginIndex != Constants.MINUS_ONE_INT &&
            existingOriginIndex != curOriginIndex &&
            existingOriginIndex != curOriginIndex + 1
        ) {
            removeMusic(existingOriginIndex)
            if (curOriginIndex != Constants.MINUS_ONE_INT && originMusicList.isNotEmpty()) {
                val adjustedOriginIndex = if (existingOriginIndex < curOriginIndex) {
                    curOriginIndex - 1
                } else {
                    curOriginIndex
                }
                updateOriginIndex(adjustedOriginIndex)
            }
        }
        insertMusic(music)
        updateEvent(PlayerEvent.AddMusicList(music.artistIds?.firstOrNull()))
    }

    /**
     * 刷新当前远程歌曲的播放地址。
     * 转码策略或网络环境变化后，需要把业务层维护的可播地址重新解析一遍。
     */
    override fun replacePlaylistItemUrl(updateMusicUrlFun: suspend (XyPlayMusic) -> XyPlayMusic) {
        val snapshot = playMusicList.toList()
        if (snapshot.isEmpty()) {
            return
        }

        val currentIndex = curOriginIndex
        val currentState = state

        snapshot.forEach { playbackSourceOf(it) }

        if ((currentState == PlayStateEnum.Playing ||
                    currentState == PlayStateEnum.Loading ||
                    currentState == PlayStateEnum.Pause) &&
            currentIndex in snapshot.indices
        ) {
            if (currentState == PlayStateEnum.Playing) {
                seekToIndex(currentIndex)
//                pause()
            }
        }
    }

    /**
     * 初始化播放列表、恢复索引与进度，并按参数决定是否立即播放。
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
        super.initMusicList(
            musicDataList,
            musicCurrentPositionMapData,
            originIndex,
            pageNum,
            pageSize,
            artistId,
            ifInitPlayerList,
            musicPlayTypeEnum
        )
        stopCurrentPlayback()

        val targetIndex = originIndex ?: 0
        updateOriginIndex(targetIndex)
        updateDuration(musicDataList[targetIndex].runTimeTicks)
        updateEvent(PlayerEvent.AddMusicList(artistId, ifInitPlayerList))

        // 远程地址需要先解析为最终可播地址；准备期间如果用户又切了别的歌，
//        ensurePlaylistPrepared(playMusicList)
        if (ifInitPlayerList) {
            updateState(PlayStateEnum.Pause)
            setCurrentPositionData(
                musicCurrentPositionMapData?.get(musicDataList[targetIndex].itemId) ?: 0L
            )
            return
        }

        seekToIndex(targetIndex)
    }

    /**
     * JVM 端当前不维护封面元数据刷新逻辑。
     */
    override fun refreshPlaylistCoverMetadata() {
    }

    /**
     * 生成当前播放模式下的歌曲列表
     */
    override fun updatePlayerMode() {
    }

    /**
     * 清空播放列表并停止当前本地/远程播放会话。
     */
    override fun clearPlayerList() {
        stopCurrentPlayback()
        super.clearPlayerList()
    }

    /**
     * 关闭控制器并释放播放器与协程资源。
     */
    override fun close() {
        stopCurrentPlayback()
        val eventApi = currentMediaPlayer()?.takeIf { mediaPlayerListenerRegistered }?.events()
        eventApi?.removeMediaPlayerEventListener(playerListener)
        eventApi?.removeMediaEventListener(listener)
        mediaPlayerListenerRegistered = false
        nativeLog?.removeLogListener(nativeLogListener)
        nativeLog?.release()
        nativeLog = null
        mediaPlayer?.release()
        mediaPlayer = null
        mediaPlayerFactory?.release()
        mediaPlayerFactory = null
        super.close()
    }

    /**
     * 为远程歌曲解析最终播放地址，并把结果回写到业务对象上。
     * 这样后续无论是列表重建还是当前歌曲地址刷新，都能复用同一套代理地址。
     */
    private fun resolveRemotePlaybackUrl(music: XyPlayMusic): String {
        return JvmReverseProxyServer.wrapTargetUrl(music.musicUrl)
    }

    private fun resolveCurrentRemotePlaybackUrl(music: XyPlayMusic): String {
        val cachePlaybackUrl = (downloadCacheController as JvmDownloadCacheController)
            .preparePlaybackUrl(music)
        return cachePlaybackUrl ?: resolveRemotePlaybackUrl(music)
    }

    /**
     * 格式化 libVLC 原生层日志，保留模块和源码位置，方便追具体解码/访问错误。
     */
    private fun formatNativeLogMessage(
        level: LogLevel,
        module: String?,
        file: String?,
        line: Int?,
        name: String?,
        header: String?,
        id: Int?,
        message: String?
    ): String {
        val context = listOfNotNull(
            module?.takeIf { it.isNotBlank() }?.let { "module=$it" },
            name?.takeIf { it.isNotBlank() }?.let { "name=$it" },
            header?.takeIf { it.isNotBlank() }?.let { "header=$it" },
            id?.let { "id=$it" },
            file?.takeIf { it.isNotBlank() }?.let { sourceFile ->
                if (line != null) {
                    "source=$sourceFile:$line"
                } else {
                    "source=$sourceFile"
                }
            }
        ).joinToString(", ")

        val prefix = if (context.isBlank()) {
            "[$level]"
        } else {
            "[$level] $context"
        }
        return "$prefix ${message.orEmpty()}"
    }

    /**
     * VLC 的 error 回调没有 Throwable，这里主动抓取播放器与当前媒体状态。
     */
    private fun logPlaybackError(
        player: MediaPlayer?,
        appState: PlayStateEnum,
        wasPlayWhenReady: Boolean,
        currentMusic: XyPlayMusic?
    ) {
        val playerState = safeVlcValue { player?.status()?.state() }
        val playerTime = safeVlcValue { player?.status()?.time() }
        val playerPosition = safeVlcValue { player?.status()?.position() }
        val playerLength = safeVlcValue { player?.status()?.length() }
        val playerPlayable = safeVlcValue { player?.status()?.isPlayable }
        val mediaValid = safeVlcValue { player?.media()?.isValid }
        val mediaMrl = safeVlcValue { player?.media()?.info()?.mrl() }
        val mediaType = safeVlcValue { player?.media()?.info()?.type() }
        val mediaState = safeVlcValue { player?.media()?.info()?.state() }
        val mediaDuration = safeVlcValue { player?.media()?.info()?.duration() }
        val mediaParsedStatus = safeVlcValue { player?.media()?.parsing()?.status() }
        val mediaStatistics = safeVlcValue { player?.media()?.info()?.statistics() }

        Log.e(
            "vlc",
            buildString {
                appendLine("播放异常")
                appendLine("appState=$appState, playWhenReady=$wasPlayWhenReady")
                appendLine(
                    "playerState=$playerState, playable=$playerPlayable, " +
                            "time=$playerTime, position=$playerPosition, length=$playerLength"
                )
                appendLine(
                    "mediaValid=$mediaValid, mediaState=$mediaState, mediaType=$mediaType, " +
                            "duration=$mediaDuration, parsed=$mediaParsedStatus"
                )
                appendLine("mrl=$mediaMrl")
                appendLine("statistics=$mediaStatistics")
                append("music=")
                append(currentMusic?.let(::formatMusicForLog) ?: "null")
            }
        )
    }

    private fun formatMusicForLog(music: XyPlayMusic): String {
        return buildString {
            append("id=${music.itemId}, ")
            append("name=${music.name}, ")
            append("filePath=${music.filePath}, ")
            append("musicUrl=${music.musicUrl}, ")
            append("playerUrl=${music.getPlayerUrl()}, ")
            append("container=${music.container}, ")
            append("ifHls=${music.ifHls}, ")
            append("static=${music.static}, ")
            append("duration=${music.runTimeTicks}")
        }
    }

    private inline fun <T> safeVlcValue(block: () -> T): String {
        return runCatching {
            block()?.toString() ?: "null"
        }.getOrElse { error ->
            "读取失败: ${error.message ?: error::class.simpleName}"
        }
    }

    /**
     * 停止当前 VLC 播放流程。
     * 直接用户停播时不屏蔽 stopped 事件，确保状态机仍然回到空闲态。
     */
    private fun stopCurrentPlayback() {
        clearIgnoredStoppedEvent()
        playWhenReady = false
        downloadCacheController.updateCacheSchedule(0f)
        runCatching { currentMediaPlayer()?.controls()?.stop() }
    }

    /**
     * 把需要访问 libVLC 的操作切到 VLC 自己的任务队列里执行，
     * 避免直接在原生回调线程里反调 libVLC 造成不稳定行为。
     */
    private fun submitMediaPlayerTask(mediaPlayer: MediaPlayer?, task: (MediaPlayer) -> Unit) {
        mediaPlayer?.let { player ->
            player.submit {
                task(player)
            }
        }
    }

    /**
     * 尝试从当前媒体的元数据里提取封面图。
     * 如果 ARTWORK_URL 还没准备好，则异步触发一次 parse，待 VLC 回填元数据后再读取。
     */
    private fun refreshArtworkBytes(media: Media) {
        updatePicBytes(null)
        readArtworkBytesFromMedia(media)?.let { bytes ->
            updatePicBytes(bytes)
            return
        }

        parseArtworkBytesAsync(media)
    }

    /**
     * 从 VLC 元数据里的 ARTWORK_URL 读取封面图内容。
     */
    private fun readArtworkBytesFromMedia(media: Media): ByteArray? {
        val artworkUrl = media.meta().get(Meta.ARTWORK_URL)
            ?.takeIf { it.isNotBlank() }
            ?: return null
        return readArtworkBytesFromLocation(artworkUrl)
    }

    /**
     * 解析 ARTWORK_URL，兼容 file/http/data URI，并统一转成字节数组。
     */
    private fun readArtworkBytesFromLocation(location: String): ByteArray? {
        val trimmedLocation = location.trim()
        if (trimmedLocation.isEmpty()) {
            return null
        }

        if (trimmedLocation.startsWith("data:", ignoreCase = true)) {
            return decodeDataUri(trimmedLocation)
        }

        val parsedUri = runCatching { URI(trimmedLocation) }.getOrNull()
        return when (parsedUri?.scheme?.lowercase()) {
            "file" -> runCatching {
                Files.readAllBytes(Paths.get(parsedUri))
            }.onFailure {
                Log.e("vlc", "读取本地封面失败: $trimmedLocation", it)
            }.getOrNull()

            "http", "https" -> runCatching {
                parsedUri.toURL().openStream().use { it.readBytes() }
            }.onFailure {
                Log.e("vlc", "下载封面失败: $trimmedLocation", it)
            }.getOrNull()

            else -> runCatching {
                Files.readAllBytes(Paths.get(trimmedLocation))
            }.onFailure {
                Log.e("vlc", "读取封面路径失败: $trimmedLocation", it)
            }.getOrNull()
        }
    }

    /**
     * 处理 data URI 形式的图片元数据。
     */
    private fun decodeDataUri(dataUri: String): ByteArray? {
        val commaIndex = dataUri.indexOf(',')
        if (commaIndex <= 0 || commaIndex >= dataUri.lastIndex) {
            return null
        }

        val metadata = dataUri.substring(0, commaIndex)
        val payload = dataUri.substring(commaIndex + 1)

        return if (metadata.contains(";base64", ignoreCase = true)) {
            runCatching { Base64.getDecoder().decode(payload) }.onFailure {
                Log.e("vlc", "解析 base64 封面失败", it)
            }.getOrNull()
        } else {
            payload.toByteArray()
        }
    }

    /**
     * 触发一次异步 parse，等待 VLC 回填元数据后再取封面。
     */
    private fun parseArtworkBytesAsync(media: Media) {
        runCatching {
            media.parsing().parse(
                5_0000,
                ParseFlag.PARSE_LOCAL,
//                ParseFlag.PARSE_NETWORK,
                ParseFlag.FETCH_LOCAL,
//                ParseFlag.FETCH_NETWORK
            )
        }.onFailure {
            Log.e("vlc", "触发封面元数据解析失败", it)
        }.getOrDefault(false)
    }

    /**
     * 优先使用 VLC 的时长事件更新总时长；
     * 如果事件值无效，则退回使用业务层歌曲本身记录的时长。
     */
    private fun updateDurationFromEvent(newLength: Long) {
        if (newLength > 0L) {
            updateDuration(newLength)
        } else {
            updateDuration(musicInfo?.runTimeTicks ?: 0L)
        }
    }

    /**
     * 预先解析业务层播放列表里的地址，确保真正切歌时已经拿到最终可播 mrl。
     */
    private fun ensurePlaylistPrepared(
        musicList: List<XyPlayMusic>
    ): Boolean {
        musicList.forEach { music ->
            playbackSourceOf(music)
        }
        return true
    }

    /**
     * 获取歌曲真正交给 VLC 播放的地址。
     */
    private fun playbackSourceOf(music: XyPlayMusic): String {
        val localPath = music.filePath
        val playerUrl = if (!localPath.isNullOrBlank()) {
            Paths.get(localPath).toUri().toASCIIString()
        } else {
            resolveCurrentRemotePlaybackUrl(music)
        }
        music.setPlayerUrl(playerUrl)
        return playerUrl
    }

    /**
     * 延迟初始化 VLC 播放器体系。
     * 这里仅创建真正承载单曲播放的 MediaPlayer。
     */
    private fun ensureMediaPlayer(): MediaPlayer? {
        mediaPlayer?.let { return it }

        if (!VlcBootstrap.ensureConfigured()) {
            return null
        }

        val factory = runCatching {
            MediaPlayerFactory(*VLC_FACTORY_ARGUMENTS)
        }.onFailure {
            Log.e("vlc", "创建 VLC 播放器工厂失败", it)
        }.getOrNull() ?: return null

        val createdNativeLog = runCatching {
            factory.application().newLog().apply {
                level = LogLevel.WARNING
                addLogListener(nativeLogListener)
            }
        }.onFailure {
            Log.e("vlc", "启用 VLC 原生日志失败", it)
        }.getOrNull()

        val createdPlayer = runCatching {
            factory.mediaPlayers().newMediaPlayer()
        }.onFailure {
            createdNativeLog?.removeLogListener(nativeLogListener)
            createdNativeLog?.release()
            runCatching { factory.release() }
            Log.e("vlc", "创建 VLC 音频播放器失败", it)
        }.getOrNull() ?: return null

        createdPlayer.events().addMediaPlayerEventListener(playerListener)
        createdPlayer.events().addMediaEventListener(listener)
        createdPlayer.audio().setVolume(60)
        mediaPlayerListenerRegistered = true
        nativeLog = createdNativeLog
        mediaPlayerFactory = factory
        mediaPlayer = createdPlayer
        return createdPlayer
    }

    /**
     * 优先使用持久化的歌曲历史进度；若当前歌曲尚未来得及写回 map，则退回到界面中的当前进度。
     */
    private fun restoredPositionForCurrentMusic(): Long {
        val currentMusicId = musicInfo?.itemId ?: return 0L
        return musicCurrentPositionMap[currentMusicId]
            ?: 0
    }

    /**
     * 等 VLC 真正进入播放态后再执行跳转，避免在媒体尚未装载完成时设置时间无效。
     */
    private fun applyPendingStartPosition(): Boolean {
        val startPositionMs = restoredPositionForCurrentMusic().takeIf { it > 0L } ?: return false
        seekTo(startPositionMs)
        return true
    }

    /**
     * 从 VLC 当前真实播放位置回写 UI 进度。
     * 优先使用毫秒级 time()；如果 time() 还不可用，再退回用 position 百分比估算。
     */
    private fun syncCurrentPositionFromPlayer(
        mediaPlayer: MediaPlayer,
        newPosition: Float? = null
    ) {
        // 优先相信 libVLC 返回的真实毫秒数；这是和实际听感最一致的来源。
        val actualTime = mediaPlayer.status().time()
        if (actualTime >= 0L) {
            if (actualTime != progressStateFlow.value) {
                setCurrentPositionData(actualTime)
            }
            return
        }

        // 某些时序下 time() 还没准备好，但 positionChanged 已经到了。
        // 这时退回用百分比和总时长估算一次，先把 UI 纠正到接近真实位置。
        val estimatedTime = newPosition
            ?.takeIf { it in 0f..1f }
            ?.let { position -> (duration * position).toLong() }
            ?.coerceAtLeast(0L)
            ?: return

        if (estimatedTime != progressStateFlow.value) {
            setCurrentPositionData(estimatedTime)
        }
    }

    /**
     * 业务层原始列表索引映射到当前实际播放顺序索引。
     */
    private fun realIndexForOriginIndex(originIndex: Int): Int? {
        if (originIndex !in originMusicList.indices) {
            return null
        }
        val targetMusicId = originMusicList[originIndex].itemId
        val realIndex = playMusicList.indexOfFirst { it.itemId == targetMusicId }
        return realIndex.takeIf { it in playMusicList.indices }
    }

    /**
     * 使用当前有效播放顺序的目标索引直接装载并播放单个媒体。
     */
    private fun playMusicAtRealIndex(realIndex: Int) {
        val player = ensureMediaPlayer() ?: return
        val music = playMusicList.getOrNull(realIndex) ?: return
        val mediaSource = playbackSourceOf(music)
        Log.i("=====", "音乐播放链接$mediaSource")
        playWhenReady = true
        updateState(PlayStateEnum.Loading)
        setCurrentPositionData(0L)
        downloadCacheController.updateCacheSchedule(0f)
        updateEvent(PlayerEvent.BeforeChangeMusic)
        updateRealIndex(realIndex)

        ignoreStoppedEventOnce()
        val played = runCatching {
            player.media().play(mediaSource,
                ":network-caching=$VLC_NETWORK_CACHING_MS",
                ":file-caching=$VLC_FILE_CACHING_MS",
                ":http-reconnect")
        }.onFailure {
            clearIgnoredStoppedEvent()
            Log.e("vlc", "直接播放媒体失败: $mediaSource", it)
        }.getOrDefault(false)

        if (!played) {
            clearIgnoredStoppedEvent()
            Log.e(
                "vlc",
                buildString {
                    appendLine("VLC 拒绝提交播放任务")
                    appendLine("mediaSource=$mediaSource")
                    append("music=")
                    append(formatMusicForLog(music))
                }
            )
            updateState(PlayStateEnum.None)
        }
    }

    /**
     * 标记下一次 stopped 回调为切歌过程中的中间事件。
     */
    private fun ignoreStoppedEventOnce() {
        ignoreNextStoppedEvent = true
    }

    /**
     * 清理 stopped 忽略标记，恢复正常停止事件处理。
     */
    private fun clearIgnoredStoppedEvent() {
        ignoreNextStoppedEvent = false
    }

    /**
     * 播放自然结束后，完全交给应用层的播放顺序与模式来决定后续行为。
     */
    private fun handlePlaybackFinished(shouldContinuePlayback: Boolean) {
        if (!shouldContinuePlayback) {
            return
        }
        val targetRealIndex = when (playMode) {
            PlayerModeEnum.SINGLE_LOOP -> curRealIndex.takeIf { it in playMusicList.indices }
            PlayerModeEnum.SEQUENTIAL_PLAYBACK, PlayerModeEnum.RANDOM_PLAY -> getNextPlayableIndex().takeIf {
                it != Constants.MINUS_ONE_INT
            }
        } ?: return
        playMusicAtRealIndex(targetRealIndex)
    }

    /**
     * 获取底层单曲播放器实例。
     */
    private fun currentMediaPlayer(): MediaPlayer? {
        return mediaPlayer
    }

    companion object {
        /**
         * 使用隔离的 libVLC 参数，避免读取用户本机 VLC 的历史配置。
         * 其中 `--ignore-config` 用来屏蔽续播等偏好设置，
         * `--no-media-library` 则避免媒体库相关的额外状态介入当前播放器实例。
         */
        private val VLC_FACTORY_ARGUMENTS = arrayOf(
            "--intf=dummy",
            "--ignore-config",
            "--no-media-library"
        )

        /**
         * VLC 网络媒体预缓冲时长，保留为缓存播放调优参数。
         */
        private const val VLC_NETWORK_CACHING_MS = 1_000
        /**
         * VLC 本地文件预缓冲时长，保留为缓存播放调优参数。
         */
        private const val VLC_FILE_CACHING_MS = 500
    }
}
