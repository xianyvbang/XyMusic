package cn.xybbz.music

import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.Constants.MUSIC_POSITION_UPDATE_INTERVAL
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.PlayerEvent
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import uk.co.caprica.vlcj.media.callback.DefaultCallbackMedia
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent
import java.io.File

class JvmMusicController : MusicCommonController() {

    private var mediaPlayerComponent: AudioPlayerComponent? = null
    private var mediaPlayerListenerRegistered = false

    private var currentRemoteSession: RemotePlaybackSession? = null
    private var playbackJob: Job? = null
    private var progressJob: Job? = null
    private var playRequestVersion = 0L
    private var ignoreNextStoppedEvent = false
    // 标记当前 VLC 是否已经装载了可继续播放/暂停恢复的媒体。
    private var hasPreparedPlayback = false
    // 记录下一次真正开始播放时需要跳转到的恢复进度。
    private var pendingStartPositionMs: Long? = null

    private val playerListener = object : MediaPlayerEventAdapter() {
        /**
         * 播放开始时同步时长并更新播放状态。
         */
        override fun playing(mediaPlayer: MediaPlayer?) {
            Log.i("vlc", "播放开始")
            submitMediaPlayerTask(mediaPlayer) { player ->
                applyPendingStartPosition(player)
                syncDurationFromPlayer(player)
                if (state != PlayStateEnum.Playing) {
                    reportedPlayEvent()
                }
                updateState(PlayStateEnum.Playing)
            }
        }

        /**
         * 播放暂停时上报暂停事件并刷新状态。
         */
        override fun paused(mediaPlayer: MediaPlayer?) {
            submitMediaPlayerTask(mediaPlayer) { player ->
                if (state == PlayStateEnum.Playing) {
                    reportedPauseEvent()
                }
                syncDurationFromPlayer(player)
                updateState(PlayStateEnum.Pause)
            }
        }

        /**
         * 播放停止时重置进度和播放状态。
         */
        override fun stopped(mediaPlayer: MediaPlayer?) {
            hasPreparedPlayback = false
            pendingStartPositionMs = null
            if (ignoreNextStoppedEvent) {
                ignoreNextStoppedEvent = false
                return
            }
            setCurrentPositionData(0L)
            updateState(PlayStateEnum.None)
        }

        /**
         * 播放自然结束时释放远程会话并重置状态。
         */
        override fun finished(mediaPlayer: MediaPlayer?) {
            hasPreparedPlayback = false
            pendingStartPositionMs = null
            closeRemoteSession()
            setCurrentPositionData(0L)
            updateState(PlayStateEnum.None)
        }

        /**
         * 播放异常时关闭当前远程流并回退为空闲状态。
         */
        override fun error(mediaPlayer: MediaPlayer?) {
            Log.i("vlc", "播放异常111111111111111111111111111111111111111111111111111")
            hasPreparedPlayback = false
            pendingStartPositionMs = null
            closeRemoteSession()
            updateState(PlayStateEnum.None)
        }
    }

    /**
     * 初始化 JVM 播放器监听器与进度轮询任务。
     */
    override fun initController(onRestorePlaylists: (() -> Unit)?) {
        startProgressObserver()
        onRestorePlaylists?.invoke()
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

        val insertIndex = if (originMusicList.isNotEmpty() && curOriginIndex != Constants.MINUS_ONE_INT) {
            curOriginIndex + 1
        } else {
            originMusicList.size
        }

        val updatedList = originMusicList.toMutableList().apply {
            addAll(insertIndex, musicList)
        }
        updateOriginMusicList(updatedList)

        if (isPlayer == true) {
            seekToIndex(insertIndex)
        }

        updateEvent(PlayerEvent.AddMusicList(artistId))
    }

    /**
     * 更新当前歌曲的收藏状态，供界面即时刷新。
     */
    override fun updateCurrentFavorite(isFavorite: Boolean) {
        updateCurrentMusic(musicInfo?.copy(ifFavoriteStatus = isFavorite))
    }

