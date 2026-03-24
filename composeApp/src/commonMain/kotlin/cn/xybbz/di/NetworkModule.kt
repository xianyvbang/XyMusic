package cn.xybbz.di

import cn.xybbz.config.network.NetWorkMonitor
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import org.koin.core.scope.Scope

expect class NetworkModule() {

    @Singleton
    fun netWorkMonitor(scope: Scope): NetWorkMonitor
}
