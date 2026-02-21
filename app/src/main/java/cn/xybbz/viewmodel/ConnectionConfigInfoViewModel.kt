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

import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import cn.xybbz.R
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.enums.LoginType
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.PasswordUtils
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.entity.data.EncryptAesData
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
    private val db: DatabaseClient,
    val backgroundConfig: BackgroundConfig
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(connectionId: Long): ConnectionConfigInfoViewModel
    }

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

                    //此处还原密码
                    if (thisConnectionConfig.currentPassword.isNotBlank()) {
                        val decryptPassword = PasswordUtils.decryptAES(
                            EncryptAesData(
                                aesKey = thisConnectionConfig.key,
                                aesIv = thisConnectionConfig.iv,
                                aesData = thisConnectionConfig.currentPassword
                            )
                        )
                        if (decryptPassword.isNotBlank()) {
                            this@ConnectionConfigInfoViewModel.password = decryptPassword
                        }
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
    fun updateConnectionConfig() {
        this.connectionConfig?.let { config ->
            saveAddress()
            saveName()
            viewModelScope.launch {
                //更新密码
                val encryptAES = PasswordUtils.encryptAES(password)
                val config = config.copy(
                    currentPassword = encryptAES.aesData,
                    iv = encryptAES.aesIv,
                    key = encryptAES.aesKey,
                    name = connectionName,
                    username = username,
                    address = address,
                    ifForceLogin = (ifPasswordChange || ifUsernameChange || ifAddressChange) && getConnectionId() != connectionId
                )
                connectionConfig = config
                dataSourceManager.updateConnectionConfig(
                    config
                )
            }
        }
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

        MessageUtils.sendPopTipSuccess("修改成功")
    }

    /**
     * 更新地址信息
     */
    fun saveAddress() {
        if (address.isBlank()) {
            MessageUtils.sendPopTip(R.string.connection_address_cannot_be_empty)
            return
        }
    }

    /**
     * 更新名称
     */
    fun saveName() {
        if (connectionName.isBlank()) {
            MessageUtils.sendPopTip(R.string.alias_cannot_be_empty)
            return
        }
    }

    fun getConnectionId(): Long {
        return dataSourceManager.getConnectionId()
    }
}