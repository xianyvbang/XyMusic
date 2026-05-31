package cn.xybbz.startup

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.utils.Log
import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.music.JvmLyricsCoordinator
import org.koin.mp.KoinPlatform
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

// JVM 歌词启动日志标签，便于和 VLC 播放日志区分。
private const val JVM_LYRICS_BOOTSTRAP_TAG = "JvmLyricsBootstrap"

@OptIn(ExperimentalAtomicApi::class)
// JVM 歌词链路只需要启动一次；保护放在桌面启动层，不放进通用 LrcServer。
private val jvmLyricsStarted = AtomicBoolean(false)

/**
 * 在 JVM 播放控制器初始化后启动桌面歌词链路。
 *
 * 这里才解析 Koin 依赖，避免应用首屏阶段提前创建歌词和数据源相关重依赖。
 */
@OptIn(ExperimentalAtomicApi::class)
fun startJvmLyrics(musicController: MusicCommonController) {
    if (!jvmLyricsStarted.compareAndSet(expectedValue = false, newValue = true)) {
        return
    }

    runCatching {
        val koin = KoinPlatform.getKoin()
        // 歌词监听复用 DataSourceManager 的作用域，随当前数据源生命周期结束。
        val dataSourceManager = koin.get<DataSourceManager>()
        JvmLyricsCoordinator(
            musicController = musicController,
            lrcServer = koin.get<LrcServer>()
        ).start(dataSourceManager.dataSourceScope())
    }.onFailure {
        Log.e(JVM_LYRICS_BOOTSTRAP_TAG, "JVM 歌词启动失败", it)
    }
}
