package cn.xybbz.api.client

import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.enums.CredentialStoreType
import cn.xybbz.localdata.enums.DataSourceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 数据源连接上下文状态测试。
 */
class DataSourceConnectionStateTest {
    /**
     * 绑定连接后应同步暴露连接配置和媒体库 ID。
     */
    @Test
    fun bindConnectionPublishesConfigAndLibraryIds() {
        val state = DataSourceConnectionState()
        val config = sampleConnectionConfig(libraryIds = listOf("library-1", "library-2"))

        state.bindConnection(config)

        val snapshot = state.snapshot()
        assertEquals(config, snapshot.connectionConfig)
        assertEquals(listOf("library-1", "library-2"), snapshot.libraryIds)
    }

    /**
     * 更新媒体库 ID 时应同步更新连接配置副本和独立快照字段。
     */
    @Test
    fun updateLibraryIdsUpdatesConfigCopyAndSnapshot() {
        val state = DataSourceConnectionState()
        state.bindConnection(sampleConnectionConfig(libraryIds = listOf("old-library")))

        state.updateLibraryIds(listOf("new-library"))

        val snapshot = state.snapshot()
        assertEquals(listOf("new-library"), snapshot.libraryIds)
        assertEquals(listOf("new-library"), snapshot.connectionConfig?.libraryIds)
    }

    /**
     * 清空连接后应返回空连接上下文。
     */
    @Test
    fun clearRemovesConnectionContext() {
        val state = DataSourceConnectionState()
        state.bindConnection(sampleConnectionConfig(libraryIds = listOf("library-1")))

        state.clear()

        val snapshot = state.snapshot()
        assertNull(snapshot.connectionConfig)
        assertNull(snapshot.libraryIds)
    }

    /**
     * 生成测试用连接配置。
     */
    private fun sampleConnectionConfig(
        libraryIds: List<String>? = null
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
            currentPassword = "credential-ref",
            credentialStoreType = CredentialStoreType.DESKTOP_KEYCHAIN,
            libraryIds = libraryIds,
            ifEnabledDownload = true,
            ifEnabledDelete = false
        )
    }
}
