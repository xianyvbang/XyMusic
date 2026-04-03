package cn.xybbz.di

import cn.xybbz.platform.ContextWrapper
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.mp.KoinPlatform


@Module
@Configuration
actual class ContextModule {

    // needs androidContext() to be setup at start
    @Single
    actual fun providesContextWrapper(): ContextWrapper =
        ContextWrapper(KoinPlatform.getKoin().get())
}
