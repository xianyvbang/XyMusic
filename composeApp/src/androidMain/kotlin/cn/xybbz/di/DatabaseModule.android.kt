package cn.xybbz.di

import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.config.DatasourceFactory
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
actual class DatabaseModule {
    @Single
    actual fun db(contextWrapper: ContextWrapper): DatabaseClient {
        return DatasourceFactory(contextWrapper.context).createDatabaseClientBuilder()
            .build()
    }
}