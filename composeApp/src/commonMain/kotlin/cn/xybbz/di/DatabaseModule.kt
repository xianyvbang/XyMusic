package cn.xybbz.di

import cn.xybbz.localdata.config.LocalDatabaseClient
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
expect class DatabaseModule {

    @Single
    fun db(contextWrapper:ContextWrapper): LocalDatabaseClient
}