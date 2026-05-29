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

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.RemoteMediator
import androidx.paging.map
import androidx.room.Transaction
import cn.xybbz.api.TokenServer
import cn.xybbz.api.client.data.ClientLoginInfoReq
import cn.xybbz.api.client.data.XyResponse
import cn.xybbz.api.enums.AudioCodecEnum
import cn.xybbz.api.exception.ConnectionException
import cn.xybbz.api.exception.ServiceException
import cn.xybbz.api.exception.UnauthorizedException
import cn.xybbz.api.state.ClientLoginInfoState
import cn.xybbz.assembler.MusicPlayAssembler
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.common.enums.LoginStateType
import cn.xybbz.common.enums.LoginType
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.PasswordUtils
import cn.xybbz.config.info.getPlatformInfo
import cn.xybbz.config.info.shouldShowLoginMessageTips
import cn.xybbz.config.scope.IoScoped
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.database.withTransaction
import cn.xybbz.download.DownloaderManager
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.entity.data.EncryptAesData
import cn.xybbz.entity.data.Sort
import cn.xybbz.entity.data.music.TranscodingAndMusicUrlData
import cn.xybbz.localdata.common.LocalConstants
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.album.FavoriteAlbum
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.FavoriteArtist
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.artist.XyArtistExt
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.data.count.XyDataCount
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.library.XyLibrary
import cn.xybbz.localdata.data.music.PlaylistMusic
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.page.bigPager
import cn.xybbz.page.defaultPager
import cn.xybbz.page.parent.AlbumOrPlaylistMusicListRemoteMediator
import cn.xybbz.page.parent.AlbumRemoteMediator
import cn.xybbz.page.parent.ArtistAlbumListRemoteMediator
import cn.xybbz.page.parent.ArtistMusicListRemoteMediator
import cn.xybbz.page.parent.ArtistRemoteMediator
import cn.xybbz.page.parent.FavoriteMusicRemoteMediator
import cn.xybbz.page.parent.GenreAlbumListRemoteMediator
import cn.xybbz.page.parent.GenresRemoteMediator
import cn.xybbz.page.parent.MusicRemoteMediator
import cn.xybbz.platform.ContextWrapper
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.get
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.logging_in
import xymusic_kmp.composeapp.generated.resources.server_version_cannot_be_obtained
import xymusic_kmp.composeapp.generated.resources.server_version_too_low
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


