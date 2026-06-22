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
    // 连续点号不允许保留，避免服务端文件名携带路径穿越语义。
    private val DOT_SEQUENCE_REGEX = "\\.{2,}".toRegex()

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

            return@withLock ResolvedPath(
                finalPath = requirePathInsideDirectory(parentDir, finalPath),
                fileName = availableName
            )
        }

    // ... (isFileNameReasonable, parseUrlPath, etc. 保持不变) ...

    private fun buildPath(directory: String, fileName: String): String {
        val trimmed = directory.trimEnd('/', '\\')
        return if (trimmed.isBlank()) fileName else "$trimmed/$fileName"
    }

    // 对最终路径做词法规范化校验，防止异常文件名逃逸下载目录。
    private fun requirePathInsideDirectory(directory: String, finalPath: String): String {
        val normalizedDirectory = normalizePathForCheck(directory)
        val normalizedFinalPath = normalizePathForCheck(finalPath)

        if (
            normalizedDirectory.isNotBlank() &&
            normalizedFinalPath != normalizedDirectory &&
            !normalizedFinalPath.startsWith("$normalizedDirectory/")
        ) {
            throw IllegalArgumentException("Resolved download path escapes final directory.")
        }

        return finalPath
    }

    // 统一路径分隔符并折叠 . 与 ..，供目录包含关系校验使用。
    private fun normalizePathForCheck(path: String): String {
        val segments = mutableListOf<String>()
        path.replace('\\', '/')
            .trimEnd('/')
            .split('/')
            .forEach { segment ->
                when {
                    segment.isBlank() || segment == "." -> Unit
                    segment == ".." && segments.isNotEmpty() && segments.last() != ".." -> {
                        segments.removeAt(segments.lastIndex)
                    }
                    segment == ".." -> segments.add(segment)
                    else -> segments.add(segment)
                }
            }
        return segments.joinToString("/")
    }

    private fun sanitize(rawName: String): String {
        // 1. 剥离查询参数 (作为双重保险)
        val nameWithoutQuery = rawName.substringBefore('?').trim()

        // 2. 替换非法字符
        var sanitizedName = normalizeDangerousFileName(
            nameWithoutQuery.replace(ILLEGAL_CHARACTERS_REGEX, "_")
        )

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
        return normalizeDangerousFileName(sanitizedName)
            .ifBlank { "${Clock.System.now().toEpochMilliseconds()}_sanitized.bin" }
    }

    // 清理 .、.. 和隐藏文件名前缀，避免文件名保留路径穿越或隐藏文件语义。
    private fun normalizeDangerousFileName(fileName: String): String {
        val trimmedName = fileName.trim()
        if (trimmedName.isBlank()) {
            return ""
        }
        if (trimmedName.all { it == '.' }) {
            return "download"
        }

        val withoutLeadingDots = trimmedName.dropWhile { it == '.' }
        val safeName = if (withoutLeadingDots.length != trimmedName.length) {
            "download_$withoutLeadingDots"
        } else {
            trimmedName
        }

        return safeName.replace(DOT_SEQUENCE_REGEX, "_")
    }
}
