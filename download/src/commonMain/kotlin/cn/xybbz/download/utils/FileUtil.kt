package cn.xybbz.download.utils

import cn.xybbz.platform.ContextWrapper
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.atomicMove
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.sink
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.io.buffered

// 普通文件系统操作统一走 FileKit，平台层只保留公开目录等系统能力差异。
object FileUtil {
    // 预留文件名需要互斥，避免多个下载任务同时拿到同一个候选名。
    private val reservationMutex = Mutex()

    // 预留一个可用文件名，并在需要时创建占位文件，避免并发命名冲突。
    suspend fun reserveAvailableFileName(
        directory: String,
        fileName: String,
    ): String = withContext(Dispatchers.IO) {
        reservationMutex.withLock {
            val targetDirectory = PlatformFile(directory)
            targetDirectory.createDirectories()

            val sanitizedName = normalizeReservedFileName(fileName)
            val baseName = sanitizedName.substringBeforeLast('.', sanitizedName)
            val extension = sanitizedName.substringAfterLast('.', "")

            suspend fun reserve(candidate: String): String? {
                // FileKit 暂无原子 createNewFile API，这里在外层互斥保护下用 sink 创建占位文件。
                val file = PlatformFile(targetDirectory, candidate)
                if (file.exists()) {
                    return null
                }
                file.writeEmptyFile()
                return candidate
            }

            reserve(sanitizedName)?.let { return@withLock it }

            for (counter in 1..999) {
                val candidate = if (extension.isNotEmpty() && baseName.isNotBlank()) {
                    "$baseName($counter).$extension"
                } else if (extension.isNotEmpty() && baseName.isBlank()) {
                    "download($counter).$extension"
                } else {
                    "$baseName($counter)"
                }
                reserve(candidate)?.let { return@withLock it }
            }

            throw IOException("Failed to find an available filename for '$fileName' after 999 attempts.")
        }
    }

    // 下载完成后将临时文件移动到最终目录；Android 会走公开下载目录逻辑。
    suspend fun moveToPublicDirectory(
        contextWrapper: ContextWrapper? = null,
        sourcePath: String,
        finalPath: String,
        fileName: String,
    ): String = moveToPublicDirectoryWithFileKit(
        contextWrapper = contextWrapper,
        sourcePath = sourcePath,
        finalPath = finalPath,
        fileName = fileName,
    )
}

// 文件名预留前的最后兜底，避免 .、.. 或隐藏文件名前缀进入平台文件 API。
private fun normalizeReservedFileName(fileName: String): String {
    val trimmedName = fileName.trim()
    if (trimmedName.isBlank() || trimmedName.all { it == '.' }) {
        return "download"
    }

    val withoutLeadingDots = trimmedName.dropWhile { it == '.' }
    return if (withoutLeadingDots.length != trimmedName.length) {
        "download_${withoutLeadingDots.ifBlank { "file" }}"
    } else {
        trimmedName
    }
}

// 删除普通文件；文件不存在时按成功处理，便于清理路径重复调用。
suspend fun deleteFileWithFileKit(path: String): Boolean = withContext(Dispatchers.IO) {
    if (path.isBlank()) {
        return@withContext false
    }
    val file = PlatformFile(path)
    if (!file.exists()) {
        return@withContext false
    }
    file.delete(mustExist = false)
    true
}

// 只删除 0 字节占位文件，避免误删真实下载结果。
suspend fun deleteFileIfEmptyWithFileKit(path: String): Boolean = withContext(Dispatchers.IO) {
    if (path.isBlank()) {
        return@withContext false
    }
    val file = PlatformFile(path)
    if (!file.exists() || file.size() != 0L) {
        return@withContext false
    }
    file.delete(mustExist = false)
    true
}

// 读取普通文件大小；不存在、空路径或目录按 0 处理，匹配旧下载进度语义。
fun fileLengthWithFileKit(path: String): Long {
    if (path.isBlank()) {
        return 0L
    }
    val file = PlatformFile(path)
    return if (file.exists()) file.size().coerceAtLeast(0L) else 0L
}

// 创建目录并返回原路径字符串，方便现有配置继续存储 String。
fun ensureDirectoryWithFileKit(path: String): String {
    val directory = PlatformFile(path)
    directory.createDirectories()
    return path
}

// 用 FileKit 完成普通文件系统内的移动，原子移动失败时退回复制再删除。
suspend fun moveFileWithFileKit(
    sourcePath: String,
    finalPath: String,
): String = withContext(Dispatchers.IO) {
    val sourceFile = PlatformFile(sourcePath)
    if (!sourceFile.exists()) {
        throw IOException("Source file does not exist: $sourcePath")
    }

    val finalFile = PlatformFile(finalPath)
    finalFile.parent()?.createDirectories()

    if (finalFile.exists()) {
        // 最终路径可能是预留占位文件，也可能是用户重试留下的旧文件；写入前统一清理。
        finalFile.delete(mustExist = false)
    }

    runCatching {
        sourceFile.atomicMove(finalFile)
    }.getOrElse {
        sourceFile.copyTo(finalFile)
        sourceFile.delete(mustExist = false)
    }

    finalFile.absolutePathOrPath()
}

// FileKit 的写入 API 暂无 createNewFile 等价物，空写入作为占位文件创建。
private suspend fun PlatformFile.writeEmptyFile() {
    sink().buffered().use { sink ->
        sink.flush()
    }
}

// Android content Uri 没有传统 absolutePath，普通文件路径则优先返回 absolutePath。
private fun PlatformFile.absolutePathOrPath(): String =
    runCatching { absolutePath() }.getOrElse { path }

// 默认公共迁移使用 FileKit；Android 公开 Downloads 会在 androidMain 覆盖该 actual。
internal expect suspend fun moveToPublicDirectoryWithFileKit(
    contextWrapper: ContextWrapper?,
    sourcePath: String,
    finalPath: String,
    fileName: String,
): String
