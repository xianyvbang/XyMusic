package cn.xybbz.di

import cn.xybbz.config.music.DownloadCacheCommonController
import cn.xybbz.music.JvmDownloadCacheController
import cn.xybbz.platform.ContextWrapper
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
actual class CacheModule {
    @Singleton
    actual fun downloadCacheController(
        contextWrapper: ContextWrapper
    ): DownloadCacheCommonController {
        return JvmDownloadCacheController()
    }
}
