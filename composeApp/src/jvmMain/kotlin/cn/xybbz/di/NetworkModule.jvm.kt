package cn.xybbz.di

import cn.xybbz.config.network.JvmNetWorkMonitor
import cn.xybbz.config.network.NetWorkMonitor
import cn.xybbz.platform.ContextWrapper
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
actual class NetworkModule actual constructor() {

    @Singleton
    actual fun netWorkMonitor(contextWrapper: ContextWrapper): NetWorkMonitor {
        return JvmNetWorkMonitor()
    }
}
