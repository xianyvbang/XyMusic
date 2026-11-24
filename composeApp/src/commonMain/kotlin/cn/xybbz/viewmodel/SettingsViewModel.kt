package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.PlaybackProgressReporter
import cn.xybbz.config.security.ConnectionCredentialManager
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.download.DownloaderManager
import cn.xybbz.download.core.DownloadDirectoryIssue
import cn.xybbz.download.core.DownloadDirectorySupport
import cn.xybbz.download.core.DownloaderConfig
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.enums.CredentialStoreType
import cn.xybbz.platform.ContextWrapper
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SettingsViewModel(
    val settingsManager: SettingsManager,
    private val db: LocalDatabaseClient,
    private val musicController: MusicCommonController,
    private val playbackProgressReporter: PlaybackProgressReporter,
    private val dataSourceManager: DataSourceManager,
    private val downloaderManager: DownloaderManager,
    private val credentialManager: ConnectionCredentialManager,
    /**
     * 平台上下文，用于重建下载配置时解析平台默认下载目录。
     */
    private val contextWrapper: ContextWrapper,
) : ViewModel() {


    /**
     * 设置信息
     */
    var settingDataNow by mutableStateOf(
        settingsManager.get()
    )

    /**
     * 当前实际生效的歌曲下载目录。
     *
     * 自定义目录不可用时会回落到平台默认下载目录。
     */
    var songStoragePath by mutableStateOf(downloaderManager.config.usableFinalDirectory())
        private set

    /**
     * 当前是否使用平台默认下载目录。
     *
     * 以设置表里是否保存了自定义路径为准：即使自定义路径当前不可用，
     * 这里仍然视为「非默认」，让用户可以通过「恢复默认」清掉失效路径。
     */
    var isDefaultDownloadPath by mutableStateOf(
        settingsManager.get().downloadFilePath.isBlank()
    )
        private set

    /**
     * 自定义下载目录是否因不可用而回退到了平台默认目录。
     */
    var isDownloadDirectoryFallback by mutableStateOf(false)
        private set

    init {
        getSetting()
        // 设置表的下载目录是唯一可信来源：启动回填与外部修改都通过这里同步到运行中的下载配置。
        viewModelScope.launch {
            settingsManager.downloadFilePath.collect { path ->
                applyDownloadConfig(path)
                refreshDownloadDirectoryState(path)
            }
        }
    }

    /**
     * 校验并切换歌曲下载目录。
     *
     * 目录不可用时不写入设置，直接返回原因交给界面提示，避免把失效路径持久化。
     *
     * @param path 用户通过系统目录选择器选中的目录。
     * @return 目录不可用原因；返回 null 表示切换成功。
     */
    suspend fun changeDownloadDirectory(path: String): DownloadDirectoryIssue? {
        if (!DownloadDirectorySupport.isCustomDirectorySupported()) {
            return DownloadDirectoryIssue.Unsupported
        }
        val issue = DownloadDirectorySupport.checkUsable(path)
        if (issue != null) {
            return issue
        }
        settingsManager.setDownloadFilePath(path)
        applyDownloadConfig(path)
        // 设置流的派发是异步的，这里先用本次写入的路径刷新一次，避免界面短暂显示旧状态。
        refreshDownloadDirectoryState(path)
        return null
    }

    /**
     * 恢复平台默认下载目录。
     *
     * 清空自定义路径后回落到平台默认目录，已下载文件保持原路径不动。
     */
    suspend fun restoreDefaultDownloadDirectory() {
        settingsManager.setDownloadFilePath("")
        applyDownloadConfig("")
        refreshDownloadDirectoryState("")
    }

    /**
     * 把自定义目录写入运行中的下载配置，使新任务立即使用新目录。
     *
     * @param customPath 自定义目录；空字符串表示使用平台默认目录。
     */
    private suspend fun applyDownloadConfig(customPath: String) {
        val currentConfig = downloaderManager.config
        // 设置流会重复下发同一路径，配置未变化时直接跳过，避免重复重建下载配置。
        if (currentConfig.finalDirectory == customPath.trim().ifEmpty {
                currentConfig.defaultFinalDirectory
            }
        ) {
            return
        }
        // 并发数从设置表读取最新值，避免本方法覆盖用户刚修改过的并发配置。
        val latestSettings = settingsManager.getLatest()
        val nextConfig = DownloaderConfig.Builder(contextWrapper)
            .setMaxConcurrentDownloads(latestSettings.maxConcurrentDownloads)
            .setFinalDirectory(customPath)
            .build()
        downloaderManager.config = nextConfig
        downloaderManager.updateConfig(nextConfig)
    }

    /**
     * 刷新下载目录展示状态。
     *
     * @param storedPath 设置表里保存的自定义目录，空字符串表示使用平台默认目录。
     */
    private fun refreshDownloadDirectoryState(storedPath: String = settingsManager.get().downloadFilePath) {
        val config = downloaderManager.config
        val customPath = storedPath.trim()
        songStoragePath = config.usableFinalDirectory()
        // 保存了自定义路径即视为非默认，避免路径失效时「恢复默认」被禁用而无法清理。
        isDefaultDownloadPath = customPath.isEmpty()
        // 自定义路径存在但实际回落到默认目录，说明该目录已不可用。
        isDownloadDirectoryFallback =
            customPath.isNotEmpty() && songStoragePath == config.defaultFinalDirectory
    }

    /**
     * 获得系统设置
     */
    private fun getSetting() {
        viewModelScope.launch {
            db.settingsDao.selectOne().distinctUntilChanged().collect {
                if (it != null) {
                    settingDataNow = it
                }
            }
        }
    }

    suspend fun setMaxConcurrentDownloads(maxConcurrentDownloads: Int) {
        settingsManager.setMaxConcurrentDownloads(maxConcurrentDownloads)
        /* downLoadManager.updateConfig(
             DownloaderConfig.Builder(context).setMaxConcurrentDownloads(maxConcurrentDownloads)
                 .build()
         )*/
    }

    suspend fun setSyncPlayProgressEnabled(enabled: Boolean) {
        val wasEnabled = settingsManager.get().ifEnableSyncPlayProgress
        if (wasEnabled == enabled) {
            return
        }

        val currentMusicId = musicController.musicInfo?.itemId
        val currentPosition = musicController.progressStateFlow.value
        val wasPlaying = musicController.state == PlayStateEnum.Playing
        settingsManager.setIfEnableSyncPlayProgress(enabled)
        settingsManager.settings.first { it.ifEnableSyncPlayProgress == enabled }

        if (!enabled) {
            playbackProgressReporter.stopAndJoin()
            if (!currentMusicId.isNullOrBlank()) {
                dataSourceManager.cancelReportProgress(currentMusicId)
            }
            return
        }

        if (wasPlaying && !currentMusicId.isNullOrBlank()) {
            dataSourceManager.reportPlaying(
                musicId = currentMusicId,
                playSessionId = settingsManager.get().playSessionId,
                positionTicks = currentPosition
            )
            playbackProgressReporter.start(currentMusicId)
        }
    }

    /**
     * 更新 iCloud Keychain 密码同步开关。
     */
    suspend fun setIfSyncPasswordsByICloud(enabled: Boolean) {
        val currentSettings = settingsManager.getLatest()
        if (currentSettings.ifSyncPasswordsByICloud == enabled) {
            return
        }
        settingsManager.setIfSyncPasswordsByICloud(enabled)
        migrateIosStoredCredentials()
    }

    /**
     * 在切换 iCloud Keychain 开关后迁移现有连接的凭据条目。
     */
    private suspend fun migrateIosStoredCredentials() {
        val connectionConfigs = db.connectionConfigDao.selectAllData()
        val migratedConfigs = connectionConfigs.mapNotNull { connectionConfig ->
            if (
                connectionConfig.credentialStoreType != CredentialStoreType.IOS_KEYCHAIN_LOCAL &&
                connectionConfig.credentialStoreType != CredentialStoreType.IOS_KEYCHAIN_SYNCABLE
            ) {
                return@mapNotNull null
            }
            val updatedConfig = credentialManager.migratePasswordStore(connectionConfig)
            if (updatedConfig != connectionConfig) updatedConfig else null
        }
        if (migratedConfigs.isNotEmpty()) {
            db.connectionConfigDao.updateBatch(migratedConfigs)
        }
    }
}
