package cn.xybbz.viewmodel

import android.graphics.Color
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import cn.xybbz.api.TokenServer
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.DatabaseUtils
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.PasswordUtils
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.IDataSourceManager
import cn.xybbz.config.SettingsConfig
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.data.library.XyLibrary
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ConnectionConfigInfoViewModel.Factory::class)
class ConnectionConfigInfoViewModel @OptIn(UnstableApi::class)
@AssistedInject constructor(
    @Assisted private val connectionId: Long,
    private val dataSourceManager: IDataSourceManager,
    private val connectionConfigServer: ConnectionConfigServer,
    private val db: DatabaseClient,
    private val musicController: MusicController,
    private val settingsConfig: SettingsConfig
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(connectionId: Long): ConnectionConfigInfoViewModel
    }

    val _connectionConfigServer = connectionConfigServer

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

    //媒体库
    val libraryList = mutableStateListOf<XyLibrary>()

    //当前媒体库id
    var libraryId by mutableStateOf<String?>("")
        private set

    init {
        getConnectionConfig()
        getLibraryList()
    }

    private fun getConnectionConfig() {
        viewModelScope.launch {
            val thisConnectionConfig = db.connectionConfigDao.selectById(connectionId.toLong())
            if (thisConnectionConfig != null) {
                connectionConfig = thisConnectionConfig
                this@ConnectionConfigInfoViewModel.password = ""
                this@ConnectionConfigInfoViewModel.username = thisConnectionConfig.username
                this@ConnectionConfigInfoViewModel.address = thisConnectionConfig.address
                this@ConnectionConfigInfoViewModel.libraryId =
                    thisConnectionConfig.libraryId ?: Constants.MINUS_ONE_INT.toString()
                this@ConnectionConfigInfoViewModel.connectionName = thisConnectionConfig.name
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
        viewModelScope.launch {
            //更新密码
            val encryptAES = PasswordUtils.encryptAES(password ?: "")
            db.connectionConfigDao.updatePassword(
                currentPassword = encryptAES.aesData,
                iv = encryptAES.aesIv,
                key = encryptAES.aesKey,
                connectionId = connectionId.toLong()
            )
            if (connectionConfigServer.connectionConfig?.id == connectionId) {
                viewModelScope.launch {
                    dataSourceManager.initDataSource()
                }
            }

            password = ""
        }


    }

    /**
     * 删除当前连接
     */
    @OptIn(UnstableApi::class)
    suspend fun removeThisConnection() {

        if (connectionConfigServer.connectionConfig?.id == connectionId) {
            //如果当前链接是最后链接,则直接删除数据里的数据
            val connectionCount = db.connectionConfigDao.selectCount()
            if (connectionCount == 0) {
                DatabaseUtils.clearDatabaseByConnectionConfig(
                    db,
                    connectionId
                )
                musicController.clearPlayerList()
                dataSourceManager.release()
                settingsConfig.setSettingsData()
            } else {
                MessageUtils.sendPopTip("当前连接无法删除", backgroundColor = Color.RED)
            }
        } else {
            DatabaseUtils.clearDatabaseByConnectionConfig(
                db,
                connectionId
            )
        }

    }

    /**
     * 设置媒体库id
     */
    fun updateLibraryId(data: String) {
        libraryId = data

        //更新媒体库
        viewModelScope.launch {
            var tmpData: String? = data
            if (data == Constants.MINUS_ONE_INT.toString()) {
                tmpData = null
            }
            db.connectionConfigDao.updateLibraryId(
                libraryId = tmpData,
                connectionId = connectionId.toLong()
            )
            if (connectionConfigServer.connectionConfig?.id == connectionId) {
                connectionConfigServer.updateLibraryId(tmpData)
            }
        }
    }

    /**
     * 获得媒体库
     */
    private fun getLibraryList() {
        viewModelScope.launch {
            val libraryData = db.libraryDao.selectListByDataSourceType()
            libraryList.add(
                XyLibrary(
                    id = "-1",
                    name = "全部媒体库",
                    connectionId = connectionId.toLong(),
                    collectionType = ""
                )
            )
            if (libraryData.isNotEmpty()) {
                libraryList.addAll(libraryData)
            }
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
            MessageUtils.sendPopTip("链接地址不能为空")
            return
        }
        //更新链接地址
        viewModelScope.launch {
            TokenServer.setBaseUrlData(baseUrl = tmpAddress)
            //更新所有数据使得列表失效
            db.albumDao.updateUrlByConnectionId(address, tmpAddress)
            db.musicDao.updateUrlByConnectionId(address, tmpAddress)
            db.artistDao.updateUrlByConnectionId(address, tmpAddress)
            db.genreDao.updateUrlByConnectionId(address, tmpAddress)

            address = tmpAddress
            db.connectionConfigDao.updateAddress(
                address = address,
                connectionId = connectionId.toLong()
            )
            connectionConfigServer.updateConnection()
            //判断是否需要重新登录
            if (connectionConfigServer.connectionConfig?.id == connectionId) {
                dataSourceManager.initDataSource()
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
     * 获得数据数量
     */
    suspend fun getServerDataCount() {
        dataSourceManager.getDataInfoCount(connectionId)
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
            MessageUtils.sendPopTip("别名不能为空")
            return
        }
        //更新链接地址
        viewModelScope.launch {
            db.connectionConfigDao.updateName(
                name = connectionName,
                connectionId = connectionId.toLong()
            )
        }
    }
}