package cn.xybbz.config.module

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import cn.xybbz.common.music.AudioFadeController
import cn.xybbz.common.music.CacheController
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.favorite.FavoriteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MusicControllerModule {

    @OptIn(UnstableApi::class)
    @Singleton
    @Provides
    fun musicController(
        @ApplicationContext application: Context,
        cheController: CacheController,
        favoriteRepository: FavoriteRepository,
        audioFadeController: AudioFadeController
    ): MusicController {
        val controller =
            MusicController(application, cheController, favoriteRepository, audioFadeController)
        return controller
    }
}