package cn.xybbz.platform

import java.io.File
import java.util.Locale

actual class ContextWrapper {
    // JVM 路径解析使用的系统属性映射，测试可通过工厂注入隔离值。
    private val properties: Map<String, String>

    // JVM 路径解析使用的安装目录，测试可通过工厂注入临时目录。
    private val installationDirectory: File

    // JVM 运行时解析到的应用名称，默认沿用桌面安装包名称。
    val appName: String

    // JVM 运行时解析到的小写包名，用于需要稳定应用标识的目录或日志场景。
    val packageName: String

    // JVM 持久应用数据根目录，用于保存数据库、缓存和下载中的临时状态。
    val dataDirectory: File

    // JVM 数据库目录，Room 主文件和 SQLite 伴生文件统一落在这里。
    val databaseDirectory: File

    // JVM 可重建缓存目录，用于播放缓存等可清理数据。
    val cacheDirectory: File

    // JVM 下载中的临时文件父目录，具体下载临时子目录由下载模块常量决定。
    val downloadTempParentDirectory: File

    // JVM 默认下载完成目录，避免下载文件落到系统盘用户下载目录。
    val downloadDirectory: File

    // 兼容旧调用点的应用数据目录，后续新代码应优先使用上面的用途化目录。
    val applicationDirectory: File

    constructor() : this(
        properties = System.getProperties().stringPropertyNames().associateWith { propertyName ->
            System.getProperty(propertyName).orEmpty()
        },
        installationDirectory = resolveDefaultInstallationDirectory(),
    )

    private constructor(
        properties: Map<String, String>,
        installationDirectory: File,
    ) {
        this.properties = properties
        this.installationDirectory = installationDirectory.absoluteFile

        appName = resolveAppName()
        packageName = resolvePackageName()
        dataDirectory = resolveDataDirectory().absoluteFile
        databaseDirectory = File(dataDirectory, DATABASE_DIRECTORY_NAME).absoluteFile
        cacheDirectory = File(dataDirectory, CACHE_DIRECTORY_NAME).absoluteFile
        downloadTempParentDirectory = File(dataDirectory, TEMP_DIRECTORY_NAME).absoluteFile
        downloadDirectory = File(File(dataDirectory, DOWNLOADS_DIRECTORY_NAME), appName).absoluteFile
        applicationDirectory = dataDirectory
    }

    // 解析 JVM 应用数据根目录，优先使用覆盖属性，否则放到当前运行目录所在盘符根目录。
    private fun resolveDataDirectory(): File {
        properties[DATA_ROOT_PROPERTY].orBlankToNull()?.let { configuredRoot ->
            return File(configuredRoot)
        }

        val installRoot = installationDirectory.toPath().root?.toFile() ?: installationDirectory
        return File(installRoot, "${appName}Data")
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

        // 数据库子目录名称。
        private const val DATABASE_DIRECTORY_NAME = "database"

        // 可清理缓存子目录名称。
        private const val CACHE_DIRECTORY_NAME = "cache"

        // 下载中临时文件父目录名称。
        private const val TEMP_DIRECTORY_NAME = "temp"

        // 默认下载完成目录父目录名称。
        private const val DOWNLOADS_DIRECTORY_NAME = "Downloads"

        /**
         * 创建 JVM 测试专用上下文，避免测试读写真实用户目录。
         */
        fun createForTest(
            properties: Map<String, String>,
            installationDirectory: File,
        ): ContextWrapper {
            return ContextWrapper(
                properties = properties,
                installationDirectory = installationDirectory,
            )
        }

        // 解析 JVM 默认安装目录，优先使用代码所在目录，失败时退回当前工作目录。
        private fun resolveDefaultInstallationDirectory(): File {
            val codeSourceFile = runCatching {
                ContextWrapper::class.java.protectionDomain
                    ?.codeSource
                    ?.location
                    ?.toURI()
                    ?.let(::File)
                    ?.absoluteFile
            }.getOrNull()

            return when {
                codeSourceFile == null -> File(System.getProperty("user.dir").orEmpty().ifBlank { "." }).absoluteFile
                codeSourceFile.isFile -> codeSourceFile.parentFile ?: codeSourceFile
                else -> codeSourceFile
            }
        }
    }
}

// 将空白配置视为未配置，避免拼出无意义路径。
private fun String?.orBlankToNull(): String? {
    return this?.takeIf { it.isNotBlank() }
}
