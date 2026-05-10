package cn.xybbz.config.storage

import cn.xybbz.download.database.getDownloadDatabaseFiles
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.config.getLocalDatabaseFiles
import cn.xybbz.platform.ContextWrapper
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

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
    return walkSize(file)
}

private fun folderSizeExcluding(
    folder: File?,
    excludedPaths: List<File>,
): Long {
    val excludedNormalizedPaths = excludedPaths.map { it.toNormalizedPath() }

    val isExcluded: (Path) -> Boolean = { path ->
        val normalizedPath = path.normalize()
        excludedNormalizedPaths.any { excludedPath ->
            normalizedPath == excludedPath || normalizedPath.startsWith(excludedPath)
        }
    }

    return walkSize(
        file = folder,
        shouldSkipDirectory = isExcluded,
        shouldSkipFile = isExcluded,
    )
}

private fun walkSize(
    file: File?,
    shouldSkipDirectory: (Path) -> Boolean = { false },
    shouldSkipFile: (Path) -> Boolean = { false },
): Long {
    return runCatching {
        if (file == null || !file.exists()) return@runCatching 0L

        var total = 0L
        Files.walkFileTree(
            file.toPath(),
            object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(
                    dir: Path,
                    attrs: BasicFileAttributes,
                ): FileVisitResult {
                    return if (shouldSkipDirectory(dir)) {
                        FileVisitResult.SKIP_SUBTREE
                    } else {
                        FileVisitResult.CONTINUE
                    }
                }

                override fun visitFile(
                    file: Path,
                    attrs: BasicFileAttributes,
                ): FileVisitResult {
                    if (!shouldSkipFile(file)) {
                        total += attrs.size()
                    }
                    return FileVisitResult.CONTINUE
                }

                override fun visitFileFailed(
                    file: Path,
                    exc: IOException,
                ): FileVisitResult = FileVisitResult.CONTINUE
            },
        )
        total
    }.getOrDefault(0L)
}

private fun File.toNormalizedPath(): Path {
    return runCatching {
        absoluteFile.toPath().normalize()
    }.getOrDefault(toPath().normalize())
}
