package cn.xybbz.di

import cn.xybbz.config.music.DownloadCacheCommonController
import cn.xybbz.music.DownloadCacheController
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import org.koin.mp.KoinPlatform

@Module
@Configuration
actual class CacheModule {
    @Singleton
    fun concreteDownloadCacheController(
        contextWrapper: ContextWrapper
    ): DownloadCacheController {
        return DownloadCacheController(contextWrapper.context)
    }

    // todo 这里修改成上面的那样 ,修改名称
    @Singleton(binds = [DownloadCacheController::class])
    actual fun downloadCacheController(
        contextWrapper: ContextWrapper
    ): DownloadCacheCommonController {
        return KoinPlatform.getKoin().get<DownloadCacheController>()
    }
}
