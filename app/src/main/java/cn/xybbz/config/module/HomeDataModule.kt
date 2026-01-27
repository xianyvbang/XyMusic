package cn.xybbz.config.module

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.HomeDataRepository
import cn.xybbz.localdata.config.DatabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeDataModule {

    @Singleton
    @Provides
    fun homeDataRepository(
        db: DatabaseClient,
        datasourceServer: DataSourceManager
    ): HomeDataRepository {
        return HomeDataRepository(db, datasourceServer)
    }
}