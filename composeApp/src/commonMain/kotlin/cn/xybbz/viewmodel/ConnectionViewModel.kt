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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.client.IDataSourceServer
import cn.xybbz.api.client.data.ClientLoginInfoReq
import cn.xybbz.common.constants.Constants.HTTP
import cn.xybbz.common.constants.Constants.HTTPS
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.extractPortOrNull
import cn.xybbz.config.image.isAbsoluteNetworkUrl
import cn.xybbz.config.info.getPlatformInfo
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.di.ContextWrapper
import cn.xybbz.entity.data.ResourceData
import cn.xybbz.localdata.enums.DataSourceType
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.KoinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.empty_info
import xymusic_kmp.composeapp.generated.resources.plex_resource_error

/**
 * 连接model
 */
@KoinViewModel
class ConnectionViewModel(
    val dataSourceManager: DataSourceManager,
    val settingsManager: SettingsManager,
    val contextWrapper: ContextWrapper
) : ViewModel() {


    val options = listOf(HTTP, HTTPS)

    var dataSourceType by mutableStateOf<DataSourceType?>(null)
        private set

    private var tmpDataSourceParentServer by mutableStateOf<IDataSourceServer?>(null)

    var address by mutableStateOf("")
        private set

    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var tmpAddressList = mutableStateListOf<String>()
        private set

    var tmpPlexInfo by mutableStateOf<List<ResourceData>>(emptyList())
        private set

    private var plexInfo by mutableStateOf<ResourceData?>(null)

    /**
     * 临时地址
     */
    var tmpAddress by mutableStateOf("")
        private set

    var selectUrlIndex by mutableIntStateOf(0)
        private set

    //加载状态
    var loading by mutableStateOf(false)
        private set

    //资源加载状态
    var resourceLoading by mutableStateOf(false)
        private set

    //是否登陆报错
    var isLoginError by mutableStateOf(false)
        private set

    //是否资源加载报错
    var isResourceLoginError by mutableStateOf(false)
        private set

    //是否登陆成功
    var isLoginSuccess by mutableStateOf(false)
        private set

    //报错信息

    var errorHint by mutableStateOf(Res.string.empty_info)
        private set

    var errorMessage by mutableStateOf("")
        private set

    init {
        clearConnectionInputData()
    }

    suspend fun inputAddress() {
        address = normalizeInputText(address)
        username = normalizeInputText(username)

        val tmpDatasource = dataSourceType ?: return

        if (address.isBlank() && tmpDatasource.ifInputUrl) {
            return
        }

        if (username.isBlank()) {
            return
        }
        loading = true
        clearLoginStatus()
        var ifTemp = true
        if (dataSourceManager.dataSourceType == null) {
            ifTemp = false
            dataSourceManager.switchDataSource(tmpDatasource)
            tmpDataSourceParentServer = dataSourceManager
        } else {
            tmpDataSourceParentServer =
                dataSourceManager.getDataSourceServerByType(tmpDatasource, true)
        }

        val platformInfo = getPlatformInfo(contextWrapper)


        val clientLoginInfoReq =
            ClientLoginInfoReq(
                username = username,
                password = password,
                address = tmpAddress,
                appName = platformInfo.appName,
                clientVersion = tmpDatasource.version,
                serverVersion = plexInfo?.serverVersion,
                serverName = plexInfo?.serverName,
                serverId = plexInfo?.serverId
            )
        tmpDataSourceParentServer?.addClientAndLogin(clientLoginInfoReq)?.onEach {
            Log.i("=====", "数据获取${it}")

            val loginSateInfo = dataSourceManager.getLoginSateInfo(it)
            loading = loginSateInfo.loading
            errorHint = loginSateInfo.errorHint ?: Res.string.empty_info
            errorMessage = loginSateInfo.errorMessage ?: ""
            isLoginSuccess = loginSateInfo.isLoginSuccess
            isLoginError = loginSateInfo.isError

        }?.launchIn(viewModelScope)


    }

    fun setAddressData(address: String) {
        this.address = normalizeInputText(address)
    }

    fun setUserNameData(username: String) {
        this.username = normalizeInputText(username)
    }

    fun setPasswordData(password: String) {
        this.password = password
    }


    fun setDataSourceTypeData(data: DataSourceType?) {
        val oldDataSourceType = dataSourceType
        this.dataSourceType = data
        if (oldDataSourceType != null && data != null && oldDataSourceType != data) {
            clearConnectionInputData()
        }
    }

    fun clearConnectionInputData() {
        address = ""
        username = ""
        password = ""
        updateSelectUrlIndexZero()
        clearLoginStatus()
        clearResourceLoginStatus()
    }

    /**
     * 判断是否已经选择或已经输入必须输入
     */
    fun isInputError(): Boolean {
        return (address.isBlank() || username.isBlank()) && dataSourceType?.ifInputUrl == true
    }

    /**
     * 判断账号密码是否输入
     */
    fun ifInputAccount(): Boolean {
        return username.isBlank()
    }


    fun createTmpAddress() {
        clearLoginStatus()
        clearResourceLoginStatus()
        updateSelectUrlIndexZero()
        if (!address.isAbsoluteNetworkUrl()) {
            options.forEach {
                val url = "${it}$address"
                tmpAddressList.add(url)
                val ipAndPort = isEndPort(url)
                if (!ipAndPort) {
                    if (url.startsWith(HTTP)) {
                        tmpAddressList.add("${url}:${dataSourceType?.httpPort}")
                    } else {
                        tmpAddressList.add("${url}:${dataSourceType?.httpsPort}")
                    }
                }
            }
        } else {
            tmpAddressList.add(address)
            val ipAndPort = isEndPort(address)
            if (!ipAndPort) {
                if (address.startsWith(HTTP)) {
                    tmpAddressList.add("${address}:${dataSourceType?.httpPort}")
                } else {
                    tmpAddressList.add("${address}:${dataSourceType?.httpsPort}")
                }
            }
        }
        setSelectUrlIndexData(0)
    }

    /**
     * 判断输入的链接是否是以http://或者http://开头,结尾是否有端口号
     */
    fun isHttpStartAndPortEnd(): Boolean {
        return (address.isAbsoluteNetworkUrl() && isEndPort(address)) && dataSourceType?.ifInputUrl == true
    }

    /**
     * 判断链接地址是否有端口号结尾
     */
    private fun isEndPort(address: String): Boolean {
        return address.extractPortOrNull() != null
    }

    /**
     * 设置选择的Url的Index
     */
    fun setSelectUrlIndexData(selectUrlIndex: Int) {
        this.selectUrlIndex = selectUrlIndex
        if (tmpAddressList.isNotEmpty())
            tmpAddress = tmpAddressList[selectUrlIndex]
    }

    /**
     * 设置
     */
    fun setSelectInfoIndexData(selectInfoIndex: Int) {
        this.selectUrlIndex = selectInfoIndex
        if (tmpPlexInfo.isNotEmpty()) {
            plexInfo = tmpPlexInfo[selectInfoIndex]
            tmpAddress = tmpPlexInfo[selectInfoIndex].addressUrl
        }
    }

    /**
     * 设置选择地址的index为0
     */
    private fun updateSelectUrlIndexZero() {
        this.selectUrlIndex = 0
        plexInfo = null
        tmpAddress = ""
        tmpPlexInfo = emptyList()
        tmpAddressList.clear()
    }

    /**
     * 设置临时地址
     */
    fun setTmpAddressData(address: String) {
        tmpAddress = address
    }

    /**
     * 清空登陆状态
     */
    private fun clearLoginStatus() {
        errorHint = Res.string.empty_info
        errorMessage = ""
        isLoginSuccess = false
        isLoginError = false
    }

    /**
     * 清空资源获取状态
     */
    private fun clearResourceLoginStatus() {
        errorHint = Res.string.empty_info
        errorMessage = ""
        isResourceLoginError = false
        resourceLoading = false
    }

    suspend fun getResources() {
        Log.i("ConnectionScreen", "读取资源")
        clearResourceLoginStatus()
        updateResourceLoading(true)
        updateSelectUrlIndexZero()
        val dataSourceTypeTmp = dataSourceType
        if (dataSourceTypeTmp != null) {
            val tmpDataSourceParentServer =
                dataSourceManager.getDataSourceServerByType(dataSourceTypeTmp, true)
            val clientLoginInfoReq =
                ClientLoginInfoReq(
                    username = username,
                    password = password,
                    address = tmpAddress,
                    appName = "",
                    clientVersion = ""
                )
            try {
                val resources = tmpDataSourceParentServer?.getResources(clientLoginInfoReq)
                if (!resources.isNullOrEmpty())
                    tmpPlexInfo = resources
            } catch (e: Exception) {
                Log.e("ConnectionScreen", "获取资源失败", e)
                isResourceLoginError = true
                errorHint = Res.string.plex_resource_error
                errorMessage = ""
            }
        } else {
            isResourceLoginError = true
            errorHint = Res.string.plex_resource_error
            errorMessage = ""
        }


    }

    /**
     * 更新资源加载状态
     */
    fun updateResourceLoading(loading: Boolean) {
        resourceLoading = loading
    }

    fun updateIfConnectionConfig() {
        settingsManager.updateIfConnectionConfig(true)
    }

    private fun normalizeInputText(value: String): String {
        return value.replace(INVISIBLE_CHAR_REGEX, "").trim()
    }

    private companion object {
        val INVISIBLE_CHAR_REGEX = Regex("[\\u200B-\\u200D\\uFEFF]")
    }
}

