package cn.xybbz.di

import cn.xybbz.platform.ContextWrapper
import cn.xybbz.platform.PlatformCacheDirectoryNames
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
        // 沿用 Sketch 默认 appCacheDirectory 逻辑（externalCacheDir 优先，否则 cacheDir），
        // 仅把默认的 sketch4 目录名替换为 assets。
        val appCacheDirectory =
            contextWrapper.context.externalCacheDir ?: contextWrapper.context.cacheDir
        val imageCacheRoot = File(appCacheDirectory, PlatformCacheDirectoryNames.ASSETS)
        return Sketch.Builder(contextWrapper.context).apply {
            // 生产环境只保留错误日志，避免每张图片的调试日志刷屏拖慢加载。
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
