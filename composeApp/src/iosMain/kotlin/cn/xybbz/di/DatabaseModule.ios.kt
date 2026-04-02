package cn.xybbz.di

import cn.xybbz.database.DatasourceFactory
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.config.getLocalRoomDatabase
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
actual class DatabaseModule {

    @Single
    actual fun db(contextWrapper: ContextWrapper): LocalDatabaseClient {
        return getLocalRoomDatabase { DatasourceFactory().createDatabaseClientBuilder(it) }
    }
}