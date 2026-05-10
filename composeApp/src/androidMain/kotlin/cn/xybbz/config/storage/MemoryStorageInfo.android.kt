package cn.xybbz.config.storage

import android.os.Environment
import cn.xybbz.platform.ContextWrapper
import cn.xybbz.localdata.config.LocalDatabaseClient
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
    return walkSize(file)
}

private fun folderSizeExcluding(
    folder: File?,
    excludedPrefixes: List<String>,
    excludedDirectories: Set<String>,
): Long {
    return walkSize(folder) { path ->
        val absolutePath = path.toFile().absolutePath
        excludedPrefixes.any { absolutePath.startsWith(it) } ||
            excludedDirectories.contains(absolutePath)
    }
}

private fun walkSize(
    file: File?,
    shouldSkipDirectory: (Path) -> Boolean = { false },
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
                    total += attrs.size()
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
