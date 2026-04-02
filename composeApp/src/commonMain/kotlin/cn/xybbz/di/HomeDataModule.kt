package cn.xybbz.di

import cn.xybbz.config.HomeDataRepository
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
    ): HomeDataRepository {
        return HomeDataRepository(db)
    }
}