package cn.xybbz.download.utils

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import cn.xybbz.download.core.DownloadIntegrityValidator
import cn.xybbz.download.core.DownloadStorageGuard
import cn.xybbz.platform.ContextWrapper
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.size
import java.io.IOException
import java.security.MessageDigest

// Android 公开 Downloads 需要 MediaStore，普通文件移动仍复用 commonMain 的 FileKit 工具。
internal actual suspend fun moveToPublicDirectoryWithFileKit(
    contextWrapper: ContextWrapper?,
    sourcePath: String,
    finalPath: String,
    fileName: String,
    expectedBytes: Long,
    expectedMd5: String?,
): String {
    val context = contextWrapper ?: throw IllegalStateException(
        "Android moveToPublicDirectory requires ContextWrapper."
    )

    // 预留文件名时创建的占位文件只用于抢名，正式写入前要清理掉。
    val placeholderPath = finalPath.substringBeforeLast('/', missingDelimiterValue = "")
        .takeIf { it.isNotBlank() }
        ?.let { directory -> "$directory/$fileName" }
    placeholderPath?.let { deleteFileIfEmptyWithFileKit(it) }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        copyToPublicDownloads(
            contextWrapper = context,
            sourcePath = sourcePath,
            displayName = fileName,
            expectedBytes = expectedBytes,
            expectedMd5 = expectedMd5,
        )
    } else {
        moveFileWithFileKit(
            sourcePath = sourcePath,
            finalPath = finalPath,
            contextWrapper = context,
            expectedBytes = expectedBytes,
            expectedMd5 = expectedMd5,
        )
    }
}

/**
 * Android 10+ 使用 MediaStore 写入公开下载目录，让系统文件管理器能立即看到下载结果。
 */
@RequiresApi(Build.VERSION_CODES.Q)
private suspend fun copyToPublicDownloads(
    contextWrapper: ContextWrapper,
    sourcePath: String,
    displayName: String,
    expectedBytes: Long,
    expectedMd5: String?,
): String {
    val publicDownloadsSubdirectory = resolvePublicDownloadsSubdirectory(contextWrapper)
    val contentResolver = contextWrapper.context.contentResolver
    val sourceFile = PlatformFile(sourcePath)
    val sourceBytes = sourceFile.size()
    val targetExpectedBytes = expectedBytes.takeIf { it > 0L } ?: sourceBytes
    DownloadIntegrityValidator.verifyFile(
        path = sourcePath,
        expectedBytes = targetExpectedBytes,
        expectedMd5 = expectedMd5,
    )
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(
            MediaStore.MediaColumns.RELATIVE_PATH,
            "${Environment.DIRECTORY_DOWNLOADS}/$publicDownloadsSubdirectory",
        )
    }

    val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        ?: throw IOException("Failed to create new MediaStore entry.")

    try {
        // MediaStore 写入本质是复制到公开下载目录，复制前先校验公开目录可用空间。
        DownloadStorageGuard.ensureEnoughSpace(
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
            needBytes = targetExpectedBytes,
            contextWrapper = contextWrapper,
        )
        sourceFile.copyTo(PlatformFile(uri))
        DownloadIntegrityValidator.verifyFileSize(
            pathLabel = uri.toString(),
            actualBytes = PlatformFile(uri).size(),
            expectedBytes = targetExpectedBytes,
        )
        DownloadIntegrityValidator.verifyMd5(uri.toString(), expectedMd5) {
            calculateMediaStoreMd5(contextWrapper, uri)
        }
        sourceFile.delete(mustExist = false)
        return displayName
    } catch (throwable: Throwable) {
        // MediaStore 目标写入失败时删除半成品记录，避免系统下载目录出现坏文件。
        contentResolver.delete(uri, null, null)
        throw throwable
    }
}

// 计算 MediaStore 目标文件 MD5，复制后校验公开下载目录中的真实内容。
private fun calculateMediaStoreMd5(contextWrapper: ContextWrapper, uri: Uri): String {
    val digest = MessageDigest.getInstance("MD5")
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    val input = contextWrapper.context.contentResolver.openInputStream(uri)
        ?: throw IOException("Failed to open MediaStore entry for MD5: $uri")
    input.use { stream ->
        while (true) {
            val bytesRead = stream.read(buffer)
            if (bytesRead == -1) {
                break
            }
            digest.update(buffer, 0, bytesRead)
        }
    }
    return digest.digest().joinToString("") { byte ->
        (byte.toInt() and 0xFF).toString(16).padStart(2, '0')
    }
}

// 公开下载目录子目录使用应用显示名，和旧实现保持一致。
internal fun resolvePublicDownloadsSubdirectory(contextWrapper: ContextWrapper): String {
    val context = contextWrapper.context
    val appName = context.packageManager
        .getApplicationLabel(context.applicationInfo)
        .toString()
        .trim()

    return appName
        .ifBlank { context.packageName }
        .replace('/', '_')
        .replace('\\', '_')
}
