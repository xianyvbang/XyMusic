package cn.xybbz.music

import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.scope.XyCloseableCoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * 桌面歌词协调器。
 *
 * 负责把 JVM 播放器的当前歌曲变化同步到通用 [LrcServer]，让桌面端拥有和 Android
 * 元数据回调相同的歌词加载流程。
 */
@OptIn(ExperimentalAtomicApi::class)
class JvmLyricsCoordinator(
    // 桌面播放控制器，提供当前歌曲与播放进度来源。
    private val musicController: MusicCommonController,
    // 通用歌词服务，负责歌词列表、偏移配置和当前高亮行状态。
    private val lrcServer: LrcServer,
    // JVM 本地文件内嵌歌词读取器，只在桌面端解析本地音频标签。
    private val lyricsMetadataReader: JvmLyricsMetadataReader = JvmLyricsMetadataReader()
) {

    // JVM 歌词协调器自身的启动保护，避免首页/播放器重入时重复注册监听；不放到 LrcServer 中。
    private val started = AtomicBoolean(false)

    /**
     * 启动桌面歌词监听。
     *
     * 这里复用数据源生命周期作用域：连接释放时歌词监听也随之结束。
     */
    fun start(scope: XyCloseableCoroutineScope) {
        if (!started.compareAndSet(expectedValue = false, newValue = true)) {
            return
        }
        lrcServer.init(scope.coroutineContext)
        scope.launch {
            musicController.musicInfoFlow
                // 歌词加载只关心歌曲是否切换，收藏状态等同一歌曲字段变化不应重复触发。
                .map { it?.itemId }
                .distinctUntilChanged()
                .collect { itemId ->
                    val music = musicController.musicInfo
                    if (itemId.isNullOrBlank() || music == null) {
                        // 播放列表清空或当前歌曲置空时，避免播放页残留上一首歌词。
                        lrcServer.clear()
                        return@collect
                    }

                    // 桌面本地文件优先尝试内嵌歌词；没有内嵌歌词时由 LrcServer 走网络兜底。
                    val embeddedLyrics = lyricsMetadataReader.readEmbeddedLyrics(music)
                    lrcServer.loadLyricsForCurrentMusic(embeddedLyrics)
                }
        }
    }
}
