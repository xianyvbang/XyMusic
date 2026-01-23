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

package cn.xybbz.viewmodel

import android.graphics.Color
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import cn.xybbz.R
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.DatabaseUtils
import cn.xybbz.common.utils.DefaultObjectUtils
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.PasswordUtils
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.connection.ConnectionConfigServer
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.connection.ConnectionConfig
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ConnectionConfigInfoViewModel.Factory::class)
class ConnectionConfigInfoViewModel @OptIn(UnstableApi::class)
@AssistedInject constructor(
    @Assisted private val connectionId: Long,
    private val dataSourceManager: DataSourceManager,
    private val _connectionConfigServer: ConnectionConfigServer,
    private val db: DatabaseClient,
    private val musicController: MusicController,
    private val settingsManager: SettingsManager,
    private val _backgroundConfig: BackgroundConfig
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(connectionId: Long): ConnectionConfigInfoViewModel
    }

    val connectionConfigServer = _connectionConfigServer
    val backgroundConfig = _backgroundConfig

    var connectionConfig by mutableStateOf<ConnectionConfig?>(null)
        private set

    //用户名
    var username by mutableStateOf("")
        private set

    //密码
    var password by mutableStateOf<String?>("")
        private set

    //连接地址
    var address by mutableStateOf("")
        private set

    //临时链接地址
    var tmpAddress by mutableStateOf("")
        private set

    //名称
    var connectionName by mutableStateOf("")
        private set

    //当前媒体库
    var library by mutableStateOf(DefaultObjectUtils.getDefaultXyLibrary(connectionId))
        private set

    init {
        getConnectionConfig()

    }

    private fun getConnectionConfig() {
        viewModelScope.launch {
            db.connectionConfigDao.selectByIdFlow(connectionId).collect { thisConnectionConfig ->
                if (thisConnectionConfig != null) {
                    connectionConfig = thisConnectionConfig
                    this@ConnectionConfigInfoViewModel.password = ""
                    this@ConnectionConfigInfoViewModel.username = thisConnectionConfig.username
                    this@ConnectionConfigInfoViewModel.address = thisConnectionConfig.address
                    this@ConnectionConfigInfoViewModel.connectionName = thisConnectionConfig.name
                    getLibrary(thisConnectionConfig.libraryId)
                }
            }


        }
    }

    //查询媒体库数据
    private suspend fun getLibrary(libraryId: String?) {
        if (libraryId.isNullOrBlank()) {
            library = DefaultObjectUtils.getDefaultXyLibrary(connectionId)
        } else {
            val libraryList = db.libraryDao.selectListByConnectionId(connectionId)
            if (libraryList.isNotEmpty()) {
                val xyLibrary = libraryList.findLast { library -> library.id == libraryId }
                if (xyLibrary != null)
                    library = xyLibrary
            }
        }

    }

    fun updatePassword(data: String?) {
        password = data
    }

    /**
     * 更新用户密码,并且判断是否需要重新登录
     */
    fun savePasswordAndLogin() {
        this.connectionConfig?.let { config ->
            viewModelScope.launch {
                //更新密码
                val encryptAES = PasswordUtils.encryptAES(password ?: "")

                connectionConfigServer.updateConnectionConfig(
                    config.copy(
                        currentPassword = encryptAES.aesData,
                        iv = encryptAES.aesIv,
                        key = encryptAES.aesKey
                    )
                )
                if (connectionConfigServer.getConnectionId() == connectionId) {
                    viewModelScope.launch {
                        dataSourceManager.restartLogin()
                    }
                }

                password = ""
            }
        }


    }

    /**
     * 删除当前连接
     */
    @OptIn(UnstableApi::class)
    suspend fun removeThisConnection() {

        if (connectionConfigServer.getConnectionId() == connectionId) {
            //如果当前链接是最后链接,则直接删除数据里的数据
            val connectionCount = db.connectionConfigDao.selectCount()
            if (connectionCount == 0) {
                DatabaseUtils.clearDatabaseByConnectionConfig(
                    db,
                    connectionId
                )
                musicController.clearPlayerList()
                dataSourceManager.release()
                settingsManager.setSettingsData()
            } else {
                MessageUtils.sendPopTip(
                    R.string.cannot_delete_current_connection,
                    backgroundColor = Color.RED
                )
            }
        } else {
            DatabaseUtils.clearDatabaseByConnectionConfig(
                db,
                connectionId
            )
        }

    }


    /**
     * 更新临时地址数据
     */
    fun updateTmpAddress(data: String) {
        this.tmpAddress = data
    }

    /**
     * 更新地址信息
     */
    fun saveAddress() {
        if (tmpAddress.isBlank()) {
            MessageUtils.sendPopTip(R.string.connection_address_cannot_be_empty)
            return
        }
        //更新链接地址
        viewModelScope.launch {
            //更新所有数据使得列表失效
            db.albumDao.updateUrlByConnectionId(address, tmpAddress)
            db.artistDao.updateUrlByConnectionId(address, tmpAddress)
            db.genreDao.updateUrlByConnectionId(address, tmpAddress)

            address = tmpAddress
            db.connectionConfigDao.updateAddress(
                address = address,
                connectionId = connectionId
            )
            //判断是否需要重新登录
            if (connectionConfigServer.getConnectionId() == connectionId) {
                Log.i("connection", "数据加载3")
                dataSourceManager.restartLogin()
            }

        }
    }

    /**
     * 还原地址
     */
    fun reductionAddress() {
        address = connectionConfig?.address ?: ""
    }

    /**
     * 设置名称
     */
    fun updateTmpName(data: String) {
        this.connectionName = data
    }

    /**
     * 还原名称
     */
    fun reductionName() {
        connectionName = connectionConfig?.name ?: ""
    }

    /**
     * 更新名称
     */
    fun saveName() {
        if (connectionName.isBlank()) {
            MessageUtils.sendPopTip(R.string.alias_cannot_be_empty)
            return
        }
        //更新链接地址
        viewModelScope.launch {
            db.connectionConfigDao.updateName(
                name = connectionName,
                connectionId = connectionId
            )
        }
    }
}