package cn.xybbz.config.initializers

import android.content.Context
import androidx.startup.Initializer
import cn.xybbz.config.module.AppInitEntryPoint
import cn.xybbz.config.proxy.ProxyConfigServer
import dagger.hilt.android.EntryPointAccessors

class ProxyConfigServerInitializer : Initializer<ProxyConfigServer> {
    override fun create(context: Context): ProxyConfigServer {
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            AppInitEntryPoint::class.java
        )
        val proxyConfigServer = entryPoint.proxyConfigServer()
        proxyConfigServer.initConfig()
        return proxyConfigServer
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return emptyList()
    }
}