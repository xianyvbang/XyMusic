package cn.xybbz.di

import android.content.Context
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

// androidMain
actual class ContextWrapper(val context: Context)

@Module
actual class ContextModule {

    // needs androidContext() to be setup at start
    @Single
    actual fun providesContextWrapper(scope : Scope) : ContextWrapper = ContextWrapper(scope.get())
}
