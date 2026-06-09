package cn.xybbz.di

import cn.xybbz.platform.ContextWrapper
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
actual class ContextModule {
    @Single
    actual fun providesContextWrapper(): ContextWrapper {
        return ContextWrapper()
    }
}