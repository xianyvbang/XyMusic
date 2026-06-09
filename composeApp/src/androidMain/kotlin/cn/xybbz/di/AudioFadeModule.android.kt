package cn.xybbz.di

import cn.xybbz.config.music.AudioFadeController
import cn.xybbz.music.AudioFadeAndroidController
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import org.koin.mp.KoinPlatform

@Module
@Configuration
actual class AudioFadeModule {
    @Singleton
    fun concreteAudioFadeController(): AudioFadeAndroidController {
        return AudioFadeAndroidController()
    }

    @Singleton
    actual fun audioFadeController(): AudioFadeController {
        return KoinPlatform.getKoin().get<AudioFadeAndroidController>()
    }
}
