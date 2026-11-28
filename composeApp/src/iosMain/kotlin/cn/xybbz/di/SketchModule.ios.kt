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
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Module
@Configuration
actual class SketchModule {
    @Single
    actual fun sketch(contextWrapper: ContextWrapper): Sketch {
        // 图片缓存放到应用 Caches 目录下的 assets。
        val imageCacheRoot = iosCachesDirectory() + "/" + PlatformCacheDirectoryNames.ASSETS
        return Sketch.Builder(PlatformContext.INSTANCE).apply {
            // 生产环境只保留错误日志，避免每张图片的调试日志刷控制台拖慢加载。
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
                DiskCache.Options(directory = "$imageCacheRoot/download".toPath())
            )
            resultCacheOptions(
                DiskCache.Options(directory = "$imageCacheRoot/result".toPath())
            )
            globalImageOptions(ImageOptions {
                crossfade()
                // more ...
            })
        }.build()
    }
}

/**
 * 获取 iOS 应用 Caches 目录。
 *
 * @return Caches 目录的绝对路径。
 */
@OptIn(ExperimentalForeignApi::class)
private fun iosCachesDirectory(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSCachesDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(url?.path)
}
