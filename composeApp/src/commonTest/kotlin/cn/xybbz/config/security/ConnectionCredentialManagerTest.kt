package cn.xybbz.config.security

import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.enums.CredentialStoreType
import cn.xybbz.localdata.enums.DataSourceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 连接凭据管理器测试。
 */
class ConnectionCredentialManagerTest {
    /**
     * 保存密码后应只回写引用值与安全存储类型，不保留旧 iv。
     */
    @Test
    fun savePasswordReturnsReferenceOnly() = runSuspendTest {
        val store = FakeConnectionCredentialStore(
            storeType = CredentialStoreType.DESKTOP_KEYCHAIN,
            available = true,
            saveResult = "credential-ref"
        )
        val manager = ConnectionCredentialManager(credentialStoreOverride = store)

        val updated = manager.savePassword(sampleConnectionConfig(iv = "legacy-iv"), "plain-password")

        assertEquals("credential-ref", updated.currentPassword)
        assertEquals("", updated.iv)
        assertEquals(CredentialStoreType.DESKTOP_KEYCHAIN, updated.credentialStoreType)
        assertEquals("plain-password", store.savedPassword)
    }

    /**
     * 当旧存储类型与当前策略不一致且读取失败时，应清空本地引用并要求重新登录。
     */
    @Test
    fun migratePasswordStoreClearsReferenceWhenCredentialMissing() = runSuspendTest {
        val store = FakeConnectionCredentialStore(
            storeType = CredentialStoreType.IOS_KEYCHAIN_SYNCABLE,
            available = true,
            loadResult = null
        )
        val manager = ConnectionCredentialManager(credentialStoreOverride = store)

        val migrated = manager.migratePasswordStore(
            sampleConnectionConfig(
                currentPassword = "old-ref",
                credentialStoreType = CredentialStoreType.IOS_KEYCHAIN_LOCAL,
                iv = "legacy-iv"
            )
        )

        assertEquals("", migrated.currentPassword)
        assertEquals("", migrated.iv)
        assertEquals(CredentialStoreType.NONE, migrated.credentialStoreType)
    }

    /**
     * 当连接没有安全存储引用时，读取密码应直接返回空。
     */
    @Test
    fun loadPasswordReturnsNullWhenCredentialReferenceMissing() = runSuspendTest {
        val store = FakeConnectionCredentialStore(
            storeType = CredentialStoreType.ANDROID_KEYSTORE,
            available = true,
            loadResult = "unused"
        )
        val manager = ConnectionCredentialManager(credentialStoreOverride = store)

        val password = manager.loadPassword(
            sampleConnectionConfig(
                currentPassword = "",
                credentialStoreType = CredentialStoreType.NONE
            )
        )

        assertNull(password)
    }

    /**
     * 使用最小 suspend 包装，避免在 commonTest 引入额外协程测试依赖。
     */
    private fun runSuspendTest(block: suspend () -> Unit) {
        kotlinx.coroutines.runBlocking {
            block()
        }
    }

    /**
     * 生成测试用连接配置。
     */
    private fun sampleConnectionConfig(
        currentPassword: String = "",
        credentialStoreType: CredentialStoreType = CredentialStoreType.NONE,
        iv: String = ""
    ): ConnectionConfig {
        return ConnectionConfig(
            id = 1,
            serverId = "server-id",
            serverName = "server-name",
            serverVersion = "1.0.0",
            deviceId = "device-id",
            name = "demo",
            address = "https://demo.example.com",
            type = DataSourceType.JELLYFIN,
            userId = "user-id",
            username = "demo-user",
            currentPassword = currentPassword,
            iv = iv,
            credentialStoreType = credentialStoreType,
            ifEnabledDownload = true,
            ifEnabledDelete = false
        )
    }
}

/**
 * 测试用安全存储实现。
 */
private class FakeConnectionCredentialStore(
    private val storeType: CredentialStoreType,
    private val available: Boolean,
    private val saveResult: String = "saved-ref",
    private val loadResult: String? = null
) : ConnectionCredentialStore {
    /**
     * 记录最后一次保存的密码，方便断言。
     */
    var savedPassword: String? = null

    /**
     * 返回测试预设的安全存储类型。
     */
    override suspend fun currentStoreType(connectionConfig: ConnectionConfig?): CredentialStoreType {
        return storeType
    }

    /**
     * 返回测试预设的可用性。
     */
    override suspend fun isAvailable(): Boolean {
        return available
    }

    /**
     * 记录保存密码并返回测试预设引用值。
     */
    override suspend fun save(connectionConfig: ConnectionConfig, password: String): String {
        savedPassword = password
        return saveResult
    }

    /**
     * 返回测试预设的读取结果。
     */
    override suspend fun load(connectionConfig: ConnectionConfig): String? {
        return loadResult
    }

    /**
     * 测试桩无需执行真实删除逻辑。
     */
    override suspend fun delete(connectionConfig: ConnectionConfig) {
    }
}
