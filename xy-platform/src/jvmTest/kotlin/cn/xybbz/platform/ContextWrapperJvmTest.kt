package cn.xybbz.platform

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * JVM 平台上下文路径测试。
 */
class ContextWrapperJvmTest {
    /**
     * Windows 路径应使用 LOCALAPPDATA 保存应用数据，并使用 USERPROFILE 下的 Downloads 保存完成下载。
     */
    @Test
    fun windowsPathsUseLocalAppDataAndUserDownloads() {
        val root = createTempDirectory()
        val localAppData = File(root, "LocalAppData")
        val userProfile = File(root, "UserProfile")
        val contextWrapper = ContextWrapper.createForTest(
            environment = mapOf(
                "LOCALAPPDATA" to localAppData.absolutePath,
                "USERPROFILE" to userProfile.absolutePath,
            ),
            osName = "Windows 11",
            userHomeDirectory = File(root, "Home"),
        )

        try {
            assertEquals(File(localAppData, "XyMusic").absoluteFile, contextWrapper.dataDirectory)
            assertEquals(File(localAppData, "XyMusic/databases").absoluteFile, contextWrapper.databaseDirectory)
            assertEquals(File(localAppData, "XyMusic/cache").absoluteFile, contextWrapper.cacheDirectory)
            assertEquals(File(localAppData, "XyMusic/temp/xy-downloads").absoluteFile, contextWrapper.downloadTempDirectory)
            assertEquals(File(userProfile, "Downloads/XyMusic").absoluteFile, contextWrapper.downloadDirectory)
            assertEquals(contextWrapper.dataDirectory, contextWrapper.applicationDirectory)
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * macOS 路径应遵循 Application Support、Caches 和用户 Downloads 目录。
     */
    @Test
    fun macOsPathsUseLibraryDirectories() {
        val root = createTempDirectory()
        val home = File(root, "Home")
        val contextWrapper = ContextWrapper.createForTest(
            environment = emptyMap(),
            osName = "Mac OS X",
            userHomeDirectory = home,
        )

        try {
            assertEquals(File(home, "Library/Application Support/XyMusic").absoluteFile, contextWrapper.dataDirectory)
            assertEquals(File(home, "Library/Application Support/XyMusic/databases").absoluteFile, contextWrapper.databaseDirectory)
            assertEquals(File(home, "Library/Caches/XyMusic").absoluteFile, contextWrapper.cacheDirectory)
            assertEquals(File(home, "Library/Application Support/XyMusic/temp/xy-downloads").absoluteFile, contextWrapper.downloadTempDirectory)
            assertEquals(File(home, "Downloads/XyMusic").absoluteFile, contextWrapper.downloadDirectory)
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * Linux 路径应优先使用 XDG_DATA_HOME 和 XDG_CACHE_HOME。
     */
    @Test
    fun linuxPathsUseXdgDirectories() {
        val root = createTempDirectory()
        val dataHome = File(root, "data-home")
        val cacheHome = File(root, "cache-home")
        val home = File(root, "Home")
        val contextWrapper = ContextWrapper.createForTest(
            environment = mapOf(
                "XDG_DATA_HOME" to dataHome.absolutePath,
                "XDG_CACHE_HOME" to cacheHome.absolutePath,
            ),
            osName = "Linux",
            userHomeDirectory = home,
        )

        try {
            assertEquals(File(dataHome, "xymusic").absoluteFile, contextWrapper.dataDirectory)
            assertEquals(File(dataHome, "xymusic/databases").absoluteFile, contextWrapper.databaseDirectory)
            assertEquals(File(cacheHome, "xymusic").absoluteFile, contextWrapper.cacheDirectory)
            assertEquals(File(dataHome, "xymusic/temp/xy-downloads").absoluteFile, contextWrapper.downloadTempDirectory)
            assertEquals(File(home, "Downloads/XyMusic").absoluteFile, contextWrapper.downloadDirectory)
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * Linux 未配置 XDG 变量时应回退到用户目录下的标准隐藏目录。
     */
    @Test
    fun linuxPathsFallbackToUserHomeWhenXdgMissing() {
        val root = createTempDirectory()
        val home = File(root, "Home")
        val contextWrapper = ContextWrapper.createForTest(
            environment = emptyMap(),
            osName = "Linux",
            userHomeDirectory = home,
        )

        try {
            assertEquals(File(home, ".local/share/xymusic").absoluteFile, contextWrapper.dataDirectory)
            assertEquals(File(home, ".cache/xymusic").absoluteFile, contextWrapper.cacheDirectory)
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 创建 JVM 路径测试临时目录。
     */
    private fun createTempDirectory(): File {
        return Files.createTempDirectory("xy-platform-context-test").toFile()
    }
}
