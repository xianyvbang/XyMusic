package cn.xybbz.di

import cn.xybbz.config.volume.VolumeIosServer
import cn.xybbz.config.volume.VolumeServer
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
actual class VolumeModule {
    @Singleton
    actual fun volumeServer(contextWrapper: ContextWrapper): VolumeServer {
        return VolumeIosServer()
    }
}
