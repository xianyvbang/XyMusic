package cn.xybbz.config.module

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import cn.xybbz.api.client.CacheApiClient
import cn.xybbz.common.music.CacheController
import cn.xybbz.config.setting.SettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CacheModule {

    @OptIn(UnstableApi::class)
    @Singleton
    @Provides
    fun cacheController(
        @ApplicationContext context: Context,
        settingsManager: SettingsManager,
        cacheApiClient: CacheApiClient
    ): CacheController {
        return CacheController(context, settingsManager,cacheApiClient)
    }
}