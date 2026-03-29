package cn.xybbz.di

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module
@Configuration
actual class ContextModule {
    @Single
    actual fun providesContextWrapper(): ContextWrapper {
        return ContextWrapper()
    }
}

actual class ContextWrapper