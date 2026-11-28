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
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.formatBytes
import cn.xybbz.config.music.CacheDirectoryChangeFailure
import cn.xybbz.config.music.CacheDirectoryChangeResult
import cn.xybbz.config.music.CacheMigrationPolicy
import cn.xybbz.config.music.CacheMigrationProgress
import cn.xybbz.config.music.DownloadCacheCommonController
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.musicCacheSizeInfoFlow
import cn.xybbz.config.music.refreshMusicCacheSize
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.config.storage.MemoryStorageInfo
import cn.xybbz.config.storage.clearPlatformCache
import cn.xybbz.config.storage.getMemoryStorageInfo
import cn.xybbz.download.DownloaderManager
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.platform.ContextWrapper
import cn.xybbz.startup.SessionCoordinator
import com.github.panpf.sketch.Sketch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import org.koin.core.annotation.KoinViewModel
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.jvm_cache_migration_cancelled
import xymusic.composeapp.generated.resources.jvm_cache_migration_failed
import xymusic.composeapp.generated.resources.jvm_cache_migration_insufficient_space
import xymusic.composeapp.generated.resources.jvm_cache_migration_invalid_path
import xymusic.composeapp.generated.resources.jvm_cache_migration_nested
import xymusic.composeapp.generated.resources.jvm_cache_migration_not_writable
import xymusic.composeapp.generated.resources.jvm_cache_migration_persist_failed
import xymusic.composeapp.generated.resources.jvm_cache_migration_success
import xymusic.composeapp.generated.resources.jvm_cache_migration_target_not_empty
import xymusic.composeapp.generated.resources.jvm_connection_address_valid

