package cn.xybbz.music

import cn.xybbz.api.TokenServer
import cn.xybbz.api.enums.AudioCodecEnum
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.PlayerEvent
import cn.xybbz.entity.data.music.TranscodingAndMusicUrlData
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum
import cn.xybbz.proxy.JvmReverseProxyServer
import io.github.oshai.kotlinlogging.KotlinLogging
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.media.Media
import uk.co.caprica.vlcj.media.MediaRef
import uk.co.caprica.vlcj.medialist.MediaList
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.list.MediaListPlayer
import uk.co.caprica.vlcj.player.list.PlaybackMode
import java.io.File

class JvmMusicController : MusicCommonController() {
    val logger = KotlinLogging.logger("jvmMusic")

    private var mediaPlayerFactory: MediaPlayerFactory? = null
    private var mediaPlayer: MediaPlayer? = null
    private var mediaListPlayer: MediaListPlayer? = null
    private var mediaList: MediaList? = null
    private var mediaPlayerListenerRegistered = false


    //防止“过期的异步播放请求”把新的播放状态覆盖掉。
    private var playRequestVersion = 0L

    // 应用层列表与 VLC 内部 MediaList 保持一份轻量镜像，
    private val playlistMediaSources = mutableListOf<String>()

    private val playerListener = object : MediaPlayerEventAdapter() {
        /**
         * 播放开始时同步当前歌曲、进度与播放状态。
         */
        override fun playing(mediaPlayer: MediaPlayer?) {
            Log.i("vlc", "播放开始")
            submitMediaPlayerTask(mediaPlayer) { _ ->
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
            if (state == PlayStateEnum.Playing) {
                reportedPauseEvent()
            }
            updateState(PlayStateEnum.Pause)
        }

        /**
         * 播放停止时重置进度和播放状态。
         */
        override fun stopped(mediaPlayer: MediaPlayer?) {
            setCurrentPositionData(0L)
            updateState(PlayStateEnum.None)
        }

        /**
         * 播放自然结束时回退为空闲状态。
         */
        override fun finished(mediaPlayer: MediaPlayer?) {
            musicInfo?.let {
                updateEvent(PlayerEvent.RemovePlaybackProgress(it.itemId))
            }
            setCurrentPositionData(0L)
        }


        override fun mediaChanged(
            mediaPlayer: MediaPlayer?,
            media: MediaRef?
        ) {
            Log.i("vlc", "播放变化: ${media}")

            val retainedMedia = media?.newMedia() ?: return
            val player = mediaPlayer ?: run {
                retainedMedia.release()
                return
            }
//            updatePicBytes(it)
            submitMediaPlayerTask(mediaPlayer) {
                try {
                    syncCurrentMusicFromMedia(retainedMedia)
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
            Log.i("vlc", "播放异常")
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

    /**
     * 初始化 JVM 播放器监听器与进度轮询任务。
     */
    override fun initController(onRestorePlaylists: (() -> Unit)?) {
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

        val currentList = originMusicList.toList()
        val insertIndex =
            if (currentList.isNotEmpty() && curOriginIndex != Constants.MINUS_ONE_INT) {
                curOriginIndex + 1
            } else {
                currentList.size
            }

        val updatedList = currentList.toMutableList().apply {
            addAll(insertIndex, musicList)
        }
        val hadValidPlaylist = isPlaylistCacheValid(currentList)
        updateOriginMusicList(updatedList)

        if (isPlayer == true) {
            //todo 这里不应该清空
            invalidatePlaylistCache()
            seekToIndex(insertIndex)
        } else if (hadValidPlaylist) {
            schedulePlaylistInsert(insertIndex, musicList, updatedList)
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
        clearPlaylistMirror()
        stopCurrentPlayback()
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
        Log.i("music", "恢复播放")
        updateState(PlayStateEnum.Loading)
        currentMediaPlayer()?.controls()?.play()
    }

    /**
     * 跳转到指定进度。
     */
    override fun seekTo(millSeconds: Long) {
        val player = currentMediaPlayer() ?: return
        player.controls().setTime(millSeconds)
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
            player.controls().play()
        }
    }

    /**
     * 播放列表内跳转到下一首歌曲。
     */
    override fun seekToNext() {
        if (originMusicList.isEmpty()) {
            return
        }
        updateState(PlayStateEnum.Loading)
        currentMediaListPlayer()?.controls()?.playNext()
    }

    /**
     * 播放列表内跳转到上一首歌曲。
     */
    override fun seekToPrevious() {
        if (originMusicList.isEmpty()) {
            return
        }
        updateState(PlayStateEnum.Loading)
        currentMediaListPlayer()?.controls()?.playPrevious()
    }

    /**
     * 根据列表索引切换歌曲，并交由 MediaListPlayer 播放对应项。
     */
    override fun seekToIndex(index: Int) {
        if (index !in originMusicList.indices) {
            return
        }
        updateState(PlayStateEnum.Loading)
        updateEvent(PlayerEvent.BeforeChangeMusic)
        startPlaybackAtIndex(index)
    }

    /**
     * 装载并播放指定索引歌曲；如提供恢复进度，则在真正开始播放后跳转到对应位置。
     * 这里不再直接给 MediaPlayer 喂单条 url，而是先确保 MediaList 已准备好，
     * 再交给 MediaListPlayer 按索引播放。
     */
    private fun startPlaybackAtIndex(index: Int) {
        val snapshot = originMusicList.toList()
        if (index !in snapshot.indices) {
            return
        }

        ensureMediaPlayer() ?: run {
            updateState(PlayStateEnum.None)
            return
        }
        val listPlayer = currentMediaListPlayer() ?: run {
            updateState(PlayStateEnum.None)
            return
        }

        playRequestVersion += 1
        updateState(PlayStateEnum.Loading)
        listPlayer.controls().play(index)
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

        val currentList = originMusicList.toList()
        val hadValidPlaylist = isPlaylistCacheValid(currentList)
        val updatedList = currentList.toMutableList().apply {
            removeAt(index)
        }
        updateOriginMusicList(updatedList)

        when {
            updatedList.isEmpty() -> {
                if (hadValidPlaylist) {
                    removePlaylistItem(index)
                }
                clearPlayerList()
            }

            index < curOriginIndex -> {
                if (hadValidPlaylist) {
                    removePlaylistItem(index)
                }
                updateOriginIndex(curOriginIndex - 1)
            }

            index == curOriginIndex -> {
                stopCurrentPlayback()
                if (hadValidPlaylist) {
                    removePlaylistItem(index)
                } else {
                    invalidatePlaylistCache()
                }
                val nextIndex = index.coerceAtMost(updatedList.lastIndex)
                updateOriginIndex(nextIndex)
                updateCurrentMusic(updatedList.getOrNull(nextIndex))
                updateDuration(updatedList.getOrNull(nextIndex)?.runTimeTicks ?: 0L)
            }

            else -> {
                if (hadValidPlaylist) {
                    removePlaylistItem(index)
                }
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
     * 更新当前播放模式并向外发送模式变化事件。
     */
    override fun setPlayTypeData(playerTypeEnum: PlayerTypeEnum) {
        playType = playerTypeEnum
        applyPlaybackMode(playerTypeEnum)
        updateEvent(PlayerEvent.PlayerTypeChange(playerTypeEnum))
    }

    /**
     * 将歌曲插入到“下一首播放”位置。
     */
    override fun addNextPlayer(music: XyPlayMusic) {
        val currentList = originMusicList.toList()
        val insertIndex = when {
            currentList.isEmpty() -> 0
            curOriginIndex == Constants.MINUS_ONE_INT -> currentList.size
            else -> curOriginIndex + 1
        }

        val updatedList = currentList.toMutableList()
        val existingIndex = updatedList.indexOfFirst { it.itemId == music.itemId }
        if (existingIndex != Constants.MINUS_ONE_INT) {
            updatedList.removeAt(existingIndex)
        }
        val targetIndex = insertIndex.coerceAtMost(updatedList.size)
        updatedList.add(targetIndex, music)
        val hadValidPlaylist = isPlaylistCacheValid(currentList)
        updateOriginMusicList(updatedList)
        if (hadValidPlaylist) {
            scheduleMoveOrInsertPlaylistItem(
                music = music,
                existingIndex = originMusicList.indexOfFirst { it.itemId == music.itemId },
                targetIndex = targetIndex,
                expectedList = updatedList
            )
        }
        updateEvent(PlayerEvent.AddMusicList(music.artistIds?.firstOrNull()))
    }

    /**
     * 获取当前列表中可播放的下一首索引。
     */
    override fun getNextPlayableIndex(): Int? {
        if (originMusicList.isEmpty()) {
            return null
        }
        val currentIndex = curOriginIndex.takeIf { it in originMusicList.indices } ?: 0
        return if (currentIndex >= originMusicList.lastIndex) 0 else currentIndex + 1
    }

    /**
     * 获取当前列表中可播放的上一首索引。
     */
    override fun getPreviousPlayableIndex(): Int? {
        if (originMusicList.isEmpty()) {
            return null
        }
        val currentIndex = curOriginIndex.takeIf { it in originMusicList.indices } ?: 0
        return if (currentIndex <= 0) originMusicList.lastIndex else currentIndex - 1
    }

    /**
     * 刷新当前远程歌曲的播放地址。
     * 转码策略或网络环境变化后，需要把整份 MediaList 里的远程地址重建一遍。
     */
    override fun replacePlaylistItemUrl() {
        val snapshot = originMusicList.toList()
        if (snapshot.isEmpty()) {
            return
        }

        val currentIndex = curOriginIndex
        val currentState = state

        if (currentState == PlayStateEnum.Pause) {
            stopCurrentPlayback()
        }

        val refreshedSources = snapshot.map { preparePlaylistSource(it) }
        if (!matchesPlaylistSnapshot(snapshot)) {
            return
        }

        // 地址刷新后直接整表重建，避免旧 mrl 残留在 VLC 内部列表里。
        val applied = applyFullPlaylist(refreshedSources)
        if (!applied) {
            invalidatePlaylistCache()
            return
        }

        if ((currentState == PlayStateEnum.Playing || currentState == PlayStateEnum.Loading) &&
            currentIndex in snapshot.indices
        ) {

            currentMediaListPlayer()?.controls()?.play(currentIndex)
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

        stopCurrentPlayback()
        clearPlaylistMirror()

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

        val requestVersion = playRequestVersion

        // 远程地址需要先解析为最终可播地址；准备期间如果用户又切了别的歌，
        // 则通过 requestVersion 放弃这次过期的准备结果。
        val playlistReady = ensurePlaylistPrepared(originMusicList, requestVersion)
        if (!playlistReady || requestVersion != playRequestVersion) {
            if (requestVersion == playRequestVersion) {
                updateState(PlayStateEnum.None)
            }
        }
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

    override fun getMusicUrl(
        musicId: String,
        plexPlayKey: String?
    ): TranscodingAndMusicUrlData {
        val musicUrl = dataSourceManager.getMusicPlayUrl(
            musicId,
            true,
            AudioCodecEnum.ROW,
            null,
            settingsManager.get().playSessionId
        )

        return TranscodingAndMusicUrlData(0, true, musicUrl)
    }

    /**
     * 关闭控制器并释放播放器与协程资源。
     */
    override fun close() {
        stopCurrentPlayback()
        currentMediaPlayer()?.takeIf { mediaPlayerListenerRegistered }?.events()
            ?.removeMediaPlayerEventListener(playerListener)
        mediaPlayerListenerRegistered = false
        mediaList?.release()
        mediaList = null
        mediaListPlayer?.release()
        mediaListPlayer = null
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
        val originUrl = getMusicUrl(music.itemId, music.plexPlayKey).musicUrl
        val proxyUrl = buildProxyPlaybackUrl(originUrl)
        return proxyUrl
    }

    /**
     * 将远程音频地址包装成本地代理地址。
     * 这样 VLC 访问的永远是本地 19180 代理，从而统一复用鉴权头、Range 与流式转发能力。
     */
    private fun buildProxyPlaybackUrl(originUrl: String): String {
        if (originUrl.isBlank()) {
            return originUrl
        }
        return JvmReverseProxyServer.wrapTargetUrl(TokenServer.baseUrl + originUrl)
    }

    /**
     * 停止当前 VLC 播放流程。
     * `ignoreStoppedEvent=true` 时会预先登记一次忽略标记，
     * 用来屏蔽 MediaListPlayer 内部切换带来的中间 stopped 事件。
     */
    private fun stopCurrentPlayback(
    ) {

        runCatching { currentMediaListPlayer()?.controls()?.stop() }
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
     * 确保 VLC 内部的 MediaList 已经和当前业务层播放列表同步完成。
     * 如果本地镜像仍然有效，则直接复用；
     * 否则重新把业务层列表解析成可播放地址，再整表写入 MediaList。
     *
     * `requestVersion` 用来防止异步准备期间发生“用户又切歌/又改列表”的竞态：
     * 一旦发现这次准备结果已经过期，就直接放弃，不把旧数据写回播放器。
     */
    private fun ensurePlaylistPrepared(
        musicList: List<XyPlayMusic>,
        requestVersion: Long
    ): Boolean {
        // 当前应用层列表和 VLC 内部列表仍然一致时，直接复用，避免每次切歌都重建。
        if (isPlaylistCacheValid(musicList)) {
            return true
        }

        val preparedSources = mutableListOf<String>()
        musicList.forEach { music ->
            if (requestVersion != playRequestVersion) {
                return false
            }
            // 远程歌曲在这里解析成最终代理地址，本地歌曲则直接转成 file uri。
            preparedSources += preparePlaylistSource(music)
        }

        if (requestVersion != playRequestVersion || !matchesPlaylistSnapshot(musicList)) {
            return false
        }

        return applyFullPlaylist(preparedSources)
    }

    /**
     * 用最新的业务列表整表覆盖 VLC 的 MediaList，并同步更新本地镜像缓存。
     */
    private fun applyFullPlaylist(
        preparedSources: List<String>
    ): Boolean {
        val mediaApi = ensureMediaList()?.media() ?: return false
        runCatching { mediaApi.clear() }.onFailure {
            Log.e("vlc", "清空 MediaList 失败", it)
            return false
        }

        preparedSources.forEach { source ->
            val added = runCatching { mediaApi.add(source) }.getOrDefault(false)
            if (!added) {
                return false
            }
        }

        playlistMediaSources.clear()
        playlistMediaSources.addAll(preparedSources)
        return true
    }

    /**
     * 在当前播放位置后插入一批歌曲时，尽量只做增量同步；
     * 如果增量失败，再由上层在下次播放时触发整表重建。
     */
    private fun schedulePlaylistInsert(
        insertIndex: Int,
        musicList: List<XyPlayMusic>,
        expectedList: List<XyPlayMusic>
    ) {
        val preparedSources = musicList.map { preparePlaylistSource(it) }
        if (!matchesPlaylistSnapshot(expectedList)) {
            return
        }

        val inserted = insertPlaylistItems(insertIndex, preparedSources)
        if (!inserted) {
            invalidatePlaylistCache()
        }
    }

    /**
     * “下一首播放”场景下，如果歌曲已存在则移动，否则插入到目标位置。
     */
    private fun scheduleMoveOrInsertPlaylistItem(
        music: XyPlayMusic,
        existingIndex: Int,
        targetIndex: Int,
        expectedList: List<XyPlayMusic>
    ) {
        var adjustedTargetIndex = targetIndex
        if (existingIndex != Constants.MINUS_ONE_INT) {
            val removed = removePlaylistItem(existingIndex)
            if (!removed) {
                invalidatePlaylistCache()
                return
            }
            if (existingIndex < adjustedTargetIndex) {
                adjustedTargetIndex -= 1
            }
        }

        val preparedSource = preparePlaylistSource(music)
        if (!matchesPlaylistSnapshot(expectedList)) {
            return
        }

        val inserted = insertPlaylistItems(
            adjustedTargetIndex,
            listOf(preparedSource)
        )
        if (!inserted) {
            invalidatePlaylistCache()
        }
    }

    /**
     * 将若干媒体项插入到 VLC 的 MediaList，并同步更新本地镜像。
     */
    private fun insertPlaylistItems(
        insertIndex: Int,
        preparedSources: List<String>
    ): Boolean {
        val mediaApi = ensureMediaList()?.media() ?: return false
        val boundedIndex = insertIndex.coerceIn(0, originMusicList.size)

        preparedSources.forEachIndexed { offset, source ->
            val actualIndex = boundedIndex + offset
            val inserted = runCatching {
                if (actualIndex >= originMusicList.size + offset) {
                    mediaApi.add(source)
                } else {
                    mediaApi.insert(actualIndex, source)
                }
            }.getOrDefault(false)
            if (!inserted) {
                return false
            }
        }

        playlistMediaSources.addAll(boundedIndex, preparedSources)
        return true
    }

    /**
     * 从 VLC 的 MediaList 中删除单条媒体，并同步更新镜像缓存。
     */
    private fun removePlaylistItem(index: Int): Boolean {
        if (index !in originMusicList.indices) {
            return false
        }

        val mediaApi = ensureMediaList()?.media() ?: return false
        val removed = runCatching { mediaApi.remove(index) }.getOrDefault(false)
        if (!removed) {
            return false
        }
        playlistMediaSources.removeAt(index)
        return true
    }

    /**
     * 统一把业务歌曲对象转换成 VLC 可消费的媒体地址。
     * 本地歌曲直接返回 file uri，远程歌曲先转成本地代理地址。
     */
    private fun preparePlaylistSource(music: XyPlayMusic): String {
        val localPath = music.filePath
        if (!localPath.isNullOrBlank()) {
            return File(localPath).toURI().toString()
        }
        return resolveRemotePlaybackUrl(music)
    }

    /**
     * 延迟初始化 VLC 播放器体系。
     * 这里会同时创建：
     * 1. 单条媒体事件来源 MediaPlayer
     * 2. 负责按列表播放的 MediaListPlayer
     * 3. 真正承载播放队列的 MediaList
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

        val createdPlayer = runCatching {
            factory.mediaPlayers().newMediaPlayer()
        }.onFailure {
            runCatching { factory.release() }
            Log.e("vlc", "创建 VLC 音频播放器失败", it)
        }.getOrNull() ?: return null

        val createdListPlayer = runCatching {
            factory.mediaPlayers().newMediaListPlayer()
        }.onFailure {
            runCatching { createdPlayer.release() }
            runCatching { factory.release() }
            Log.e("vlc", "创建 VLC MediaListPlayer 失败", it)
        }.getOrNull() ?: return null

        val createdMediaList = runCatching {
            factory.media().newMediaList()
        }.onFailure {
            runCatching { createdListPlayer.release() }
            runCatching { createdPlayer.release() }
            runCatching { factory.release() }
            Log.e("vlc", "创建 VLC MediaList 失败", it)
        }.getOrNull() ?: return null

        val bound = runCatching {
            // MediaListPlayer 本身不直接输出音频，
            // 需要绑定一个真正的 MediaPlayer 和一份 MediaList。
            createdListPlayer.mediaPlayer().setMediaPlayer(createdPlayer)
            val mediaListRef = createdMediaList.newMediaListRef()
            try {
                createdListPlayer.list().setMediaList(mediaListRef)
            } finally {
                mediaListRef.release()
            }
        }.isSuccess
        if (!bound) {
            runCatching { createdMediaList.release() }
            runCatching { createdListPlayer.release() }
            runCatching { createdPlayer.release() }
            runCatching { factory.release() }
            return null
        }
        createdPlayer.events().addMediaPlayerEventListener(playerListener)
        mediaPlayerListenerRegistered = true
        mediaPlayerFactory = factory
        mediaPlayer = createdPlayer
        mediaListPlayer = createdListPlayer
        mediaList = createdMediaList
        applyPlaybackMode(playType)
        return createdPlayer
    }

    /**
     * 确保 MediaList 已可用。
     * 由于 MediaList 是随播放器一起懒初始化的，这里通过确保播放器存在来顺带拿到它。
     */
    private fun ensureMediaList(): MediaList? {
        ensureMediaPlayer()
        return mediaList
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
     * MediaListPlayer 自动切到下一首后，业务层的 curOriginIndex/musicInfo 不会自动更新，
     * 这里通过当前媒体的 mrl 反查回应用层索引，并补发切歌事件。
     */
    private fun syncCurrentMusicFromMedia(media: Media) {
        val currentMrl = media.info().mrl() ?: return
        val currentIndex = resolvePlaylistIndex(currentMrl)
        if (currentIndex !in originMusicList.indices) {
            return
        }

        val currentMusic = originMusicList[currentIndex]
        if (currentIndex == curOriginIndex && musicInfo?.itemId == currentMusic.itemId) {
            return
        }

        if (originMusicList.isNotEmpty() && curOriginIndex >= originMusicList.size - 1 && ifNextPage) {
            updateEvent(PlayerEvent.NextList(pageNum))
        }

        updateOriginIndex(currentIndex)
        updateCurrentMusic(currentMusic)
        updateCurrentFavorite(musicInfo?.ifFavoriteStatus ?: false)
        updateDuration(currentMusic.runTimeTicks)
        updateEvent(
            PlayerEvent.ChangeMusic(
                currentMusic.itemId,
                currentMusic.artistIds?.firstOrNull(),
                currentMusic.artists?.firstOrNull()
            )
        )
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
     * 获取底层单曲播放器实例。
     */
    private fun currentMediaPlayer(): MediaPlayer? {
        return mediaPlayer
    }

    /**
     * 获取负责调度播放列表的 MediaListPlayer 实例。
     */
    private fun currentMediaListPlayer(): MediaListPlayer? {
        return mediaListPlayer
    }

    /**
     * 将应用层播放模式映射到 VLC 的列表播放模式。
     * 当前实现里：
     * SINGLE_LOOP -> REPEAT
     * 其他模式 -> LOOP
     */
    private fun applyPlaybackMode(playerTypeEnum: PlayerTypeEnum) {
        val mode = when (playerTypeEnum) {
            PlayerTypeEnum.SINGLE_LOOP -> PlaybackMode.REPEAT
            else -> PlaybackMode.LOOP
        }
        currentMediaListPlayer()?.controls()?.setMode(mode)
    }

    /**
     * 通过 VLC 当前播放媒体的 mrl，反查它在业务层列表中的索引。
     */
    private fun resolvePlaylistIndex(mrl: String): Int {
        return playlistMediaSources.indexOf(mrl)
    }

    /**
     * 判断异步准备期间，业务层列表是否已经发生变化。
     * 如果用户中途又改了播放列表，就放弃这次旧快照的同步结果。
     */
    private fun matchesPlaylistSnapshot(snapshot: List<XyPlayMusic>): Boolean {
        if (snapshot.size != originMusicList.size) {
            return false
        }
        return snapshot.indices.all { snapshot[it].itemId == originMusicList[it].itemId }
    }

    /**
     * 判断当前 VLC 内部列表镜像是否还能和业务层列表一一对应。
     */
    private fun isPlaylistCacheValid(musicList: List<XyPlayMusic>): Boolean {
        if (originMusicList.size != musicList.size ||
            playlistMediaSources.size != musicList.size
        ) {
            return false
        }
        return musicList.indices.all { originMusicList[it].itemId == musicList[it].itemId }
    }

    /**
     * 仅清空应用层维护的播放列表镜像缓存，
     * 不直接修改 VLC 内部列表，供上层决定何时重建。
     */
    private fun invalidatePlaylistCache() {
        playlistMediaSources.clear()
    }

    /**
     * 清空 VLC 内部播放列表及其镜像缓存。
     */
    private fun clearPlaylistMirror() {
        invalidatePlaylistCache()
        runCatching { mediaList?.media()?.clear() }
    }

    companion object {
        /**
         * 使用隔离的 libVLC 参数，避免读取用户本机 VLC 的历史配置。
         * 其中 `--ignore-config` 用来屏蔽续播等偏好设置，
         * `--no-media-library` 则避免媒体库相关的额外状态介入当前播放器实例。
         */
        private val VLC_FACTORY_ARGUMENTS = arrayOf(
            "--quiet",
            "--intf=dummy",
            "--ignore-config",
            "--no-media-library"
        )
    }
}
