package cn.xybbz.di

import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.localdata.config.DatabaseClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
object ProxyConfigServerModule {


    @Singleton
    fun proxyConfigServer(
        db: DatabaseClient
    ): ProxyConfigServer {
        val proxyConfigServer = ProxyConfigServer(db)
        return proxyConfigServer
    }
}