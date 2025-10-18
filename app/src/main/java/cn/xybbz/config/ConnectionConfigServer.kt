package cn.xybbz.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.common.constants.Constants
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.connection.ConnectionConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

/**
 * 用户信息关联
 */
class ConnectionConfigServer(
    private val db: DatabaseClient,
    private val settingsConfig: SettingsConfig
) {

    var connectionConfig by mutableStateOf<ConnectionConfig?>(null)
        private set

    var libraryId by mutableStateOf<String?>(null)
        private set

    /**
     * 登录状态
     */
    private val _loginStateFlow = MutableStateFlow(false)

    val loginStateFlow: StateFlow<Boolean> = _loginStateFlow.asStateFlow()


    /**
     * 初始化数据
     */
    fun initData() {
        val connection = runBlocking {
            db.connectionConfigDao.selectConnectionConfig()
        }
        connectionConfig = connection
        libraryId = connection?.libraryId
    }

    suspend fun updateConnection() {
        val connection = db.connectionConfigDao.selectConnectionConfig()
        connectionConfig = connection
        libraryId = connection?.libraryId
    }

    suspend fun setConnectionConfigData(userInfo: ConnectionConfig?) {
        if (userInfo != null) {
            this.connectionConfig = userInfo
            settingsConfig.saveConnectionId(connectionId = userInfo.id)
            libraryId = userInfo.libraryId
        } else {
            _loginStateFlow.value = false
            this.connectionConfig = null
            settingsConfig.saveConnectionId(connectionId = null)
        }
    }


    fun getUserId(): String {
        return connectionConfig?.userId ?: ""
    }

    fun getConnectionId(): Long {
        return connectionConfig?.id ?: Constants.ZERO.toLong()
    }

    fun clear() {
        connectionConfig = null
    }

    fun updateLoginStates(value: Boolean) {
        _loginStateFlow.value = value
    }

    fun updateLibraryId(libraryId: String?) {
        this.libraryId = libraryId
    }

    fun getIsLocal(): Boolean {
        return settingsConfig.get().isLocal
    }

}