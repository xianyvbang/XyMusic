package cn.xybbz.config.module

import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.localdata.config.DatabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProxyConfigServerModule {


    @Singleton
    @Provides
    fun proxyConfigServer(
        db: DatabaseClient
    ): ProxyConfigServer {
        val proxyConfigServer = ProxyConfigServer(db)
        return proxyConfigServer
    }
}