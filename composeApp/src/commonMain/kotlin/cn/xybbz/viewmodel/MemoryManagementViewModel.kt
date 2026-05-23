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
import cn.xybbz.common.utils.DatabaseUtils
import cn.xybbz.common.utils.formatBytes
import cn.xybbz.config.music.DownloadCacheCommonController
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.config.storage.MemoryStorageInfo
import cn.xybbz.config.storage.clearPlatformCache
import cn.xybbz.config.storage.getMemoryStorageInfo
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.platform.ContextWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class MemoryManagementViewModel(
    private val contextWrapper: ContextWrapper,
    private val downloadCacheController: DownloadCacheCommonController,
    private val db: LocalDatabaseClient,
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

    var musicCachePath by mutableStateOf(downloadCacheController.cacheDirectoryPath)
        private set

    var isDefaultMusicCachePath by mutableStateOf(downloadCacheController.isDefaultCacheDirectory)
        private set

    init {
        viewModelScope.launch {
            downloadCacheController.allCacheSizeFlow.collect {
                musicCacheSize = formatBytes(it)
            }
        }
        viewModelScope.launch {
            refreshMusicCacheSize()
        }
        viewModelScope.launch {
            settingsManager.cacheFilePath.collect {
                updateMusicCachePathState()
            }
        }
    }

    fun logStorageInfo() {
        viewModelScope.launch {
            refreshStorageInfo()
        }
    }

    private suspend fun refreshStorageInfo() {
        val storageInfo = withContext(Dispatchers.IO) {
            getMemoryStorageInfo(contextWrapper, db)
        }
        updateStorageInfo(storageInfo)
    }

    private suspend fun refreshMusicCacheSize() {
        withContext(Dispatchers.IO) {
            downloadCacheController.getCacheSize()
        }
    }

    private fun updateStorageInfo(storageInfo: MemoryStorageInfo) {
        appDataSize = formatBytes(storageInfo.appDataSize)
        cacheSize = formatBytes(storageInfo.cacheSize)
        databaseSize = formatBytes(storageInfo.databaseSize)
    }

    fun clearAllCache() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                clearPlatformCache(contextWrapper)
            }
            refreshStorageInfo()
        }
    }

    fun clearMusicCache() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                downloadCacheController.clearCache()
            }
            refreshStorageInfo()
        }
    }

    fun changeMusicCacheDirectory(path: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                downloadCacheController.changeCacheDirectory(path)
            }
            updateMusicCachePathState()
            refreshStorageInfo()
        }
    }

    fun restoreDefaultMusicCacheDirectory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                downloadCacheController.restoreDefaultCacheDirectory()
            }
            updateMusicCachePathState()
            refreshStorageInfo()
        }
    }

    fun clearDatabaseData() {
        viewModelScope.launch {
            musicController.clearPlayerList()
            withContext(Dispatchers.IO) {
                dataSourceManager.release()
                DatabaseUtils.clearAllDatabaseData(db)
                settingsManager.setSettingsData()
            }
            databaseSize = "0B"
            refreshStorageInfo()
        }
    }

    private fun updateMusicCachePathState() {
        musicCachePath = downloadCacheController.cacheDirectoryPath
        isDefaultMusicCachePath = downloadCacheController.isDefaultCacheDirectory
    }
}
