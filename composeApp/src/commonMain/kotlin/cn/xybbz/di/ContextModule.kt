package cn.xybbz.di

import cn.xybbz.platform.ContextWrapper
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
expect class ContextModule() {

    @Single
    fun providesContextWrapper() : ContextWrapper
}