package cn.xybbz.di

import cn.xybbz.config.network.AndroidNetWorkMonitor
import cn.xybbz.config.network.NetWorkMonitor
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import org.koin.core.scope.Scope
import org.koin.core.scope.get

@Module
@Configuration
actual class NetworkModule actual constructor() {

    @Singleton
    actual fun netWorkMonitor(scope: Scope): NetWorkMonitor {
        return AndroidNetWorkMonitor(scope.get<ContextWrapper>().context.applicationContext)
    }
}
