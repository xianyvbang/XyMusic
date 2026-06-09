package cn.xybbz.download.utils

import cn.xybbz.download.core.DownloadRequest
import cn.xybbz.download.core.ResolvedPath
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

/**
 * FileName: AppDatabase
 * Author: haosen
 * Date: 10/3/2025 4:55 AM
 * Description:
 **/
internal object FileNameResolver {

    private const val MAX_FILENAME_LENGTH = 128
    private val ILLEGAL_CHARACTERS_REGEX = "[\\\\/:*?\"<>|]".toRegex()

    private val mutex = Mutex()

    /**
     * The main entry point. Resolves a unique, final, and sanitized path for a download request.
     * This method is thread-safe and handles filesystem race conditions by creating a placeholder file.
     *
     * @param request The user's download request.
     * @param globalFinalDir The default final directory from the global configuration.
     * @return A [ResolvedPath] object containing the guaranteed unique final path and file name.
     */
    suspend fun resolve(
        request: DownloadRequest,
        globalFinalDir: String,
    ): ResolvedPath =
        mutex.withLock {

            val parentDir = globalFinalDir
            val rawName = request.fileName

            // 清理原始文件名，真正的文件名占位和冲突处理交给平台 FileUtil。
            val sanitizedName = sanitize(rawName)
            val availableName = FileUtil.reserveAvailableFileName(parentDir, sanitizedName)
            val finalPath = buildPath(parentDir, availableName)

            return@withLock ResolvedPath(finalPath = finalPath, fileName = availableName)
        }

    // ... (isFileNameReasonable, parseUrlPath, etc. 保持不变) ...

    private fun buildPath(directory: String, fileName: String): String {
        val trimmed = directory.trimEnd('/', '\\')
        return if (trimmed.isBlank()) fileName else "$trimmed/$fileName"
    }

    private fun sanitize(rawName: String): String {
        // 1. 剥离查询参数 (作为双重保险)
        val nameWithoutQuery = rawName.substringBefore('?')

        // 2. 替换非法字符
        var sanitizedName = nameWithoutQuery.replace(ILLEGAL_CHARACTERS_REGEX, "_")

        // 3. 截断过长的文件名 (逻辑保持不变)
        if (sanitizedName.length > MAX_FILENAME_LENGTH) {
            val extension = sanitizedName.substringAfterLast('.', "")
            val nameWithoutExtension = sanitizedName.substringBeforeLast('.')
            val extensionLength = if (extension.isNotEmpty()) extension.length + 1 else 0
            val availableLengthForName = (MAX_FILENAME_LENGTH - extensionLength).coerceAtLeast(0)
            val truncatedName = nameWithoutExtension.take(availableLengthForName)

            sanitizedName = if (extension.isNotEmpty()) {
                if (truncatedName.isNotEmpty()) "$truncatedName.$extension" else extension.take(
                    MAX_FILENAME_LENGTH
                )
            } else {
                truncatedName
            }
        }
        return sanitizedName.ifBlank { "${Clock.System.now().toEpochMilliseconds()}_sanitized.bin" }
    }
}
