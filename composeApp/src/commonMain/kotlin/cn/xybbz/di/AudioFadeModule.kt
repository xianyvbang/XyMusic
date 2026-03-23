package cn.xybbz.di

import cn.xybbz.config.music.AudioFadeController
import org.koin.core.annotation.Singleton


expect class AudioFadeModule {
    @Singleton
    fun audioFadeController(): AudioFadeController
}