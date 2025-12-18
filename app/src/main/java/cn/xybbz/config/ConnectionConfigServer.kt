package cn.xybbz.config

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.connection.ConnectionConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 用户信息关联
 */
class ConnectionConfigServer(
    private val db: DatabaseClient,
    private val settingsManager: SettingsManager
) {

    private val scope = CoroutineScopeUtils.getIo(this.javaClass.name)


    private var connectionConfig by mutableStateOf<ConnectionConfig?>(null)


    var libraryId by mutableStateOf<String?>(null)
        private set

    /**
     * 登录状态
     */
    private val _loginStateFlow = MutableStateFlow(false)

    private val loginStateFlow: StateFlow<Boolean> = _loginStateFlow.asStateFlow()

    private val _loginSuccessEvent = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 1
    )
    val loginSuccessEvent = _loginSuccessEvent.asSharedFlow()



    /**
     * 初始化数据
     */
    fun initData() {
        scope.launch {
            val connection = db.connectionConfigDao.selectConnectionConfig()
            connectionConfig = connection
            libraryId = connection?.libraryId
        }
    }

    suspend fun updateConnectionAddress() {
        val connection = db.connectionConfigDao.selectConnectionConfig()
        connectionConfig = connection
        libraryId = connection?.libraryId
    }

    suspend fun setConnectionConfigData(userInfo: ConnectionConfig?) {
        if (userInfo != null) {
            this.connectionConfig = userInfo
            settingsManager.saveConnectionId(connectionId = userInfo.id)
            libraryId = userInfo.libraryId
        } else {
            updateLoginStates(false)
            Log.i("===============","登录状态变化2 false")
            this.connectionConfig = null
            settingsManager.saveConnectionId(connectionId = null)
        }
    }


    fun getUserId(): String {
        return connectionConfig?.userId ?: ""
    }

    fun getConnectionId(): Long {
        return connectionConfig?.id ?: Constants.ZERO.toLong()
    }

    fun getAddress():String {
        return connectionConfig?.address ?: ""
    }

    fun clear() {
        connectionConfig = null
    }

    fun updateLoginStates(value: Boolean) {
        Log.i("===============","登录状态变化$value")
        _loginStateFlow.value = value
        if (value){
            scope.launch {
                _loginSuccessEvent.emit(Unit)
            }
        }
    }

    fun updateLibraryId(libraryId: String?) {
        this.libraryId = libraryId
    }

}