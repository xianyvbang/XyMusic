package cn.xybbz.di

import cn.xybbz.platform.ContextWrapper
import cn.xybbz.platform.PlatformCacheDirectoryNames
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.DiskCache
import com.github.panpf.sketch.fetch.KtorHttpUriFetcher
import com.github.panpf.sketch.fetch.internal.KtorHttpUriFetcherProvider
import com.github.panpf.sketch.http.KtorStack
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.PauseLoadWhenScrollingInterceptor
import com.github.panpf.sketch.util.Logger
import okio.Path.Companion.toOkioPath
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import java.io.File

@Module
@Configuration
actual class SketchModule {
    @Single
    actual fun sketch(contextWrapper: ContextWrapper): Sketch {
        // 图片缓存统一放到可重建缓存根目录下的 assets。
        val imageCacheRoot = File(contextWrapper.cacheDirectory, PlatformCacheDirectoryNames.ASSETS)
        return Sketch.Builder(PlatformContext.INSTANCE).apply {
            // 生产环境只保留错误日志，避免每张图片的调试日志同步刷控制台拖慢加载。
            logger(level = Logger.Level.Error)
            addIgnoreFetcherProvider(KtorHttpUriFetcherProvider::class)
            addComponents {
                // Sketch 在首页首帧前就可能创建，不能依赖延后恢复的数据源 HttpClient。
                val httpStack = KtorStack(createSketchHttpClient())
                addFetcher(KtorHttpUriFetcher.Factory(httpStack))
                addInterceptor(PauseLoadWhenScrollingInterceptor())
            }
            // 下载缓存与结果缓存分目录存放，避免缓存 key 冲突。
            downloadCacheOptions(
                DiskCache.Options(directory = File(imageCacheRoot, "download").toOkioPath())
            )
            resultCacheOptions(
                DiskCache.Options(directory = File(imageCacheRoot, "result").toOkioPath())
            )
            globalImageOptions(ImageOptions {
                crossfade()
                // more ...
            })
        }.build()
    }
}
