package cn.xybbz.download.core

import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.download.utils.ensureDirectoryWithFileKit
import cn.xybbz.platform.ContextWrapper
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readAvailable
import java.io.File
import java.io.RandomAccessFile
import kotlin.time.Clock

internal actual fun platformDefaultDownloadDirectoryPath(contextWrapper: ContextWrapper): String {
    return File(File(contextWrapper.applicationDirectory, "Downloads"), "XyMusic").absolutePath
}

internal actual fun createPlatformTempDownloadFilePath(contextWrapper: ContextWrapper): String {
    val directory = File(File(contextWrapper.applicationDirectory, "temp"), "xy-downloads")
    // 父目录创建统一走 FileKit，平台层只负责调用 JVM 临时文件命名能力。
    ensureDirectoryWithFileKit(directory.absolutePath)
    return File.createTempFile("download_", ".tmp", directory).absolutePath
}

// JVM 端通过 java.io.File 查询路径所在磁盘的可用空间。
internal actual fun platformUsableSpace(path: String, contextWrapper: ContextWrapper): Long {
    val target = resolveSpaceTarget(path, contextWrapper.applicationDirectory)
    return target.usableSpace.coerceAtLeast(0L)
}

@Suppress("BlockingMethodInNonBlockingContext")
internal actual suspend fun writeResponseToPlatformFile(
    path: String,
    startOffset: Long,
    contextWrapper: ContextWrapper?,
    expectedTotalBytes: Long,
    source: ByteReadChannel,
    onChunkWritten: suspend (currentBytes: Long) -> DownloadStatus,
): DownloadWriteResult {
    val targetFile = File(path)
    targetFile.parentFile?.absolutePath?.let(::ensureDirectoryWithFileKit)
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var currentBytes = startOffset
    // 记录上次空间校验时的写入位置，避免每个小分片都查询文件系统。
    var lastSpaceCheckBytes = startOffset
    // 记录上次空间校验时间，防止低速下载长时间不触发字节阈值。
    var lastSpaceCheckTime = Clock.System.now().toEpochMilliseconds()

    // 写入开始前先做一次空间兜底校验，避免打开文件后才发现磁盘不足。
    contextWrapper?.let {
        DownloadStorageGuard.ensureWriteCanContinue(
            path = path,
            currentBytes = currentBytes,
            expectedTotalBytes = expectedTotalBytes,
            contextWrapper = it,
        )
    }

    RandomAccessFile(targetFile, "rw").use { output ->
        // 断点续传必须裁剪旧尾部并 seek 到断点，FileKit sink 不能替代这类随机写入。
        output.setLength(startOffset)
        output.seek(startOffset)

        while (!source.exhausted()) {
            val bytesRead = source.readAvailable(buffer, 0, buffer.size)
            if (bytesRead == -1) {
                break
            }
            if (bytesRead == 0) {
                continue
            }

            // 写入过程中按字节间隔或时间间隔周期性检查空间，处理外部进程抢占磁盘的情况。
            contextWrapper?.let {
                val currentTime = Clock.System.now().toEpochMilliseconds()
                val shouldCheckSpace =
                    currentBytes - lastSpaceCheckBytes >= DownloadStorageGuard.WRITE_SPACE_CHECK_INTERVAL_BYTES ||
                            currentTime - lastSpaceCheckTime >= DownloadStorageGuard.WRITE_SPACE_CHECK_INTERVAL_MILLIS
                if (shouldCheckSpace) {
                    DownloadStorageGuard.ensureWriteCanContinue(
                        path = path,
                        currentBytes = currentBytes,
                        expectedTotalBytes = expectedTotalBytes,
                        contextWrapper = it,
                    )
                    lastSpaceCheckBytes = currentBytes
                    lastSpaceCheckTime = currentTime
                }
            }

            output.write(buffer, 0, bytesRead)
            currentBytes += bytesRead

            when (onChunkWritten(currentBytes)) {
                DownloadStatus.PAUSED ->
                    return DownloadWriteResult(DownloadStatus.PAUSED, currentBytes)

                DownloadStatus.CANCEL ->
                    return DownloadWriteResult(DownloadStatus.CANCEL, currentBytes)

                else -> Unit
            }
        }
    }

    return DownloadWriteResult(DownloadStatus.COMPLETED, currentBytes)
}

// 查询磁盘空间时路径可能还不存在，因此逐级回退到已存在的父目录。
private fun resolveSpaceTarget(path: String, fallbackDirectory: File): File {
    val rawTarget = path.takeIf { it.isNotBlank() }?.let(::File) ?: fallbackDirectory
    val absoluteTarget = rawTarget.absoluteFile
    return generateSequence(absoluteTarget) { it.parentFile }
        .firstOrNull { it.exists() }
        ?.let { if (it.isFile) it.parentFile ?: it else it }
        ?: fallbackDirectory
}

fun getJvmDownloadDirectory(contextWrapper: ContextWrapper): File {
    return File(platformDefaultDownloadDirectoryPath(contextWrapper))
}

fun getJvmDownloadTempDirectory(contextWrapper: ContextWrapper): File {
    return File(File(contextWrapper.applicationDirectory, "temp"), "xy-downloads")
}
