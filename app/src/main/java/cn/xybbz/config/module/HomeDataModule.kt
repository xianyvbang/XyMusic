package cn.xybbz.config.module

import cn.xybbz.config.ConnectionConfigServer
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
        connectionConfigServer: ConnectionConfigServer
    ): HomeDataRepository {
        return HomeDataRepository(db, connectionConfigServer)
    }
}