    /**
     * 清空播放列表并停止当前本地/远程播放会话。
     */
    override fun clearPlayerList() {
        playbackJob?.cancel()
        playbackJob = null
        stopCurrentPlayback()
        pendingStartPositionMs = null
        super.clearPlayerList()
    }

    /**
     * 暂停当前 vlcj 播放器。
     */
    override fun pause() {
        currentMediaPlayer()?.controls()?.pause()
    }

    /**
     * 恢复播放；若当前为空闲状态则重新播放当前索引歌曲。
     */
    override fun resume() {
        if (state == PlayStateEnum.Pause || state == PlayStateEnum.Loading) {
            if (hasPreparedPlayback) {
                ensureMediaPlayer()?.controls()?.play()
            } else if (curOriginIndex in originMusicList.indices) {
                // 恢复列表场景下仅同步了上层状态，此时需要重新装载当前歌曲后再播放。
                startPlaybackAtIndex(
                    index = curOriginIndex,
                    startPositionMs = restoredPositionForCurrentMusic()
                )
            }
            return
        }

        if (state == PlayStateEnum.None && curOriginIndex in originMusicList.indices) {
            seekToIndex(curOriginIndex)
        }
    }

    /**
     * 跳转到指定进度；远程流式播放本轮暂不支持拖动。
     */
    override fun seekTo(millSeconds: Long) {
        if (currentRemoteSession != null) {
            return
        }

        val mediaPlayer = ensureMediaPlayer() ?: return
        if (mediaPlayer.controls().setTime(millSeconds)) {
            setCurrentPositionData(millSeconds)
            musicInfo?.let {
                updateEvent(
                    PlayerEvent.PositionSeekTo(
                        millSeconds,
                        it.itemId,
                        settingsManager.get().playSessionId
                    )
                )
            }
            if (state == PlayStateEnum.Pause) {
                mediaPlayer.controls().play()
            }
        }
    }

    /**
     * 播放列表内跳转到下一首歌曲。
     */
    override fun seekToNext() {
        val nextIndex = curOriginIndex + 1
        if (nextIndex in originMusicList.indices) {
            seekToIndex(nextIndex)
        }
    }

    /**
     * 播放列表内跳转到上一首歌曲。
     */
    override fun seekToPrevious() {
        val previousIndex = curOriginIndex - 1
        if (previousIndex in originMusicList.indices) {
            seekToIndex(previousIndex)
        }
    }

    /**
     * 根据列表索引切换歌曲，并区分本地文件与远程流式播放。
     */
    override fun seekToIndex(index: Int) {
        if (index !in originMusicList.indices) {
            return
        }

        startPlaybackAtIndex(index)
    }

