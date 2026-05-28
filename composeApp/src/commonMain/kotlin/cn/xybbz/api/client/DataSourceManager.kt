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

package cn.xybbz.api.client

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import androidx.room.Transaction
import cn.xybbz.api.client.data.ClientLoginInfoReq
import cn.xybbz.api.client.data.XyResponse
import cn.xybbz.api.client.navidrome.data.TranscodingInfo
import cn.xybbz.api.client.version.VersionApiClient
import cn.xybbz.api.exception.ServiceException
import cn.xybbz.api.state.ClientLoginInfoState
import cn.xybbz.api.state.Source
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.DownloadTypes
import cn.xybbz.common.enums.LoginStateType
import cn.xybbz.common.enums.LoginType
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.enums.koinQualifier
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.OperationTipUtils
import cn.xybbz.common.utils.PlaylistParser
import cn.xybbz.config.scope.IoScoped
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.entity.data.LoginStateData
import cn.xybbz.entity.data.LrcEntryData
import cn.xybbz.entity.data.ResourceData
import cn.xybbz.entity.data.SearchData
import cn.xybbz.entity.data.Sort
import cn.xybbz.entity.data.music.TranscodingAndMusicUrlData
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.artist.XyArtistExt
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.get
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.add_music_to_playlist_failed
import xymusic_kmp.composeapp.generated.resources.add_music_to_playlist_success
import xymusic_kmp.composeapp.generated.resources.adding_music_to_playlist
import xymusic_kmp.composeapp.generated.resources.connection_successful
import xymusic_kmp.composeapp.generated.resources.create_playlist_failed
import xymusic_kmp.composeapp.generated.resources.create_playlist_success
import xymusic_kmp.composeapp.generated.resources.creating_playlist
import xymusic_kmp.composeapp.generated.resources.delete_playlist_failed
import xymusic_kmp.composeapp.generated.resources.delete_playlist_success
import xymusic_kmp.composeapp.generated.resources.deleting_playlist
import xymusic_kmp.composeapp.generated.resources.edit_playlist_name_failed
import xymusic_kmp.composeapp.generated.resources.edit_playlist_name_success
import xymusic_kmp.composeapp.generated.resources.editing_playlist_name
import xymusic_kmp.composeapp.generated.resources.empty_info
import xymusic_kmp.composeapp.generated.resources.get_server_resources_failed
import xymusic_kmp.composeapp.generated.resources.import_playlist_failed
import xymusic_kmp.composeapp.generated.resources.import_playlist_success
import xymusic_kmp.composeapp.generated.resources.importing_playlist
import xymusic_kmp.composeapp.generated.resources.login_failed
import xymusic_kmp.composeapp.generated.resources.login_failed_no_token
import xymusic_kmp.composeapp.generated.resources.no_connection_selected
import xymusic_kmp.composeapp.generated.resources.remove_music_from_playlist_failed
import xymusic_kmp.composeapp.generated.resources.remove_music_from_playlist_success
import xymusic_kmp.composeapp.generated.resources.removing_music_from_playlist
import xymusic_kmp.composeapp.generated.resources.server_connection_error
import xymusic_kmp.composeapp.generated.resources.server_connection_timeout


/**
 * 本地数据源管理类
 * @author xybbz
 * @date 2024/06/12
 */
