package cn.xybbz.config.info

/**
 * JVM 桌面端不支持 iCloud Keychain 密码同步。
 */
actual fun supportsICloudPasswordSync(): Boolean = false
