package cn.xybbz.platform

import java.io.File
import java.util.Locale

actual class ContextWrapper {
    // JVM 路径解析使用的环境变量映射，测试可通过工厂注入隔离值。
    private val environment: Map<String, String>

    // JVM 路径解析使用的系统名称，测试可通过工厂注入不同平台名称。
    private val osName: String

    // JVM 路径解析使用的用户目录，测试可通过工厂注入临时目录。
    private val userHomeDirectory: File

    // JVM 持久应用数据根目录，用于保存数据库和下载中的临时状态。
    val dataDirectory: File

    // JVM 数据库目录，Room 主文件和 SQLite 伴生文件统一落在这里。
    val databaseDirectory: File

    // JVM 可重建缓存目录，用于播放缓存等可清理数据。
    val cacheDirectory: File

    // JVM 下载中的临时文件目录，放在持久数据目录避免系统临时目录清理断点文件。
    val downloadTempDirectory: File

    // JVM 默认下载完成目录，面向用户文件管理器展示。
    val downloadDirectory: File

    // 兼容旧调用点的应用数据目录，后续新代码应优先使用上面的用途化目录。
    val applicationDirectory: File

    constructor() : this(
        environment = System.getenv(),
        osName = System.getProperty("os.name").orEmpty(),
        //用户的home
        userHomeDirectory = File(System.getProperty("user.home").orEmpty().ifBlank { "." }).absoluteFile,
    )

    private constructor(
        environment: Map<String, String>,
        osName: String,
        userHomeDirectory: File,
    ) {
        this.environment = environment
        this.osName = osName
        this.userHomeDirectory = userHomeDirectory.absoluteFile

        dataDirectory = resolveDataDirectory().absoluteFile
        databaseDirectory = File(dataDirectory, DATABASE_DIRECTORY_NAME).absoluteFile
        cacheDirectory = resolveCacheDirectory().absoluteFile
        downloadTempDirectory = File(dataDirectory, DOWNLOAD_TEMP_DIRECTORY_NAME).absoluteFile
        downloadDirectory = File(resolveUserDownloadsDirectory(), DOWNLOAD_DIRECTORY_NAME).absoluteFile
        applicationDirectory = dataDirectory
    }

    // 解析 JVM 持久应用数据目录，按系统遵循桌面应用常用位置。
    private fun resolveDataDirectory(): File {
        return when (currentOperatingSystem()) {
            OperatingSystem.WINDOWS -> File(
                environment["LOCALAPPDATA"].orBlankToNull()?.let(::File) ?: userHomeDirectory,
                WINDOWS_APP_DIRECTORY_NAME,
            )

            OperatingSystem.MACOS -> File(
                File(userHomeDirectory, MACOS_APPLICATION_SUPPORT_DIRECTORY),
                APP_DIRECTORY_NAME,
            )

            OperatingSystem.LINUX -> File(
                environment["XDG_DATA_HOME"].orBlankToNull()?.let(::File)
                    ?: File(userHomeDirectory, LINUX_DEFAULT_DATA_DIRECTORY),
                LINUX_APP_DIRECTORY_NAME,
            )
        }
    }

    // 解析 JVM 可重建缓存目录，缓存和持久数据分离便于后续清理。
    private fun resolveCacheDirectory(): File {
        return when (currentOperatingSystem()) {
            OperatingSystem.WINDOWS -> File(
                File(environment["LOCALAPPDATA"].orBlankToNull() ?: userHomeDirectory.absolutePath),
                "$WINDOWS_APP_DIRECTORY_NAME/$CACHE_DIRECTORY_NAME",
            )

            OperatingSystem.MACOS -> File(
                File(userHomeDirectory, MACOS_CACHES_DIRECTORY),
                APP_DIRECTORY_NAME,
            )

            OperatingSystem.LINUX -> File(
                environment["XDG_CACHE_HOME"].orBlankToNull()?.let(::File)
                    ?: File(userHomeDirectory, LINUX_DEFAULT_CACHE_DIRECTORY),
                LINUX_APP_DIRECTORY_NAME,
            )
        }
    }

    // 解析用户下载目录，当前不读取系统本地化目录配置，缺省统一使用 ~/Downloads。
    private fun resolveUserDownloadsDirectory(): File {
        val homeDirectory = when (currentOperatingSystem()) {
            OperatingSystem.WINDOWS -> environment["USERPROFILE"].orBlankToNull()?.let(::File) ?: userHomeDirectory
            OperatingSystem.MACOS,
            OperatingSystem.LINUX -> userHomeDirectory
        }
        return File(homeDirectory, USER_DOWNLOADS_DIRECTORY_NAME)
    }

    // 解析当前 JVM 所在操作系统，未知桌面系统按 Linux/XDG 规则兜底。
    private fun currentOperatingSystem(): OperatingSystem {
        val normalizedOsName = osName.lowercase(Locale.ROOT)
        return when {
            normalizedOsName.contains("win") -> OperatingSystem.WINDOWS
            normalizedOsName.contains("mac") || normalizedOsName.contains("darwin") -> OperatingSystem.MACOS
            else -> OperatingSystem.LINUX
        }
    }

    // JVM 桌面端路径分流时使用的操作系统分类。
    private enum class OperatingSystem {
        WINDOWS,
        MACOS,
        LINUX,
    }

    companion object {
        /**
         * 创建 JVM 测试专用上下文，避免测试读写真实用户目录。
         */
        fun createForTest(
            environment: Map<String, String>,
            osName: String,
            userHomeDirectory: File,
        ): ContextWrapper {
            return ContextWrapper(
                environment = environment,
                osName = osName,
                userHomeDirectory = userHomeDirectory,
            )
        }

        // 桌面应用对外展示的应用目录名称。
        private const val APP_DIRECTORY_NAME = "XyMusic"

        // Linux 遵循 XDG 目录命名习惯，使用小写应用目录。
        private const val LINUX_APP_DIRECTORY_NAME = "xymusic"

        // Windows 本地应用数据目录下的应用目录名称。
        private const val WINDOWS_APP_DIRECTORY_NAME = APP_DIRECTORY_NAME

        // 数据库子目录名称。
        private const val DATABASE_DIRECTORY_NAME = "databases"

        // 可清理缓存子目录名称。
        private const val CACHE_DIRECTORY_NAME = "cache"

        // 下载中临时文件子目录名称。
        private const val DOWNLOAD_TEMP_DIRECTORY_NAME = "temp/xy-downloads"

        // 默认下载完成目录名称。
        private const val DOWNLOAD_DIRECTORY_NAME = APP_DIRECTORY_NAME

        // 用户下载目录名称。
        private const val USER_DOWNLOADS_DIRECTORY_NAME = "Downloads"

        // macOS 持久应用数据父目录。
        private const val MACOS_APPLICATION_SUPPORT_DIRECTORY = "Library/Application Support"

        // macOS 可重建缓存父目录。
        private const val MACOS_CACHES_DIRECTORY = "Library/Caches"

        // Linux 默认持久应用数据父目录。
        private const val LINUX_DEFAULT_DATA_DIRECTORY = ".local/share"

        // Linux 默认可重建缓存父目录。
        private const val LINUX_DEFAULT_CACHE_DIRECTORY = ".cache"
    }
}

// 将空白环境变量视为未配置，避免拼出无意义根路径。
private fun String?.orBlankToNull(): String? {
    return this?.takeIf { it.isNotBlank() }
}
