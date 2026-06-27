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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.enums.LoginType
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.config.security.ConnectionCredentialManager
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.enums.CredentialStoreType
import cn.xybbz.config.security.CredentialStoreException
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.alias_cannot_be_empty
import xymusic.composeapp.generated.resources.connection_address_cannot_be_empty
import xymusic.composeapp.generated.resources.connection_credential_store_unavailable
import xymusic.composeapp.generated.resources.modify_success

@KoinViewModel
class ConnectionConfigInfoViewModel(
    @InjectedParam private val connectionId: Long,
    private val dataSourceManager: DataSourceManager,
    private val db: LocalDatabaseClient,
    private val credentialManager: ConnectionCredentialManager
) : ViewModel() {
    var connectionConfig by mutableStateOf<ConnectionConfig?>(null)
        private set

    //用户名
    var username by mutableStateOf("")
        private set

    //密码
    var password by mutableStateOf("")
        private set

    //连接地址
    var address by mutableStateOf("")
        private set

    //名称
    var connectionName by mutableStateOf("")
        private set

    //用户名是否变更
    private var ifUsernameChange = false

    //密码是否变更
    private var ifPasswordChange = false

    //地址是否变更
    private var ifAddressChange = false


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

                    if (thisConnectionConfig.currentPassword.isNotBlank() &&
                        thisConnectionConfig.credentialStoreType != CredentialStoreType.NONE
                    ) {
                        this@ConnectionConfigInfoViewModel.password = runCatching {
                            credentialManager.loadPassword(thisConnectionConfig).orEmpty()
                        }.getOrDefault("")
                    }
                    this@ConnectionConfigInfoViewModel.connectionName = thisConnectionConfig.name
                }
            }
        }
    }

    fun updatePassword(data: String) {
        ifPasswordChange = data != password
        password = data
    }

    fun updateUsername(data: String) {
        ifUsernameChange = data != username
        username = data
    }

    fun updateAddress(data: String) {
        ifAddressChange = data != address
        address = data
    }

    /**
     * 设置名称
     */
    fun updateConnectionName(data: String) {
        this.connectionName = data
    }

    /**
     * 更新用户密码,并且判断是否需要重新登录
     */
    suspend fun updateConnectionConfig(): Boolean {
        val currentConfig = connectionConfig ?: return false
        if (!saveAddress() || !saveName()) {
            return false
        }

        var updatedConfig = currentConfig.copy(
            name = connectionName,
            username = username,
            address = address
        )
        if (password.isNotBlank()) {
            try {
                updatedConfig = credentialManager.savePassword(updatedConfig, password)
            } catch (e: CredentialStoreException) {
                MessageUtils.sendPopTipError(Res.string.connection_credential_store_unavailable)
                return false
            }
        }
        connectionConfig = updatedConfig
        dataSourceManager.updateConnectionConfig(updatedConfig)
        return true
    }

    fun restartLogin() {
        //如果是当前连接就重新登陆,否则就不重新登陆了,等待下次开启连接的时候登陆
        //判断是否修改了数据,如果修改了的话,点击保存的时候重新登陆
        if ((ifPasswordChange || ifUsernameChange || ifAddressChange) && getConnectionId() == connectionId)
            viewModelScope.launch {
                dataSourceManager.serverLogin(
                    loginType = LoginType.API,
                    connectionConfig = connectionConfig
                )
            }

        MessageUtils.sendPopTipSuccess(Res.string.modify_success)
    }

    /**
     * 更新地址信息
     */
    fun saveAddress(): Boolean {
        if (address.isBlank()) {
            MessageUtils.sendPopTip(Res.string.connection_address_cannot_be_empty)
            return false
        }
        return true
    }

    /**
     * 更新名称
     */
    fun saveName(): Boolean {
        if (connectionName.isBlank()) {
            MessageUtils.sendPopTip(Res.string.alias_cannot_be_empty)
            return false
        }
        return true
    }

    fun getConnectionId(): Long {
        return dataSourceManager.getConnectionId()
    }
}

