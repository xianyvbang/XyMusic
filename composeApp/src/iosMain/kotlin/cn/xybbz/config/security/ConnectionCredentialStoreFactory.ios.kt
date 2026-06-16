package cn.xybbz.config.security

import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.enums.CredentialStoreType
import cn.xybbz.config.info.supportsICloudPasswordSync
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Singleton
import org.koin.mp.KoinPlatform
import cn.xybbz.config.setting.SettingsManager
import platform.CoreFoundation.CFDictionaryRef
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.*

/**
 * iOS 连接凭据存储工厂。
 */
@Singleton
actual class ConnectionCredentialStoreFactory actual constructor() {
    actual fun create(): ConnectionCredentialStore {
        val settingsSyncPolicy = KoinPlatform.getKoin().get<IosCredentialSyncPolicy>()
        return IosConnectionCredentialStore(settingsSyncPolicy)
    }
}

/**
 * iOS Keychain 同步策略。
 */
class IosCredentialSyncPolicy(
    private val settingsRepository: IosCredentialSyncSettingsProvider,
) {
    /**
     * 当前是否允许 iCloud Keychain 同步。
     */
    suspend fun isSyncEnabled(): Boolean {
        return settingsRepository.isSyncPasswordsByICloudEnabled()
    }
}

/**
 * iOS Keychain 设置读取接口。
 */
interface IosCredentialSyncSettingsProvider {
    /**
     * 是否允许 iCloud Keychain 同步。
     */
    suspend fun isSyncPasswordsByICloudEnabled(): Boolean
}

/**
 * iOS Keychain 同步设置提供者。
 */
class SettingsManagerIosCredentialSyncSettingsProvider(
    private val settingsManager: SettingsManager,
) : IosCredentialSyncSettingsProvider {
    override suspend fun isSyncPasswordsByICloudEnabled(): Boolean {
        return settingsManager.getLatest().ifSyncPasswordsByICloud
    }
}

/**
 * iOS Keychain 凭据存储实现。
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosConnectionCredentialStore(
    private val syncPolicy: IosCredentialSyncPolicy,
) : ConnectionCredentialStore {
    override suspend fun currentStoreType(connectionConfig: ConnectionConfig?): CredentialStoreType {
        if (!supportsICloudPasswordSync()) {
            return CredentialStoreType.IOS_KEYCHAIN_LOCAL
        }
        return if (syncPolicy.isSyncEnabled()) {
            CredentialStoreType.IOS_KEYCHAIN_SYNCABLE
        } else {
            CredentialStoreType.IOS_KEYCHAIN_LOCAL
        }
    }

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.Default) {
        runCatching {
            currentStoreType(null)
            true
        }.getOrDefault(false)
    }

    override suspend fun save(connectionConfig: ConnectionConfig, password: String): String = withContext(Dispatchers.Default) {
        val key = connectionConfig.currentPassword.takeIf { it.isNotBlank() }
            ?: ConnectionCredentialStoreKey.build(connectionConfig)
        val passwordData = password.encodeToByteArray().toNSData()
        val synchronizable = syncPolicy.isSyncEnabled()
        deleteByKey(key)
        val query = mutableMapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to KEYCHAIN_SERVICE,
            kSecAttrAccount to key,
            kSecValueData to passwordData,
            kSecAttrSynchronizable to if (synchronizable) kCFBooleanTrue else kCFBooleanFalse
        )
        val status = SecItemAdd(query as CFDictionaryRef, null)
        if (status != errSecSuccess) {
            throw CredentialStoreException("Keychain 写入失败: $status")
        }
        return@withContext key
    }

    override suspend fun load(connectionConfig: ConnectionConfig): String? = withContext(Dispatchers.Default) {
        val synchronizable = when (connectionConfig.credentialStoreType) {
            CredentialStoreType.IOS_KEYCHAIN_SYNCABLE -> kSecAttrSynchronizableAny
            else -> kCFBooleanFalse
        }
        memScoped {
            val query = mutableMapOf<Any?, Any?>(
                kSecClass to kSecClassGenericPassword,
                kSecAttrService to KEYCHAIN_SERVICE,
                kSecAttrAccount to connectionConfig.currentPassword,
                kSecReturnData to kCFBooleanTrue!!,
                kSecMatchLimit to kSecMatchLimitOne,
                kSecAttrSynchronizable to synchronizable
            )
            val result = alloc<platform.CoreFoundation.CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, result.ptr)
            if (status == errSecItemNotFound) {
                return@withContext null
            }
            if (status != errSecSuccess) {
                throw CredentialStoreException("Keychain 读取失败: $status")
            }
            val data = result.value as NSData
            return@withContext data.toByteArray().decodeToString()
        }
    }

    override suspend fun delete(connectionConfig: ConnectionConfig) = withContext(Dispatchers.Default) {
        deleteByKey(connectionConfig.currentPassword)
    }

    private fun deleteByKey(key: String) {
        val query = mutableMapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to KEYCHAIN_SERVICE,
            kSecAttrAccount to key,
            kSecAttrSynchronizable to kSecAttrSynchronizableAny
        )
        SecItemDelete(query as CFDictionaryRef)
    }

    private fun ByteArray.toNSData(): NSData {
        return usePinned {
            NSData.create(bytes = it.addressOf(0), length = size.toULong())
        }
    }

    private fun NSData.toByteArray(): ByteArray {
        val length = this.length.toInt()
        val byteArray = ByteArray(length)
        byteArray.usePinned {
            platform.posix.memcpy(it.addressOf(0), this.bytes, this.length)
        }
        return byteArray
    }

    private companion object {
        const val KEYCHAIN_SERVICE = "cn.xybbz.connection.credentials"
    }
}