    /**
     * 装载并播放指定索引歌曲；如提供恢复进度，则在真正开始播放后跳转到对应位置。
     */
    private fun startPlaybackAtIndex(index: Int, startPositionMs: Long = 0L) {
        if (index !in originMusicList.indices) {
            return
        }

        val music = originMusicList[index]
        val previousMusicId = musicInfo?.itemId

        playbackJob?.cancel()
        playbackJob = null
        playRequestVersion += 1
        val requestVersion = playRequestVersion

        updateState(PlayStateEnum.Loading)
        setCurrentPositionData(0L)
        updateOriginIndex(index)
        updateCurrentMusic(music)
        updateDuration(music.runTimeTicks)
        pendingStartPositionMs = startPositionMs.takeIf { it > 0L }

        if (previousMusicId != null && previousMusicId != music.itemId) {
            updateEvent(PlayerEvent.BeforeChangeMusic)
        }
        updateEvent(
            PlayerEvent.ChangeMusic(
                music.itemId,
                music.artistIds?.firstOrNull(),
                music.artists?.firstOrNull()
            )
        )

        val mediaPlayer = ensureMediaPlayer()
        if (mediaPlayer == null) {
            hasPreparedPlayback = false
            pendingStartPositionMs = null
            updateState(PlayStateEnum.None)
            return
        }

        val localPath = music.filePath
        if (!localPath.isNullOrBlank()) {
            stopCurrentPlayback(clearPendingStartPosition = false)
            val mediaPath = File(localPath).toURI().toString()
            val started = mediaPlayer.media().play(mediaPath)
            hasPreparedPlayback = started
            if (!started) {
                pendingStartPositionMs = null
                updateState(PlayStateEnum.None)
            }
            return
        }

        playbackJob = scope.launch(Dispatchers.IO) {
            val session = runCatching { createRemoteSession(music) }.getOrNull()
            if (session == null || requestVersion != playRequestVersion) {
                session?.close()
                pendingStartPositionMs = null
                if (requestVersion == playRequestVersion) {
                    updateState(PlayStateEnum.None)
                }
                return@launch
            }

            stopCurrentPlayback(clearPendingStartPosition = false)
            currentRemoteSession = session
            val started = mediaPlayer.media().play(session.callbackMedia)
            hasPreparedPlayback = started
            if (!started) {
                pendingStartPositionMs = null
                closeRemoteSession()
                updateState(PlayStateEnum.None)
            }
        }
    }

    /**
     * 根据歌曲 id 查找索引并切换播放。
     */
    override fun seekToIndex(itemId: String) {
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

        val updatedList = originMusicList.toMutableList().apply {
            removeAt(index)
        }
        updateOriginMusicList(updatedList)

        when {
            updatedList.isEmpty() -> clearPlayerList()
            index < curOriginIndex -> updateOriginIndex(curOriginIndex - 1)
            index == curOriginIndex -> {
                stopCurrentPlayback()
                val nextIndex = index.coerceAtMost(updatedList.lastIndex)
                updateOriginIndex(nextIndex)
                updateCurrentMusic(updatedList.getOrNull(nextIndex))
            }
        }
    }

    /**
     * 设置当前播放器倍速。
     */
    override fun setDoubleSpeed(value: Float) {
        ensureMediaPlayer()?.controls()?.setRate(value)
    }

    /**
     * 更新当前播放模式并向外发送模式变化事件。
     */
    override fun setPlayTypeData(playerTypeEnum: PlayerTypeEnum) {
        playType = playerTypeEnum
        updateEvent(PlayerEvent.PlayerTypeChange(playerTypeEnum))
    }

    /**
     * 将歌曲插入到“下一首播放”位置。
     */
    override fun addNextPlayer(music: XyPlayMusic) {
        val insertIndex = when {
            originMusicList.isEmpty() -> 0
            curOriginIndex == Constants.MINUS_ONE_INT -> originMusicList.size
            else -> curOriginIndex + 1
        }

        val updatedList = originMusicList.toMutableList()
        val existingIndex = updatedList.indexOfFirst { it.itemId == music.itemId }
        if (existingIndex != Constants.MINUS_ONE_INT) {
            updatedList.removeAt(existingIndex)
        }
        updatedList.add(insertIndex.coerceAtMost(updatedList.size), music)
        updateOriginMusicList(updatedList)
        updateEvent(PlayerEvent.AddMusicList(music.artistIds?.firstOrNull()))
    }

    /**
     * 获取当前列表中可播放的下一首索引。
     */
    override fun getNextPlayableIndex(): Int? {
        val nextIndex = curOriginIndex + 1
        return nextIndex.takeIf { it in originMusicList.indices }
    }

    /**
     * 获取当前列表中可播放的上一首索引。
     */
    override fun getPreviousPlayableIndex(): Int? {
        val previousIndex = curOriginIndex - 1
        return previousIndex.takeIf { it in originMusicList.indices }
    }

