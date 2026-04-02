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
import cn.xybbz.common.utils.formatBytes
import cn.xybbz.config.music.DownloadCacheCommonController
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.config.storage.MemoryStorageInfo
import cn.xybbz.config.storage.clearPlatformCache
import cn.xybbz.config.storage.getMemoryStorageInfo
import cn.xybbz.di.ContextWrapper
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.config.withTransaction
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class MemoryManagementViewModel(
    private val contextWrapper: ContextWrapper,
    private val downloadCacheController: DownloadCacheCommonController,
    private val db: DatabaseClient,
    private val settingsManager: SettingsManager,
    private val dataSourceManager: DataSourceManager,
    private val musicController: MusicCommonController,
) : ViewModel() {

    var cacheSize by mutableStateOf("0B")
        private set

    var appDataSize by mutableStateOf("0B")
        private set

    var musicCacheSize by mutableStateOf("0B")
        private set

    var databaseSize by mutableStateOf("0B")
        private set

    init {
        viewModelScope.launch {
            downloadCacheController.getCacheSize()
            downloadCacheController.allCacheSizeFlow.collect {
                musicCacheSize = formatBytes(it)
            }
        }
    }

    fun logStorageInfo() {
        viewModelScope.launch {
            refreshStorageInfo()
        }
    }

    private fun refreshStorageInfo() {
        updateStorageInfo(getMemoryStorageInfo(contextWrapper, db))
    }

    private fun updateStorageInfo(storageInfo: MemoryStorageInfo) {
        appDataSize = formatBytes(storageInfo.appDataSize)
        cacheSize = formatBytes(storageInfo.cacheSize)
        databaseSize = formatBytes(storageInfo.databaseSize)
    }

    fun clearAllCache() {
        viewModelScope.launch {
            clearPlatformCache(contextWrapper)
            refreshStorageInfo()
        }
    }

    fun clearMusicCache() {
        viewModelScope.launch {
            downloadCacheController.clearCache()
        }
    }

    fun clearDatabaseData() {
        viewModelScope.launch {
            musicController.clearPlayerList()
            dataSourceManager.release()
            db.withTransaction {
                db.musicDao.removeAll()
                db.albumDao.removeAll()
                db.artistDao.removeAll()
                db.playerDao.removeAll()
                db.progressDao.removeAll()
                db.enableProgressDao.removeAll()
                db.connectionConfigDao.removeAll()
                db.libraryDao.removeAll()
                db.genreDao.removeAll()
                db.eraItemDao.removeAll()
                db.remoteCurrentDao.removeAll()
                db.searchHistoryDao.deleteAll()
                db.skipTimeDao.removeAll()
                db.settingsDao.remove()
            }
            databaseSize = "0B"
            settingsManager.setSettingsData()
            refreshStorageInfo()
        }
    }
}
