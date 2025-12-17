package cn.xybbz.config.module

import cn.xybbz.config.SettingsConfig
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.proxy.ProxyConfigServer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppInitEntryPoint {
    fun proxyConfigServer(): ProxyConfigServer
    fun downLoadManager(): DownLoadManager
    fun settingsConfig(): SettingsConfig
}