    /**
     * 刷新当前远程歌曲的播放地址。
     */
    override fun replacePlaylistItemUrl() {
        musicInfo?.takeIf { it.filePath.isNullOrBlank() }?.let { currentMusic ->
            val refreshedUrl = getMusicUrl(currentMusic.itemId, currentMusic.plexPlayKey).musicUrl
            currentMusic.setMusicUrl(refreshedUrl)
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
        playDataType = musicPlayTypeEnum
        updateRestartCount()

        playbackJob?.cancel()
        playbackJob = null
        stopCurrentPlayback()

        if (!musicCurrentPositionMapData.isNullOrEmpty()) {
            musicCurrentPositionMap.clear()
            musicCurrentPositionMap.putAll(musicCurrentPositionMapData)
        }

        updateOriginMusicList(musicDataList.toList())
        setPageNumData(pageNum)
        updatePageSize(pageSize)

        if (musicDataList.isEmpty()) {
            updateOriginIndex(Constants.MINUS_ONE_INT)
            updateCurrentMusic(null)
            updateState(PlayStateEnum.None)
            updateEvent(PlayerEvent.AddMusicList(artistId, ifInitPlayerList))
            return
        }

        val targetIndex = (originIndex ?: 0).coerceIn(0, musicDataList.lastIndex)
        updateOriginIndex(targetIndex)
        updateCurrentMusic(musicDataList[targetIndex])
        updateDuration(musicDataList[targetIndex].runTimeTicks)
        updateEvent(PlayerEvent.AddMusicList(artistId, ifInitPlayerList))

        if (ifInitPlayerList) {
            hasPreparedPlayback = false
            pendingStartPositionMs = null
            updateState(PlayStateEnum.Pause)
            setCurrentPositionData(musicCurrentPositionMapData?.get(musicDataList[targetIndex].itemId) ?: 0L)
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
     * 关闭控制器并释放播放器、协程与远程流资源。
     */
    override fun close() {
        playbackJob?.cancel()
        playbackJob = null
        stopCurrentPlayback()
        pendingStartPositionMs = null
        currentMediaPlayer()?.takeIf { mediaPlayerListenerRegistered }?.events()
            ?.removeMediaPlayerEventListener(playerListener)
        mediaPlayerListenerRegistered = false
        mediaPlayerComponent?.release()
        mediaPlayerComponent = null
        super.close()
    }

    private suspend fun createRemoteSession(music: XyPlayMusic): RemotePlaybackSession {
        val url = getMusicUrl(music.itemId, music.plexPlayKey).musicUrl
        music.setMusicUrl(url)
        val response = dataSourceManager.getHttpClient().prepareGet(url).execute()
        return RemotePlaybackSession(
            response = response,
            channel = response.bodyAsChannel()
        )
    }

    private fun stopCurrentPlayback(clearPendingStartPosition: Boolean = true) {
        // 只有当前播放器确实处于已装载状态时，才需要忽略 stop() 主动触发的 stopped 回调。
        val shouldIgnoreStoppedEvent = hasPreparedPlayback
        hasPreparedPlayback = false
        if (clearPendingStartPosition) {
            pendingStartPositionMs = null
        }
        ignoreNextStoppedEvent = shouldIgnoreStoppedEvent
        playbackJob?.cancel()
        playbackJob = null
        runCatching { currentMediaPlayer()?.controls()?.stop() }
        closeRemoteSession()
    }

    private fun closeRemoteSession() {
        currentRemoteSession?.close()
        currentRemoteSession = null
    }

    private fun submitMediaPlayerTask(mediaPlayer: MediaPlayer?, task: (MediaPlayer) -> Unit) {
        mediaPlayer?.let { player ->
            player.submit {
                task(player)
            }
        }
    }

    private fun syncDurationFromPlayer() {
        val duration = currentMediaPlayer()?.status()?.length() ?: 0L
        if (duration > 0) {
            updateDuration(duration)
        } else {
            updateDuration(musicInfo?.runTimeTicks ?: 0L)
        }
    }

    private fun syncDurationFromPlayer(mediaPlayer: MediaPlayer?) {
        val duration = mediaPlayer?.status()?.length() ?: 0L
        if (duration > 0) {
            updateDuration(duration)
        } else {
            updateDuration(musicInfo?.runTimeTicks ?: 0L)
        }
    }

    private fun startProgressObserver() {
        if (progressJob?.isActive == true) {
            return
        }

        progressJob = scope.launch {
            while (isActive) {
                if (hasPreparedPlayback && (state == PlayStateEnum.Playing || state == PlayStateEnum.Pause)) {
                    val currentPosition = currentMediaPlayer()?.status()?.time()?.coerceAtLeast(0L) ?: 0L
                    setCurrentPositionData(currentPosition)
                    syncDurationFromPlayer()
                }
                delay(MUSIC_POSITION_UPDATE_INTERVAL.coerceAtLeast(100L))
            }
        }
    }

    private fun ensureMediaPlayer(): MediaPlayer? {
        return ensureMediaPlayerComponent()?.mediaPlayer()
    }

    /**
     * 优先使用持久化的歌曲历史进度；若当前歌曲尚未来得及写回 map，则退回到界面中的当前进度。
     */
    private fun restoredPositionForCurrentMusic(): Long {
        val currentMusicId = musicInfo?.itemId ?: return 0L
        return musicCurrentPositionMap[currentMusicId]
            ?: progressStateFlow.value
    }

    /**
     * 等 VLC 真正进入播放态后再执行跳转，避免在媒体尚未装载完成时设置时间无效。
     */
    private fun applyPendingStartPosition(mediaPlayer: MediaPlayer?) {
        val startPositionMs = pendingStartPositionMs ?: return
        pendingStartPositionMs = null

        if (currentRemoteSession != null) {
            return
        }

        if (mediaPlayer?.controls()?.setTime(startPositionMs) == true) {
            setCurrentPositionData(startPositionMs)
        }
    }

    private fun currentMediaPlayer(): MediaPlayer? {
        return mediaPlayerComponent?.mediaPlayer()
    }

    private fun ensureMediaPlayerComponent(): AudioPlayerComponent? {
        mediaPlayerComponent?.let { return it }

        if (!VlcBootstrap.ensureConfigured()) {
            return null
        }

        val component = runCatching {
            AudioPlayerComponent()
        }.onFailure {
            Log.e("vlc", "创建 VLC 播放器失败", it)
        }.getOrNull() ?: return null

        component.mediaPlayer().events().addMediaPlayerEventListener(playerListener)
        mediaPlayerListenerRegistered = true
        mediaPlayerComponent = component
        return component
    }

    private class RemotePlaybackSession(
        private val response: HttpResponse,
        private val channel: ByteReadChannel
    ) : AutoCloseable {

        @Volatile
        private var closed = false

        val callbackMedia = object : DefaultCallbackMedia(false) {
            /**
             * 返回当前响应头中的媒体长度，供 VLC 读取元信息。
             */
            override fun onGetSize(): Long =
                response.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: 0L

            /**
             * 打开远程回调媒体时仅校验当前会话未关闭。
             */
            override fun onOpen(): Boolean = !closed

            /**
             * 从 Ktor 响应流中持续读取字节并喂给 VLC。
             */
            override fun onRead(buffer: ByteArray, bufferSize: Int): Int {
                if (closed) {
                    return -1
                }
                return runBlocking(Dispatchers.IO) {
                    channel.readAvailable(buffer, 0, bufferSize)
                }
            }

            /**
             * 当前远程流播放不支持 seek，统一返回 false。
             * //todo 应该改为支持
             */
            override fun onSeek(offset: Long): Boolean = false

            /**
             * VLC 关闭媒体时同步关闭底层远程会话。
             */
            override fun onClose() {
                close()
            }
        }

        /**
         * 关闭当前远程读取通道，停止后续流式读取。
         */
        override fun close() {
            if (closed) {
                return
            }
            closed = true
            runCatching { channel.cancel(null) }
        }
    }
}
