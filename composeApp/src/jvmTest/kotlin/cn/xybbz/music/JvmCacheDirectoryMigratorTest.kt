package cn.xybbz.music

import cn.xybbz.config.music.CacheMigrationPhase
import cn.xybbz.config.music.CacheMigrationProgress
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * JVM 缓存目录迁移工具测试。
 */
class JvmCacheDirectoryMigratorTest {

    /**
     * 同卷迁移应搬走条目内容，同时保留旧缓存根目录。
     */
    @Test
    fun moveOrCopyKeepsSourceDirectory() = runBlocking {
        val root = Files.createTempDirectory("xy-cache-migrator-test").toFile()
        try {
            val source = File(root, "source")
            val target = File(root, "target")
            val sourceSong = File(source, "song")
            sourceSong.mkdirs()
            File(sourceSong, "index.json").writeText("{}")
            File(sourceSong, "span-0-9.cache").writeBytes(ByteArray(10))
            target.mkdirs()

            val progress = mutableListOf<CacheMigrationProgress>()
            val migrated = JvmCacheDirectoryMigrator.moveOrCopy(
                sourceDirectory = source,
                targetDirectory = target,
                totalBytes = 12L,
                onProgress = { item -> progress += item },
            )

            assertTrue(migrated)
            // 旧缓存根目录必须保留，只清空其中的条目。
            assertTrue(source.exists())
            assertTrue(source.listFiles().orEmpty().isEmpty())
            assertEquals("{}", File(target, "song/index.json").readText())
            assertEquals(10L, File(target, "song/span-0-9.cache").length())
            assertEquals(CacheMigrationPhase.COMPLETED, progress.last().phase)
        } finally {
            root.deleteRecursively()
        }
    }

    /**
     * 目标已有同名条目时应保留目标缓存，并清理源条目。
     */
    @Test
    fun moveOrCopyKeepsTargetEntryWhenMerging() = runBlocking {
        val root = Files.createTempDirectory("xy-cache-merge-test").toFile()
        try {
            val source = File(root, "source")
            val target = File(root, "target")
            val sourceSong = File(source, "song")
            sourceSong.mkdirs()
            File(sourceSong, "index.json").writeText("source")
            val targetSong = File(target, "song")
            targetSong.mkdirs()
            File(targetSong, "index.json").writeText("target")

            val migrated = JvmCacheDirectoryMigrator.moveOrCopy(
                sourceDirectory = source,
                targetDirectory = target,
                totalBytes = 12L,
                onProgress = { },
            )

            assertTrue(migrated)
            // 合并时以目标已有缓存为准，源条目被清理，旧根目录保留。
            assertEquals("target", File(target, "song/index.json").readText())
            assertTrue(source.exists())
            assertTrue(source.listFiles().orEmpty().isEmpty())
        } finally {
            root.deleteRecursively()
        }
    }
}
