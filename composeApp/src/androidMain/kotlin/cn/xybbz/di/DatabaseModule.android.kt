package cn.xybbz.di

import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.config.DatasourceFactory
import cn.xybbz.localdata.config.getRoomDatabase
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
actual class DatabaseModule {
    @Single
    actual fun db(contextWrapper: ContextWrapper): LocalDatabaseClient {
        return getRoomDatabase(DatasourceFactory(contextWrapper.context).createDatabaseClientBuilder())
    }
}