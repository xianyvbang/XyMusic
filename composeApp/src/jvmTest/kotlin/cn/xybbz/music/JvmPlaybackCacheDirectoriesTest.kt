package cn.xybbz.music

import cn.xybbz.platform.ContextWrapper
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * JVM 播放缓存目录解析测试。
 */
class JvmPlaybackCacheDirectoriesTest {
    /**
     * 默认播放缓存目录应位于 ContextWrapper 的可重建缓存根目录下。
     */
    @Test
    fun defaultCacheDirectoryUsesContextCacheDirectory() {
        val root = Files.createTempDirectory("xy-playback-cache-directory-test").toFile()
        val contextWrapper = ContextWrapper.createForTest(
            environment = mapOf(
                "XDG_DATA_HOME" to File(root, "data").absolutePath,
                "XDG_CACHE_HOME" to File(root, "cache").absolutePath,
            ),
            osName = "Linux",
            userHomeDirectory = File(root, "home"),
        )

        try {
            assertEquals(
                File(contextWrapper.cacheDirectory, "jvm-playback-cache").canonicalFile,
                JvmPlaybackCacheDirectories.defaultCacheDirectory(contextWrapper),
            )
        } finally {
            root.deleteRecursively()
        }
    }
}