@KoinViewModel
class MemoryManagementViewModel(
    private val contextWrapper: ContextWrapper,
    private val downloadCacheController: DownloadCacheCommonController,
    private val db: LocalDatabaseClient,
    private val downloadDb: DownloadDatabaseClient,
    private val settingsManager: SettingsManager,
    private val dataSourceManager: DataSourceManager,
    private val musicController: MusicCommonController,
    /**
     * 下载管理器用于清库前同步删除下载任务对应的磁盘文件。
     */
    private val downloaderManager: DownloaderManager,
    /**
     * 会话协调器用于清库后重置会话状态，保证重新登录时能触发页面数据刷新。
     */
    private val sessionCoordinator: SessionCoordinator,
    /**
     * 图片加载器，用于统计和清理 Sketch 图片磁盘缓存。
     */
    private val sketch: Sketch,
) : ViewModel() {

    var cacheSize by mutableStateOf("0B")
        private set

    var appDataSize by mutableStateOf("0B")
        private set

    var musicCacheSize by mutableStateOf("0B")
        private set

    /**
     * 当前歌曲缓存占用字节数，用于判断切换目录时是否需要提示迁移。
     */
    var musicCacheBytes by mutableStateOf(0L)
        private set

    /**
     * 当前缓存迁移进度；为 null 表示没有进行中的迁移。
     */
    var cacheMigrationProgress by mutableStateOf<CacheMigrationProgress?>(null)
        private set

    /**
     * 是否正在迁移缓存目录。
     */
    val isMigratingCacheDirectory: Boolean
        get() = cacheMigrationProgress != null

    var imageCacheSize by mutableStateOf("0B")
        private set

    var databaseSize by mutableStateOf("0B")
        private set

    var musicCachePath by mutableStateOf(downloadCacheController.cacheDirectoryPath)
        private set

    var isDefaultMusicCachePath by mutableStateOf(downloadCacheController.isDefaultCacheDirectory)
        private set

    init {
        viewModelScope.launch {
            downloadCacheController.musicCacheSizeInfoFlow().collect { sizeInfo ->
                musicCacheSize = sizeInfo.label
                musicCacheBytes = sizeInfo.bytes
            }
        }
        viewModelScope.launch {
            downloadCacheController.cacheMigrationProgress.collect { progress ->
                cacheMigrationProgress = progress
            }
        }
        viewModelScope.launch {
            refreshMusicCacheUsage()
        }
        viewModelScope.launch {
            settingsManager.cacheFilePath.collect {
                updateMusicCachePathState()
            }
        }
    }

    fun logStorageInfo() {
        viewModelScope.launch {
            refreshMusicCacheUsage()
            refreshImageCacheSize()
            refreshStorageInfo()
        }
    }

    private suspend fun refreshStorageInfo() {
        // 自定义缓存目录可能不在默认缓存根目录下，需要单独传入统计口径。
        val extraCachePaths = listOf(musicCachePath).filter { path -> path.isNotBlank() }
        val storageInfo = withContext(Dispatchers.IO) {
            getMemoryStorageInfo(contextWrapper, db, downloadDb, extraCachePaths)
        }
        updateStorageInfo(storageInfo)
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
            refreshMusicCacheUsage()
            refreshStorageInfo()
        }
    }

    /**
     * 只清理 Sketch 图片缓存，不触碰音频缓存和其它缓存目录。
     */
    fun clearImageCache() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                sketch.downloadCache.clear()
                sketch.resultCache.clear()
            }
            refreshImageCacheSize()
            refreshStorageInfo()
        }
    }

    fun clearMusicCache() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                downloadCacheController.clearCache()
            }
            refreshMusicCacheUsage()
            refreshStorageInfo()
        }
    }

    /**
     * 切换歌曲缓存目录。
     *
     * @param path 新目录路径。
     * @param policy 旧缓存处理策略。
     */
    fun changeMusicCacheDirectory(
        path: String,
        policy: CacheMigrationPolicy = CacheMigrationPolicy.MIGRATE,
    ) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                downloadCacheController.changeCacheDirectory(path, policy)
            }
            handleCacheDirectoryChangeResult(result)
            updateMusicCachePathState()
            refreshMusicCacheUsage()
            refreshStorageInfo()
        }
    }

    /**
     * 恢复默认歌曲缓存目录。
     *
     * @param policy 旧缓存处理策略。
     */
    fun restoreDefaultMusicCacheDirectory(
        policy: CacheMigrationPolicy = CacheMigrationPolicy.MIGRATE,
    ) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                downloadCacheController.restoreDefaultCacheDirectory(policy)
            }
            handleCacheDirectoryChangeResult(result)
            updateMusicCachePathState()
            refreshMusicCacheUsage()
            refreshStorageInfo()
        }
    }

    /**
     * 处理缓存目录切换结果并给出用户提示。
     *
     * @param result 控制器返回的切换结果。
     */
    private fun handleCacheDirectoryChangeResult(result: CacheDirectoryChangeResult) {
        when (result) {
            CacheDirectoryChangeResult.Success -> {
                MessageUtils.sendPopTipSuccess(Res.string.jvm_cache_migration_success)
            }

            CacheDirectoryChangeResult.Cancelled -> {
                MessageUtils.sendPopTip(Res.string.jvm_cache_migration_cancelled)
            }

            is CacheDirectoryChangeResult.Failed -> {
                MessageUtils.sendPopTipError(result.reason.toMessageResource())
            }
        }
    }

    /**
     * 把切换失败原因映射为提示文案。
     *
     * @return 对应的字符串资源。
     */
    private fun CacheDirectoryChangeFailure.toMessageResource(): StringResource {
        return when (this) {
            CacheDirectoryChangeFailure.INVALID_PATH -> Res.string.jvm_cache_migration_invalid_path
            CacheDirectoryChangeFailure.NOT_WRITABLE -> Res.string.jvm_cache_migration_not_writable
            CacheDirectoryChangeFailure.SAME_DIRECTORY -> Res.string.jvm_cache_migration_success
            CacheDirectoryChangeFailure.NESTED_DIRECTORY -> Res.string.jvm_cache_migration_nested
            CacheDirectoryChangeFailure.TARGET_NOT_EMPTY -> Res.string.jvm_cache_migration_target_not_empty
            CacheDirectoryChangeFailure.INSUFFICIENT_SPACE -> Res.string.jvm_cache_migration_insufficient_space
            CacheDirectoryChangeFailure.MIGRATION_FAILED -> Res.string.jvm_cache_migration_failed
            CacheDirectoryChangeFailure.PERSIST_FAILED -> Res.string.jvm_cache_migration_persist_failed
        }
    }

    fun clearDatabaseData() {
        viewModelScope.launch {
            musicController.clearPlayerList()
            withContext(Dispatchers.IO) {
                dataSourceManager.release()
                // 清库后同步重置会话状态，避免会话里仍保留已删除的旧连接，
                // 重新登录时 Ready 状态不发生变化，导致首页等页面不刷新数据。
                sessionCoordinator.clear()
                DatabaseUtils.clearAllDatabaseData(db, downloadDb, downloaderManager)
                settingsManager.initSet()
            }
            databaseSize = "0B"
            refreshStorageInfo()
        }
    }

    private fun updateMusicCachePathState() {
        musicCachePath = downloadCacheController.cacheDirectoryPath
        isDefaultMusicCachePath = downloadCacheController.isDefaultCacheDirectory
    }

    private suspend fun refreshMusicCacheUsage() {
        downloadCacheController.refreshMusicCacheSize()
    }

    /**
     * 刷新图片缓存占用。
     *
     * Sketch 的下载缓存和结果缓存共同构成图片缓存占用。
     */
    private suspend fun refreshImageCacheSize() {
        withContext(Dispatchers.IO) {
            imageCacheSize = formatBytes(sketch.downloadCache.size + sketch.resultCache.size)
        }
    }
}
