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
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

// 平台命令可能因为目录很大或系统异常卡住，超时后会回退到 walkFileTree。
private const val COMMAND_TIMEOUT_SECONDS = 30L
// Windows dir 汇总行中只需要提取“文件数”和“字节数”两个数字。
private val windowsDirNumberRegex = Regex("\\d+")
// macOS du 输出格式为“大小 路径”，用空白分隔取第一列。
private val whitespaceRegex = Regex("\\s+")

actual fun getMemoryStorageInfo(
    contextWrapper: ContextWrapper,
    db: LocalDatabaseClient,
): MemoryStorageInfo {
    // JVM 端数据库文件位于 java.io.tmpdir，包含主文件和 Room/SQLite 可能生成的附属文件。
    val databaseFiles = getLocalDatabaseFiles() + getDownloadDatabaseFiles()
    // 应用数据统计需要排除数据库文件，避免 databaseSize 和 appDataSize 重复计算。
    val excludedPaths = databaseFiles.map { it.absoluteFile }

    return MemoryStorageInfo(
        // 桌面端目前没有单独暴露给内存管理页的可清理缓存目录。
        cacheSize = 0L,
        // 应用数据以 applicationDirectory 为根目录，排除数据库相关文件。
        appDataSize = folderSizeExcluding(
            folder = contextWrapper.applicationDirectory,
            excludedPaths = excludedPaths,
        ),
        // 数据库大小单独统计，便于页面分项展示。
        databaseSize = databaseFiles.sumOf(::folderSize),
    )
}

actual fun clearPlatformCache(contextWrapper: ContextWrapper) {
    // JVM currently has no storage-management-visible temporary cache item.
}

private fun folderSize(file: File?): Long {
    // 空路径、不存在路径和普通文件可以快速返回，不启动平台命令。
    if (file == null || !file.exists()) return 0L
    if (file.isFile) return file.length()
    // 桌面端优先用系统命令统计目录，失败后再回退到 Java NIO 遍历。
    return commandFolderSize(file) ?: walkSize(file)
}

