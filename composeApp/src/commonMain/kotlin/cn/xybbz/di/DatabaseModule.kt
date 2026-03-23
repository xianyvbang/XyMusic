package cn.xybbz.di

import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.config.DatasourceFactory
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
expect class DatabaseModule {

    @Single
    fun db(contextWrapper:ContextWrapper): DatabaseClient
}