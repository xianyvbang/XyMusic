package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.state.ClientLoginInfoState
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

data class StartupState(
    // 设置数据已读出后，App 才能判断当前应该显示连接页还是继续启动主壳。
    val settingsLoaded: Boolean = false,
    // 当前本地设置中是否存在连接配置，用于启动阶段选择首开连接页。
    val hasConnectionConfig: Boolean = false,
    // 数据源服务对象是否已创建完成；不代表自动登录已经成功。
    val dataSourcePrepared: Boolean = false,
    // 自动登录是否仍在后台执行，供启动层或后续 UI 展示局部状态。
    val autoLoginRunning: Boolean = false,
    // 自动登录的最近一次状态结果，异常已经在数据源 flow 内转换为状态。
    val autoLoginResult: ClientLoginInfoState? = null,
    // 主壳放行标记：只要求设置和数据源服务可安全读取，不等待登录全链路结束。
    val readyForContent: Boolean = false,
    // 启动基础初始化失败时保留错误信息，避免卡在启动页。
    val errorMessage: String? = null,
)

@KoinViewModel
class StartupViewModel(
    private val homeDataRepository: HomeDataRepository,
    private val proxyConfigServer: ProxyConfigServer,
    private val settingsManager: SettingsManager,
    private val dataSourceManager: DataSourceManager,
) : ViewModel(), KoinComponent {

    private val _uiState = MutableStateFlow(StartupState())
    val uiState = _uiState.asStateFlow()

    // 防止 App 重组导致启动初始化重复执行。
    private var started = false

    init {
        viewModelScope.launch {
            // 透传 DataSourceManager 中的自动登录运行态，启动流程本身不阻塞它。
            dataSourceManager.autoLoginRunning.collect { autoLoginRunning ->
                _uiState.update { it.copy(autoLoginRunning = autoLoginRunning) }
            }
        }
        viewModelScope.launch {
            // 自动登录失败/超时会以 ClientLoginInfoState 形式回传，而不是向外抛异常。
            dataSourceManager.autoLoginState.collect { autoLoginResult ->
                _uiState.update { it.copy(autoLoginResult = autoLoginResult) }
            }
        }
    }

    fun start() {
        if (started) {
            return
        }
        started = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 这些是主界面可用前需要恢复的轻量配置和首页缓存。
                homeDataRepository.initData()
                proxyConfigServer.initConfig()
                val settings = settingsManager.setSettingsData()
                val hasConnectionConfig = settings.connectionId != null && settings.dataSourceType != null
                _uiState.update {
                    it.copy(
                        settingsLoaded = true,
                        hasConnectionConfig = hasConnectionConfig,
                    )
                }

                // 播放器/VLC/播放队列恢复是重依赖，独立后台执行，不能再挡住主壳显示。
                startPlayerInitialization(
                    connectionId = settings.connectionId,
                    jvmVolume = settings.jvmVolume
                )

                if (hasConnectionConfig) {
                    _uiState.update { it.copy(autoLoginRunning = true) }
                    // initDataSource 只需创建可读的数据源服务；serverLogin 会继续在 DataSourceManager 内后台执行。
                    dataSourceManager.initDataSource(settings.dataSourceType, settings.connectionId)
                    _uiState.update {
                        it.copy(
                            dataSourcePrepared = true,
                            readyForContent = true,
                        )
                    }
                } else {
                    // 没有连接配置时直接放行 App 显示连接页，不等待数据源或播放器初始化。
                    _uiState.update {
                        it.copy(
                            dataSourcePrepared = false,
                            readyForContent = true,
                        )
                    }
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.e("StartupViewModel", "应用启动初始化失败", throwable)
                _uiState.update {
                    it.copy(
                        readyForContent = true,
                        autoLoginRunning = false,
                        errorMessage = throwable.message
                    )
                }
            }
        }
    }

    private fun startPlayerInitialization(
        connectionId: Long?,
        jvmVolume: Int?,
    ) {
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
                            musicPlayContext = get(),
                            connectionId = connectionId?.toString()
                        )
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        // 音量恢复不影响首屏，独立后台写入播放器。
                        get<VolumeServer>().updateVolume(jvmVolume ?: 60)
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
