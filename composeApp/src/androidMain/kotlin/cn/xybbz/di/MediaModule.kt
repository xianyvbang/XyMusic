package cn.xybbz.di

import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.media.MediaServer
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
object MediaModule {

    @Singleton
    fun mediaServer(
        lrcServer: LrcServer
    ): MediaServer {
        val mediaServer = MediaServer(lrcServer)
        return mediaServer
    }
}