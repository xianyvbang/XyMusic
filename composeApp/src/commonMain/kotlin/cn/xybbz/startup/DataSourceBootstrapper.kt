package cn.xybbz.startup

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.events.ReLoginEvent
import cn.xybbz.common.enums.LoginType
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * 首页首帧后再启动的数据源后置协调器。
 * 这里统一接管自动登录、播放器恢复和 401 重登录监听，避免启动阶段过早拉起重依赖。
 */
@OptIn(ExperimentalAtomicApi::class)
class DataSourceBootstrapper : KoinComponent {
    /**
     * 后置启动只允许执行一次，避免首页重组或多端入口重复触发自动登录和播放器恢复。
     */
    private val started = AtomicBoolean(false)

    /**
     * 首页首帧可见后调用的数据源启动入口。
     * 方法内部才解析 DataSourceManager、播放器等重依赖，保证启动页和首页首帧阶段保持轻量。
     */
    fun startAfterHomeVisible() {
        if (!started.compareAndSet(expectedValue = false, newValue = true)) {
            return
        }
        // 这里改为在首页可见后再解析依赖，避免启动页阶段提前创建 DataSourceManager 和播放器相关对象。
        val dataSourceManager = get<DataSourceManager>()
        val db = get<LocalDatabaseClient>()
        val dataSourceScope = dataSourceManager.dataSourceScope()
        dataSourceScope.launch {
            // 先恢复本地连接上下文，让首页立即拥有当前连接状态，再决定是否执行自动登录。
            val connectionConfig = dataSourceManager.restoreLocalDataSourceContext()
            if (connectionConfig == null) {
                Log.i("DataSourceBootstrapper", "无可恢复连接，跳过首页后置登录")
                return@launch
            }
            // 登录、播放器恢复、重登录监听都放到这里统一启动。
            startPlayerInitialization(dataSourceManager, db)
            startLoginEventBus(dataSourceManager, db)
            dataSourceManager.loginConnection(LoginType.API, connectionConfig)
        }
    }

    /**
     * 启动 401 重登录监听。
     * 监听绑定到 DataSourceManager 的作用域，随数据源释放一起结束。
     *
     * @param dataSourceManager 当前数据源管理器，用于读取事件流并发起重登录。
     * @param db 本地数据库，用于读取当前启用的连接配置。
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startLoginEventBus(
        dataSourceManager: DataSourceManager,
        db: LocalDatabaseClient
    ) {
        // 监听服务端登录态事件，只有真正建立连接后才开始订阅，避免无效的早期收集。
        dataSourceManager.dataSourceServerFlow
            .filterNotNull()
            .flatMapLatest { server ->
                server.getApiClient().eventBus.events
            }
            .onEach { event ->
                if (event is ReLoginEvent.Unauthorized) {
                    dataSourceManager.serverLogin(
                        loginType = LoginType.API,
                        db.connectionConfigDao.selectConnectionConfig()
                    )
                }
            }
            .launchIn(dataSourceManager.dataSourceScope())
    }

    /**
     * 启动播放器相关后置初始化。
     * 包含播放器控制器、播放队列恢复、音量恢复和播放资源策略监听。
     *
     * @param dataSourceManager 当前数据源管理器，用于复用数据源 IO 作用域。
     * @param db 本地数据库，用于恢复播放队列和读取音量设置。
     */
    private fun startPlayerInitialization(
        dataSourceManager: DataSourceManager,
        db: LocalDatabaseClient
    ) {
        try {
            // 播放器、播放队列恢复和音量恢复都放到首页可见后执行，降低首屏启动压力。
            val downloadDb = get<DownloadDatabaseClient>()
            val musicController = get<MusicCommonController>()
            val musicPlayContext = get<MusicPlayContext>()
            val playerEventCoordinator = get<PlayerEventCoordinator>()
            val settingsManager = get<SettingsManager>()
            val volumeServer = get<VolumeServer>()
            val dataSourceScope = dataSourceManager.dataSourceScope()
            // 播放器事件协调器在这里开始接管事件流，后续播放行为才会进入统一管线。
            playerEventCoordinator.start()
            musicController.initController {
                // 播放队列恢复依赖播放器控制器已就绪，所以放在 initController 回调里。
                dataSourceScope.launch {
                    PlayerListRestoreUtils.restoreCurrentDataSourcePlayerList(
                        db = db,
                        downloadDb = downloadDb,
                        musicPlayContext = musicPlayContext
                    )
                }
                // 音量恢复不影响首屏显示，单独后台写回播放器。
                dataSourceScope.launch {
                    volumeServer.updateVolume(
                        db.settingsDao.selectOneData()?.jvmVolume ?: 60
                    )
                }

                // 监听启动后的资源策略变化，保持当前播放状态和缓存策略一致。
                settingsManager.setOnListener(object : OnSettingsChangeListener {
                    override fun onCacheMaxBytesChanged(
                        cacheUpperLimit: CacheUpperLimitEnum,
                        oldCacheUpperLimit: CacheUpperLimitEnum
                    ) {
                        // 缓存上限从“不缓存”切回“缓存”时，如果正在播放就补拉缓存任务。
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
                        // 资源策略变化后刷新当前播放列表封面元数据，避免 UI 和资源策略脱节。
                        refreshPlaylistCoverMetadata()
                    }
                })
            }
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e("DataSourceBootstrapper", "播放器后置初始化失败", throwable)
        }
    }
}
