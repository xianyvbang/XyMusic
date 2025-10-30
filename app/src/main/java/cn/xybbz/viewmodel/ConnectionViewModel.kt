package cn.xybbz.viewmodel

import android.util.Log
import android.webkit.URLUtil
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.R
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.api.client.IDataSourceServer
import cn.xybbz.api.client.jellyfin.data.ClientLoginInfoReq
import cn.xybbz.config.SettingsConfig
import cn.xybbz.entity.data.ResourceData
import cn.xybbz.localdata.enums.DataSourceType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.net.URI
import javax.inject.Inject

/**
 * 连接model
 */
@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val _settingsConfig: SettingsConfig,
    private val _dataSourceManager: IDataSourceManager
) : ViewModel() {

    val settingsConfig = _settingsConfig
    val dataSourceManager = _dataSourceManager

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

    //是否登陆报错
    var isLoginError by mutableStateOf(false)
        private set

    //是否登陆成功
    var isLoginSuccess by mutableStateOf(false)
        private set

    //报错信息

    var errorHint by mutableStateOf<Int>(R.string.empty_info)
        private set

    var errorMessage by mutableStateOf("")
        private set

    suspend fun inputAddress() {

        if (dataSourceType == null) {
            return
        }

        if (address.isBlank() && dataSourceType?.ifInputUrl == true) {
            return
        }

        if (username.isBlank()) {
            return
        }
        loading = true
        clearLoginStatus()
        var ifEnable = true
        if (_dataSourceManager.dataSourceType == null) {
            _dataSourceManager.switchDataSource(dataSourceType)
            tmpDataSourceParentServer = _dataSourceManager
        } else if (tmpDataSourceParentServer == null) {
            tmpDataSourceParentServer =
                dataSourceType?.let { _dataSourceManager.getDataSourceServerByType(it, true) }
            ifEnable = false
        }

        val clientLoginInfoReq =
            ClientLoginInfoReq(
                address = tmpAddress,
                username = username,
                password = password,
                serverId = plexInfo?.serverId,
                serverVersion = plexInfo?.serverVersion,
                serverName = plexInfo?.serverName,
                ifEnable = ifEnable
            )
        tmpDataSourceParentServer?.addClientAndLogin(clientLoginInfoReq)?.onEach {
            Log.i("=====", "数据获取${it}")

            val loginSateInfo = _dataSourceManager.getLoginSateInfo(it)
            loading = loginSateInfo.loading
            errorHint = loginSateInfo.errorHint ?: R.string.empty_info
            errorMessage = loginSateInfo.errorMessage ?: ""
            isLoginSuccess = loginSateInfo.isLoginSuccess
            isLoginError = loginSateInfo.isError

        }?.launchIn(viewModelScope)


    }

    fun setAddressData(address: String) {
        this.address = address
    }

    fun setUserNameData(username: String) {
        this.username = username
    }

    fun setPasswordData(password: String) {
        this.password = password
    }


    fun setDataSourceTypeData(data: DataSourceType?) {
        this.dataSourceType = data
    }

    /**
     * 判断是否已经选择或已经输入必须输入
     */
    fun isInputError(): Boolean {
        return (address.isBlank() || username.isBlank()) && dataSourceType?.ifInputUrl == true
    }

    fun createTmpAddress() {
        tmpAddressList.clear()
        if (!URLUtil.isNetworkUrl(address)) {
            dataSourceType?.options?.forEach {
                val url = "${it}$address"
                tmpAddressList.add(url)
                val ipAndPort = isEndPort(url)
                if (!ipAndPort) {
                    if (URLUtil.isHttpUrl(url)) {
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
                if (URLUtil.isHttpUrl(address)) {
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
        return (URLUtil.isNetworkUrl(address) && isEndPort(address)) || dataSourceType?.ifInputUrl == false
    }

    /**
     * 判断链接地址是否有端口号结尾
     */
    private fun isEndPort(address: String): Boolean {
        val uri = URI(address)
        val port = uri.port
        return port != -1
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
     * 设置临时地址
     */
    fun setTmpAddressData(address: String) {
        tmpAddress = address
    }

    /**
     * 切换数据源
     */
    fun changeDataSource() {
        _dataSourceManager.updateDataSourceType(dataSourceType)
    }

    /**
     * 清空登陆状态
     */
    fun clearLoginStatus() {
        errorHint = R.string.empty_info
        errorMessage = ""
        isLoginSuccess = false
        isLoginError = false
    }

    suspend fun getResources() {
        clearLoginStatus()
        tmpDataSourceParentServer = _dataSourceManager
        if (_dataSourceManager.dataSourceType == null) {
            _dataSourceManager.switchDataSource(dataSourceType)
        } else {
            tmpDataSourceParentServer =
                dataSourceType?.let { _dataSourceManager.getDataSourceServerByType(it, true) }
        }

        val clientLoginInfoReq =
            ClientLoginInfoReq(
                address = tmpAddress,
                username = username,
                password = password,
                ifEnable = true
            )
        try {
            val resources = tmpDataSourceParentServer?.getResources(clientLoginInfoReq)
            if (!resources.isNullOrEmpty())
                tmpPlexInfo = resources
        } catch (e: Exception) {
            isLoginError = true
            errorHint = R.string.plex_resource_error
            errorMessage = ""
        }
    }

    /**
     * 更新登陆loading状态
     */
    fun updateLoading(loading: Boolean) {
        this.loading = loading
    }
}