abstract class IDataSourceParentServer(
    private val defaultParentApiClient: DefaultParentApiClient,
) : IDataSourceServer, IoScoped() {

    protected val db: LocalDatabaseClient = get()
    private val downloadDb: DownloadDatabaseClient = get()
    protected val settingsManager: SettingsManager = get()
    protected val contextWrapper: ContextWrapper = get()
    protected val downloaderManager: DownloaderManager = get()

    private var connectionConfig: ConnectionConfig? = null

    var libraryIds: List<String>? = null
        private set

    private val _mediaLibraryIdFlow = MutableStateFlow<String?>(Constants.MINUS_ONE_INT.toString())
    val mediaLibraryIdFlow: StateFlow<String?> = _mediaLibraryIdFlow.asStateFlow()

    /**
     * 登录状态
     */
    private val _loginSuccessEvent = MutableStateFlow(LoginStateType.UNKNOWN)
    val loginSuccessEvent = _loginSuccessEvent.asSharedFlow()


    private var ifTmpObject = false

    init {
        createScope()
    }

    @OptIn(ExperimentalAtomicApi::class)
    private val loginRetryGate = AtomicBoolean(false)

    fun ifTmpObject(): Boolean {
        return ifTmpObject
    }

    fun updateIfTmpObject(ifTmp: Boolean) {
        ifTmpObject = ifTmp
    }

    fun getApiClient(): DefaultParentApiClient {
        return defaultParentApiClient
    }

    /**
     * 预绑定本地连接上下文。
     * 启动/切换阶段会先创建数据源服务，再后台自动登录；这里仅让 getConnectionId 和媒体库选择先可读。
     * 不发送登录成功事件、不保存设置、不触发远程同步，避免把“服务可读”和“登录完成”混在一起。
     */
    fun bindLocalConnectionConfig(connectionConfig: ConnectionConfig) {
        this.connectionConfig = connectionConfig
        this.libraryIds = connectionConfig.libraryIds
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun resetLoginRetry() {
        loginRetryGate.store(false)
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun tryMarkLoginRetry(): Boolean {
        return loginRetryGate.compareAndSet(expectedValue = false, newValue = true)
    }

    /**
     * 获得当前数据源类型
     */
    abstract fun getDataSourceType(): DataSourceType

    /**
     * 根据输入地址获取服务器信息
     * @param [clientLoginInfoReq] 输入信息
     */
    override suspend fun addClientAndLogin(
        clientLoginInfoReq: ClientLoginInfoReq,
        connectionConfig: ConnectionConfig?
    ): Flow<ClientLoginInfoState> {
        val popTipHint = if (shouldShowLoginMessageTips()) {
            MessageUtils.sendPopTipHint(Res.string.logging_in)
        } else {
            null
        }
        resetLoginRetry()
        return flow {
            Log.i("=====", "输入的地址: ${clientLoginInfoReq.address}")
            emit(ClientLoginInfoState.Connected(clientLoginInfoReq.address))
            var deviceId = getDeviceId()
            connectionConfig?.let {
                deviceId = it.deviceId
            }

            //保存客户端数据
            initApiClient(
                clientLoginInfoReq.address,
                deviceId,
                username = clientLoginInfoReq.username,
                password = clientLoginInfoReq.password
            )

            //获得服务端信息
            val responseData =
                defaultParentApiClient.login(clientLoginInfoReq)
            Log.i("=====", "返回响应值: $responseData")

            //开始校验版本
            val version = responseData.version
            if (!version.isNullOrBlank()) {
                val versionAtLeast = isVersionAtLeast(version)
                if (!versionAtLeast) {
                    throw ServiceException(
                        getString(
                            Res.string.server_version_too_low,
                            version,
                            getDataSourceType().version
                        )
                    )
                }
            } else {
                throw ServiceException(getString(Res.string.server_version_cannot_be_obtained))
            }
            val accessToken =
                responseData.accessToken
            val userId =
                responseData.userId

            val encryptAES = PasswordUtils.encryptAES(clientLoginInfoReq.password)

            val tmpConfig = connectionConfig?.copy(
                serverId = responseData.serverId ?: "",
                name = responseData.serverName ?: getDataSourceType().title,
                serverName = responseData.serverName ?: "",
                address = clientLoginInfoReq.address,
                type = getDataSourceType(),
                userId = userId.toString(),
                username = clientLoginInfoReq.username,
                accessToken = accessToken,
                currentPassword = encryptAES.aesData,
                iv = encryptAES.aesIv,
                key = encryptAES.aesKey,
                serverVersion = version,
                updateTime = Clock.System.now().toEpochMilliseconds(),
                lastLoginTime = Clock.System.now().toEpochMilliseconds(),
                deviceId = deviceId,
                navidromeExtendToken = responseData.navidromeExtendToken,
                navidromeExtendSalt = responseData.navidromeExtendSalt,
                machineIdentifier = responseData.machineIdentifier
            ) ?: ConnectionConfig(
                serverId = responseData.serverId ?: "",
                serverName = responseData.serverName ?: "",
                name = responseData.serverName ?: getDataSourceType().title,
                address = clientLoginInfoReq.address,
                type = getDataSourceType(),
                userId = userId.toString(),
                username = clientLoginInfoReq.username,
                accessToken = accessToken,
                currentPassword = encryptAES.aesData,
                iv = encryptAES.aesIv,
                key = encryptAES.aesKey,
                serverVersion = version,
                deviceId = deviceId,
                navidromeExtendToken = responseData.navidromeExtendToken,
                navidromeExtendSalt = responseData.navidromeExtendSalt,
                machineIdentifier = responseData.machineIdentifier,
                ifEnabledDownload = responseData.ifEnabledDownload,
                ifEnabledDelete = responseData.ifEnabledDelete,
                ifForceLogin = false
            )
            this@IDataSourceParentServer.connectionConfig = tmpConfig
            popTipHint?.dismiss()
            emitAll(loginAfter(tmpConfig))
        }.flowOn(Dispatchers.IO).catch {
            it.printStackTrace()
            popTipHint?.dismiss()
            sendLoginCompleted(LoginStateType.FAILURE)
            when (it) {
                is SocketTimeoutException -> {
                    emit(ClientLoginInfoState.ServiceTimeOutState)
                }

                is ConnectionException -> {
                    emit(ClientLoginInfoState.ConnectError)
                }

                is UnauthorizedException -> {
                    emit(ClientLoginInfoState.UnauthorizedErrorState)
                }

                else -> {
                    emit(ClientLoginInfoState.ErrorState(it))
                }
            }
        }
    }

    /**
     * 登录后的数据
     */
    private fun loginAfter(
        connectionConfig: ConnectionConfig
    ): Flow<ClientLoginInfoState> {
        return flow {
            val connectionId = db.withTransaction {
                val connectionId = if (connectionConfig.id != 0L) {
                    db.connectionConfigDao.update(connectionConfig)
                    connectionConfig.id
                } else {
                    db.connectionConfigDao.save(connectionConfig)
                }

                connectionId
            }
            if (!ifTmpObject()) {
                connection(connectionConfig.copy(id = connectionId), connectionConfig.id != 0L)
                if (shouldShowLoginMessageTips()) {
                    MessageUtils.sendDismiss()
                }
            }

            emit(ClientLoginInfoState.UserLoginSuccess)
            // 登录成功后立刻返回结果，剩余初始化任务放到后台执行，避免阻塞当前登录流程
            if (!ifTmpObject()) {
                launchPostLoginTasks(connectionId)
            }
        }

    }

    /**
     * 获得设备id
     */
    @OptIn(ExperimentalUuidApi::class)
    open fun getDeviceId(): String {
        return Uuid.random().toString()
    }

    //todo 需要拆分方法
    /**
     * 创建连接客户端
     * @param [address] 地址
     */
    open suspend fun initApiClient(
        address: String,
        deviceId: String,
        username: String,
        password: String
    ) {
        createApiClient(deviceId, username, password)
        defaultParentApiClient.createHttpClient(address, ifTmpObject())
    }

    abstract suspend fun createApiClient(
        deviceId: String,
        username: String,
        password: String
    )

    /**
     * 自动登录
     */
    override suspend fun autoLogin(
        loginType: LoginType,
        connectionConfig: ConnectionConfig?
    ): Flow<ClientLoginInfoState> {


        //获得启用的连接信息
        val connectionConfig =
            connectionConfig ?: this.connectionConfig
            ?: db.connectionConfigDao.selectConnectionConfig() ?: return flowOf(
                ClientLoginInfoState.SelectServer
            )

        this.connectionConfig = connectionConfig
        settingsManager.saveConnectionId(connectionId = connectionConfig.id, connectionConfig.type)

        val address = connectionConfig.address

        val platformInfo = getPlatformInfo(contextWrapper)
        //判断是否能连接
        return flow {

            var password = connectionConfig.currentPassword
            if (connectionConfig.key.isNotBlank() && connectionConfig.iv.isNotBlank() && connectionConfig.currentPassword.isNotBlank()) {
                password = PasswordUtils.decryptAES(
                    EncryptAesData(
                        aesKey = connectionConfig.key,
                        aesIv = connectionConfig.iv,
                        aesData = connectionConfig.currentPassword
                    )
                )
            }
            val clientLoginInfoReq = ClientLoginInfoReq(
                username = connectionConfig.username,
                password = password,
                address = address,
                appName = platformInfo.appName,
                clientVersion = getDataSourceType().version,
                serverVersion = connectionConfig.serverVersion,
                serverName = connectionConfig.serverName,
                serverId = connectionConfig.serverId,
            )
            if (loginType == LoginType.API || connectionConfig.accessToken.isNullOrBlank() || connectionConfig.ifForceLogin) {
                emitAll(
                    addClientAndLogin(
                        clientLoginInfoReq = clientLoginInfoReq,
                        connectionConfig = connectionConfig
                    )
                )
            } else {
                resetLoginRetry()
                emit(ClientLoginInfoState.Connected(clientLoginInfoReq.address))
                //保存客户端数据
                initApiClient(
                    address,
                    connectionConfig.deviceId,
                    username = connectionConfig.username,
                    password = password
                )
                defaultParentApiClient.loginAfter(
                    connectionConfig.accessToken,
                    connectionConfig.userId,
                    connectionConfig.navidromeExtendToken,
                    connectionConfig.navidromeExtendSalt,
                    clientLoginInfoReq = clientLoginInfoReq
                )

                defaultParentApiClient.pingAfter(connectionConfig.machineIdentifier)
                emitAll(loginAfter(connectionConfig))
            }

        }.flowOn(Dispatchers.IO).catch {
            Log.e(Constants.LOG_ERROR_PREFIX, "自动登录异常 ${it.message}", it)
            if (loginType == LoginType.TOKEN)
                sendLoginCompleted(LoginStateType.FAILURE)
            when (it) {
                is SocketTimeoutException -> {
                    emit(ClientLoginInfoState.ServiceTimeOutState)
                }

                is UnauthorizedException -> {
                    emit(ClientLoginInfoState.UnauthorizedErrorState)
                }

                else -> {
                    emit(ClientLoginInfoState.ErrorState(it))
                }
            }
        }
    }


    /**
     * 获得连接id
     */
    override fun getConnectionId(): Long {
        return connectionConfig?.id ?: 0
    }

    /**
     * 获得连接地址
     */
    override fun getConnectionAddress(): String {
        return connectionConfig?.address ?: ""
    }

    /**
     * 更新连接设置
     */
    override suspend fun updateConnectionConfig(connectionConfig: ConnectionConfig) {
        db.connectionConfigDao.update(connectionConfig)
    }

    /**
     * 更新媒体库设置
     */
    override suspend fun updateLibraryId(libraryIds: List<String>?, connectionId: Long) {
        if (connectionId == getConnectionId()) {
            setUpLibraryId(libraryIds)
        } else {
            db.connectionConfigDao.updateLibraryId(
                libraryIds = libraryIds?.joinToString(LocalConstants.ARTIST_DELIMITER),
                connectionId = connectionId
            )
        }
    }

    /**
     * 同步读取本地媒体库数据
     * 登录流程中只做本地读取，保证依赖媒体库数据的页面可以立即使用本地缓存
     */
    protected suspend fun selectMediaLibrary(connectionId: Long) {
        try {
            db.libraryDao.selectListByConnectionId(connectionId)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "读取本地媒体库列表失败", e)
        }
    }

    /**
     * 从远程刷新媒体库列表
     * 该方法放到后台执行，避免登录时因远程请求阻塞主流程
     */
    protected suspend fun refreshMediaLibraryFromRemote(connectionId: Long) {
        try {
            val libraries = selectMediaLibraryList(connectionId)
            db.withTransaction {
                db.libraryDao.remove(connectionId)
                if (!libraries.isNullOrEmpty()) {
                    db.libraryDao.saveBatch(libraries)
                }
            }
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "从远程刷新媒体库列表失败", e)
        }
    }

    /**
     * 获得所有收藏数据
     */
    override suspend fun initFavoriteData(connectionId: Long) {

    }

    /**
     * 检验链接版本是否大于等于支持最小版本
     */
    fun isVersionAtLeast(currentVersion: String): Boolean {

        val version = currentVersion
            .lowercase()                 // 转小写，避免 "V1.2.3" 这种
            .replace(Regex("[^0-9.]"), ".") // 把非数字和点替换成点
            .replace(Regex("\\.+"), ".")    // 合并多余的点
            .trim('.')

        val curParts = version.split(".").map { it.toIntOrNull() ?: 0 }
        val tarParts = getDataSourceType().version.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(curParts.size, tarParts.size)

        for (i in 0 until maxLength) {
            val c = curParts.getOrElse(i) { 0 }
            val t = tarParts.getOrElse(i) { 0 }
            if (c > t) {
                return true
            } else if (c < t) {
                return false
            }
        }
        return true // 全部相等
    }


    /**
     * 获得专辑列表数据
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectAlbumFlowList(
        sort: Sort
    ): Flow<PagingData<XyAlbum>> {
        return defaultPager(
            pageSize = Constants.UI_LIST_PAGE,
            remoteMediator = AlbumRemoteMediator(
                dataSource = getDataSourceType(),
                db = db,
                datasourceServer = this,
                connectionId = getConnectionId(),
                sort = sort
            )
        ) {
            db.albumDao.selectHomeAlbumListPage()
        }.flow
    }

    /**
     * 获得音乐列表数据
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectMusicFlowList(
        sort: Sort
    ): Flow<PagingData<XyMusic>> {
        return defaultPager(
            remoteMediator = MusicRemoteMediator(
                db = db,
                datasourceServer = this,
                connectionId = getConnectionId(),
                sort = sort
            )
        ) {
            db.musicDao.selectHomeMusicListPage(getConnectionId())
        }.flow.map { pagingData ->
            pagingData.map { it.toPagingMusic() }
        }
    }

    /**
     * 获得艺术家
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectArtistFlowList(): Flow<PagingData<XyArtistExt>> {
        return bigPager(
            remoteMediator = ArtistRemoteMediator(
                db = db,
                datasourceServer = this,
                dataSource = getDataSourceType(),
                connectionId = getConnectionId()
            )
        ) {
            db.artistDao.selectListPagingSource()
        }.flow
    }

    /**
     * 获得最近播放音乐列表
     */
    override suspend fun getPlayRecordMusicList(pageSize: Int): List<XyMusic> {
        return db.musicDao.selectPlayHistoryMusicList(pageSize)
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
        val xyAlbum = selectAlbumInfoByRemotely(albumId, dataType)

        /*if (xyAlbum == null) {
            xyAlbum = selectAlbumInfoByRemotely(albumId, dataType)
        } else {
            val ifFavorite = db.albumDao.selectFavoriteById(albumId) == true
            xyAlbum = xyAlbum.copy(ifFavorite = ifFavorite)
        }*/
        return xyAlbum
    }

    /**
     * 从本地缓存获得专辑信息
     */
    override suspend fun selectLocalAlbumInfoById(albumId: String): XyAlbum? {
        var albumInfo = db.albumDao.selectById(albumId)
        if (albumInfo != null) {
            albumInfo = albumInfo.copy(
                ifFavorite = db.albumDao.selectFavoriteById(albumId) ?: false
            )
        }
        return albumInfo
    }

    /**
     * 从远程获得专辑信息
     */
    override suspend fun selectServerAlbumInfoById(
        albumId: String,
        dataType: MusicDataTypeEnum
    ): XyAlbum? {
        return selectAlbumInfoByRemotely(albumId, dataType)
    }

    /**
     * 获得专辑或歌单内音乐列表
     * @param [itemId] 专辑id
     * @param [dataType] 数据类型
     * @return [Flow<PagingData<XyMusic>>]
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectMusicListByParentId(
        itemId: String,
        dataType: MusicDataTypeEnum,
        sort: Sort
    ): Flow<PagingData<XyMusic>> {
        return defaultPager(
            remoteMediator = AlbumOrPlaylistMusicListRemoteMediator(
                itemId = itemId,
                datasourceServer = this,
                db = db,
                dataType = dataType,
                connectionId = getConnectionId(),
                sort = sort
            )
        ) {
            if (dataType == MusicDataTypeEnum.ALBUM)
                db.musicDao.selectAlbumMusicListPage(albumId = itemId)
            else
                db.musicDao.selectPlaylistMusicListPage(playlistId = itemId)
        }.flow
    }

    /**
     * 根据艺术家获得专辑列表
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectAlbumListByArtistId(artistId: String): Flow<PagingData<XyAlbum>> {
        return defaultPager(
            remoteMediator = getAlbumListRemoteMediator(artistId = artistId)
        ) {
            db.albumDao.selectArtistAlbumListPage(
                artistId
            )
        }.flow
    }

    /**
     * 根据艺术家获得音乐列表
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectMusicListByArtistId(
        artistId: String,
        artistName: String
    ): Flow<PagingData<XyMusic>> {
        return defaultPager(
            remoteMediator = ArtistMusicListRemoteMediator(
                artistId = artistId,
                artistName = artistName,
                datasourceServer = this,
                db = db,
                connectionId = getConnectionId()
            )
        ) {
            db.musicDao.selectArtistMusicListPage(
                artistId = artistId
            )
        }.flow
    }

    /**
     * 获得歌曲列表
     */
    override suspend fun getMusicList(
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return MusicPlayAssembler.attachFilePath(
            playMusicList = db.musicDao.selectMusicExtendList(pageSize, pageNum * pageSize),
            downloadDb = downloadDb,
            mediaLibraryId = getConnectionId().toString()
        )
    }

    /**
     * getMusicListByAlbumId
     */
    override suspend fun getMusicListByAlbumId(
        albumId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return MusicPlayAssembler.attachFilePath(
            playMusicList = db.musicDao.selectMusicExtendListByAlbumId(albumId, pageSize, pageNum * pageSize),
            downloadDb = downloadDb,
            mediaLibraryId = getConnectionId().toString()
        )
    }

    /**
     * 根据艺术家获得歌曲列表
     */
    override suspend fun getMusicListByArtistId(
        artistId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return MusicPlayAssembler.attachFilePath(
            playMusicList = db.musicDao.selectMusicExtendListByArtistId(
                artistId,
                pageSize,
                pageNum * pageSize
            ),
            downloadDb = downloadDb,
            mediaLibraryId = getConnectionId().toString()
        )
    }

    /**
     * 获得收藏歌曲列表
     */
    override suspend fun getMusicListByFavorite(
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return MusicPlayAssembler.attachFilePath(
            playMusicList = db.musicDao.selectMusicExtendListByFavorite(pageSize, pageNum * pageSize),
            downloadDb = downloadDb,
            mediaLibraryId = getConnectionId().toString()
        )
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
    abstract override suspend fun getRemoteServerMusicListByAlbumOrPlaylist(
        startIndex: Int,
        pageSize: Int,
        isFavorite: Boolean?,
        sortType: SortTypeEnum?,
        years: List<Int>?,
        parentId: String,
        dataType: MusicDataTypeEnum
    ): XyResponse<XyMusic>

    /**
     * 获得随机音乐
     */
    override suspend fun getRandomMusicExtendList(
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return transitionPlayMusic(
            getRandomMusicList(pageSize, pageNum)
        )
    }

    /**
     * 增加歌单
     * @param [name] 名称
     * @return [String?] 歌单id
     */
    override suspend fun addPlaylist(name: String): Boolean {
        return try {
            val playlistId = createPlaylist(name)
            if (!playlistId.isNullOrBlank()) {

                val album = XyAlbum(
                    itemId = playlistId,
                    name = name,
                    connectionId = getConnectionId(),
                    ifPlaylist = true,
                    musicCount = 0
                )
                db.albumDao.save(
                    album
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }

    /**
     * 保存自建歌单中的音乐
     * @param [playlistId] 歌单id
     * @param [musicIds] 音乐id集合
     */
    override suspend fun saveMusicPlaylist(
        playlistId: String,
        musicIds: List<String>
    ): Boolean {
        var playlistIndex = db.musicDao.selectPlaylistIndex() ?: -1
        val playlists = musicIds.map { musicId ->
            playlistIndex += 1
            PlaylistMusic(
                playlistId = playlistId,
                musicId = musicId,
                index = playlistIndex,
                connectionId = getConnectionId()
            )
        }
        db.musicDao.savePlaylistMusic(playlists)
        //更新歌单的封面信息
        db.albumDao.updatePic(playlistId)
        return true
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
        db.musicDao.removeByPlaylistMusicByMusicId(
            playlistId = playlistId,
            musicIds = musicIds
        )
        //获得歌单中的第一个音乐,并写入歌单封面
        val musicInfo = db.musicDao.selectPlaylistMusicOneById(playlistId)
        if (musicInfo != null && !musicInfo.pic.isNullOrBlank()) {
            musicInfo.pic?.let {
                db.albumDao.updatePicAndCount(playlistId, it)
            }
        }
        return true
    }


    /**
     * 批量写入艺术家
     * @param [items] 艺术家信息
     */
    @Transaction
    open suspend fun saveBatchArtist(items: List<XyArtist>) {
        if (items.isNotEmpty()) {
            db.artistDao.saveArtistBatch(
                items,
                getConnectionId()
            )
        }
    }

    /**
     * 存储专辑和音乐中的艺术家
     */
    suspend fun saveBatchAlbum(
        baseItemList: List<XyAlbum>,
        dataType: MusicDataTypeEnum,
        ifPlaylist: Boolean = false,
        artistId: String? = null,
        genreId: String? = null
    ): List<XyAlbum> {
        val albumList = baseItemList.map { it.copy(ifPlaylist = ifPlaylist) }
        if (albumList.isNotEmpty()) {
            try {
                db.albumDao.saveBatch(
                    data = albumList,
                    dataType = dataType,
                    connectionId = getConnectionId(),
                    artistId = artistId,
                    genreId = genreId
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return albumList
    }

    /**
     * 批量写入音乐
     * @param [items] 艺术家信息
     */
    @Transaction
    open suspend fun saveBatchMusic(
        items: List<XyMusic>,
        dataType: MusicDataTypeEnum,
        artistId: String? = null,
        playlistId: String? = null
    ) {
        if (items.isNotEmpty()) {
            db.musicDao.saveBatch(
                data = items,
                dataType = dataType,
                connectionId = getConnectionId(),
                artistId = artistId,
                playlistId = playlistId
            )
        }
    }

    /**
     * 批量写入流派
     * @param [items] 艺术家信息
     */
    @Transaction
    open suspend fun saveBatchGenre(
        items: List<XyGenre>
    ) {
        if (items.isNotEmpty()) {
            db.genreDao.saveBatch(
                data = items
            )
        }
    }

    /**
     * 根据id集合获得艺术家信息集合
     * @param [artistIds] 艺术家id
     * @return [List<ArtistItem>?] 艺术家信息
     */
    override suspend fun selectArtistInfoByIds(artistIds: List<String>): List<XyArtist> {
        return try {
            val tmpXyArtists = mutableListOf<XyArtist>()
            val artistItems = db.artistDao.selectByIds(artistIds)
            if (artistItems.isEmpty()) {
                val items = selectArtistsByIds(artistIds)
                tmpXyArtists.addAll(items)
            } else if (artistItems.size < artistIds.size) {
                tmpXyArtists.addAll(artistItems)
                val newArtistIds = artistItems.map { it.artistId }
                val noRepeated = newArtistIds.subtract(artistIds.toSet())
                if (noRepeated.isNotEmpty()) {
                    val items = selectArtistsByIds(noRepeated.map { it })
                    tmpXyArtists.addAll(items)
                }
            } else {
                tmpXyArtists.addAll(artistItems)
            }
            return tmpXyArtists
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "根据id集合获得艺术家信息集合失败", e)
            emptyList()
        }

    }

    /**
     * 获得专辑,艺术家,音频,歌单数量
     */
    suspend fun updateOrSaveDataInfoCount(
        music: Int?,
        album: Int?,
        artist: Int?,
        playlist: Int?,
        genres: Int?,
        favorite: Int?,
        connectionId: Long
    ) {
        val dataCount = db.dataCountDao.selectOne(connectionId)
        if (dataCount != null) {
            db.dataCountDao.update(
                XyDataCount(
                    connectionId = connectionId,
                    musicCount = music ?: dataCount.musicCount,
                    albumCount = album ?: dataCount.albumCount,
                    artistCount = artist ?: dataCount.artistCount,
                    playlistCount = playlist ?: dataCount.playlistCount,
                    genreCount = genres ?: dataCount.genreCount,
                    favoriteCount = favorite ?: dataCount.favoriteCount
                )
            )
        } else {
            db.dataCountDao.save(
                XyDataCount(
                    connectionId = connectionId,
                    musicCount = music,
                    albumCount = album,
                    artistCount = artist,
                    playlistCount = playlist,
                    genreCount = genres,
                    favoriteCount = favorite
                )
            )
        }
    }


    /**
     * 创建歌单
     */
    abstract suspend fun createPlaylist(name: String): String?

    /**
     * 获得艺术家列表
     */
    abstract suspend fun getArtistList(
        startIndex: Int,
        pageSize: Int,
        isFavorite: Boolean? = null,
        search: String? = null,
    ): XyResponse<XyArtist>

    /**
     * 从远程获得专辑信息
     */
    abstract suspend fun selectAlbumInfoByRemotely(
        albumId: String,
        dataType: MusicDataTypeEnum
    ): XyAlbum?

    /**
     * 根据id获得艺术家信息
     */
    override suspend fun selectArtistInfoById(artistId: String): XyArtist? {
        var artistInfo: XyArtist? = db.artistDao.selectById(artistId)
        if (artistInfo != null) {
            artistInfo =
                artistInfo.copy(ifFavorite = db.artistDao.selectFavoriteById(artistId) ?: false)
        }
        return artistInfo
    }

    /**
     * 获得流派列表
     */
    @OptIn(ExperimentalPagingApi::class)
    override suspend fun selectGenresPage(): Flow<PagingData<XyGenre>> {
        return defaultPager(
            remoteMediator = GenresRemoteMediator(
                datasourceServer = this,
                db = db,
                connectionId = getConnectionId()
            )
        ) {
            db.genreDao.selectByDataSourceType()
        }.flow
    }

    /**
     * 获得流派内音乐列表/或者专辑
     * @param [genreId] 流派id
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectAlbumListByGenreId(genreId: String): Flow<PagingData<XyAlbum>> {
        return defaultPager(
            remoteMediator = GenreAlbumListRemoteMediator(
                genreId = genreId,
                datasourceServer = this,
                db = db,
                connectionId = getConnectionId()
            )
        ) {
            db.albumDao.selectGenreAlbumListPage(genreId)
        }.flow
    }

    /**
     * 获得收藏歌曲列表
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectFavoriteMusicFlowList(): Flow<PagingData<XyMusic>> {
        return defaultPager(
            pageSize = Constants.UI_LIST_PAGE,
            initialLoadSize = Constants.UI_INIT_LIST_PAGE,
            prefetchDistance = 5,
            remoteMediator = FavoriteMusicRemoteMediator(
                datasourceServer = this,
                db = db,
                connectionId = getConnectionId()
            )
        ) {
            db.musicDao.selectFavoriteMusicListPage()
        }.flow
    }

    /**
     * 获得播放连接
     */
    override suspend fun getMusicPlayUrl(
        musicId: String,
        plexPlayKey: String?
    ): TranscodingAndMusicUrlData {
        val audioBitRate = settingsManager.audioBitRate.first()

        val static: Boolean =
            settingsManager.getStatic()

        val musicUrl = getChildMusicUrl(
            if (static) musicId else plexPlayKey ?: musicId,
            static,
            AudioCodecEnum.getAudioCodec(settingsManager.settings.first().transcodeFormat),
            audioBitRate,
            settingsManager.settings.first().playSessionId
        )

        return TranscodingAndMusicUrlData(
            audioBitRate = audioBitRate,
            static = static,
            musicUrl = musicUrl,
            ifHls = !static && getDataSourceType().ifHls
        )
    }

    /**
     * 获得播放链接
     */
    abstract fun getChildMusicUrl(
        musicId: String,
        static: Boolean,
        audioCodec: AudioCodecEnum?,
        audioBitRate: Int?,
        session: String?
    ): String

    /**
     * 删除数据
     * @param [musicId] 需要删除数据的id
     * @return true->删除成功,false->删除失败
     */
    abstract suspend fun removeById(musicId: String): Boolean

    /**
     * 批量删除数据
     * 按 ID 删除
     * @param [musicIds] 需要删除数据的
     * @return [Boolean?]
     */
    abstract suspend fun removeByIds(musicIds: List<String>): Boolean

    //各个服务获得媒体库方法
    abstract suspend fun selectMediaLibraryList(connectionId: Long): List<XyLibrary>?


    /**
     * 根据艺术家id获得艺术家列表
     */
    abstract suspend fun selectArtistsByIds(artistIds: List<String>): List<XyArtist>


    /**
     * 根据艺术家获得音乐列表
     */
    abstract suspend fun selectMusicListByArtistServer(
        artistId: String,
        artistName: String,
        pageSize: Int,
        startIndex: Int
    ): XyResponse<XyMusic>

    /**
     * 获取远程服务器专辑列表
     * @param [startIndex] 开始索引
     * @param [pageSize] 页面大小
     * @param [sortType] 排序类型
     * @param [isFavorite] 是否收藏
     * @param [years] 年列表
     * @return [AllResponse<XyAlbum>]
     */
    abstract suspend fun getRemoteServerAlbumList(
        startIndex: Int,
        pageSize: Int,
        sortType: SortTypeEnum? = null,
        isFavorite: Boolean? = null,
        years: List<Int>? = null,
        artistId: String? = null,
        genreId: String? = null,
    ): XyResponse<XyAlbum>


    /**
     * 获得专辑列表的RemoteMediator
     */
    @OptIn(ExperimentalPagingApi::class)
    open fun getAlbumListRemoteMediator(artistId: String): RemoteMediator<Int, XyAlbum>? {
        return ArtistAlbumListRemoteMediator(
            artistId = artistId,
            datasourceServer = this,
            db = db,
            connectionId = getConnectionId()
        )
    }

    /**
     * 获取远程服务器收藏音乐列表
     * @param [startIndex] 启动索引
     * @param [pageSize] 页面大小
     * @param [isFavorite] 是最喜欢
     * @return [AllResponse<XyMusic>]
     */
    abstract suspend fun getRemoteServerFavoriteMusicList(
        startIndex: Int,
        pageSize: Int,
        isFavorite: Boolean
    ): XyResponse<XyMusic>

    /**
     * 获取远程服务器流派列表
     * @param [startIndex] 启动索引
     * @param [pageSize] 页面大小
     * @return [AllResponse<XyGenre>]
     */
    abstract suspend fun getRemoteServerGenreList(
        startIndex: Int,
        pageSize: Int
    ): XyResponse<XyGenre>


    /**
     * 获取远程服务器音乐列表
     * @param [startIndex] 启动索引
     * @param [pageSize] 页面大小
     * @param [isFavorite] 是最喜欢
     * @param [sortType] 排序类型
     * @param [years] 年
     * @return [AllResponse<XyMusic>]
     */
    abstract suspend fun getRemoteServerMusicList(
        startIndex: Int,
        pageSize: Int,
        isFavorite: Boolean?,
        sortType: SortTypeEnum?,
        years: List<Int>?
    ): XyResponse<XyMusic>

    suspend fun transitionPlayMusic(musicList: List<XyMusic>?): List<XyPlayMusic>? {
        return MusicPlayAssembler.toPlayMusicList(
            musicList = musicList,
            downloadDb = downloadDb,
            mediaLibraryId = getConnectionId().toString()
        )
    }

    fun transitionMusic(musicList: List<XyMusic>?): List<XyMusic>? {
        return musicList
    }

    /**
     * 创建下载链接
     */
    abstract fun createDownloadUrl(musicId: String): String


    /**
     * 获得是否可以下载
     */
    fun getCanDownload(): Boolean {
        return connectionConfig?.ifEnabledDownload ?: false
    }

    /**
     * 获取是否可以删除
     */
    fun getCanDelete(): Boolean {
        return connectionConfig?.ifEnabledDelete ?: false
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
        val favorite = if (ifFavorite) {
            unmarkFavoriteItem(itemId = itemId, type)
        } else {
            markFavoriteItem(itemId = itemId, type)
        }

        if (favorite != ifFavorite) {
            when (type) {
                MusicTypeEnum.MUSIC -> {
                    db.musicDao.updateFavoriteByItemId(
                        favorite,
                        itemId,
                        getConnectionId()
                    )
                }

                MusicTypeEnum.ALBUM -> {
                    val favoriteCount = db.albumDao.selectFavoriteCount(itemId)
                    if (favoriteCount <= 0) {
                        db.albumDao.saveFavoriteAlbum(
                            FavoriteAlbum(
                                albumId = itemId,
                                connectionId = getConnectionId(),
                                ifFavorite = favorite
                            )
                        )
                    } else {
                        db.albumDao.updateFavoriteByItemId(favorite, itemId)
                    }
                }

                MusicTypeEnum.ARTIST -> {
                    val favoriteCount = db.artistDao.selectFavoriteCount(itemId)
                    if (favoriteCount <= 0) {
                        db.artistDao.saveFavoriteArtist(
                            FavoriteArtist(
                                artistId = itemId,
                                connectionId = getConnectionId(),
                                ifFavorite = favorite
                            )
                        )
                    } else {
                        db.artistDao.updateFavoriteByItemId(favorite, itemId)
                    }
                }
            }
        }

        return favorite
    }

    /**
     * 获得用户id
     */
    protected fun getUserId(): String {
        return connectionConfig?.userId ?: ""
    }

    /**
     * 写入连接信息
     */
    suspend fun connection(connectionConfig: ConnectionConfig, ifAutoLogin: Boolean) {
        if (!ifAutoLogin)
            this.connectionConfig = connectionConfig
        // 登录阶段同步读取本地媒体库缓存，保证依赖媒体库数据的页面能立即加载
        selectMediaLibrary(connectionId = connectionConfig.id)
        updateLibraryIds(this.connectionConfig?.libraryIds, true)
        settingsManager.saveConnectionId(connectionId = connectionConfig.id, connectionConfig.type)
        sendLoginCompleted(LoginStateType.SUCCESS)
    }

    /**
     * 登录完成后后台执行的初始化任务
     * 这里统一处理媒体库、收藏和统计信息的异步预加载
     */
    private fun launchPostLoginTasks(connectionId: Long) {
        scope.launch {
            // 登录成功后后台刷新远程媒体库，避免阻塞当前登录流程
            runCatching {
                refreshMediaLibraryFromRemote(connectionId = connectionId)
            }.onFailure {
                Log.e(Constants.LOG_ERROR_PREFIX, "refresh media library after login failed", it)
            }

            // 根据当前连接的同步状态决定是否继续执行后续数据同步
            val shouldSync = runCatching {
                shouldSync(connectionId)
            }.getOrElse {
                Log.e(Constants.LOG_ERROR_PREFIX, "check post login sync state failed", it)
                false
            }

            // 只有需要同步时才继续拉取收藏、统计等附加数据
            if (shouldSync) {
                initOtherData(connectionId)
            }
        }
    }

    /**
     * 初始化登录后的其他数据
     */
    suspend fun initOtherData(connectionId: Long) {
        try {
            downloaderManager.initData(connectionId.toString())
            Log.i(Constants.LOG_ERROR_PREFIX, "start syncing media library/favorites/counts")
            db.withTransaction {
                val remoteId = RemoteIdConstants.MEDIA_LIBRARY_AND_FAVORITE + connectionId

                initFavoriteData(connectionId = connectionId)
                try {
                    getDataInfoCount(connectionId)
                } catch (e: Exception) {
                    Log.e(
                        Constants.LOG_ERROR_PREFIX,
                        "failed to fetch media/album/artist/favorite/genre counts",
                        e
                    )
                }

                try {
                    getApiClient().ping()
                } catch (e: Exception) {
                    tryMarkLoginRetry()
                    throw e
                }

                db.remoteCurrentDao.deleteById(remoteId)
                db.remoteCurrentDao.insertOrReplace(
                    RemoteCurrent(
                        id = remoteId,
                        nextKey = 0,
                        total = 0,
                        connectionId = connectionId,
                        refresh = false
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "sync media/favorite/count failed", e)
        }
    }

    /**
     * 判断当前连接是否需要执行登录后的附加同步
     */
    suspend fun shouldSync(connectionId: Long = getConnectionId()): Boolean {
        val state = db.remoteCurrentDao.remoteKeyById(
            RemoteIdConstants.MEDIA_LIBRARY_AND_FAVORITE + connectionId
        )
        return (state == null) ||
                ((Clock.System.now()
                    .toEpochMilliseconds() - state.createTime) > 10.minutes.inWholeMilliseconds)
    }

    fun unConnection() {
        connectionConfig = null
    }


    /**
     * 设置媒体库id
     */
    protected open suspend fun setUpLibraryId(
        libraryIds: List<String>?,
        ifLoginSet: Boolean = false
    ) {
        updateLibraryIds(libraryIds, ifLoginSet)
        updateLocalLibraryId(libraryIds)
    }

    /**
     * 更新本地媒体库数据
     */
    protected suspend fun updateLocalLibraryId(libraryIds: List<String>?) {
        this.connectionConfig = this.connectionConfig?.copy(libraryIds = libraryIds)
        db.connectionConfigDao.updateLibraryId(
            libraryIds = libraryIds?.joinToString(LocalConstants.ARTIST_DELIMITER),
            connectionId = getConnectionId()
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun updateLibraryIds(libraryIds: List<String>?, ifLoginSet: Boolean = false) {
        this.libraryIds = libraryIds
        if (!ifLoginSet) {
            updateDataSourceRemoteKey()
            _mediaLibraryIdFlow.update {
                libraryIds?.sorted()?.joinToString(LocalConstants.ARTIST_DELIMITER)
            }
        }

    }

    /**
     * 发送登录动作完成通知(不管失败或成功)
     */
    private fun sendLoginCompleted(loginState: LoginStateType) {
        _loginSuccessEvent.tryEmit(loginState)
    }

    suspend fun updateDataSourceRemoteKey(remoteCurrentId: String? = null) {
        if (!remoteCurrentId.isNullOrBlank()) {
            db.remoteCurrentDao.updateByIdAndConnectionId(
                getConnectionId(),
                remoteCurrentId + getConnectionId()
            )
        } else {
            db.remoteCurrentDao.updateByConnectionId(getConnectionId())
        }
    }


    override fun close() {
        defaultParentApiClient.release()
        TokenServer.clearAllData()
        unConnection()
        sendLoginCompleted(LoginStateType.UNKNOWN)
        //这里这样置空是为了防止触发DataSourceManager.mediaLibraryIdFlow的流变化
        this.libraryIds = null
    }

}
