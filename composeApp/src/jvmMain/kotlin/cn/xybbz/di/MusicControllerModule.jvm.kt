package cn.xybbz.di

import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.music.JvmMusicController
import cn.xybbz.music.VlcBootstrap
import cn.xybbz.platform.ContextWrapper
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
actual class MusicControllerModule {
    @Singleton
    actual fun musicController(
        wrapper: ContextWrapper
    ): MusicCommonController {
        // 在播放器控制器创建前先完成 VLC 运行时预热，
        // 这样后续真正实例化 AudioPlayerComponent 时可以直接命中本地 native。
        VlcBootstrap.ensureConfigured()
        return JvmMusicController()
    }
}
