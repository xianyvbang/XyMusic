package cn.xybbz.config.info

import cn.xybbz.common.constants.Constants
import cn.xybbz.platform.ContextWrapper
import java.net.InetAddress

private const val DESKTOP_PLATFORM_NAME = "Desktop"
private const val UNKNOWN_DEVICE_NAME = "Unknown Device"

actual fun getPlatformInfo(contextWrapper: ContextWrapper): PlatformInfo {
    val osName = System.getProperty("os.name").orEmpty()
    val osVersion = System.getProperty("os.version").orEmpty()
    val javaVersion = System.getProperty("java.version").orEmpty()

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

    val appVersion = PlatformInfo::class.java.`package`?.implementationVersion
        ?.takeIf { it.isNotBlank() }
        ?: javaVersion

    return PlatformInfo(
        appName = Constants.APP_NAME,
        deviceName = deviceName,
        platformName = osName.ifBlank { DESKTOP_PLATFORM_NAME },
        platformVersion = appVersion
    )
}
