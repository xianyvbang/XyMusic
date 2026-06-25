package cn.xybbz.music

import cn.xybbz.platform.ContextWrapper
import java.io.File

/**
 * JVM 播放缓存目录解析工具。
 */
internal object JvmPlaybackCacheDirectories {
    /**
     * JVM 播放缓存默认目录名。
     */
    private const val CACHE_DIRECTORY_NAME = "jvm-playback-cache"

    /**
     * 解析默认播放缓存目录。
     *
     * @param contextWrapper JVM 平台上下文。
     * @return 位于可重建缓存根目录下的播放缓存目录。
     */
    fun defaultCacheDirectory(contextWrapper: ContextWrapper): File {
        return File(contextWrapper.cacheDirectory, CACHE_DIRECTORY_NAME).normalizedDirectory()
    }
}

/**
 * 规范化 JVM 播放缓存目录路径。
 */
private fun File.normalizedDirectory(): File {
    return runCatching {
        canonicalFile
    }.getOrDefault(absoluteFile)
}
