package cn.xybbz.config.info

/**
 * Android 不支持 iCloud Keychain 密码同步。
 */
actual fun supportsICloudPasswordSync(): Boolean = false
