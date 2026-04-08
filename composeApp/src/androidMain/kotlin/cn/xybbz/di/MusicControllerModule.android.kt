package cn.xybbz.di

import cn.xybbz.config.music.AudioFadeController
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.music.MusicController
import cn.xybbz.platform.ContextWrapper
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import org.koin.mp.KoinPlatform

@Module
@Configuration
actual class MusicControllerModule {
    @Singleton
    fun concreteMusicController(wrapper: ContextWrapper): MusicController {
        return MusicController(
            wrapper.context,
            KoinPlatform.getKoin()
                .get<AudioFadeController>() as cn.xybbz.music.AudioFadeAndroidController
        )
    }

    @Singleton
    actual fun musicController(wrapper: ContextWrapper): MusicCommonController {
        return KoinPlatform.getKoin().get<MusicController>()
    }
}
