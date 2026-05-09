package cn.xybbz.config.storage

import cn.xybbz.download.database.getDownloadDatabaseFiles
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.config.getLocalDatabaseFiles
import cn.xybbz.platform.ContextWrapper
import java.io.File

actual fun getMemoryStorageInfo(
    contextWrapper: ContextWrapper,
    db: LocalDatabaseClient,
): MemoryStorageInfo {
    val databaseFiles = getLocalDatabaseFiles() + getDownloadDatabaseFiles()
    val excludedPaths = databaseFiles.map { it.absoluteFile }

    return MemoryStorageInfo(
        cacheSize = 0L,
        appDataSize = folderSizeExcluding(
            folder = contextWrapper.applicationDirectory,
            excludedPaths = excludedPaths,
        ),
        databaseSize = databaseFiles.sumOf(::folderSize),
    )
}

actual fun clearPlatformCache(contextWrapper: ContextWrapper) {
    // JVM currently has no storage-management-visible temporary cache item.
}

private fun folderSize(file: File?): Long {
    return runCatching {
        when {
            file == null || !file.exists() -> 0L
            file.isFile -> file.length()
            else -> file.listFiles()?.sumOf(::folderSize) ?: 0L
        }
    }.getOrDefault(0L)
}

private fun folderSizeExcluding(
    folder: File?,
    excludedPaths: List<File>,
): Long {
    return runCatching {
        when {
            folder == null || !folder.exists() -> 0L
            isExcluded(folder, excludedPaths) -> 0L
            folder.isFile -> folder.length()
            else -> folder.listFiles()?.sumOf { file ->
                folderSizeExcluding(file, excludedPaths)
            } ?: 0L
        }
    }.getOrDefault(0L)
}

private fun isExcluded(
    file: File,
    excludedPaths: List<File>,
): Boolean {
    val absoluteFile = file.absoluteFile
    return excludedPaths.any { excludedPath ->
        absoluteFile == excludedPath || absoluteFile.startsWithDirectory(excludedPath)
    }
}

private fun File.startsWithDirectory(parent: File): Boolean {
    return runCatching {
        toPath().normalize().startsWith(parent.toPath().normalize())
    }.getOrDefault(false)
}
