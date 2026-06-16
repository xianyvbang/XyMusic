package cn.xybbz.localdata.enums

/**
 * 连接凭据持久化类型。
 */
enum class CredentialStoreType {
    /**
     * 不存在可用凭据。
     */
    NONE,

    /**
     * Android Keystore 本地安全存储。
     */
    ANDROID_KEYSTORE,

    /**
     * iOS 本机 Keychain。
     */
    IOS_KEYCHAIN_LOCAL,

    /**
     * iOS 可同步 Keychain。
     */
    IOS_KEYCHAIN_SYNCABLE,

    /**
     * 桌面端系统钥匙串。
     */
    DESKTOP_KEYCHAIN,
}
