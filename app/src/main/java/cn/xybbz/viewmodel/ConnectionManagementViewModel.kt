package cn.xybbz.viewmodel

import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.DataSourceChangeUtils
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.connection.ConnectionConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(UnstableApi::class)
class ConnectionManagementViewModel @Inject constructor(
    private val _connectionConfigServer: ConnectionConfigServer,
    private val _dataSourceManager: IDataSourceManager,
    private val db: DatabaseClient,
    private val musicController: MusicController,
    private val _backgroundConfig: BackgroundConfig
) : ViewModel() {

    //获得所有链接信息
    var connectionList by mutableStateOf<List<ConnectionConfig>>(emptyList())

    val connectionConfigServer = _connectionConfigServer
    val dataSourceManager = _dataSourceManager
    val backgroundConfig = _backgroundConfig

    init {
        getConnectionListData()
    }

    private fun getConnectionListData() {
        viewModelScope.launch {
            db.connectionConfigDao.selectAllDataFlow().collect {
                connectionList = it
            }
        }
    }

    /**
     * 切换连接服务
     */
    @OptIn(UnstableApi::class)
    fun changeDataSource(connectionConfig: ConnectionConfig) {
        viewModelScope.launch {
            DataSourceChangeUtils.changeDataSource(connectionConfig,dataSourceManager,musicController)
        }
    }
}