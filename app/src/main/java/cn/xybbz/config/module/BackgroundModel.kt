package cn.xybbz.config.module

import android.content.Context
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.localdata.config.DatabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class BackgroundModel {
    @Singleton
    @Provides
    fun backgroundConfig(
        db: DatabaseClient,
        @ApplicationContext applicationContext: Context
    ): BackgroundConfig {
        return BackgroundConfig(db, applicationContext)
    }


}