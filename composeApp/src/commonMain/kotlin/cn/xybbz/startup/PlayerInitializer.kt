package cn.xybbz.startup

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.PlayerListRestoreUtils
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.config.music.PlayerEventCoordinator
import cn.xybbz.config.setting.OnSettingsChangeListener
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.config.volume.VolumeServer
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * 播放器独立初始化器。
 *
 * 播放器只在会话已经恢复后初始化，播放器失败不会阻断连接登录和首页缓存展示。
 */
class PlayerInitializer(
    private val db: LocalDatabaseClient,
    private val downloadDb: DownloadDatabaseClient,
    private val musicController: MusicCommonController,
    private val musicPlayContext: MusicPlayContext,
    private val playerEventCoordinator: PlayerEventCoordinator,
    private val settingsManager: SettingsManager,
    private val volumeServer: VolumeServer,
) {
    /** 初始化播放器控制器、播放列表、音量和资源策略监听。 */
    fun initialize(dataSourceManager: DataSourceManager) {
        try {
            val dataSourceScope = dataSourceManager.dataSourceScope()
            playerEventCoordinator.start()
            musicController.initController {
                dataSourceScope.launch {
                    PlayerListRestoreUtils.restoreCurrentDataSourcePlayerList(
                        db = db,
                        downloadDb = downloadDb,
                        musicPlayContext = musicPlayContext,
                    )
                }
                dataSourceScope.launch {
                    volumeServer.updateVolume(db.settingsDao.selectOneData()?.jvmVolume ?: 60)
                }
                settingsManager.setOnListener(object : OnSettingsChangeListener {
                    override fun onCacheMaxBytesChanged(
                        cacheUpperLimit: CacheUpperLimitEnum,
                        oldCacheUpperLimit: CacheUpperLimitEnum,
                    ) {
                        if (
                            oldCacheUpperLimit == CacheUpperLimitEnum.No &&
                            cacheUpperLimit != CacheUpperLimitEnum.No &&
                            musicController.state == PlayStateEnum.Playing
                        ) {
                            musicController.musicInfo?.let {
                                musicController.startCache(it, settingsManager.getStatic())
                            }
                        }
                    }

                    override suspend fun onMusicResourceConfigChanged() {
                        musicController.refreshPlaylistCoverMetadata()
                    }
                })
            }
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e("SessionCoordinator", "播放器初始化失败", throwable)
        }
    }

    /** 连接切换后恢复新连接对应的本地播放列表。 */
    fun restoreForConnection(dataSourceManager: DataSourceManager) {
        dataSourceManager.dataSourceScope().launch {
            PlayerListRestoreUtils.restoreCurrentDataSourcePlayerList(
                db = db,
                downloadDb = downloadDb,
                musicPlayContext = musicPlayContext,
            )
        }
    }
}