open class DataSourceManager(
    private val db: LocalDatabaseClient,
    private val versionApiClient: VersionApiClient,
) : IDataSourceServer, IoScoped() {


    val settingsManager: SettingsManager = get()

    var dataSourceType by mutableStateOf<DataSourceType?>(null)
        private set

    init {
        createScope()
    }

    /**
     * 当前可安全读取的数据源服务对象。
     * MainScreen 的主壳门禁只看这个值是否非空，不再等待 serverLogin 全链路完成。
     */
    private val _dataSourceServerFlow = MutableStateFlow<IDataSourceParentServer?>(null)
    val dataSourceServerFlow: StateFlow<IDataSourceParentServer?> = _dataSourceServerFlow.asStateFlow()

    /**
     * 安全读取当前数据源服务。
     * 启动自动登录还未切换数据源时返回 null，让调用方走空态兜底。
     */
    private fun currentDataSourceServerOrNull(): IDataSourceParentServer? =
        _dataSourceServerFlow.value

    /**
     * 读取必须已经初始化的数据源服务。
     */
    private fun requireDataSourceServer(): IDataSourceParentServer =
        currentDataSourceServerOrNull()
            ?: throw IllegalStateException("Data source server is not initialized")

    @OptIn(ExperimentalCoroutinesApi::class)
    val loginStateFlow: Flow<LoginStateType> =
        dataSourceServerFlow
            .filterNotNull()
            .flatMapLatest { server ->
                server.loginSuccessEvent
            }.filter { it != LoginStateType.UNKNOWN }


    @OptIn(ExperimentalCoroutinesApi::class)
    private val mediaLibraryIdFlow: Flow<String?> =
        dataSourceServerFlow
            .filterNotNull()
            .flatMapLatest { server ->
                server.mediaLibraryIdFlow.drop(1)
            }.distinctUntilChanged()

    val taggedLoginFlow = loginStateFlow.map { Source.Login(it) }
    val taggedMediaFlow = mediaLibraryIdFlow.map { Source.Library(it) }

    val mergeFlow = merge(
        taggedLoginFlow,
        taggedMediaFlow
    ).filter { (it is Source.Login && it.value != LoginStateType.UNKNOWN) || (it is Source.Library /*&& it.value != null*/) }


    //加载状态
    var loading by mutableStateOf(false)
        private set

    /**
     * 自动登录是否正在后台执行。
     * 启动时创建数据源服务后即可进入主壳，这个状态只用于局部 loading 或提示。
     */
    private val _autoLoginRunning = MutableStateFlow(false)
    val autoLoginRunning: StateFlow<Boolean> = _autoLoginRunning.asStateFlow()

    /**
     * 自动登录最近一次状态。
     * IDataSourceParentServer.autoLogin 内部会 catch 异常并 emit 状态，这里不再额外 catch。
     */
    private val _autoLoginState = MutableStateFlow<ClientLoginInfoState?>(null)
    val autoLoginState: StateFlow<ClientLoginInfoState?> = _autoLoginState.asStateFlow()

    //是够登陆异常
    var ifLoginError by mutableStateOf(false)
        private set

    //报错信息
    var errorHint by mutableStateOf(Res.string.empty_info)
        private set

    var errorMessage by mutableStateOf("")
        private set


    /**
     * 事件监听
     */
    private val listeners = mutableListOf<OnDatasourceListener>()

    /**
     * 初始化对象信息
     */
    suspend fun initDataSource() {
        settingsManager.settings.collect {
            Log.i("=====", "开始自动登录")
            val connectionId = it?.connectionId
            val dataSourceType = it?.dataSourceType
            if (dataSourceType != null && connectionId != null) {
                Log.i("=====", "开始自动登录中")
                //获得启用的连接信息
                val connectionConfig =
                    db.connectionConfigDao.selectById(connectionId)
                // 先把本地连接配置绑定到具体数据源，getConnectionId 可以直接读取同一个来源。
                switchDataSource(dataSourceType, connectionConfig)
                for (listener in listeners) {
                    listener.autoLoginBefore(connectionConfig)
                }
                scope.launch {
                    // 自动登录不再阻塞 initDataSource 返回，主壳只需要数据源服务对象先可读。
                    serverLogin(LoginType.TOKEN, connectionConfig)
                    for (listener in listeners) {
                        listener.autoLoginSuccessAfter(connectionConfig)
                    }
                }
            }
        }

    }

    /**
     * 根据下载类型获得数据源
     */
    fun getApiClient(downloadTypes: DownloadTypes): DownloadFactory {
        return when (downloadTypes) {
            DownloadTypes.APK -> {
                versionApiClient
            }

            else -> {
                requireDataSourceServer().getApiClient()
            }
        }
    }

    /**
     * 登陆服务端
     */
    suspend fun serverLogin(
        loginType: LoginType = LoginType.TOKEN,
        connectionConfig: ConnectionConfig?
    ) {
        // 手动刷新登录时先同步本地连接配置，避免 UI 和后续同步读取到旧连接上下文。
        connectionConfig?.let { currentDataSourceServerOrNull()?.bindLocalConnectionConfig(it) }
        ifLoginError = false
        // serverLogin 只消费 autoLogin 发出的状态；异常已经在数据源 flow 内转成 ClientLoginInfoState。
        _autoLoginRunning.value = true
        autoLogin(loginType, connectionConfig).collect { loginState ->
            // 登录状态只写入 autoLoginState，避免 loginStatus 和 StateFlow 维护两份状态。
            _autoLoginState.value = loginState
            val loginSateInfo = getLoginSateInfo(loginState)
            errorHint = loginSateInfo.errorHint ?: Res.string.empty_info
            errorMessage = loginSateInfo.errorMessage ?: ""
            ifLoginError = loginSateInfo.isError
            loading = loginSateInfo.loading
            _autoLoginRunning.value = loginSateInfo.loading
            if (loginSateInfo.isError) {
                MessageUtils.sendPopTipError(
                    Res.string.login_failed,
                    delay = 2000
                )
            } else if (loginSateInfo.isLoginSuccess) {
                MessageUtils.sendPopTipSuccess(
                    Res.string.connection_successful,
                    delay = 2000
                )
            }
        }
        _autoLoginRunning.value = false
    }

    /**
     * 根据登录状态返回不同登录信息
     */
    fun getLoginSateInfo(loginState: ClientLoginInfoState): LoginStateData {
        return when (loginState) {
            is ClientLoginInfoState.Connected -> {
                Log.i("=====", "连接中")
                LoginStateData(loading = true)
            }

            is ClientLoginInfoState.ConnectError -> {
                Log.i(Constants.LOG_ERROR_PREFIX, "服务端连接错误")
                LoginStateData(
                    loading = false,
                    isError = true,
                    errorHint = Res.string.server_connection_error
                )
            }

            ClientLoginInfoState.ServiceTimeOutState -> {
                Log.i(Constants.LOG_ERROR_PREFIX, "服务端连接超时")
                LoginStateData(
                    loading = false,
                    isError = true,
                    errorHint = Res.string.server_connection_timeout
                )
            }

            is ClientLoginInfoState.ErrorState -> {
                Log.i(Constants.LOG_ERROR_PREFIX, loginState.error.message.toString())
                LoginStateData(
                    loading = false,
                    isError = true,
                    errorHint = Res.string.server_connection_error,
                    errorMessage = loginState.error.message.toString()
                )
            }

            ClientLoginInfoState.SelectServer -> {
                Log.i(Constants.LOG_ERROR_PREFIX, "未选择连接")
                LoginStateData(
                    loading = false,
                    isError = true,
                    errorHint = Res.string.no_connection_selected
                )
            }

            ClientLoginInfoState.UnauthorizedErrorState -> {
                Log.i(Constants.LOG_ERROR_PREFIX, "登录失败,账号或密码错误")
                LoginStateData(
                    loading = false,
                    isError = true,
                    errorHint = Res.string.login_failed_no_token
                )
            }

            ClientLoginInfoState.UserLoginSuccess -> {
                Log.i("=====", "登陆成功")
                LoginStateData(
                    loading = false,
                    isError = false,
                    isLoginSuccess = true
                )
            }
        }
    }

    /**
     * 切换数据源
     * @param [dataSourceType] 数据源类型
     * @param [connectionConfig] 本地连接配置；首次新增连接时尚未保存，所以允许为空。
     */
    fun switchDataSource(
        dataSourceType: DataSourceType,
        connectionConfig: ConnectionConfig? = null
    ) {
        updateDataSourceType(dataSourceType)
        Log.i("=====", "数据源开始切换")
        getDataSourceServerByType(dataSourceType, false).let {
            // 发布服务前先绑定本地连接配置，getConnectionId 不再依赖额外缓存状态。
            connectionConfig?.let(it::bindLocalConnectionConfig)
            // 发布非空服务对象后，MainScreen 即可创建主壳；自动登录仍可继续后台执行。
            _dataSourceServerFlow.value = it
        }
        Log.i("=====", "数据源切换完成")
    }

    fun updateDataSourceType(type: DataSourceType) {
        this.dataSourceType = type
    }

    /**
     * 根据数据源获得相应的服务类
     */
    fun getDataSourceServerByType(
        dataSourceType: DataSourceType,
        ifTmp: Boolean
    ): IDataSourceParentServer {
        val iDataSourceParentServer: IDataSourceParentServer = get(dataSourceType.koinQualifier())
        iDataSourceParentServer.updateIfTmpObject(ifTmp)
        return iDataSourceParentServer
    }

    /**
     * 切换连接服务
     */
    suspend fun changeDataSource(connectionConfig: ConnectionConfig) {
        release()
        // 切换连接时先绑定本地连接配置，等待自动登录期间 UI 仍可展示当前选择。
        switchDataSource(connectionConfig.type, connectionConfig)
        for (listener in listeners) {
            listener.autoLoginBefore(connectionConfig)
        }
        serverLogin(connectionConfig = connectionConfig)
        for (listener in listeners) {
            listener.autoLoginSuccessAfter(connectionConfig)
        }
    }


    /**
     * 绑定地址
     */
    override suspend fun addClientAndLogin(
        clientLoginInfoReq: ClientLoginInfoReq,
        connectionConfig: ConnectionConfig?
    ): Flow<ClientLoginInfoState> {
        // 新增连接成功后，具体数据源会在登录流程中写入自己的连接配置；这里不再维护第二份连接 ID。
        return requireDataSourceServer().addClientAndLogin(clientLoginInfoReq, connectionConfig)
    }

    override suspend fun autoLogin(
        loginType: LoginType,
        connectionConfig: ConnectionConfig?
    ): Flow<ClientLoginInfoState> {
        loading = true
        Log.i("=====", "开始登录.............")
        return requireDataSourceServer().autoLogin(loginType, connectionConfig)
    }

    /**
     * 获得资源地址
     */
    override suspend fun getResources(
        clientLoginInfoReq: ClientLoginInfoReq
    ): List<ResourceData> {
        return try {
            requireDataSourceServer().getResources(clientLoginInfoReq)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得服务器资源失败", e)
            throw ServiceException(message = getString(Res.string.get_server_resources_failed))
        }
    }

    /**
     * 获得OkHttpClient
     */
    override fun getHttpClient(): HttpClient {
        return requireDataSourceServer().getHttpClient()
    }

    /**
     * 获得连接id
     */
    override fun getConnectionId(): Long {
        // 连接 ID 的唯一来源是当前可用数据源服务；没有配置时保持原来的 0 兜底。
        return currentDataSourceServerOrNull()?.getConnectionId()?.takeIf { it != 0L } ?: 0L
    }

    /**
     * 获得连接地址
     */
    override fun getConnectionAddress(): String {
        // 数据源尚未初始化时返回空地址，避免组合阶段因 lateinit 未就绪崩溃。
        return currentDataSourceServerOrNull()?.getConnectionAddress().orEmpty()
    }

    /**
     * 更新连接设置
     */
    override suspend fun updateConnectionConfig(connectionConfig: ConnectionConfig) {
        requireDataSourceServer().updateConnectionConfig(connectionConfig)
    }

    /**
     * 更新媒体库id
     */
    override suspend fun updateLibraryId(libraryIds: List<String>?, connectionId: Long) {
        return requireDataSourceServer().updateLibraryId(libraryIds, connectionId)
    }

    /**
     * 获得专辑,艺术家,音频,歌单数量
     */
    override suspend fun getDataInfoCount(connectionId: Long) {
        requireDataSourceServer().getDataInfoCount(connectionId)
    }


    /**
     * 初始化收藏数据
     */
    override suspend fun initFavoriteData(connectionId: Long) {
        return requireDataSourceServer().initFavoriteData(connectionId)
    }

    /**
     * 获得专辑数据
     */
    override fun selectAlbumFlowList(
        sort: Sort
    ): Flow<PagingData<XyAlbum>> {
        return requireDataSourceServer().selectAlbumFlowList(sort)
    }

    /**
     * 获得音乐数据
     */
    override fun selectMusicFlowList(
        sort: Sort
    ): Flow<PagingData<XyMusic>> {
        return requireDataSourceServer().selectMusicFlowList(sort)
    }

    /**
     * 获得艺术家
     */
    override fun selectArtistFlowList(): Flow<PagingData<XyArtistExt>> {
        return requireDataSourceServer().selectArtistFlowList()
    }

    /**
     * 搜索音乐,艺术家,专辑
     */
    override suspend fun searchAll(search: String): SearchData {
        return requireDataSourceServer().searchAll(search)
    }

    /**
     * 获得最近播放音乐
     */
    override suspend fun playRecordMusicOrAlbumList(pageSize: Int) {
        try {
            requireDataSourceServer().playRecordMusicOrAlbumList(pageSize)
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得最近播放音乐更新超时", e)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得最近播放音乐更新失败", e)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得最近播放音乐未知错误失败", e)
        }
    }

    /**
     * 获得最近播放音乐列表
     */
    override suspend fun getPlayRecordMusicList(pageSize: Int): List<XyMusic> {
        return requireDataSourceServer().getPlayRecordMusicList(pageSize)
    }

    /**
     * 获得最多播放
     */
    override suspend fun getMostPlayerMusicList() {
        try {
            requireDataSourceServer().getMostPlayerMusicList()
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得最多播放专辑失败", e)
        }
    }

    /**
     * 获得最新专辑
     */
    override suspend fun getNewestAlbumList() {
        try {
            requireDataSourceServer().getNewestAlbumList()
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得最新专辑失败", e)
        }
    }


    /**
     * 获得专辑信息
     * @param [albumId] 专辑id
     * @return 专辑+艺术家信息
     */
    override suspend fun selectAlbumInfoById(
        albumId: String,
        dataType: MusicDataTypeEnum
    ): XyAlbum? {
        return try {
            requireDataSourceServer().selectAlbumInfoById(albumId, dataType)
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取专辑信息超时", e)
            return null
        } catch (e: ServiceException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取专辑信息失败", e)
            return null
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取专辑信息未知异常失败", e)
            return null
        }

    }

    /**
     * 从本地缓存获得专辑信息
     */
    override suspend fun selectLocalAlbumInfoById(albumId: String): XyAlbum? {
        return try {
            requireDataSourceServer().selectLocalAlbumInfoById(albumId)
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取本地专辑信息超时", e)
            null
        } catch (e: ServiceException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取本地专辑信息失败", e)
            null
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取本地专辑信息未知异常失败", e)
            null
        }
    }

    /**
     * 从远程获得专辑信息
     */
    override suspend fun selectServerAlbumInfoById(
        albumId: String,
        dataType: MusicDataTypeEnum
    ): XyAlbum? {
        return try {
            requireDataSourceServer().selectServerAlbumInfoById(albumId, dataType)
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取远程专辑信息超时", e)
            null
        } catch (e: ServiceException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取远程专辑信息失败", e)
            null
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取远程专辑信息未知异常失败", e)
            null
        }
    }

    override suspend fun selectMusicInfoById(itemId: String): XyMusic? {
        return try {
            requireDataSourceServer().selectMusicInfoById(itemId)
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取歌曲信息超时", e)
            null
        } catch (e: ServiceException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取歌曲信息失败", e)
            null
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取歌曲信息未知异常失败", e)
            null
        }
    }

    /**
     * 获得专辑内音乐列表
     * @param [itemId] 专辑id
     */
    override fun selectMusicListByParentId(
        itemId: String,
        dataType: MusicDataTypeEnum,
        sort: Sort
    ): Flow<PagingData<XyMusic>> {
        return requireDataSourceServer().selectMusicListByParentId(
            itemId = itemId,
            dataType = dataType,
            sort = sort
        )
    }

    /**
     * 根据艺术家获得专辑列表
     */
    override fun selectAlbumListByArtistId(artistId: String): Flow<PagingData<XyAlbum>> {
        return requireDataSourceServer().selectAlbumListByArtistId(artistId)
    }

    /**
     * 根据艺术家获得音乐列表
     */
    override fun selectMusicListByArtistId(
        artistId: String,
        artistName: String
    ): Flow<PagingData<XyMusic>> {
        return requireDataSourceServer().selectMusicListByArtistId(artistId, artistName)
    }

    /**
     * 获得歌曲列表
     */
    override suspend fun getMusicList(
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return try {
            requireDataSourceServer().getMusicList(pageSize, pageNum)
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "加载分页数据超时", e)
            null
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "加载分页数据报错", e)
            null
        }
    }

    /**
     * 根据专辑获得歌曲列表
     */
    override suspend fun getMusicListByAlbumId(
        albumId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return try {
            requireDataSourceServer().getMusicListByAlbumId(albumId, pageSize, pageNum)
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "加载分页数据超时", e)
            null
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "加载分页数据报错", e)
            null
        }
    }

    /**
     * 根据艺术家获得歌曲列表
     */
    override suspend fun getMusicListByArtistId(
        artistId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return try {
            requireDataSourceServer().getMusicListByArtistId(artistId, pageSize, pageNum)
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "加载分页数据超时", e)
            null
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "加载分页数据报错", e)
            null
        }
    }

    /**
     * 根据艺术家列表获得歌曲列表
     */
    override suspend fun getMusicListByArtistIds(
        artistIds: List<String>,
        pageSize: Int
    ): List<XyMusic>? {
        return try {
            requireDataSourceServer().getMusicListByArtistIds(artistIds, pageSize)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "根据艺术家列表获得歌曲列表失败", e)
            null
        }
    }

    /**
     * 获得收藏歌曲列表
     */
    override suspend fun getMusicListByFavorite(
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return try {
            requireDataSourceServer().getMusicListByFavorite(pageSize, pageNum)
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "加载分页数据超时", e)
            null
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "加载分页数据报错", e)
            null
        }
    }

    /**
     * 获取远程服务器的专辑和歌单音乐列表
     * @param [startIndex] 开始索引
     * @param [pageSize] 页面大小
     * @param [isFavorite] 是否收藏
     * @param [sortType] 排序类型
     * @param [years] 年列表
     * @param [parentId] 上级id
     * @param [dataType] 数据类型
     * @return [AllResponse<XyMusic>]
     */
    override suspend fun getRemoteServerMusicListByAlbumOrPlaylist(
        startIndex: Int,
        pageSize: Int,
        isFavorite: Boolean?,
        sortType: SortTypeEnum?,
        years: List<Int>?,
        parentId: String,
        dataType: MusicDataTypeEnum
    ): XyResponse<XyMusic> {
        return requireDataSourceServer().getRemoteServerMusicListByAlbumOrPlaylist(
            startIndex = startIndex,
            pageSize = pageSize,
            isFavorite = isFavorite,
            sortType = sortType,
            years = years,
            parentId = parentId,
            dataType = dataType
        )
    }


    /**
     * 获得随机音乐
     */
    override suspend fun getRandomMusicList(
        pageSize: Int,
        pageNum: Int,
    ): List<XyMusic>? {
        return try {
            requireDataSourceServer().getRandomMusicList(pageSize, pageNum)
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "加载分页数据超时", e)
            return null
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "加载分页数据报错", e)
            return null
        }
    }

    /**
     * 获得随机音乐
     */
    override suspend fun getRandomMusicExtendList(
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return try {
            requireDataSourceServer().getRandomMusicExtendList(pageSize, pageNum)
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "加载分页数据超时", e)
            return null
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "加载分页数据报错", e)
            return null
        }
    }

    /**
     * 根据音乐获得歌词信息
     * @param [itemId] 音乐id
     * @return 返回歌词列表
     */
    override suspend fun getMusicLyricList(itemId: String): List<LrcEntryData>? {
        return try {
            requireDataSourceServer().getMusicLyricList(itemId)?.takeIf { it.isNotEmpty() }
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得歌词超时", e)
            null
        } catch (e: ServiceException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得歌词失败", e)
            null
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得歌词未知异常失败", e)
            null
        }
    }


    /**
     * 获取歌单列表
     * @return [List<XyPlaylist>?]
     */
    override suspend fun getPlaylists(): List<XyAlbum>? {
        // 首次进入侧边栏时可能还没完成自动登录，未就绪就跳过这次远程刷新。
        val server = currentDataSourceServerOrNull() ?: return null
        return try {
            server.getPlaylists()
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取歌单失败", e)
            null
        }
    }

    /**
     * 增加歌单
     * @param [name] 名称
     * @return [String?] 歌单id
     */
    override suspend fun addPlaylist(name: String): Boolean {
        return OperationTipUtils.operationTipNotToBlock(
            loadingMessage = Res.string.creating_playlist,
            successMessage = Res.string.create_playlist_success,
            errorMessage = Res.string.create_playlist_failed
        ) {
            requireDataSourceServer().addPlaylist(name)
        }
    }

    /**
     * 导入歌单
     */
    override suspend fun importPlaylist(
        playlistData: PlaylistParser.Playlist,
        playlistId: String
    ): Boolean {
        return OperationTipUtils.operationTipNotToBlock(
            loadingMessage = Res.string.importing_playlist,
            successMessage = Res.string.import_playlist_success,
            errorMessage = Res.string.import_playlist_failed
        ) {
            try {
                requireDataSourceServer().importPlaylist(playlistData, playlistId)
            } catch (e: Exception) {
                Log.e(Constants.LOG_ERROR_PREFIX, "导入歌单失败", e)
                false
            }

        }

    }

    /**
     * 编辑歌单名称
     * @param [id] ID
     * @param [name] 姓名
     */
    override suspend fun editPlaylistName(id: String, name: String): Boolean {
        return OperationTipUtils.operationTipNotToBlock(
            loadingMessage = Res.string.editing_playlist_name,
            successMessage = Res.string.edit_playlist_name_success,
            errorMessage = Res.string.edit_playlist_name_failed
        ) {
            try {
                requireDataSourceServer().editPlaylistName(id, name)
            } catch (e: Exception) {
                Log.e(Constants.LOG_ERROR_PREFIX, "编辑歌单名称失败", e)
                false
            }

        }
    }

    /**
     * 删除歌单
     * @param [id] ID
     */
    override suspend fun removePlaylist(id: String): Boolean {
        return OperationTipUtils.operationTipNotToBlock(
            loadingMessage = Res.string.deleting_playlist,
            successMessage = Res.string.delete_playlist_success,
            errorMessage = Res.string.delete_playlist_failed
        ) {
            try {
                requireDataSourceServer().removePlaylist(id)
            } catch (e: Exception) {
                Log.e(Constants.LOG_ERROR_PREFIX, "删除歌单失败", e)
                false
            }
        }

    }

    /**
     * 保存自建歌单中的音乐
     * @param [playlistId] 歌单id
     * @param [musicIds] 音乐id
     */
    override suspend fun saveMusicPlaylist(
        playlistId: String,
        musicIds: List<String>
    ): Boolean {
        return OperationTipUtils.operationTipNotToBlock(
            loadingMessage = Res.string.adding_music_to_playlist,
            successMessage = Res.string.add_music_to_playlist_success,
            errorMessage = Res.string.add_music_to_playlist_failed
        ) {
            try {
                requireDataSourceServer().saveMusicPlaylist(playlistId, musicIds)
            } catch (e: Exception) {
                Log.e(Constants.LOG_ERROR_PREFIX, "保存自建歌单中的音乐失败", e)
                false
            }

        }
    }

    /**
     * 删除自建歌单中的音乐
     * @param [playlistId] 歌单id
     * @param [musicIds] 音乐id集合
     */
    override suspend fun removeMusicPlaylist(
        playlistId: String,
        musicIds: List<String>
    ): Boolean {
        return OperationTipUtils.operationTipNotToBlock(
            loadingMessage = Res.string.removing_music_from_playlist,
            successMessage = Res.string.remove_music_from_playlist_success,
            errorMessage = Res.string.remove_music_from_playlist_failed
        ) {
            try {
                requireDataSourceServer().removeMusicPlaylist(playlistId, musicIds)
            } catch (e: Exception) {
                Log.e(Constants.LOG_ERROR_PREFIX, "删除自建歌单中的音乐失败", e)
                false
            }

        }
    }


    /**
     * 获得艺术家信息
     */
    override suspend fun selectArtistInfoByIds(artistIds: List<String>): List<XyArtist> {
        return requireDataSourceServer().selectArtistInfoByIds(artistIds)
    }

    /**
     * 获得艺术家信息
     */
    override suspend fun selectArtistInfoById(artistId: String): XyArtist? {
        return try {
            requireDataSourceServer().selectArtistInfoById(artistId)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "根据id从远程获得艺术家信息失败", e)
            null
        }
    }

    /**
     * 从远程获得艺术家描述
     */
    override suspend fun selectServerArtistInfo(artistId: String): XyArtist? {
        return try {
            requireDataSourceServer().selectServerArtistInfo(artistId)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "根据id从远程获得艺术家描述失败", e)
            null
        }
    }

    /**
     * 获得歌手热门歌曲列表
     */
    override suspend fun getArtistPopularMusicList(
        artistId: String?,
        artistName: String?
    ): List<XyMusic> {
        return try {
            requireDataSourceServer().getArtistPopularMusicList(artistId, artistName)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得歌手热门歌曲列表失败", e)
            null
        } ?: emptyList()
    }

    /**
     * 远程获得相似艺术家
     */
    override suspend fun getSimilarArtistsRemotely(
        artistId: String,
        startIndex: Int,
        pageSize: Int
    ): List<XyArtist> {
        return try {
            requireDataSourceServer().getSimilarArtistsRemotely(artistId, startIndex, pageSize)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得歌手热门歌曲列表失败", e)
            null
        } ?: emptyList()
    }

    /**
     * 获得流派列表
     */
    override suspend fun selectGenresPage(): Flow<PagingData<XyGenre>> {
        return requireDataSourceServer().selectGenresPage()
    }

    /**
     * 获得流派详情
     */
    override suspend fun getGenreById(genreId: String): XyGenre? {
        return try {
            return requireDataSourceServer().getGenreById(genreId)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得流派详情失败", e)
            null
        }
    }

    /**
     * 获得流派内音乐列表/或者专辑
     * @param [genreId] 流派id
     */
    override fun selectAlbumListByGenreId(genreId: String): Flow<PagingData<XyAlbum>> {
        return requireDataSourceServer().selectAlbumListByGenreId(genreId)
    }

    /**
     * 获得流派内音乐列表/或者专辑
     * @param [genreIds] 流派名称
     */
    override suspend fun selectMusicListByGenreIds(
        genreIds: List<String>,
        pageSize: Int
    ): List<XyMusic>? {
        return try {
            requireDataSourceServer().selectMusicListByGenreIds(genreIds, pageSize)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得流派内音乐列表失败", e)
            null
        }
    }

    /**
     * 获得收藏歌曲列表
     */
    override fun selectFavoriteMusicFlowList(): Flow<PagingData<XyMusic>> {
        return requireDataSourceServer().selectFavoriteMusicFlowList()
    }

    /**
     * 将项目标记为收藏
     * @param [itemId] 专辑/音乐id
     */
    override suspend fun markFavoriteItem(itemId: String, dataType: MusicTypeEnum): Boolean {
        return try {
            requireDataSourceServer().markFavoriteItem(itemId, dataType)
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "收藏超时", e)
            return false
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "收藏失败", e)
            return false
        }

    }

    /**
     * 取消项目收藏
     * @param [itemId] 专辑/音乐id
     */
    override suspend fun unmarkFavoriteItem(itemId: String, dataType: MusicTypeEnum): Boolean {
        return try {
            requireDataSourceServer().unmarkFavoriteItem(itemId, dataType)
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "取消收藏超时", e)
            return true
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "取消收藏失败", e)
            return true
        }

    }

    /**
     * 获得播放连接
     */
    override suspend fun getMusicPlayUrl(
        musicId: String,
        plexPlayKey: String?
    ): TranscodingAndMusicUrlData {
        return requireDataSourceServer().getMusicPlayUrl(
            musicId,
            plexPlayKey
        )
    }

    /**
     * 上报播放
     */
    override suspend fun reportPlaying(
        musicId: String,
        playSessionId: String,
        isPaused: Boolean,
        positionTicks: Long?
    ) {

        try {
            requireDataSourceServer().reportPlaying(musicId, playSessionId, isPaused, positionTicks)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "播放上报失败", e)
        }
    }

    /**
     * 上报播放进度
     */
    override suspend fun reportProgress(
        musicId: String,
        playSessionId: String,
        positionTicks: Long?
    ) {
        try {
            requireDataSourceServer().reportProgress(musicId, playSessionId, positionTicks)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "播放上报失败", e)
        }
    }

    /**
     * 取消上报播放进度
     */
    override suspend fun cancelReportProgress(musicId: String) {
        try {
            requireDataSourceServer().cancelReportProgress(musicId)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "取消播放上报失败", e)
        }
    }

    /**
     * 获得相似歌曲列表
     */
    override suspend fun getSimilarMusicList(musicId: String): List<XyMusic> {
        return try {
            requireDataSourceServer().getSimilarMusicList(musicId) ?: emptyList()
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得相似歌曲列表失败", e)
            emptyList()
        }
    }

    /**
     * 获得数据源支持的转码类型
     */
    override suspend fun getTranscodingType(): List<TranscodingInfo> {
        return try {
            requireDataSourceServer().getTranscodingType()
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取转码类型失败", e)
            emptyList()
        }
    }

    /**
     * 获得是否可以下载
     */
    fun getCanDownload(): Boolean {
        return requireDataSourceServer().getCanDownload()
    }

    fun getCanDelete(): Boolean {
        return requireDataSourceServer().getCanDelete()
    }

    /**
     * 设置收藏音乐信息
     */
    @Transaction
    suspend fun setFavoriteData(
        type: MusicTypeEnum,
        itemId: String,
        ifFavorite: Boolean
    ): Boolean {
        return requireDataSourceServer().setFavoriteData(
            type = type,
            itemId = itemId,
            ifFavorite = ifFavorite
        )
    }


    /**
     * 根据musicId删除音乐数据
     * @param [musicId] 音乐id
     */
    @Transaction
    suspend fun removeMusicById(musicId: String) {
        OperationTipUtils.operationTipNotToBlock {
            val bool = requireDataSourceServer().removeById(musicId)
            db.musicDao.removeByItemId(musicId)
            bool
        }
    }

    /**
     * 根据musicIds删除音乐数据
     * @param [musicIds] 音乐id
     */
    @Transaction
    suspend fun removeMusicByIds(musicIds: List<String>): Boolean {
        return OperationTipUtils.operationTipNotToBlock {
            val bool = try {
                requireDataSourceServer().removeByIds(musicIds)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
            db.musicDao.removeByItemIds(musicIds)
            bool
        }
    }

    /**
     * 更新数据源远程键数据管理
     */
    suspend fun updateDataSourceRemoteKey(remoteCurrentId: String) {
        requireDataSourceServer().updateDataSourceRemoteKey(remoteCurrentId)
    }


    fun dataSourceScope() = scope


    fun addListener(listener: OnDatasourceListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: OnDatasourceListener) {
        listeners.remove(listener)
    }

    fun release() {
        val dataSourceServer = currentDataSourceServerOrNull()
        // 先把服务置空，主界面会退回局部 loading，避免继续读旧数据源。
        _dataSourceServerFlow.value = null
        dataSourceServer?.close()
        dataSourceType = null
        // 清空自动登录状态，防止下一次启动/切换复用旧状态展示。
        _autoLoginState.value = null
        _autoLoginRunning.value = false
    }

    override fun close() {
        super.close()
        versionApiClient.release()
        release()
    }
}
