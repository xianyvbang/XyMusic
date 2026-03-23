package cn.xybbz.di

import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.music.MusicController
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import org.koin.core.scope.Scope

@Module
@Configuration
actual class MusicControllerModule {
    @Singleton
    actual fun musicController(wrapper: ContextWrapper, scope: Scope): MusicCommonController {
        return MusicController(
            wrapper.context,
            scope.get(),
            scope.get(),
            scope.get(),
            scope.get()
        )
    }
}