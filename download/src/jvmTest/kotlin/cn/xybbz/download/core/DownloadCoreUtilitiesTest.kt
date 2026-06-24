package cn.xybbz.download.core

import cn.xybbz.config.download.state.DownloadState
import cn.xybbz.database.getJvmDatabaseFiles
import cn.xybbz.database.getRoomDatabase
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.download.internal.DownloadIoScoped
import cn.xybbz.download.utils.deleteFileIfEmptyWithFileKit
import cn.xybbz.download.utils.deleteFileWithFileKit
import cn.xybbz.download.utils.fileLengthWithFileKit
import cn.xybbz.download.utils.moveFileWithFileKit
import cn.xybbz.download.utils.playableDownloadFilePath
import cn.xybbz.platform.ContextWrapper
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
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
     * 本地播放路径只接受仍存在且非空的下载文件。
     */
    @Test
    fun playableDownloadFilePathRequiresExistingNonEmptyFile() {
        val root = createTempDirectory()
        val emptyFile = File(root, "empty.mp3").apply { writeBytes(ByteArray(0)) }
        val musicFile = File(root, "song.mp3").apply { writeText("music") }
        val missingFile = File(root, "missing.mp3")

        try {
            assertEquals(null, playableDownloadFilePath(null))
            assertEquals(null, playableDownloadFilePath(""))
            assertEquals(null, playableDownloadFilePath(missingFile.absolutePath))
            assertEquals(null, playableDownloadFilePath(emptyFile.absolutePath))
            assertEquals(musicFile.absolutePath, playableDownloadFilePath(musicFile.absolutePath))
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
     * 下载空间保护应对非正数需求跳过强校验，并按 50MB 下限计算安全余量。
     */
    @Test
    fun downloadStorageGuardCalculatesReserveAndSkipsUnknownSize() {
        val contextWrapper = ContextWrapper()
        val root = createTempDirectory()

        try {
            DownloadStorageGuard.ensureEnoughSpace(
                path = root.absolutePath,
                needBytes = 0L,
                contextWrapper = contextWrapper,
            )
            assertEquals(
                60L * 1024L * 1024L,
                DownloadStorageGuard.requiredBytesWithReserve(10L * 1024L * 1024L),
            )
            assertEquals(
                21L * 1024L * 1024L * 1024L,
                DownloadStorageGuard.requiredBytesWithReserve(20L * 1024L * 1024L * 1024L),
            )
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * JVM 可用空间查询应支持尚未创建的目标文件路径。
     */
    @Test
    fun usableSpaceFallsBackToExistingParentDirectory() {
        val contextWrapper = ContextWrapper()
        val root = createTempDirectory()
        val missingTarget = File(root, "nested/song.tmp")

        try {
            val usableSpace = DownloadPlatformFiles.usableSpace(
                path = missingTarget.absolutePath,
                contextWrapper = contextWrapper,
            )

            assertTrue(usableSpace > 0L)
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 写入入口在预期文件远超磁盘容量时应提前失败，避免真正开始写满磁盘。
     */
    @Test
    fun writeResponseToFileFailsBeforeWritingWhenExpectedSizeExceedsUsableSpace() = runBlocking {
        val contextWrapper = ContextWrapper()
        val root = createTempDirectory()
        val target = File(root, "huge.tmp")
        val expectedTotalBytes = DownloadPlatformFiles.usableSpace(
            path = target.absolutePath,
            contextWrapper = contextWrapper,
        ) + DownloadStorageGuard.MIN_RESERVED_SPACE_BYTES + 1L

        try {
            assertFailsWith<InsufficientStorageException> {
                DownloadPlatformFiles.writeResponseToFile(
                    path = target.absolutePath,
                    startOffset = 0L,
                    contextWrapper = contextWrapper,
                    expectedTotalBytes = expectedTotalBytes,
                    source = ByteReadChannel(ByteArray(1)),
                ) {
                    DownloadStatus.DOWNLOADING
                }
            }
            assertFalse(target.exists())
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
     * 同步删除入口应删除只存在于数据库中的已完成任务和对应磁盘文件。
     */
    @Test
    fun deleteAndAwaitDeletesCompletedTaskFilesFromDatabaseRecord() = runBlocking {
        val root = createTempDirectory()
        val dbName = createTestDatabaseName()
        cleanupTestDatabase(dbName)
        val db = createTestDownloadDatabase(dbName)
        val downloader = createTestDownloader(db, root)
        val finalFile = File(root, "song.mp3").apply { writeText("music") }
        val tempFile = File(root, "song.tmp").apply { writeText("partial") }

        try {
            val downloadId = insertDownloadTask(
                db = db,
                finalFile = finalFile,
                tempFile = tempFile,
                status = DownloadStatus.COMPLETED,
            )

            downloader.deleteAndAwait(downloadId)

            assertFalse(finalFile.exists())
            assertFalse(tempFile.exists())
            assertEquals(null, db.downloadDao.selectById(downloadId))
        } finally {
            downloader.close()
            db.close()
            root.deleteRecursively()
            cleanupTestDatabase(dbName)
        }
    }

    /**
     * 保留文件删除模式应只清理临时文件和数据库记录，不删除非空最终文件。
     */
    @Test
    fun deleteAndAwaitKeepsFinalFileWhenDeleteFileFalse() = runBlocking {
        val root = createTempDirectory()
        val dbName = createTestDatabaseName()
        cleanupTestDatabase(dbName)
        val db = createTestDownloadDatabase(dbName)
        val downloader = createTestDownloader(db, root)
        val finalFile = File(root, "kept.mp3").apply { writeText("music") }
        val tempFile = File(root, "kept.tmp").apply { writeText("partial") }

        try {
            val downloadId = insertDownloadTask(
                db = db,
                finalFile = finalFile,
                tempFile = tempFile,
                status = DownloadStatus.COMPLETED,
            )

            downloader.deleteAndAwait(downloadId, deleteFile = false)

            assertTrue(finalFile.exists())
            assertFalse(tempFile.exists())
            assertEquals("music", finalFile.readText())
            assertEquals(null, db.downloadDao.selectById(downloadId))
        } finally {
            downloader.close()
            db.close()
            root.deleteRecursively()
            cleanupTestDatabase(dbName)
        }
    }

    /**
     * 按歌曲 ID 查询下载任务时应限定当前媒体库并排除 APK 类型。
     */
    @Test
    fun getMusicTasksByUidsFiltersMediaLibraryAndApkTasks() = runBlocking {
        val root = createTempDirectory()
        val dbName = createTestDatabaseName()
        cleanupTestDatabase(dbName)
        val db = createTestDownloadDatabase(dbName)

        try {
            val expectedOne = insertDownloadTask(
                db = db,
                finalFile = File(root, "song-one.mp3"),
                tempFile = File(root, "song-one.tmp"),
                uid = "song-one",
                typeData = "SUBSONIC",
                mediaLibraryId = "1",
                status = DownloadStatus.FAILED,
            )
            val expectedTwo = insertDownloadTask(
                db = db,
                finalFile = File(root, "song-two.mp3"),
                tempFile = File(root, "song-two.tmp"),
                uid = "song-two",
                typeData = "EMBY",
                mediaLibraryId = "1",
            )
            insertDownloadTask(
                db = db,
                finalFile = File(root, "apk.bin"),
                tempFile = File(root, "apk.tmp"),
                uid = "song-one",
                typeData = "APK",
                mediaLibraryId = "1",
            )
            insertDownloadTask(
                db = db,
                finalFile = File(root, "other-library.mp3"),
                tempFile = File(root, "other-library.tmp"),
                uid = "song-one",
                typeData = "SUBSONIC",
                mediaLibraryId = "2",
            )

            val taskIds = db.downloadDao.getMusicTasksByUids(
                musicIds = listOf("song-one", "song-two"),
                notTypeData = "APK",
                mediaLibraryId = "1",
            ).map { it.id }.toSet()

            assertEquals(setOf(expectedOne, expectedTwo), taskIds)
        } finally {
            db.close()
            root.deleteRecursively()
            cleanupTestDatabase(dbName)
        }
    }

    /**
     * 创建隔离的测试临时目录。
     */
    private fun createTempDirectory(): File {
        return Files.createTempDirectory("xy-download-core-test").toFile()
    }

    /**
     * 创建隔离的下载数据库文件名，避免测试之间共享下载任务。
     */
    private fun createTestDatabaseName(): String {
        return "xy-download-test-${System.nanoTime()}.db"
    }

    /**
     * 创建 JVM 下载数据库实例。
     */
    private fun createTestDownloadDatabase(dbName: String): DownloadDatabaseClient {
        return getRoomDatabase(dbName, ContextWrapper())
    }

    /**
     * 删除测试数据库及其伴生 WAL 文件。
     */
    private fun cleanupTestDatabase(dbName: String) {
        getJvmDatabaseFiles(dbName).forEach { file ->
            if (file.exists()) {
                file.delete()
            }
        }
    }

    /**
     * 创建只用于删除路径测试的下载器实例。
     */
    private fun createTestDownloader(db: DownloadDatabaseClient, root: File): DownloadImpl {
        val contextWrapper = ContextWrapper()
        val dispatcher = DownloadDispatcherImpl(
            contextWrapper = contextWrapper,
            db = db,
            config = DownloaderConfig.Builder(contextWrapper)
                .setFinalDirectory(root.absolutePath)
                .build(),
        )
        return DownloadImpl(dispatcher, db)
    }

    /**
     * 插入一条下载任务并返回数据库生成的任务 ID。
     */
    private suspend fun insertDownloadTask(
        db: DownloadDatabaseClient,
        finalFile: File,
        tempFile: File,
        uid: String = "song-id",
        typeData: String = "SUBSONIC",
        mediaLibraryId: String = "1",
        status: DownloadStatus = DownloadStatus.COMPLETED,
    ): Long {
        return db.downloadDao.insert(
            XyDownload(
                url = "https://example.test/${finalFile.name}",
                fileName = finalFile.name,
                filePath = finalFile.absolutePath,
                fileSize = finalFile.length(),
                tempFilePath = tempFile.absolutePath,
                typeData = typeData,
                status = status,
                uid = uid,
                mediaLibraryId = mediaLibraryId,
            )
        ).single()
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
