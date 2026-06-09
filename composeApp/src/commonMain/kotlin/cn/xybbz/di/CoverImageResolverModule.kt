package cn.xybbz.di

import cn.xybbz.api.client.custom.CustomMediaApiClient
import cn.xybbz.config.image.CoverImageResolver
import cn.xybbz.config.setting.SettingsManager
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
object CoverImageResolverModule {

    @Singleton
    fun coverImageResolver(
        settingsManager: SettingsManager,
        customMediaApiClient: CustomMediaApiClient
    ): CoverImageResolver {
        return CoverImageResolver(
            settingsManager = settingsManager,
            customMediaApiClient = customMediaApiClient
        )
    }
}
