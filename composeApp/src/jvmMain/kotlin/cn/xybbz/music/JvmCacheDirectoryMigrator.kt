package cn.xybbz.music

import cn.xybbz.config.music.CacheMigrationPhase
import cn.xybbz.config.music.CacheMigrationProgress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File

/**
 * JVM 播放缓存目录迁移工具。
 *
 * 只负责把旧目录内容搬到新目录，不处理设置持久化和缓存存储层重建。
 * 迁移按顶层缓存条目逐个处理，搬完后保留旧缓存根目录本身，只清空其中的内容。
 */
internal object JvmCacheDirectoryMigrator {

    /**
     * 迁移复制缓冲区大小，单位是 byte。
     */
    private const val BUFFER_SIZE = 256 * 1024

    /**
     * 把旧缓存目录的内容迁移到新目录，保留旧目录本身。
     *
     * 同卷优先按条目移动；跨卷复制成功后删除源条目；目标已有同名条目时保留目标版本并清理源条目。
     *
     * @param sourceDirectory 旧缓存目录，迁移后仍然存在，只是内容被清空。
     * @param targetDirectory 新缓存目录，允许已有内容，按条目合并。
     * @param totalBytes 旧缓存总字节数，用于进度计算。
     * @param onProgress 进度回调。
     * @return true 表示迁移成功。
     */
    suspend fun moveOrCopy(
        sourceDirectory: File,
        targetDirectory: File,
        totalBytes: Long,
        onProgress: (CacheMigrationProgress) -> Unit,
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(CacheMigrationProgress(0L, totalBytes, CacheMigrationPhase.COPYING))
        targetDirectory.mkdirs()

        var processedBytes = 0L
        val buffer = ByteArray(BUFFER_SIZE)
        // 记录本次复制新建的顶层条目，取消或失败时只回滚这些条目，避免误删目标已有缓存。
        val createdEntries = mutableListOf<File>()

        /**
         * 复制单个文件并累计进度。
         *
         * @param sourceFile 源文件。
         * @param targetFile 目标文件。
         */
        suspend fun copyFile(sourceFile: File, targetFile: File) {
            targetFile.parentFile?.mkdirs()
            sourceFile.inputStream().use { input ->
                targetFile.outputStream().use { output ->
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val readSize = input.read(buffer)
                        if (readSize <= 0) break
                        output.write(buffer, 0, readSize)
                        processedBytes += readSize
                        onProgress(
                            CacheMigrationProgress(
                                processedBytes = processedBytes,
                                totalBytes = totalBytes,
                                phase = CacheMigrationPhase.COPYING,
                            )
                        )
                    }
                }
            }
            // 保留原文件修改时间，LRU 清理仍能按访问时间排序。
            targetFile.setLastModified(sourceFile.lastModified())
        }

        /**
         * 递归复制目录内容。
         *
         * @param sourceDir 源目录。
         * @param targetDir 目标目录。
         */
        suspend fun copyDirectory(sourceDir: File, targetDir: File) {
            targetDir.mkdirs()
            sourceDir.listFiles().orEmpty().forEach { child ->
                currentCoroutineContext().ensureActive()
                val childTarget = File(targetDir, child.name)
                if (child.isDirectory) {
                    copyDirectory(child, childTarget)
                } else {
                    copyFile(child, childTarget)
                }
            }
        }

        try {
            // 顶层是每首歌的缓存条目目录，逐条搬移，旧根目录本身不删除。
            sourceDirectory.listFiles().orEmpty().forEach { sourceEntry ->
                currentCoroutineContext().ensureActive()
                val entryBytes = JvmPlaybackCacheDirectories.directorySizeBytes(sourceEntry)
                val targetEntry = File(targetDirectory, sourceEntry.name)
                if (targetEntry.exists()) {
                    // 目标已有同名条目时以目标缓存为准，源条目直接清理，避免旧目录残留内容。
                    deleteEntry(sourceEntry)
                    processedBytes += entryBytes
                    onProgress(
                        CacheMigrationProgress(
                            processedBytes = processedBytes,
                            totalBytes = totalBytes,
                            phase = CacheMigrationPhase.COPYING,
                        )
                    )
                    return@forEach
                }
                // 同卷直接移动条目，速度最快；移动失败说明跨卷，退化为复制后删除。
                if (runCatching { sourceEntry.renameTo(targetEntry) }.getOrDefault(false)) {
                    processedBytes += entryBytes
                    onProgress(
                        CacheMigrationProgress(
                            processedBytes = processedBytes,
                            totalBytes = totalBytes,
                            phase = CacheMigrationPhase.COPYING,
                        )
                    )
                    return@forEach
                }
                createdEntries += targetEntry
                if (sourceEntry.isDirectory) {
                    copyDirectory(sourceEntry, targetEntry)
                } else {
                    copyFile(sourceEntry, targetEntry)
                }
                // 复制成功后删除源条目，旧根目录继续保留。
                deleteEntry(sourceEntry)
            }
            onProgress(CacheMigrationProgress(totalBytes, totalBytes, CacheMigrationPhase.COMPLETED))
            true
        } catch (cancellation: CancellationException) {
            // 取消时只回滚本次新建的目标条目，不能整体删除目标目录，否则会误删已有缓存。
            createdEntries.forEach { entry -> runCatching { entry.deleteRecursively() } }
            throw cancellation
        } catch (error: Exception) {
            createdEntries.forEach { entry -> runCatching { entry.deleteRecursively() } }
            false
        }
    }

    /**
     * 删除单个缓存条目。
     *
     * 只删除条目自身，绝不删除缓存根目录。
     *
     * @param entry 要删除的缓存条目，可能是目录或文件。
     */
    private fun deleteEntry(entry: File) {
        runCatching {
            if (entry.isDirectory) {
                entry.deleteRecursively()
            } else {
                entry.delete()
            }
        }
    }
}
