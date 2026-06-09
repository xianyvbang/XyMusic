package cn.xybbz.di

import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.fetch.KtorHttpUriFetcher
import com.github.panpf.sketch.fetch.internal.KtorHttpUriFetcherProvider
import com.github.panpf.sketch.http.KtorStack
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.PauseLoadWhenScrollingInterceptor
import com.github.panpf.sketch.util.Logger
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
actual class SketchModule {
    @Single
    actual fun sketch(): Sketch {
        return Sketch.Builder(PlatformContext.INSTANCE).apply {
            logger(level = Logger.Level.Debug)
            addIgnoreFetcherProvider(KtorHttpUriFetcherProvider::class)
            addComponents {
                // Sketch 在首页首帧前就可能创建，不能依赖延后恢复的数据源 HttpClient。
                val httpStack = KtorStack(createSketchHttpClient())
                addFetcher(KtorHttpUriFetcher.Factory(httpStack))
                addInterceptor(PauseLoadWhenScrollingInterceptor())
            }
            globalImageOptions(ImageOptions {
                crossfade()
                // more ...
            })
        }.build()
    }
}
