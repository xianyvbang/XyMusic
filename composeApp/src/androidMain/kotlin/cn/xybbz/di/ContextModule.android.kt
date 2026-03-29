package cn.xybbz.di

import android.content.Context
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope
import org.koin.mp.KoinPlatform

// androidMain
actual class ContextWrapper(val context: Context)

@Module
@Configuration
actual class ContextModule {

    // needs androidContext() to be setup at start
    @Single
    actual fun providesContextWrapper() : ContextWrapper = ContextWrapper(KoinPlatform.getKoin().get())
}
