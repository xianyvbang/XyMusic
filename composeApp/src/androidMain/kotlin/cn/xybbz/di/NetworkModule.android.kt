package cn.xybbz.di

import cn.xybbz.config.network.AndroidNetWorkMonitor
import cn.xybbz.config.network.NetWorkMonitor
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
actual class NetworkModule actual constructor() {

    @Singleton
    actual fun netWorkMonitor(contextWrapper: ContextWrapper): NetWorkMonitor {
        return AndroidNetWorkMonitor(contextWrapper.context.applicationContext)
    }
}
