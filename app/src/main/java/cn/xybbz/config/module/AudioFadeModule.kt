package cn.xybbz.config.module

import cn.xybbz.common.music.AudioFadeController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioFadeModule {
    @Singleton
    @Provides
    fun audioFadeController():AudioFadeController{
        return AudioFadeController()
    }
}