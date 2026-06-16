package cn.xybbz.config.security

import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.enums.CredentialStoreType
import org.koin.core.annotation.Singleton

/**
 * 连接凭据管理器。
 */
@Singleton
class ConnectionCredentialManager(
    private val credentialStoreFactory: ConnectionCredentialStoreFactory? = null,
    /**
     * 测试时可直接注入假的安全存储实现。
     */
    private val credentialStoreOverride: ConnectionCredentialStore? = null,
) {
    private val credentialStore by lazy {
        credentialStoreOverride ?: requireNotNull(credentialStoreFactory) {
            "缺少 ConnectionCredentialStoreFactory"
        }.create()
    }

    /**
     * 当前平台使用的凭据存储类型。
     */
    suspend fun currentStoreType(connectionConfig: ConnectionConfig? = null): CredentialStoreType {
        return credentialStore.currentStoreType(connectionConfig)
    }

    /**
     * 当前平台安全存储是否可用。
     */
    suspend fun isAvailable(): Boolean {
        return credentialStore.isAvailable()
    }

    /**
     * 保存连接密码并回写引用值。
     */
    suspend fun savePassword(connectionConfig: ConnectionConfig, password: String): ConnectionConfig {
        ensureAvailable()
        val reference = credentialStore.save(connectionConfig, password)
        val storeType = credentialStore.currentStoreType(connectionConfig)
        return connectionConfig.copy(
            currentPassword = reference,
            iv = "",
            credentialStoreType = storeType
        )
    }

    /**
     * 读取连接密码。
     */
    suspend fun loadPassword(connectionConfig: ConnectionConfig): String? {
        if (connectionConfig.currentPassword.isBlank() || connectionConfig.credentialStoreType == CredentialStoreType.NONE) {
            return null
        }
        ensureAvailable()
        return credentialStore.load(connectionConfig)
    }

    /**
     * 删除连接密码并清空引用值。
     */
    suspend fun clearPassword(connectionConfig: ConnectionConfig) {
        if (connectionConfig.currentPassword.isBlank()) {
            return
        }
        runCatching {
            credentialStore.delete(connectionConfig)
        }
    }

    /**
     * 按当前平台配置迁移已保存凭据的存储类型。
     */
    suspend fun migratePasswordStore(connectionConfig: ConnectionConfig): ConnectionConfig {
        val targetStoreType = credentialStore.currentStoreType(connectionConfig)
        if (connectionConfig.currentPassword.isBlank()) {
            return connectionConfig.copy(
                iv = "",
                credentialStoreType = targetStoreType
            )
        }
        if (connectionConfig.credentialStoreType == targetStoreType) {
            return connectionConfig
        }
        ensureAvailable()
        val password = credentialStore.load(connectionConfig) ?: return connectionConfig.copy(
            currentPassword = "",
            iv = "",
            credentialStoreType = CredentialStoreType.NONE
        )
        val updatedConfig = connectionConfig.copy(
            iv = "",
            credentialStoreType = targetStoreType
        )
        val reference = credentialStore.save(updatedConfig, password)
        return updatedConfig.copy(
            currentPassword = reference,
            iv = ""
        )
    }

    private suspend fun ensureAvailable() {
        if (!credentialStore.isAvailable()) {
            throw CredentialStoreException("当前设备安全存储不可用")
        }
    }
}
