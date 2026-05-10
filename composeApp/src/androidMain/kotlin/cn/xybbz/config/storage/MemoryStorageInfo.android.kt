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
    // Room 数据库路径用于单独展示数据库大小，并在应用数据统计中排除数据库目录。
    val databasePath = runCatching { db.openHelper.writableDatabase.path }.getOrNull().orEmpty()
    val databaseFile = databasePath.takeIf { it.isNotBlank() }?.let(::File)

    // Android 缓存分为内部 cacheDir 和可能存在的外部 externalCacheDir。
    val cacheSize = buildList {
        add(context.cacheDir)
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            context.externalCacheDir?.let(::add)
        }
    }.sumOf(::folderSize)

    // 应用数据目录可能因为系统权限或包信息读取失败而不可用，失败时降级为 0。
    val appDataSize = runCatching {
        val dataDir = context.packageManager
            .getPackageInfo(context.packageName, 0)
            .applicationInfo
            ?.dataDir
            ?.let(::File)

        // 应用数据不重复计算缓存和数据库目录，避免页面上各分类大小互相叠加。
        folderSizeExcluding(
            folder = dataDir,
            excludedPrefixes = listOfNotNull(context.cacheDir.path, context.externalCacheDir?.path),
            excludedDirectories = setOfNotNull(databaseFile?.parentFile?.absolutePath),
        )
    }.getOrElse {
        0L
    }

    // Android 端数据库当前只取 Room 打开的主数据库文件大小。
    val databaseSize = databaseFile?.takeIf(File::exists)?.length() ?: 0L

    return MemoryStorageInfo(
        cacheSize = cacheSize,
        appDataSize = appDataSize,
        databaseSize = databaseSize,
    )
}

actual fun clearPlatformCache(contextWrapper: ContextWrapper) {
    val context = contextWrapper.context
    // 清理内部缓存目录。
    deleteRecursively(context.cacheDir)
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        // 外部缓存目录只有在外部存储挂载后才尝试清理。
        context.externalCacheDir?.let(::deleteRecursively)
    }
}

private fun folderSize(file: File?): Long {
    // Android 保持 Java NIO 遍历实现，避免递归 listFiles 带来的额外调用开销。
    return walkSize(file)
}

private fun folderSizeExcluding(
    folder: File?,
    excludedPrefixes: List<String>,
    excludedDirectories: Set<String>,
): Long {
    // 进入目录前判断是否需要跳过整棵子树，减少不必要的文件遍历。
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
        // 不存在的文件或目录按 0 处理，避免存储页因为临时文件变化崩溃。
        if (file == null || !file.exists()) return@runCatching 0L

        var total = 0L
        Files.walkFileTree(
            file.toPath(),
            object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(
                    dir: Path,
                    attrs: BasicFileAttributes,
                ): FileVisitResult {
                    // 对缓存、数据库等已单独统计的目录直接跳过整个子树。
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
                    // 使用 BasicFileAttributes.size，避免再次通过 File 查询文件信息。
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
    // 文件被其他进程删除或不存在时，视为清理成功。
    if (file == null || !file.exists()) return true
    if (file.isDirectory) {
        file.listFiles()?.forEach { child ->
            // 任意子文件删除失败就向上返回失败，便于未来需要时接入提示。
            if (!deleteRecursively(child)) {
                return false
            }
        }
    }
    return file.delete()
}
