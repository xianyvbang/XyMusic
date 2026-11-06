package cn.xybbz.startup

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.events.ReLoginEvent
import cn.xybbz.api.state.ClientLoginInfoState
import cn.xybbz.common.enums.LoginType
import cn.xybbz.localdata.data.connection.ConnectionConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.time.Clock
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.empty_info

/**
 * 应用会话协调器。
 * 所有恢复、登录、重试和连接切换都在这里串行执行，页面只消费会话状态。
 */
@OptIn(ExperimentalAtomicApi::class, ExperimentalCoroutinesApi::class)
class SessionCoordinator(
    private val dataSourceManager: DataSourceManager,
) : KoinComponent {
    /** 当前应用会话状态。 */
    private val _state = MutableStateFlow<AppSessionState>(AppSessionState.NoConnection)
    val state: StateFlow<AppSessionState> = _state.asStateFlow()

    /** 会话就绪或媒体库切换时的统一数据刷新触发流。 */
    val readyOrLibraryChangedFlow = state.flatMapLatest { sessionState ->
        if (sessionState is AppSessionState.Ready) {
            merge(
                flowOf(Unit),
                dataSourceManager.mediaLibraryChangedFlow.map { Unit },
            )
        } else {
            emptyFlow()
        }
    }

    /** 连接或媒体库发生变化时的上下文流，供分页数据重新建立查询。 */
    val dataContextChangedFlow = state
        .mapNotNull { it.connection?.id }
        .distinctUntilChanged()
        .flatMapLatest { connectionId ->
            merge(
                flowOf(connectionId to null),
                dataSourceManager.mediaLibraryChangedFlow.map { connectionId to it },
            )
        }
        .distinctUntilChanged()

    private val started = AtomicBoolean(false)
    private val sessionMutex = Mutex()
    private var appScope: CoroutineScope? = null
    private var sessionJob: Job? = null
    private var playerInitialized = false
    private var loginEventBusStarted = false
    /** 应用上次进入后台的时间戳。 */
    private var appInactiveAtMs: Long? = null

    /** 在应用启动时开始一次连接恢复流程。 */
    fun start(scope: CoroutineScope) {
        if (!started.compareAndSet(expectedValue = false, newValue = true)) {
            return
        }
        appScope = scope
        /** 交互式登录可能先于应用级启动器完成，此时直接接管现有会话。 */
        if (state.value !is AppSessionState.Ready) {
            sessionJob = scope.launch { restoreAndLogin() }
        }
    }

    /** 重新登录当前连接，供首页重试按钮使用。 */
    fun retry() {
        val scope = appScope ?: return
        sessionJob?.cancel()
        sessionJob = scope.launch {
            val connection = state.value.connection
                ?: dataSourceManager.restoreLocalDataSourceContext()
            if (connection == null) {
                _state.value = AppSessionState.NoConnection
                return@launch
            }
            sessionMutex.withLock { login(connection) }
        }
    }

    /** 记录应用进入后台的时间，用于恢复前台时判断是否需要重登录。 */
    fun markAppInactive() {
        appInactiveAtMs = Clock.System.now().toEpochMilliseconds()
    }

    /** 应用从后台恢复较久时，通过当前会话统一重登录。 */
    fun requestReloginOnAppActive() {
        val inactiveAt = appInactiveAtMs ?: return
        appInactiveAtMs = null
        if (Clock.System.now().toEpochMilliseconds() - inactiveAt >= ACTIVE_RELOGIN_INTERVAL_MS) {
            retry()
        }
    }

    /** 切换连接，并复用恢复、缓存和登录状态机。 */
    fun switchConnection(connection: ConnectionConfig) {
        val scope = appScope ?: return
        sessionJob?.cancel()
        sessionJob = scope.launch {
            sessionMutex.withLock {
                _state.value = AppSessionState.Restoring(connection)
                dataSourceManager.release()
                dataSourceManager.switchDataSource(connection.type, connection)
                _state.value = AppSessionState.OfflineReady(connection)
                login(connection)
            }
        }
    }

    /** 清除当前会话，通常在删除最后一个连接时调用。 */
    fun clear() {
        sessionJob?.cancel()
        dataSourceManager.release()
        _state.value = AppSessionState.NoConnection
    }

    /** 接收连接页完成的交互式登录，避免依赖首页创建时机。 */
    suspend fun markInteractiveLoginSuccess(connection: ConnectionConfig? = null) {
        val currentConnection = connection
            ?: state.value.connection
            ?: dataSourceManager.restoreLocalDataSourceContext()
        if (currentConnection != null) {
            prepareSessionServices()
            initializePlayerIfNeeded()
            _state.value = AppSessionState.Ready(currentConnection)
        }
    }

    private suspend fun restoreAndLogin() {
        sessionMutex.withLock {
            val connection = dataSourceManager.restoreLocalDataSourceContext()
            if (connection == null) {
                _state.value = AppSessionState.NoConnection
                return
            }
            _state.value = AppSessionState.Restoring(connection)
            _state.value = AppSessionState.OfflineReady(connection)
            login(connection)
        }
    }

    private suspend fun login(connection: ConnectionConfig) {
        _state.value = AppSessionState.LoggingIn(connection)
        var latestLoginState: ClientLoginInfoState? = null
        val result = dataSourceManager.loginConnection(LoginType.API, connection) { progress, loginResult ->
            latestLoginState = progress
            _state.value = if (loginResult.isError) {
                AppSessionState.LoginFailed(
                    connection = connection,
                    loginState = progress,
                    errorHint = loginResult.errorHint ?: Res.string.empty_info,
                    errorMessage = loginResult.errorMessage.orEmpty(),
                )
            } else {
                AppSessionState.LoggingIn(connection, progress)
            }
        }
        if (result.isError || !result.isLoginSuccess) {
            _state.value = AppSessionState.LoginFailed(
                connection = connection,
                loginState = latestLoginState,
                errorHint = result.errorHint ?: Res.string.empty_info,
                errorMessage = result.errorMessage.orEmpty(),
            )
        } else {
            prepareSessionServices()
            initializePlayerIfNeeded()
            _state.value = AppSessionState.Ready(connection)
        }
    }

    /** 启动会话级登录事件监听。 */
    private fun prepareSessionServices() {
        if (!loginEventBusStarted) {
            startLoginEventBus()
            loginEventBusStarted = true
        }
    }

    /** 登录成功后初始化播放器，避免播放器生命周期参与登录和首页首帧。 */
    private fun initializePlayerIfNeeded() {
        val initializer = get<PlayerInitializer>()
        if (!playerInitialized) {
            playerInitialized = true
            initializer.initialize(dataSourceManager)
        } else {
            /** 已有播放器控制器时，只恢复当前连接自己的播放列表。 */
            initializer.restoreForConnection(dataSourceManager)
        }
    }

    /** 将 401 事件统一转为当前会话的重试请求。 */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startLoginEventBus() {
        dataSourceManager.dataSourceServerFlow
            .filterNotNull()
            .flatMapLatest { server -> server.getApiClient().eventBus.events }
            .onEach { event ->
                if (event is ReLoginEvent.Unauthorized) {
                    retry()
                }
            }
            .launchIn(dataSourceManager.dataSourceScope())
    }

    private companion object {
        /** 应用在后台超过该时长后，恢复时重新验证连接。 */
        const val ACTIVE_RELOGIN_INTERVAL_MS = 5L * 60L * 1000L
    }
}
