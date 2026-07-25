package cn.xybbz.di

import cn.xybbz.config.music.PlaybackProgressReporter
import org.koin.dsl.module
import org.koin.dsl.onClose

/**
 * 播放进度上报器的显式 Koin 模块，容器关闭时同步释放其协程资源。
 */
val playbackProgressModule = module {
    single<PlaybackProgressReporter> {
        PlaybackProgressReporter(
            musicController = get(),
            dataSourceManager = get(),
            settingsManager = get()
        )
    }.onClose { reporter ->
        reporter?.close()
    }
}
