package cn.xybbz.download.utils

import cn.xybbz.download.core.DownloadRequest
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 下载文件名解析测试。
 *
 * 覆盖非法字符清理、查询参数剥离、隐藏文件名兜底和重名冲突处理路径。
 */
class FileNameResolverTest {

    /**
     * 文件名中的查询参数和系统非法字符应被清理，最终路径使用安全文件名。
     */
    @Test
    fun resolveSanitizesIllegalCharactersAndDropsQuery() = runBlocking {
        val root = createTempDirectory()

        try {
            val resolvedPath = FileNameResolver.resolve(
                request = request(fileName = "track:name.mp3?token=secret"),
                globalFinalDir = root.absolutePath,
            )

            assertEquals("track_name.mp3", resolvedPath.fileName)
            assertTrue(resolvedPath.finalPath.endsWith("/track_name.mp3"))
            assertTrue(File(resolvedPath.finalPath).exists())
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 点开头文件名不应保留隐藏文件语义，避免下载结果在部分系统中不可见。
     */
    @Test
    fun resolveRemovesLeadingDotFromReservedFileName() = runBlocking {
        val root = createTempDirectory()

        try {
            val resolvedPath = FileNameResolver.resolve(
                request = request(fileName = ".mp3"),
                globalFinalDir = root.absolutePath,
            )

            assertEquals("mp3", resolvedPath.fileName)
            assertTrue(File(resolvedPath.finalPath).exists())
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 同名文件已存在时应追加序号，避免覆盖用户已有文件或并发任务占位文件。
     */
    @Test
    fun resolveAppendsCounterWhenFileAlreadyExists() = runBlocking {
        val root = createTempDirectory()
        File(root, "song.mp3").writeText("existing")

        try {
            val resolvedPath = FileNameResolver.resolve(
                request = request(fileName = "song.mp3"),
                globalFinalDir = root.absolutePath,
            )

            assertEquals("song(1).mp3", resolvedPath.fileName)
            assertTrue(File(resolvedPath.finalPath).exists())
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 过长文件名应保留扩展名并截断主体，避免超过下载文件名长度限制。
     */
    @Test
    fun resolveTruncatesLongNameAndKeepsExtension() = runBlocking {
        val root = createTempDirectory()
        val longName = "${"a".repeat(180)}.flac"

        try {
            val resolvedPath = FileNameResolver.resolve(
                request = request(fileName = longName),
                globalFinalDir = root.absolutePath,
            )

            assertEquals(128, resolvedPath.fileName.length)
            assertTrue(resolvedPath.fileName.endsWith(".flac"))
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 创建隔离的测试临时目录。
     */
    private fun createTempDirectory(): File {
        return Files.createTempDirectory("xy-download-name-test").toFile()
    }

    /**
     * 创建最小下载请求，便于不同文件名分支复用。
     */
    private fun request(fileName: String): DownloadRequest {
        return DownloadRequest(
            url = "https://demo.test/$fileName",
            fileName = fileName,
            fileSize = 1L,
            type = "music",
        )
    }
}
