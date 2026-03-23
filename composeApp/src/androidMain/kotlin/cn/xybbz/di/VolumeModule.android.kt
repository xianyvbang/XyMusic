package cn.xybbz.di

import cn.xybbz.config.volume.VolumeAndroidServer
import cn.xybbz.config.volume.VolumeServer
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
actual class VolumeModule {
    @Singleton
    actual fun volumeServer(contextWrapper: ContextWrapper): VolumeServer {
        val volumeAndroidServer = VolumeAndroidServer(contextWrapper.context)
        volumeAndroidServer.createVolumeManager()
        return volumeAndroidServer
    }
}