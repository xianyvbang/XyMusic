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
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.extractPortOrNull
import cn.xybbz.config.image.isAbsoluteNetworkUrl
import cn.xybbz.config.info.getPlatformInfo
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.entity.data.ResourceData
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.platform.ContextWrapper
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.connection_address_or_username_cannot_be_empty
import xymusic_kmp.composeapp.generated.resources.empty_info
import xymusic_kmp.composeapp.generated.resources.plex_resource_error
import xymusic_kmp.composeapp.generated.resources.username_cannot_be_empty

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

    var selectUrlIndex by mutableIntStateOf(UNSELECTED_RESOURCE_INDEX)
        private set

    val hasSelectedResource: Boolean
        get() = selectUrlIndex != UNSELECTED_RESOURCE_INDEX && tmpAddress.isNotBlank()

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

    /**
     * 新版桌面连接页是否已经进入资源确认区域。
     *
     * 有些业务分支会先展示候选地址或资源加载中的空态，列表数据可能暂时为空，
     * 因此需要一个显式状态让 UI 继续停留在资源面板，而不是回退到表单预览。
     */
    var resourcePanelRequested by mutableStateOf(false)
        private set

    /**
     * 连接页右侧资源面板的统一可见性。
     *
     * UI 只订阅这个派生结果，不再自己拼接资源加载、登录、错误等业务状态。
     */
    val showResourcePanel: Boolean
        get() = resourceLoading ||
            isResourceLoginError ||
            tmpAddressList.isNotEmpty() ||
            tmpPlexInfo.isNotEmpty() ||
            loading ||
            isLoginError ||
            isLoginSuccess ||
            resourcePanelRequested

    init {
        clearConnectionInputData()
    }

    /**
     * 初始化连接页默认数据源。
     *
     * 页面首次打开时只负责传入可展示的数据源列表，默认选择逻辑统一放在 ViewModel。
     */
    fun ensureDefaultDataSource(dataSourceTypes: List<DataSourceType>) {
        if (dataSourceType == null) {
            setDataSourceTypeData(dataSourceTypes.firstOrNull() ?: DataSourceType.JELLYFIN)
        }
    }

    /**
     * 选择新的数据源。
     *
     * 新版桌面页把“点击左侧数据源”视为一次新的连接配置开始：
     * 1. 不同数据源切换时继续复用 setDataSourceTypeData 的既有清理逻辑；
     * 2. 重复点击当前数据源时也主动清空输入、候选资源和登录状态，避免页面层补业务判断。
     */
    fun selectDataSource(dataSourceType: DataSourceType) {
        val previousDataSourceType = this.dataSourceType
        setDataSourceTypeData(dataSourceType)
        if (previousDataSourceType == null || previousDataSourceType == dataSourceType) {
            clearConnectionInputData()
        }
    }

    /**
     * 回到连接信息输入状态。
     *
     * 新版 UI 的“重新选择/修改连接信息”都走这里，确保输入、候选地址、Plex 资源和错误状态同步清理。
     */
    fun resetConnectionInput() {
        clearConnectionInputData()
    }

    /**
     * 提交连接信息。
     *
     * 这里复用旧连接页已有校验和业务方法：
     * 1. 需要输入 URL 的数据源，根据地址是否完整决定直接登录或生成候选地址；
     * 2. 不需要输入 URL 的数据源，先读取账号下可用资源；
     * 3. 错误提示也统一在 ViewModel 层发出，页面只关心状态展示。
     */
    fun connect() {
        val selectedDataSource = dataSourceType ?: return
        if (selectedDataSource.ifInputUrl) {
            // URL 输入型协议必须同时具备地址和用户名，错误提示沿用旧连接页资源文案。
            if (isInputError()) {
                MessageUtils.sendPopTipError(Res.string.connection_address_or_username_cannot_be_empty)
                return
            }
            resourcePanelRequested = true
            if (!isHttpStartAndPortEnd()) {
                // 地址缺少协议或端口时先生成候选地址，等待用户点选后再登录。
                createTmpAddress()
            } else {
                // 已经是完整 URL + 端口时直接复用 inputAddress 进入真实登录流程。
                viewModelScope.launch {
                    setTmpAddressData(address)
                    inputAddress()
                }
            }
        } else {
            // Plex 这类资源发现型协议只校验账号，再读取账号下真实资源。
            if (ifInputAccount()) {
                MessageUtils.sendPopTipError(Res.string.username_cannot_be_empty)
                return
            }
            resourcePanelRequested = true
            viewModelScope.launch {
                getResources()
            }.invokeOnCompletion {
                updateResourceLoading(false)
            }
        }
    }

    /**
     * 选择候选地址或 Plex 资源并立即登录。
     *
     * 新版桌面页没有额外确认按钮，因此点击资源项即完成选择并复用 inputAddress 登录流程。
     */
    fun selectResourceAndLogin(index: Int) {
        resourcePanelRequested = true
        val resourceSelected = if (tmpPlexInfo.isNotEmpty()) {
            // Plex 资源列表使用资源索引设置服务器信息和临时登录地址。
            setSelectInfoIndexData(index)
        } else {
            // URL 候选列表使用地址索引设置临时登录地址。
            setSelectUrlIndexData(index)
        }
        if (!resourceSelected) {
            return
        }
        viewModelScope.launch {
            inputAddress()
        }
    }

    suspend fun inputAddress() {
        address = normalizeServiceAddress(address)
        username = normalizeInputText(username)
        tmpAddress = normalizeServiceAddress(tmpAddress)

        val tmpDatasource = dataSourceType ?: return

        if (address.isBlank() && tmpDatasource.ifInputUrl) {
            return
        }

        if (username.isBlank()) {
            return
        }
        if (tmpAddress.isBlank()) {
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
        this.address = normalizeServiceAddress(address)
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
        // 同步清理新版桌面连接页的资源面板请求状态，避免空列表场景下 UI 停留在旧面板。
        resourcePanelRequested = false
        address = ""
        username = ""
        password = ""
        clearResourceSelection()
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
        clearResourceSelection()
        address = normalizeServiceAddress(address)
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
    fun setSelectUrlIndexData(selectUrlIndex: Int): Boolean {
        if (selectUrlIndex !in tmpAddressList.indices) {
            return false
        }
        this.selectUrlIndex = selectUrlIndex
        tmpAddress = tmpAddressList[selectUrlIndex]
        plexInfo = null
        return true
    }

    /**
     * 设置
     */
    fun setSelectInfoIndexData(selectInfoIndex: Int): Boolean {
        if (selectInfoIndex !in tmpPlexInfo.indices) {
            return false
        }
        this.selectUrlIndex = selectInfoIndex
        plexInfo = tmpPlexInfo[selectInfoIndex]
        tmpAddress = tmpPlexInfo[selectInfoIndex].addressUrl
        return true
    }

    /**
     * 清空资源选择。
     */
    private fun clearResourceSelection() {
        this.selectUrlIndex = UNSELECTED_RESOURCE_INDEX
        plexInfo = null
        tmpAddress = ""
        tmpPlexInfo = emptyList()
        tmpAddressList.clear()
    }

    /**
     * 设置临时地址
     */
    fun setTmpAddressData(address: String) {
        tmpAddress = normalizeServiceAddress(address)
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
        clearResourceSelection()
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
                val resources = tmpDataSourceParentServer.getResources(clientLoginInfoReq)
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

    private fun normalizeServiceAddress(value: String): String {
        return normalizeInputText(value).trimEnd('/')
    }

    private companion object {
        const val UNSELECTED_RESOURCE_INDEX = -1
        val INVISIBLE_CHAR_REGEX = Regex("[\\u200B-\\u200D\\uFEFF]")
    }
}

