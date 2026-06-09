package cn.xybbz.di

import cn.xybbz.config.network.NetWorkMonitor
import cn.xybbz.platform.ContextWrapper
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

expect class NetworkModule() {

    @Singleton
    fun netWorkMonitor(contextWrapper: ContextWrapper): NetWorkMonitor
}
