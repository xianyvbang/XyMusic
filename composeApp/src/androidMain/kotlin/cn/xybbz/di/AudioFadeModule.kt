package cn.xybbz.di

import cn.xybbz.music.AudioFadeController
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
object AudioFadeModule {
    @Singleton
    fun audioFadeController():AudioFadeController{
        return AudioFadeController()
    }
}