private fun folderSizeExcluding(
    folder: File?,
    excludedPaths: List<File>,
): Long {
    // 根目录不存在时按 0 处理，页面不因为目录被删除而失败。
    if (folder == null || !folder.exists()) return 0L

    val rootPath = folder.toNormalizedPath()
    // 同时保留 File 和规范化 Path：File 用于实际大小统计，Path 用于包含关系判断。
    val excludedPathPairs = excludedPaths
        .map { file -> file.absoluteFile to file.toNormalizedPath() }
        .distinctBy { (_, path) -> path }

    // 如果根目录本身已经在排除路径内，整棵树都不应该计入应用数据。
    if (excludedPathPairs.any { (_, excludedPath) ->
            rootPath == excludedPath || rootPath.startsWith(excludedPath)
        }
    ) {
        return 0L
    }

    // walkFileTree 回退路径使用同一套排除判断，进入目录前即可跳过子树。
    val isExcluded: (Path) -> Boolean = { path ->
        val normalizedPath = path.normalize()
        excludedPathPairs.any { (_, excludedPath) ->
            normalizedPath == excludedPath || normalizedPath.startsWith(excludedPath)
        }
    }

    // 命令只能快速获取整棵目录大小，排除项需要在 Kotlin 侧单独减掉。
    commandFolderSize(folder)?.let { totalSize ->
        val excludedSize = excludedPathPairs
            // 只减去位于根目录内部的排除路径，根目录外的数据库文件不会影响本次统计。
            .filter { (excludedFile, excludedPath) ->
                excludedFile.exists() &&
                    (excludedPath == rootPath || excludedPath.startsWith(rootPath))
            }
            // 如果排除路径之间存在父子关系，只减最外层，避免重复扣减。
            .filterNot { (_, excludedPath) ->
                excludedPathPairs.any { (_, otherPath) ->
                    excludedPath != otherPath && excludedPath.startsWith(otherPath)
                }
            }
            .sumOf { (excludedFile, _) -> folderSize(excludedFile) }
        // 防御命令统计和排除统计口径差异导致的负数。
        return (totalSize - excludedSize).coerceAtLeast(0L)
    }

    // 平台命令不可用或解析失败时，回退到 Java NIO 的逐文件遍历。
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
        // 文件系统在统计期间可能变化，不存在时直接返回 0。
        if (file == null || !file.exists()) return@runCatching 0L

        var total = 0L
        Files.walkFileTree(
            file.toPath(),
            object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(
                    dir: Path,
                    attrs: BasicFileAttributes,
                ): FileVisitResult {
                    // 被排除的目录直接跳过子树，避免继续访问子文件。
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
                    // 数据库等排除文件不计入应用数据，其他文件累加字节数。
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

private fun commandFolderSize(folder: File): Long? {
    // 使用 os.name 做轻量平台判断，非 Windows/macOS 平台直接走 NIO 回退。
    val osName = System.getProperty("os.name").orEmpty().lowercase(Locale.ROOT)
    return when {
        osName.contains("win") -> windowsDirSize(folder)
        osName.contains("mac") || osName.contains("darwin") -> macOsDuSize(folder)
        else -> null
    }
}

private fun windowsDirSize(folder: File): Long? {
    // Windows 通过 dir 的汇总行获取总字节数，/-c 关闭千分位分隔符便于解析。
    val quotedPath = folder.absolutePath.toWindowsCommandArgument() ?: return null
    val command = "dir /s /a:-d /-c $quotedPath | findstr /R /C:\"^  *[0-9][0-9]* .* [0-9][0-9]*\""
    val output = commandOutput("cmd", "/d", "/c", command) ?: return null
    val summarySizes = output
        .lineSequence()
        .mapNotNull(::parseWindowsDirSummarySize)
        .toList()

    // dir /s 会输出每个目录的小计和最后的总计；总计通常是倒数第二个 File(s) 行。
    return when {
        summarySizes.size >= 2 -> summarySizes[summarySizes.lastIndex - 1]
        else -> summarySizes.lastOrNull()
    }
}

private fun parseWindowsDirSummarySize(line: String): Long? {
    // 只接受“文件数 + 字节数”两个数字的汇总行，过滤 free bytes 等其他输出。
    val numbers = windowsDirNumberRegex
        .findAll(line)
        .map { it.value.toLongOrNull() }
        .toList()

    return if (numbers.size == 2) numbers[1] else null
}

private fun macOsDuSize(folder: File): Long? {
    // macOS 的 du -A 使用 apparent size，BLOCKSIZE=1 让第一列直接返回字节数。
    val output = commandOutput(
        command = listOf("du", "-sA", folder.absolutePath),
        environment = mapOf("BLOCKSIZE" to "1"),
    ) ?: return null

    return output
        .lineSequence()
        .mapNotNull { line ->
            line.trim().split(whitespaceRegex, limit = 2).firstOrNull()?.toLongOrNull()
        }
        .firstOrNull()
}

private fun commandOutput(vararg command: String): String? {
    // 便捷重载：无额外环境变量时直接传命令参数。
    return commandOutput(command.toList())
}

private fun commandOutput(
    command: List<String>,
    environment: Map<String, String> = emptyMap(),
): String? {
    return runCatching {
        // redirectErrorStream 合并错误输出，避免子进程因 stderr 未读取而阻塞。
        val processBuilder = ProcessBuilder(command)
            .redirectErrorStream(true)
        processBuilder.environment().putAll(environment)

        val process = processBuilder
            .start()
        // 异步读取 stdout，确保 waitFor 时子进程不会因为输出缓冲区满而卡住。
        val output = CompletableFuture.supplyAsync {
            process.inputStream.bufferedReader().use { it.readText() }
        }

        // 命令超时后强制结束，并让上层回退到 walkFileTree。
        if (!process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            return@runCatching null
        }

        // 非 0 退出码通常表示命令不可用、权限不足或路径不可访问。
        if (process.exitValue() != 0) {
            return@runCatching null
        }

        output.get(1L, TimeUnit.SECONDS)
    }.getOrNull()
}

private fun String.toWindowsCommandArgument(): String? {
    // 目录路径进入 cmd 字符串前做最小转义保护，遇到危险字符则放弃命令统计。
    if (any { it == '"' || it == '%' || it == '\r' || it == '\n' }) return null
    return "\"$this\""
}

private fun File.toNormalizedPath(): Path {
    // absoluteFile 能减少相对路径带来的 startsWith 误判，失败时保留普通 Path 回退。
    return runCatching {
        absoluteFile.toPath().normalize()
    }.getOrDefault(toPath().normalize())
}
