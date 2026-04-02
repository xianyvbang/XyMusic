package cn.xybbz.di

import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.localdata.config.LocalDatabaseClient
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
class ProxyConfigServerModule {


    @Singleton
    fun proxyConfigServer(
        db: LocalDatabaseClient
    ): ProxyConfigServer {
        val proxyConfigServer = ProxyConfigServer(db)
        return proxyConfigServer
    }
}