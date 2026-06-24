package cn.xybbz.download.core

import cn.xybbz.platform.ContextWrapper
import kotlinx.io.IOException

// 下载磁盘空间不足时抛出的专用异常，便于上层展示明确失败原因。
internal class InsufficientStorageException(message: String) : IOException(message)

// 下载磁盘空间校验统一入口，集中管理安全余量和错误文案。
internal object DownloadStorageGuard {
    // 写入前至少保留 50MB，避免把系统空间完全打满。
    internal const val MIN_RESERVED_SPACE_BYTES: Long = 50L * 1024L * 1024L

    // 流式写入过程中每累计 5MB 至少重新检查一次磁盘空间。
    internal const val WRITE_SPACE_CHECK_INTERVAL_BYTES: Long = 5L * 1024L * 1024L

    // 流式写入过程中每 1 秒至少重新检查一次磁盘空间。
    internal const val WRITE_SPACE_CHECK_INTERVAL_MILLIS: Long = 1000L

    // 已知需要写入的字节数时，额外保留 5% 空间作为安全余量。
    private const val SAFETY_RESERVE_DIVISOR: Long = 20L

    // 计算包含安全余量的实际空间需求，溢出时按 Long.MAX_VALUE 处理。
    internal fun requiredBytesWithReserve(needBytes: Long): Long {
        if (needBytes <= 0L) {
            return 0L
        }
        val reserveBytes = maxOf(MIN_RESERVED_SPACE_BYTES, needBytes / SAFETY_RESERVE_DIVISOR)
        return if (Long.MAX_VALUE - needBytes < reserveBytes) {
            Long.MAX_VALUE
        } else {
            needBytes + reserveBytes
        }
    }

    // 已知文件大小时校验可用空间，未知或非正数需求不做强校验。
    fun ensureEnoughSpace(
        path: String,
        needBytes: Long,
        contextWrapper: ContextWrapper,
    ) {
        if (needBytes <= 0L) {
            return
        }
        ensureUsableSpaceAtLeast(
            path = path,
            requiredBytes = requiredBytesWithReserve(needBytes),
            contextWrapper = contextWrapper,
        )
    }

    // 写入过程中兜底校验；文件大小未知时只保证保底空间仍然存在。
    fun ensureWriteCanContinue(
        path: String,
        currentBytes: Long,
        expectedTotalBytes: Long,
        contextWrapper: ContextWrapper,
    ) {
        if (expectedTotalBytes > 0L) {
            ensureEnoughSpace(
                path = path,
                needBytes = (expectedTotalBytes - currentBytes).coerceAtLeast(0L),
                contextWrapper = contextWrapper,
            )
        } else {
            ensureUsableSpaceAtLeast(
                path = path,
                requiredBytes = MIN_RESERVED_SPACE_BYTES,
                contextWrapper = contextWrapper,
            )
        }
    }

    // 校验路径所在磁盘是否至少还有指定可用空间。
    private fun ensureUsableSpaceAtLeast(
        path: String,
        requiredBytes: Long,
        contextWrapper: ContextWrapper,
    ) {
        val usableBytes = DownloadPlatformFiles.usableSpace(path, contextWrapper)
        if (usableBytes < requiredBytes) {
            throw InsufficientStorageException(
                "磁盘空间不足：需要至少 ${formatBytes(requiredBytes)}，当前可用 ${formatBytes(usableBytes)}。",
            )
        }
    }

    // 将字节数格式化成更适合错误提示阅读的文本。
    private fun formatBytes(bytes: Long): String {
        val gib = 1024L * 1024L * 1024L
        val mib = 1024L * 1024L
        return when {
            bytes >= gib -> "${bytes / gib}GB"
            bytes >= mib -> "${bytes / mib}MB"
            else -> "${bytes.coerceAtLeast(0L)}B"
        }
    }
}
