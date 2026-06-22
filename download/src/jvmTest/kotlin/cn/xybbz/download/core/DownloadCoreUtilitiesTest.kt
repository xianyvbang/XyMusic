package cn.xybbz.download.core

import cn.xybbz.config.download.state.DownloadState
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.download.internal.DownloadIoScoped
import cn.xybbz.download.utils.deleteFileIfEmptyWithFileKit
import cn.xybbz.download.utils.deleteFileWithFileKit
import cn.xybbz.download.utils.fileLengthWithFileKit
import cn.xybbz.download.utils.moveFileWithFileKit
import cn.xybbz.platform.ContextWrapper
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 下载核心工具测试。
 *
 * 覆盖下载配置、状态对象和 JVM 普通文件操作路径。
 */
class DownloadCoreUtilitiesTest {

    /**
     * 下载配置构造器应使用默认并发数，并允许调用方覆盖目录和并发数。
     */
    @Test
    fun downloaderConfigBuilderUsesDefaultsAndOverrides() {
        val contextWrapper = ContextWrapper()
        val defaultConfig = DownloaderConfig.Builder(contextWrapper = contextWrapper).build()
        val customConfig = DownloaderConfig.Builder(contextWrapper = contextWrapper)
            .setMaxConcurrentDownloads(5)
            .setFinalDirectory("/tmp/music")
            .build()

        assertEquals(3, defaultConfig.maxConcurrentDownloads)
        assertTrue(defaultConfig.finalDirectory.isNotBlank())
        assertEquals(5, customConfig.maxConcurrentDownloads)
        assertEquals("/tmp/music", customConfig.finalDirectory)
    }

    /**
     * 下载状态对象应保留进度、错误原因和枚举顺序。
     */
    @Test
    fun downloadStateAndStatusKeepBusinessValues() {
        val state = DownloadState.InProgress(
            progress = 0.5f,
            downloadedBytes = 50,
            totalBytes = 100,
            speedBps = 1024,
        )
        val cause = IllegalStateException("network")
        val error = DownloadState.Error(message = "failed", cause = cause)

        assertEquals(0.5f, state.progress)
        assertEquals(50, state.downloadedBytes)
        assertEquals("failed", error.message)
        assertEquals(cause, error.cause)
        assertEquals(DownloadStatus.QUEUED, DownloadStatus.valueOf("QUEUED"))
        assertEquals("download_id", DownloadConstants.WORK_INPUT_DOWNLOAD_ID)
    }

    /**
     * 文件长度与删除工具应正确处理空路径、不存在文件和普通文件。
     */
    @Test
    fun fileLengthAndDeleteHandleBlankMissingAndExistingFiles() = runBlocking {
        val root = createTempDirectory()
        val file = File(root, "song.tmp").apply { writeText("abc") }

        try {
            assertEquals(0L, fileLengthWithFileKit(""))
            assertEquals(3L, fileLengthWithFileKit(file.absolutePath))
            assertFalse(deleteFileWithFileKit(""))
            assertTrue(deleteFileWithFileKit(file.absolutePath))
            assertFalse(file.exists())
            assertFalse(deleteFileWithFileKit(file.absolutePath))
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 只删除空占位文件的工具不应误删已有内容的下载文件。
     */
    @Test
    fun deleteFileIfEmptyOnlyDeletesZeroLengthFiles() = runBlocking {
        val root = createTempDirectory()
        val emptyFile = File(root, "empty.tmp").apply { writeBytes(ByteArray(0)) }
        val nonEmptyFile = File(root, "full.tmp").apply { writeText("data") }

        try {
            assertTrue(deleteFileIfEmptyWithFileKit(emptyFile.absolutePath))
            assertFalse(emptyFile.exists())
            assertFalse(deleteFileIfEmptyWithFileKit(nonEmptyFile.absolutePath))
            assertTrue(nonEmptyFile.exists())
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * JVM 文件移动应创建目标目录，并在目标占位文件存在时覆盖写入。
     */
    @Test
    fun moveFileWithFileKitMovesIntoTargetDirectory() = runBlocking {
        val root = createTempDirectory()
        val source = File(root, "source.tmp").apply { writeText("music") }
        val target = File(root, "nested/song.tmp").apply {
            parentFile.mkdirs()
            writeText("")
        }

        try {
            val movedPath = moveFileWithFileKit(
                sourcePath = source.absolutePath,
                finalPath = target.absolutePath,
            )

            assertFalse(source.exists())
            assertEquals("music", File(movedPath).readText())
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 下载 IO 作用域关闭后应取消内部协程作用域。
     */
    @Test
    fun downloadIoScopedCloseCancelsScope() {
        val scoped = TestDownloadIoScoped()

        scoped.createScope()
        assertTrue(scoped.scopeJob().isActive)

        scoped.close()

        assertFalse(scoped.scopeJob().isActive)
    }

    /**
     * 创建隔离的测试临时目录。
     */
    private fun createTempDirectory(): File {
        return Files.createTempDirectory("xy-download-core-test").toFile()
    }

    /**
     * 暴露受保护 scope 的测试专用实现。
     */
    private class TestDownloadIoScoped : DownloadIoScoped() {
        /**
         * 获取内部协程作用域的 Job，便于断言 close 后是否取消。
         */
        fun scopeJob(): Job {
            return requireNotNull(scope.coroutineContext[Job])
        }
    }
}
