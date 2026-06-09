package cn.xybbz.config.info

import cn.xybbz.common.constants.Constants
import cn.xybbz.platform.ContextWrapper
import java.net.InetAddress

private const val DESKTOP_PLATFORM_NAME = "Desktop"
private const val UNKNOWN_DEVICE_NAME = "Unknown Device"
// 桌面端包版本属性由 desktopApp 的 Gradle 配置注入。
private const val DESKTOP_PACKAGE_VERSION_PROPERTY = "cn.xybbz.packageVersion"

actual fun getPlatformInfo(contextWrapper: ContextWrapper): PlatformInfo {
    val osName = System.getProperty("os.name").orEmpty()
    val osVersion = System.getProperty("os.version").orEmpty()
    // 优先读取桌面端 packageVersion，避免关于页显示 Java 运行时版本。
    val packageVersion = System.getProperty(DESKTOP_PACKAGE_VERSION_PROPERTY).orEmpty()

    val deviceName = runCatching {
        InetAddress.getLocalHost().hostName
    }.getOrNull()?.takeIf { it.isNotBlank() }
        ?: buildString {
            append(osName.ifBlank { DESKTOP_PLATFORM_NAME })
            if (osVersion.isNotBlank()) {
                append(" ")
                append(osVersion)
            }
        }.ifBlank { UNKNOWN_DEVICE_NAME }

    val appVersion = packageVersion.takeIf { it.isNotBlank() }
        ?: PlatformInfo::class.java.`package`?.implementationVersion
            ?.takeIf { it.isNotBlank() }
        ?: ""

    return PlatformInfo(
        appName = Constants.APP_NAME,
        deviceName = deviceName,
        platformName = osName.ifBlank { DESKTOP_PLATFORM_NAME },
        platformVersion = appVersion
    )
}
