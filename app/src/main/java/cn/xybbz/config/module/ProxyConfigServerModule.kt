package cn.xybbz.config.module

import android.content.Context
import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.localdata.config.DatabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProxyConfigServerModule {


    @Singleton
    @Provides
    fun proxyConfigServer(
        db: DatabaseClient,
        @ApplicationContext applicationContext: Context
    ): ProxyConfigServer {
        val proxyConfigServer = ProxyConfigServer(db)
        proxyConfigServer.initConfig()
        return proxyConfigServer
    }
}