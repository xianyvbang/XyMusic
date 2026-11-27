package cn.xybbz.platform

import java.io.File
import java.util.Locale

actual class ContextWrapper {
    // JVM 路径解析使用的系统属性映射，测试可通过工厂注入隔离值。
    private val properties: Map<String, String>

    // JVM 运行时解析到的应用名称，默认沿用桌面安装包名称。
    val appName: String

    // JVM 运行时解析到的小写包名，用于需要稳定应用标识的目录或日志场景。
    val packageName: String

    // JVM 持久应用数据根目录（数据库/配置），默认位于用户漫游数据目录。
    val dataDirectory: File

    // JVM 用户本地数据根目录（缓存文件），默认位于用户本地数据目录。
    val localDataDirectory: File

    // JVM 数据库目录，Room 主文件和 SQLite 伴生文件统一落在这里。
    val databaseDirectory: File

    // JVM 可重建缓存目录，默认位于用户本地数据目录，用于播放缓存等可清理数据。
    val cacheDirectory: File

    // JVM 默认下载完成目录，默认位于用户音乐目录，方便用户直接查看。
    val downloadDirectory: File

    // 兼容旧调用点的应用数据目录，后续新代码应优先使用上面的用途化目录。
    val applicationDirectory: File

    constructor() : this(
        properties = System.getProperties().stringPropertyNames().associateWith { propertyName ->
            System.getProperty(propertyName).orEmpty()
        },
    )

    private constructor(
        properties: Map<String, String>,
    ) {
        this.properties = properties

        appName = resolveAppName()
        packageName = resolvePackageName()

        // 显式覆盖根目录优先，测试或高级用户可通过系统属性统一指定所有目录。
        val overrideRoot = properties[DATA_ROOT_PROPERTY].orBlankToNull()?.let(::File)

        dataDirectory = (overrideRoot ?: File(resolveRoamingRoot(), appName)).absoluteFile
        localDataDirectory = (overrideRoot ?: File(resolveLocalRoot(), appName)).absoluteFile
        databaseDirectory = File(dataDirectory, DATABASE_DIRECTORY_NAME).absoluteFile
        cacheDirectory = File(localDataDirectory, CACHE_DIRECTORY_NAME).absoluteFile
        downloadDirectory = (overrideRoot?.let { File(File(it, DOWNLOADS_DIRECTORY_NAME), appName) }
            ?: File(resolveMusicRoot(), appName)).absoluteFile
        applicationDirectory = dataDirectory
    }

    // 解析用户漫游数据根目录（Windows: %APPDATA%），数据库和配置放在这里。
    private fun resolveRoamingRoot(): File {
        return envDirectory(APPDATA_ENV) ?: File(userHome(), ".config")
    }

    // 解析用户本地数据根目录（Windows: %LOCALAPPDATA%），缓存文件放在这里。
    private fun resolveLocalRoot(): File {
        return envDirectory(LOCAL_APPDATA_ENV) ?: File(userHome(), ".cache")
    }

    // 解析用户音乐目录（Windows: %USERPROFILE%\Music），下载的音乐放在这里。
    private fun resolveMusicRoot(): File {
        return envDirectory(USER_PROFILE_ENV)?.let { File(it, MUSIC_DIRECTORY_NAME) }
            ?: File(userHome(), MUSIC_DIRECTORY_NAME)
    }

    // 读取目录类环境变量，优先使用注入属性，便于测试隔离。
    private fun envDirectory(name: String): File? {
        val value = properties[name].orBlankToNull() ?: System.getenv(name).orBlankToNull()
        return value?.let(::File)
    }

    // 解析用户主目录，作为非 Windows 平台的兜底。
    private fun userHome(): File {
        val home = properties[USER_HOME_PROPERTY].orBlankToNull()
            ?: System.getProperty(USER_HOME_PROPERTY).orEmpty()
        return File(home.ifBlank { "." })
    }

    // 解析桌面应用名称，空白配置回退到默认名称。
    private fun resolveAppName(): String {
        return properties[PACKAGE_NAME_PROPERTY].orBlankToNull() ?: DEFAULT_APP_NAME
    }

    // 解析小写包名，使用 Locale.ROOT 避免系统语言影响大小写转换。
    private fun resolvePackageName(): String {
        return resolveAppName().lowercase(Locale.ROOT)
    }

    companion object {
        // 桌面端注入包名使用的系统属性名。
        const val PACKAGE_NAME_PROPERTY: String = "cn.xybbz.packageName"

        // 测试或开发环境覆盖 JVM 数据根目录使用的系统属性名。
        const val DATA_ROOT_PROPERTY: String = "cn.xybbz.dataRoot"

        // 桌面应用默认名称，未注入包名时使用。
        const val DEFAULT_APP_NAME: String = "XyMusic"

        // Windows 用户漫游数据目录环境变量名。
        private const val APPDATA_ENV = "APPDATA"

        // Windows 用户本地数据目录环境变量名。
        private const val LOCAL_APPDATA_ENV = "LOCALAPPDATA"

        // Windows 用户主目录环境变量名。
        private const val USER_PROFILE_ENV = "USERPROFILE"

        // 用户主目录系统属性名，用于非 Windows 平台兜底。
        private const val USER_HOME_PROPERTY = "user.home"

        // 数据库子目录名称。
        private const val DATABASE_DIRECTORY_NAME = "database"

        // 可清理缓存子目录名称。
        private const val CACHE_DIRECTORY_NAME = "cache"

        // 默认下载完成目录父目录名称。
        private const val DOWNLOADS_DIRECTORY_NAME = "Downloads"

        // 用户音乐目录名称。
        private const val MUSIC_DIRECTORY_NAME = "Music"

        /**
         * 创建 JVM 测试专用上下文，避免测试读写真实用户目录。
         */
        fun createForTest(
            properties: Map<String, String>,
        ): ContextWrapper {
            return ContextWrapper(
                properties = properties,
            )
        }
    }
}

// 将空白配置视为未配置，避免拼出无意义路径。
private fun String?.orBlankToNull(): String? {
    return this?.takeIf { it.isNotBlank() }
}
