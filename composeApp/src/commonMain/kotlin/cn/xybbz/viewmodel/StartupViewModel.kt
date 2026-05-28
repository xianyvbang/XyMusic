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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
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
    // 主壳放行标记：start() 中的轻量启动加载完成后才放行，不等待登录全链路结束。
    val readyForContent: Boolean = false,
    //背景图片地址
    val imageFilePath: String? = null
)

/**
 * 启动门闩状态。
 * settingsLoaded 表示设置已经读取完成，可以判断首开连接页还是启动页。
 * readyForContent 表示 start() 中的轻量启动任务已经完成，可以创建主壳。
 */
internal data class StartupReadiness(
    val settingsLoaded: Boolean = false,
    val readyForContent: Boolean = false
)

/**
 * 根内容路由决策结果。
 * 同时返回主壳是否已经显示过，用于避免刷新登录、切源过程中的短暂状态把页面拉回 STARTUP。
 */
internal data class StartupContentDecision(
    val content: AppStartupContent,
    val hasShownMainContent: Boolean
)

/**
 * 只根据启动门闩和首开状态决定外层页面。
 * 登录刷新属于主壳内的后台行为，不能作为 STARTUP 的触发条件。
 */
internal fun resolveStartupContent(
    settingsLoaded: Boolean,
    ifEntryPage: Boolean,
    readyForContent: Boolean,
    hasShownMainContent: Boolean
): StartupContentDecision {
    if (!settingsLoaded) {
        return StartupContentDecision(
            content = AppStartupContent.STARTUP,
            hasShownMainContent = hasShownMainContent
        )
    }

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

    // 主壳一旦展示过，后续刷新登录或数据源短暂重建都不再把根页面切回启动页。
    private var hasShownMainContent = false
    // readyForContent 由 start() 的轻量启动流程显式推进，不直接绑定登录状态。
    private val startupReadiness = MutableStateFlow(StartupReadiness())

    val appState: Flow<StartupState?> = combine(
        settingsManager.themeType,
        settingsManager.ifConnectionConfig,
        startupReadiness,
        settingsManager.imageFilePath,
        settingsManager.ifEntryPage
    ) { themeSettings, ifConnectionConfig, readiness, imageFilePath, ifEntryPage ->
        val startupContentDecision = resolveStartupContent(
            settingsLoaded = readiness.settingsLoaded,
            ifEntryPage = ifEntryPage,
            readyForContent = readiness.readyForContent,
            hasShownMainContent = hasShownMainContent
        )
        hasShownMainContent = startupContentDecision.hasShownMainContent
        StartupState(
            themeTypeEnum = themeSettings,
            mainSceneInitialPage = startupContentDecision.content,
            settingsLoaded = readiness.settingsLoaded,
            hasConnectionConfig = ifConnectionConfig,
            readyForContent = readiness.readyForContent,
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
        // 数据源设置监听是长期任务；启动门闩只等待现有连接的数据源服务首次发布。
        viewModelScope.launch {
            dataSourceManager.initDataSource()
        }
        viewModelScope.launch {
            settingsManager.initSet()
            startupReadiness.value = startupReadiness.value.copy(settingsLoaded = true)

            // 直接读取数据库最新设置，避免 StateFlow 初始值还没同步时误判连接配置。
            val latestSettings = settingsManager.getLatest()
            val hasConnectionConfig =
                latestSettings.connectionId != null && latestSettings.dataSourceType != null
            // 这些是主界面可用前需要恢复的轻量配置和首页缓存。
            val homeDataJob = launch {
                homeDataRepository.initData()
            }
            val proxyConfigJob = launch(Dispatchers.IO) {
                proxyConfigServer.initConfig()
            }

            homeDataJob.join()
            proxyConfigJob.join()
            if (hasConnectionConfig) {
                // 有连接配置时，至少等数据源服务发布后再放行主壳；自动登录仍继续后台执行。
                dataSourceManager.dataSourceServerFlow.filterNotNull().first()
            }
            startupReadiness.value = startupReadiness.value.copy(readyForContent = true)
        }
        // 播放器/VLC/播放队列恢复是重依赖，独立后台执行，不能再挡住主壳显示。
        startPlayerInitialization()
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
