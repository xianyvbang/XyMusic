package cn.xybbz.di

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.HomeDataRepository
import cn.xybbz.localdata.config.DatabaseClient
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
object HomeDataModule {

    @Singleton
    fun homeDataRepository(
        db: DatabaseClient,
        datasourceServer: DataSourceManager
    ): HomeDataRepository {
        return HomeDataRepository(db, datasourceServer)
    }
}