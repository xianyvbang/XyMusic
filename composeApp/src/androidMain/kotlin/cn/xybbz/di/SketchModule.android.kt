package cn.xybbz.di

import cn.xybbz.api.client.DataSourceManager
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
import org.koin.mp.KoinPlatform

@Module
@Configuration
actual class SketchModule {
    @Single
    actual fun sketch(dataSourceManager: DataSourceManager): Sketch {
        return Sketch.Builder(KoinPlatform.getKoin().get()).apply {
            logger(level = Logger.Level.Debug)
            addIgnoreFetcherProvider(KtorHttpUriFetcherProvider::class)
            addComponents {
                val httpStack = KtorStack(dataSourceManager.getHttpClient())
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