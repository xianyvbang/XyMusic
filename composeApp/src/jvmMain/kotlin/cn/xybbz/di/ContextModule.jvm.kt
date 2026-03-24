package cn.xybbz.di

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

actual class ContextWrapper

@Module
@Configuration
actual class ContextModule actual constructor() {
    @Single
    actual fun providesContextWrapper(scope: Scope): ContextWrapper {
        return ContextWrapper()
    }
}