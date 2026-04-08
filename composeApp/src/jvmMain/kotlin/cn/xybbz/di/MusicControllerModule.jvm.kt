package cn.xybbz.di

import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.music.JvmMusicController
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
        return JvmMusicController()
    }
}
