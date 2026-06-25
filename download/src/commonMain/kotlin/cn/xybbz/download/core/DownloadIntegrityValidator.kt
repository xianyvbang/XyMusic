package cn.xybbz.download.core

import kotlinx.io.IOException

// 下载文件完整性校验统一入口，集中处理 size 和可选 MD5 的错误语义。
internal object DownloadIntegrityValidator {

    // 校验刚写完的临时文件，确保写入计数、磁盘文件长度和可选 MD5 一致。
    fun verifyCompletedTempFile(
        path: String,
        expectedBytes: Long,
        expectedMd5: String?,
        writtenBytes: Long,
    ) {
        val actualBytes = DownloadPlatformFiles.fileLength(path)
        if (actualBytes != writtenBytes) {
            throw IOException(
                "Downloaded file length mismatch: path=$path, written=$writtenBytes, actual=$actualBytes"
            )
        }
        verifyFileSize(
            pathLabel = path,
            actualBytes = writtenBytes,
            expectedBytes = expectedBytes,
        )
        verifyMd5(path, expectedMd5) {
            DownloadPlatformFiles.fileMd5(path)
        }
    }

    // 校验一个已存在文件的长度和可选 MD5；expectedBytes 小于等于 0 时跳过长度校验。
    fun verifyFile(
        path: String,
        expectedBytes: Long,
        expectedMd5: String?,
    ) {
        verifyFileSize(
            pathLabel = path,
            actualBytes = DownloadPlatformFiles.fileLength(path),
            expectedBytes = expectedBytes,
        )
        verifyMd5(path, expectedMd5) {
            DownloadPlatformFiles.fileMd5(path)
        }
    }

    // 校验文件大小；expectedBytes 小于等于 0 表示调用方没有可靠的期望大小。
    fun verifyFileSize(
        pathLabel: String,
        actualBytes: Long,
        expectedBytes: Long,
    ) {
        if (expectedBytes > 0L && actualBytes != expectedBytes) {
            throw IOException(
                "File size mismatch: path=$pathLabel, expected=$expectedBytes, actual=$actualBytes"
            )
        }
    }

    // 校验 MD5；expectedMd5 为空时保持兼容并跳过内容哈希。
    fun verifyMd5(
        pathLabel: String,
        expectedMd5: String?,
        actualMd5Provider: () -> String,
    ) {
        val normalizedExpected = normalizeExpectedMd5(expectedMd5) ?: return
        val actualMd5 = actualMd5Provider()
        if (!normalizedExpected.equals(actualMd5, ignoreCase = true)) {
            throw IOException(
                "File MD5 mismatch: path=$pathLabel, expected=$normalizedExpected, actual=$actualMd5"
            )
        }
    }

    // 规范化外部传入的 MD5，空白字符串视为未提供。
    private fun normalizeExpectedMd5(expectedMd5: String?): String? {
        return expectedMd5?.trim()?.takeIf { it.isNotEmpty() }
    }
}
