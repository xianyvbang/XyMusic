package cn.xybbz.di

import cn.xybbz.config.music.AudioFadeController
import cn.xybbz.music.AudioFadeJvmController
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
actual class AudioFadeModule {
    @Singleton
    actual fun audioFadeController(): AudioFadeController {
        return AudioFadeJvmController()
    }
}
