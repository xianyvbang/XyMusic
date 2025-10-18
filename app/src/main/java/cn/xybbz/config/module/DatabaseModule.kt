package cn.xybbz.config.module

import android.content.Context
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.config.DatasourceConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun db(@ApplicationContext context: Context): DatabaseClient {
        val datasourceConfig = DatasourceConfig()
        return datasourceConfig.createDatabaseClient(context)
    }
}