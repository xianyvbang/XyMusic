package cn.xybbz.config.storage

import android.os.Environment
import cn.xybbz.di.ContextWrapper
import cn.xybbz.localdata.config.DatabaseClient
import java.io.File

actual fun getMemoryStorageInfo(
    contextWrapper: ContextWrapper,
    db: DatabaseClient,
): MemoryStorageInfo {
    val context = contextWrapper.context
    val databasePath = runCatching { db.openHelper.writableDatabase.path }.getOrNull().orEmpty()
    val databaseFile = databasePath.takeIf { it.isNotBlank() }?.let(::File)

    val cacheSize = buildList {
        add(context.cacheDir)
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            context.externalCacheDir?.let(::add)
        }
    }.sumOf(::folderSize)

    val appDataSize = runCatching {
        val dataDir = context.packageManager
            .getPackageInfo(context.packageName, 0)
            .applicationInfo
            ?.dataDir
            ?.let(::File)

        folderSizeExcluding(
            folder = dataDir,
            excludedPrefixes = listOfNotNull(context.cacheDir.path, context.externalCacheDir?.path),
            excludedDirectories = setOfNotNull(databaseFile?.parentFile?.absolutePath),
        )
    }.getOrElse {
        0L
    }

    val databaseSize = databaseFile?.takeIf(File::exists)?.length() ?: 0L

    return MemoryStorageInfo(
        cacheSize = cacheSize,
        appDataSize = appDataSize,
        databaseSize = databaseSize,
    )
}

actual fun clearPlatformCache(contextWrapper: ContextWrapper) {
    val context = contextWrapper.context
    deleteRecursively(context.cacheDir)
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        context.externalCacheDir?.let(::deleteRecursively)
    }
}

private fun folderSize(file: File?): Long {
    if (file == null || !file.exists()) return 0L
    if (file.isFile) return file.length()
    return file.listFiles()?.sumOf(::folderSize) ?: 0L
}

private fun folderSizeExcluding(
    folder: File?,
    excludedPrefixes: List<String>,
    excludedDirectories: Set<String>,
): Long {
    if (folder == null || !folder.exists()) return 0L
    if (folder.isFile) return folder.length()

    val files = folder.listFiles() ?: return 0L
    var total = 0L
    for (file in files) {
        val absolutePath = file.absolutePath
        if (file.isDirectory) {
            val shouldSkip = excludedPrefixes.any { absolutePath.startsWith(it) } ||
                excludedDirectories.contains(absolutePath)
            if (!shouldSkip) {
                total += folderSize(file)
            }
        } else {
            total += file.length()
        }
    }
    return total
}

private fun deleteRecursively(file: File?): Boolean {
    if (file == null || !file.exists()) return true
    if (file.isDirectory) {
        file.listFiles()?.forEach { child ->
            if (!deleteRecursively(child)) {
                return false
            }
        }
    }
    return file.delete()
}
