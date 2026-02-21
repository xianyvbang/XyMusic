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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.DataSourceChangeUtils
import cn.xybbz.common.utils.DatabaseUtils
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.data.connection.ConnectionConfigExt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(UnstableApi::class)
class ConnectionManagementViewModel @Inject constructor(
    val dataSourceManager: DataSourceManager,
    private val db: DatabaseClient,
    private val musicController: MusicController,
    val backgroundConfig: BackgroundConfig,
    private val settingsManager: SettingsManager
) : ViewModel() {

    //获得所有链接信息
    var connectionList by mutableStateOf<List<ConnectionConfigExt>>(emptyList())

    var connectionId by mutableLongStateOf(0L)
        private set


    init {
        getConnectionListData()
        getNowConnectionId()
    }

    private fun getConnectionListData() {
        viewModelScope.launch {
            db.connectionConfigDao.selectAllDataExtFlow().collect {
                connectionList = it
            }
        }
    }

    private fun getNowConnectionId() {
        viewModelScope.launch {
            db.settingsDao.selectOne().collect {
                if (it?.connectionId != null) {
                    connectionId = it.connectionId ?: 0L
                }
            }
        }
    }

    /**
     * 切换连接服务
     */
    @OptIn(UnstableApi::class)
    fun changeDataSource(connectionConfig: ConnectionConfig) {
        viewModelScope.launch {
            DataSourceChangeUtils.changeDataSource(
                connectionConfig,
                dataSourceManager,
                musicController
            )
        }
    }


    /**
     * 删除当前连接
     */
    @OptIn(UnstableApi::class)
    suspend fun removeConnection(connectionId: Long) {
        DatabaseUtils.clearDatabaseByConnectionConfig(
            db,
            connectionId
        )
        if (dataSourceManager.getConnectionId() == connectionId) {
            //如果当前链接是最后链接,则直接删除数据里的数据
            val connectionCount = db.connectionConfigDao.selectCount()
            if (connectionCount == 0) {
                DataSourceChangeUtils.clearDataSourceAfter(
                    musicController,
                    dataSourceManager,
                    settingsManager
                )
            } else {
                val connectionConfig =
                    db.connectionConfigDao.selectConnectionConfigOrderByCreateTime()
                if (connectionConfig != null) {
                    //切换数据源
                    changeDataSource(connectionConfig)
                }
            }
        }
    }

}