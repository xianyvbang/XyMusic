package cn.xybbz.di

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

// commonMain

expect class ContextWrapper

@Module
expect class ContextModule() {

    @Single
    fun providesContextWrapper() : ContextWrapper
}