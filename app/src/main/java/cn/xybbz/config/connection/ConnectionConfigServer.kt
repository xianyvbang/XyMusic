/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.config.connection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.connection.ConnectionConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    //是否有连接配置
    var ifConnectionConfig by mutableStateOf(false)
        private set

    //是否显示SnackBar
    var ifShowSnackBar by mutableStateOf(false)
        private set

    /**
     * 登录状态
     */
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
            val tmpConnectionConfig = db.connectionConfigDao.selectConnectionConfig()
            ifConnectionConfig = tmpConnectionConfig != null
            ifShowSnackBar = tmpConnectionConfig != null
            db.connectionConfigDao.selectConnectionConfigFlow().collect { connection ->
                connectionConfig = connection
                libraryId = connection?.libraryId
            }
        }
    }

    fun setConnectionConfigData(userInfo: ConnectionConfig?) {
        scope.launch {
            if (userInfo != null) {
                this@ConnectionConfigServer.connectionConfig = userInfo
                settingsManager.saveConnectionId(connectionId = userInfo.id)
                libraryId = userInfo.libraryId
            } else {
                updateLoginStates(false)
                this@ConnectionConfigServer.connectionConfig = null
                settingsManager.saveConnectionId(connectionId = null)
            }
        }

    }


    fun getUserId(): String {
        return connectionConfig?.userId ?: ""
    }

    fun getConnectionId(): Long {
        return connectionConfig?.id ?: Constants.ZERO.toLong()
    }

    fun getAddress(): String {
        return connectionConfig?.address ?: ""
    }

    fun clear() {
        connectionConfig = null
        ifConnectionConfig = false
        ifShowSnackBar = false
    }

    fun updateLoginStates(value: Boolean) {
        if (value) {
            scope.launch {
                _loginSuccessEvent.emit(Unit)
            }
        }
    }

    fun updateLibraryId(libraryId: String?) {
        this.libraryId = libraryId
    }

    fun updateIfShowSnackBar(ifShowSnackBar: Boolean) {
        this.ifShowSnackBar = ifShowSnackBar
    }

    fun updateIfConnectionConfig(ifConnectionConfig: Boolean) {
        this.ifConnectionConfig = ifConnectionConfig
        this.ifShowSnackBar = ifConnectionConfig
    }

}