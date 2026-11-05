package cn.xybbz.startup

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.events.ReLoginEvent
import cn.xybbz.common.enums.LoginType
import cn.xybbz.localdata.data.connection.ConnectionConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * 应用会话协调器。
 * 所有恢复、登录、重试和连接切换都在这里串行执行，页面只消费会话状态。
 */
@OptIn(ExperimentalAtomicApi::class)
class SessionCoordinator(
    private val dataSourceManager: DataSourceManager,
) : KoinComponent {
    /** 当前应用会话状态。 */
    private val _state = MutableStateFlow<AppSessionState>(AppSessionState.NoConnection)
    val state: StateFlow<AppSessionState> = _state.asStateFlow()

    private val started = AtomicBoolean(false)
    private val sessionMutex = Mutex()
    private var appScope: CoroutineScope? = null
    private var sessionJob: Job? = null
    private var playerInitialized = false
    private var loginEventBusStarted = false

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
        dataSourceManager.loginConnection(LoginType.API, connection)
        if (dataSourceManager.ifLoginError) {
            _state.value = AppSessionState.LoginFailed(
                connection = connection,
                message = dataSourceManager.errorMessage,
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
}
