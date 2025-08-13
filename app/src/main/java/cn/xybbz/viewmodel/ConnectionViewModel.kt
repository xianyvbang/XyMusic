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
import cn.xybbz.api.client.jellyfin.data.ClientLoginInfoReq
import cn.xybbz.api.state.ClientLoginInfoState
import cn.xybbz.common.utils.PasswordUtils
import cn.xybbz.config.IDataSourceManager
import cn.xybbz.config.SettingsConfig
import cn.xybbz.entity.data.ResourceData
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.connection.ConnectionConfig
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
    private val _dataSourceManager: IDataSourceManager,
    private val db: DatabaseClient,
) : ViewModel() {

    val settingsConfig = _settingsConfig
    val dataSourceManager = _dataSourceManager

    var dataSourceType by mutableStateOf<DataSourceType?>(null)
        private set

    var address by mutableStateOf("")
        private set

    var username by mutableStateOf("liu893043230@gmail.com")
        private set

    var password by mutableStateOf("Liu893043230!")
        private set

    var tmpAddressList = mutableStateListOf<String>()
        private set

    var tmpPlexInfo by mutableStateOf<List<ResourceData>>(emptyList())
        private set

    /**
     * 临时地址
     */
    var tmpAddress by mutableStateOf("")
        private set

    var selectUrlIndex by mutableIntStateOf(0)
        private set

    var ifScan by mutableStateOf<Boolean>(false)
        private set

    //加载状态
    var loading by mutableStateOf(false)
        private set

    //登录状态
    var loginStatus by mutableStateOf<ClientLoginInfoState?>(null)
        private set

    //报错信息
    var errorMessage by mutableStateOf("")

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
        errorMessage = ""
        loginStatus = null
        if (_dataSourceManager.dataSourceType == null) {
            _dataSourceManager.switchDataSource(dataSourceType)
            val clientLoginInfoReq =
                ClientLoginInfoReq(
                    address = tmpAddress,
                    username = username,
                    password = password
                )
            _dataSourceManager.addClientAndLogin(clientLoginInfoReq)?.onEach {
                Log.i("=====", "数据获取${it}")
                loginStatus = it
                when (it) {
                    is ClientLoginInfoState.Connected -> {
                        Log.i("=====", "连接中")
                    }

                    is ClientLoginInfoState.ConnectionSuccess -> {
                        //路由跳转,根据id跳转
                        Log.i("=====", "服务端连接成功")
                    }

                    is ClientLoginInfoState.ConnectError -> {
                        errorMessage = "服务端接错误"
                        Log.i("=====", "服务端连接错误")
                        loading = false
                    }

                    ClientLoginInfoState.ServiceTimeOutState -> {
                        errorMessage = "服务端连接超时"
                        Log.i("=====", "服务端连接超时")
                        loading = false
                    }

                    is ClientLoginInfoState.ErrorState -> {
                        errorMessage = it.error.message.toString()
                        Log.i("=====", it.error.message.toString())
                        loading = false
                    }

                    ClientLoginInfoState.SelectServer -> {
                        errorMessage = "未选择连接"
                        Log.i("=====", "未选择连接")
                        loading = false
                    }

                    ClientLoginInfoState.UnauthorizedErrorState -> {
                        errorMessage = "登录失败,账号或密码错误"
                        Log.i("=====", "权限报错")
                        loading = false
                    }

                    ClientLoginInfoState.UserLoginSuccess -> {
                        Log.i("=====", "登陆成功")
                        loading = false
                        //存储数据

//                    onRouter()
                    }
                }

            }?.launchIn(viewModelScope)
        } else {
            dataSourceType?.let { dataSourceType ->
                val encryptAES = PasswordUtils.encryptAES(password)
                val connectionConfig = ConnectionConfig(
                    serverId = "0",
                    name = dataSourceType.title,
                    address = tmpAddressList[selectUrlIndex],
                    type = dataSourceType,
                    username = username,
                    currentPassword = encryptAES.aesData,
                    iv = encryptAES.aesIv,
                    key = encryptAES.aesKey,
                    userId = "",
                    ifEnable = false,
                    serverVersion = ""
                )
                db.connectionConfigDao.save(connectionConfig)
                loginStatus = ClientLoginInfoState.UserLoginSuccess
                loading = false
            }

        }


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

    fun setIfScanData(data: Boolean) {
        this.ifScan = data
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
                    tmpAddressList.add("${url}:${dataSourceType?.port}")
                }
            }
        } else {
            tmpAddressList.add(address)
            val ipAndPort = isEndPort(address)
            if (!ipAndPort) {
                tmpAddressList.add("${address}:${dataSourceType?.port}")
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
        if (tmpPlexInfo.isNotEmpty())
            tmpAddress = tmpPlexInfo[selectInfoIndex].addressUrl
    }

    /**
     * 设置临时地址
     */
    fun setTmpAddressData(address: String) {
        tmpAddress = address
    }

    fun clearError() {
        errorMessage = ""
    }

    /**
     * 切换数据源
     */
    fun changeDataSource() {
        _dataSourceManager.setDataSourceTypeFun(dataSourceType)
    }

    /**
     * 清空登陆状态
     */
    fun clearLoginStatus() {
        loginStatus = null
    }

    suspend fun getResources() {
        errorMessage = ""
        if (_dataSourceManager.dataSourceType == null) {
            _dataSourceManager.switchDataSource(dataSourceType)
            val clientLoginInfoReq =
                ClientLoginInfoReq(
                    address = tmpAddress,
                    username = username,
                    password = password
                )
            try {
                val resources = _dataSourceManager.getResources(clientLoginInfoReq)
                tmpPlexInfo = resources
            }catch (e: Exception){
                errorMessage = e.message?:""
            }

        }
    }

    /**
     * 更新登陆loading状态
     */
    fun updateLoading(loading: Boolean){
        this.loading = loading
    }
}