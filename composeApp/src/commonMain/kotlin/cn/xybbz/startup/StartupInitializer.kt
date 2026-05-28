package cn.xybbz.startup

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.PlayerListRestoreUtils
import cn.xybbz.config.HomeDataRepository
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.PlayerEventCoordinator
import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.config.setting.OnSettingsChangeListener
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.config.volume.VolumeServer
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * 启动门闩状态。
 */
internal data class StartupReadiness(
    // 设置已经读取完成后，才能判断是首次连接页还是继续等待启动完成。
    val settingsLoaded: Boolean = false,
    // 轻量启动任务完成后，才允许创建 MainScreen 主壳。
    val readyForContent: Boolean = false
)

class StartupInitializer(
    private val homeDataRepository: HomeDataRepository,
    private val proxyConfigServer: ProxyConfigServer,
    private val settingsManager: SettingsManager,
    private val dataSourceManager: DataSourceManager,
) : KoinComponent {

    private val _readiness = MutableStateFlow(StartupReadiness())
    internal val readiness: StateFlow<StartupReadiness> = _readiness.asStateFlow()

    private var started = false

    fun start(coroutineScope: CoroutineScope) {
        if (started) {
            return
        }
        started = true

        // 数据源设置监听是长期任务；启动门闩只等待现有连接的数据源服务首次发布。
        coroutineScope.launch(Dispatchers.IO) {
            dataSourceManager.initDataSource()
        }

        coroutineScope.launch(Dispatchers.IO) {
            settingsManager.initSet()
            _readiness.value = _readiness.value.copy(settingsLoaded = true)

            // 直接读取数据库最新设置，避免 StateFlow 初始值还没同步时误判连接配置。
            val latestSettings = settingsManager.getLatest()
            val hasConnectionConfig =
                latestSettings.connectionId != null && latestSettings.dataSourceType != null

            // 这些是主界面可用前需要恢复的轻量配置和首页缓存。
            val homeDataJob = launch {
                homeDataRepository.initData()
            }
            val proxyConfigJob = launch {
                proxyConfigServer.initConfig()
            }

            homeDataJob.join()
            proxyConfigJob.join()
            if (hasConnectionConfig) {
                // 有连接配置时，至少等数据源服务发布后再放行主壳；自动登录仍继续后台执行。
                dataSourceManager.dataSourceServerFlow.filterNotNull().first()
            }
            _readiness.value = _readiness.value.copy(readyForContent = true)
        }

        // 播放器/VLC/播放队列恢复是重依赖，独立后台执行，不能挡住 Koin 启动返回和主壳显示。
        startPlayerInitialization(coroutineScope)
    }

    private fun startPlayerInitialization(coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val musicController = get<MusicCommonController>()
                get<PlayerEventCoordinator>().start()
                musicController.initController {
                    coroutineScope.launch(Dispatchers.IO) {
                        // 播放队列恢复依赖播放器控制器已初始化完成，因此放在 initController 回调里。
                        PlayerListRestoreUtils.restoreCurrentDataSourcePlayerList(
                            db = get<LocalDatabaseClient>(),
                            downloadDb = get<DownloadDatabaseClient>(),
                            musicPlayContext = get()
                        )
                    }
                    coroutineScope.launch(Dispatchers.IO) {
                        // 音量恢复不影响首屏，独立后台写入播放器。
                        get<VolumeServer>().updateVolume(
                            get<LocalDatabaseClient>().settingsDao.selectOneData()?.jvmVolume ?: 60
                        )
                    }

                    settingsManager.setOnListener(object : OnSettingsChangeListener {
                        override fun onCacheMaxBytesChanged(
                            cacheUpperLimit: CacheUpperLimitEnum,
                            oldCacheUpperLimit: CacheUpperLimitEnum
                        ) {
                            // 启动后缓存策略从“不缓存”切到“缓存”时，如果正在播放则补启动缓存任务。
                            if (
                                oldCacheUpperLimit == CacheUpperLimitEnum.No &&
                                cacheUpperLimit != CacheUpperLimitEnum.No &&
                                state == PlayStateEnum.Playing
                            ) {
                                musicInfo?.let {
                                    startCache(it, settingsManager.getStatic())
                                }
                            }
                        }

                        override suspend fun onMusicResourceConfigChanged() {
                            // 音乐资源配置变化后刷新播放列表封面元数据，保持当前播放 UI 和资源策略一致。
                            refreshPlaylistCoverMetadata()
                        }
                    })
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.e("StartupInitializer", "播放器后台初始化失败", throwable)
            }
        }
    }
}
