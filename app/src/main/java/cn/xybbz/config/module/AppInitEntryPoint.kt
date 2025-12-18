package cn.xybbz.config.module

import cn.xybbz.config.download.DownLoadManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppInitEntryPoint {
    fun downLoadManager(): DownLoadManager
}
