package cn.xybbz.api.client

import cn.xybbz.localdata.data.connection.ConnectionConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * 数据源连接上下文快照持有器。
 */
internal class DataSourceConnectionState {
    /**
     * 当前连接上下文快照。
     */
    data class Snapshot(
        /**
         * 当前连接配置。
         */
        val connectionConfig: ConnectionConfig? = null,
        /**
         * 当前连接选中的媒体库 ID 列表。
         */
        val libraryIds: List<String>? = null
    )

    /**
     * 当前连接上下文的原子发布流。
     */
    private val state = MutableStateFlow(Snapshot())

    /**
     * 读取当前连接上下文快照。
     */
    fun snapshot(): Snapshot {
        return state.value
    }

    /**
     * 绑定完整连接配置，并同步媒体库 ID。
     */
    fun bindConnection(connectionConfig: ConnectionConfig) {
        state.value = Snapshot(
            connectionConfig = connectionConfig,
            libraryIds = connectionConfig.libraryIds
        )
    }

    /**
     * 更新当前连接配置。
     */
    fun updateConnection(connectionConfig: ConnectionConfig) {
        bindConnection(connectionConfig)
    }

    /**
     * 更新当前连接的媒体库 ID，并同步回连接配置副本。
     */
    fun updateLibraryIds(libraryIds: List<String>?) {
        state.update { snapshot ->
            snapshot.copy(
                connectionConfig = snapshot.connectionConfig?.copy(libraryIds = libraryIds),
                libraryIds = libraryIds
            )
        }
    }

    /**
     * 清空当前连接上下文。
     */
    fun clear() {
        state.value = Snapshot()
    }
}
