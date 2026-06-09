package cn.xybbz.di

import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.music.JvmMusicController
import cn.xybbz.platform.ContextWrapper
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
actual class MusicControllerModule {
    @Singleton
    actual fun musicController(
        wrapper: ContextWrapper
    ): MusicCommonController {
        // 这里只创建轻量控制器对象，不做 VLC native 发现；真正播放器初始化由 StartupViewModel 后台触发。
        return JvmMusicController()
    }
}
