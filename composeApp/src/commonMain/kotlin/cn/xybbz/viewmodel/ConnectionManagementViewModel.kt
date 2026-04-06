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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.utils.DataSourceChangeUtils
import cn.xybbz.common.utils.DatabaseUtils
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.download.DownloaderManager
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.connection.ConnectionConfig
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ConnectionManagementViewModel (
    val dataSourceManager: DataSourceManager,
    private val db: LocalDatabaseClient,
    private val downloadDb: DownloadDatabaseClient,
    private val downloaderManager: DownloaderManager,
    private val musicController: MusicCommonController,
    private val musicPlayContext: MusicPlayContext,
    private val settingsManager: SettingsManager
) : ViewModel() {

    //获得所有链接信息
    var connectionList by mutableStateOf<List<ConnectionConfig>>(emptyList())

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
    fun changeDataSource(connectionConfig: ConnectionConfig) {
        viewModelScope.launch {
            DataSourceChangeUtils.changeDataSource(
                connectionConfig,
                dataSourceManager,
                musicController,
                db,
                downloadDb,
                musicPlayContext
            )
        }
    }

    private suspend fun clearDownloadDataByConnectionId(connectionId: Long) {
        val mediaLibraryId = connectionId.toString()
        val downloadTasks = downloadDb.downloadDao.getAllTasksSuspend(mediaLibraryId = mediaLibraryId)
        val downloadIds = downloadTasks.map { it.id }.toLongArray()

        if (downloadIds.isNotEmpty()) {
            downloaderManager.delete(*downloadIds)
        }
    }


    /**
     * 删除当前连接
     */
    suspend fun removeConnection(connectionId: Long) {
        clearDownloadDataByConnectionId(connectionId)
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
