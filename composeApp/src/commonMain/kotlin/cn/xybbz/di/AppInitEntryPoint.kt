package cn.xybbz.di

import cn.xybbz.config.download.DownLoadManager
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
interface AppInitEntryPoint {
    @Singleton
    fun downLoadManager(): DownLoadManager
}
