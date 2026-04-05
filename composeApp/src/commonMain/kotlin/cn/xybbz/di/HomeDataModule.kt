package cn.xybbz.di

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.HomeDataRepository
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.config.LocalDatabaseClient
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
object HomeDataModule {

    @Singleton
    fun homeDataRepository(
        db: LocalDatabaseClient,
        downloadDb: DownloadDatabaseClient,
        dataSourceManager: DataSourceManager,
    ): HomeDataRepository {
        return HomeDataRepository(db, downloadDb, dataSourceManager)
    }
}
