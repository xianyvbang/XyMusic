package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.AppStartupContent
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
import cn.xybbz.localdata.enums.ThemeTypeEnum
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

data class StartupState(
    //主体颜色
    val themeTypeEnum: ThemeTypeEnum,
    val mainSceneInitialPage: AppStartupContent,
    // 设置数据已读出后，App 才能判断当前应该显示连接页还是继续启动主壳。
    val settingsLoaded: Boolean = false,
    // 当前本地设置中是否存在连接配置，用于启动阶段选择首开连接页。
    val hasConnectionConfig: Boolean = false,
    // 主壳放行标记：只要求设置和数据源服务可安全读取，不等待登录全链路结束。
    val readyForContent: Boolean = false,
    //背景图片地址
    val imageFilePath: String? = null
)

internal data class StartupContentDecision(
    val content: AppStartupContent,
    val hasShownMainContent: Boolean
)

internal fun resolveStartupContent(
    ifEntryPage: Boolean,
    readyForContent: Boolean,
    hasShownMainContent: Boolean
): StartupContentDecision {
    if (!ifEntryPage) {
        return StartupContentDecision(
            content = AppStartupContent.CONNECTION,
            hasShownMainContent = false
        )
    }

    val nextHasShownMainContent = hasShownMainContent || readyForContent
    return StartupContentDecision(
        content = if (nextHasShownMainContent) {
            AppStartupContent.MAIN
        } else {
            AppStartupContent.STARTUP
        },
        hasShownMainContent = nextHasShownMainContent
    )
}

@KoinViewModel
class StartupViewModel(
    private val homeDataRepository: HomeDataRepository,
    private val proxyConfigServer: ProxyConfigServer,
    private val settingsManager: SettingsManager,
    private val dataSourceManager: DataSourceManager,
) : ViewModel(), KoinComponent {

    private var hasShownMainContent = false

    val appState: Flow<StartupState?> = combine(
        settingsManager.themeType,
        settingsManager.ifConnectionConfig,
        dataSourceManager.dataSourceServerFlow,
        settingsManager.imageFilePath,
        settingsManager.ifEntryPage
    ) { themeSettings, ifConnectionConfig, dataSourceServer, imageFilePath, ifEntryPage ->
        val readyForContent = dataSourceServer != null
        val startupContentDecision = resolveStartupContent(
            ifEntryPage = ifEntryPage,
            readyForContent = readyForContent,
            hasShownMainContent = hasShownMainContent
        )
        hasShownMainContent = startupContentDecision.hasShownMainContent
        StartupState(
            themeTypeEnum = themeSettings,
            mainSceneInitialPage = startupContentDecision.content,
            settingsLoaded = true,
            hasConnectionConfig = ifConnectionConfig,
            readyForContent = readyForContent,
            imageFilePath = imageFilePath
        )
    }.shareIn(
        viewModelScope,
        started = SharingStarted.Eagerly,
        replay = 1,
    )


    init {
        start()
    }

    fun start() {
        viewModelScope.launch {
            settingsManager.initSet()
        }
        // 这些是主界面可用前需要恢复的轻量配置和首页缓存。
        viewModelScope.launch {
            homeDataRepository.initData()
        }
        viewModelScope.launch(Dispatchers.IO) {
            proxyConfigServer.initConfig()
        }
        // 播放器/VLC/播放队列恢复是重依赖，独立后台执行，不能再挡住主壳显示。
        startPlayerInitialization()
        viewModelScope.launch {
            dataSourceManager.initDataSource()
        }
    }

    private fun startPlayerInitialization() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 从这里开始才触发播放器控制器和事件协调器，避免 Koin 启动阶段提前加载 VLC。
                val musicController = get<MusicCommonController>()
                get<PlayerEventCoordinator>().start()
                musicController.initController {
                    viewModelScope.launch(Dispatchers.IO) {
                        // 播放队列恢复依赖播放器控制器已初始化完成，因此放在 initController 回调里。
                        PlayerListRestoreUtils.restoreCurrentDataSourcePlayerList(
                            db = get<LocalDatabaseClient>(),
                            downloadDb = get<DownloadDatabaseClient>(),
                            musicPlayContext = get()
                        )
                    }
                    viewModelScope.launch(Dispatchers.IO) {
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
                Log.e("StartupViewModel", "播放器后台初始化失败", throwable)
            }
        }
    }
}
