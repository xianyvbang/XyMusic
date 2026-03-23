package cn.xybbz.di

import cn.xybbz.config.lrc.LrcServer
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import org.koin.core.scope.Scope

@Module
@Configuration
class LrcModule {

    @Singleton
    fun lrcServer(
        scope: Scope
    ): LrcServer {
        val lrcServer = LrcServer(
            scope.get(),
            scope.get(),
            scope.get(),
            scope.get(),
            scope.get()
        )
        return lrcServer
    }
}
