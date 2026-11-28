package cn.xybbz.music

import cn.xybbz.platform.ContextWrapper
import cn.xybbz.platform.PlatformCacheDirectoryNames
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
            properties = mapOf(
                ContextWrapper.PACKAGE_NAME_PROPERTY to ContextWrapper.DEFAULT_APP_NAME,
                ContextWrapper.DATA_ROOT_PROPERTY to File(root, "XyMusicData").absolutePath,
            ),
        )

        try {
            assertEquals(
                File(contextWrapper.cacheDirectory, PlatformCacheDirectoryNames.SONG).canonicalFile,
                JvmPlaybackCacheDirectories.defaultCacheDirectory(contextWrapper),
            )
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 相同目录和父子目录都应被识别为不可迁移。
     */
    @Test
    fun isSameOrNestedDetectsSameAndNestedDirectories() {
        val root = Files.createTempDirectory("xy-cache-nested-test").toFile()
        try {
            val parent = File(root, "parent")
            val child = File(parent, "child")
            val sibling = File(root, "sibling")
            parent.mkdirs()
            child.mkdirs()
            sibling.mkdirs()

            assertTrue(JvmPlaybackCacheDirectories.isSameOrNested(parent, parent))
            assertTrue(JvmPlaybackCacheDirectories.isSameOrNested(parent, child))
            assertTrue(JvmPlaybackCacheDirectories.isSameOrNested(child, parent))
            assertFalse(JvmPlaybackCacheDirectories.isSameOrNested(parent, sibling))
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 目录大小应累加所有文件。
     */
    @Test
    fun directorySizeBytesSumsAllFiles() {
        val root = Files.createTempDirectory("xy-cache-size-test").toFile()
        try {
            File(root, "a.cache").writeBytes(ByteArray(10))
            val nested = File(root, "nested")
            nested.mkdirs()
            File(nested, "b.cache").writeBytes(ByteArray(20))

            assertEquals(30L, JvmPlaybackCacheDirectories.directorySizeBytes(root))
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 已存在目录应能读取到可用空间。
     */
    @Test
    fun freeSpaceBytesReturnsPositiveForExistingDirectory() {
        val root = Files.createTempDirectory("xy-cache-free-space-test").toFile()
        try {
            assertTrue(JvmPlaybackCacheDirectories.freeSpaceBytes(root) > 0L)
        } finally {
            root.deleteRecursively()
        }
    }
}
