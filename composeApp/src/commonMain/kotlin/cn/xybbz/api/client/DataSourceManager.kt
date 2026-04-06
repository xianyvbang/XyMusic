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
import cn.xybbz.api.enums.AudioCodecEnum
import cn.xybbz.api.exception.ServiceException
import cn.xybbz.api.state.ClientLoginInfoState
import cn.xybbz.api.state.Source
import cn.xybbz.common.constants.Constants
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
import cn.xybbz.common.enums.DownloadTypes
import cn.xybbz.entity.data.LoginStateData
import cn.xybbz.entity.data.LrcEntryData
import cn.xybbz.entity.data.ResourceData
import cn.xybbz.entity.data.SearchData
import cn.xybbz.entity.data.Sort
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.artist.XyArtistExt
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
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


    var dataSourceType by mutableStateOf<DataSourceType?>(null)
        private set


    init {
        createScope()
    }

    /**
     * 用户数据源数据信息服务类
     */
    private lateinit var dataSourceServer: IDataSourceParentServer

    val dataSourceServerFlow = MutableStateFlow<IDataSourceParentServer?>(null)

    @kotlin.OptIn(ExperimentalCoroutinesApi::class)
    val loginStateFlow: Flow<LoginStateType> =
        dataSourceServerFlow
            .filterNotNull()
            .flatMapLatest { server ->
                server.loginSuccessEvent
            }.filter { it != LoginStateType.UNKNOWN }


    @kotlin.OptIn(ExperimentalCoroutinesApi::class)
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

    //登陆状态
    var loginStatus by mutableStateOf<ClientLoginInfoState?>(null)
        private set

    //是够登陆异常
    var ifLoginError by mutableStateOf(false)
        private set

    //报错信息
    var errorHint by mutableStateOf(Res.string.empty_info)
        private set

    var errorMessage by mutableStateOf("")
        private set

    /**
     * 初始化对象信息
     */
    suspend fun initDataSource(dataSourceType: DataSourceType?) {

        Log.i("=====", "开始自动登录")
        if (dataSourceType != null) {
            Log.i("=====", "开始自动登录中")
            switchDataSource(dataSourceType)
            serverLogin(LoginType.TOKEN, null)
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
                dataSourceServer.getApiClient()
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
        ifLoginError = false
        autoLogin(loginType, connectionConfig).collect { loginState ->
            loginStatus = loginState
            //                ifLoginError = false
            val loginSateInfo = getLoginSateInfo(loginState)
            errorHint = loginSateInfo.errorHint ?: Res.string.empty_info
            errorMessage = loginSateInfo.errorMessage ?: ""
            ifLoginError = loginSateInfo.isError
            loading = loginSateInfo.loading
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
     */
    fun switchDataSource(
        dataSourceType: DataSourceType
    ) {
        updateDataSourceType(dataSourceType)
        Log.i("=====", "数据源开始切换")
        getDataSourceServerByType(dataSourceType, false).let {
            dataSourceServer = it
            dataSourceServerFlow.value = it

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
        switchDataSource(connectionConfig.type)
        serverLogin(connectionConfig = connectionConfig)
    }


    /**
     * 绑定地址
     */
    override suspend fun addClientAndLogin(
        clientLoginInfoReq: ClientLoginInfoReq,
        connectionConfig: ConnectionConfig?
    ): Flow<ClientLoginInfoState> {
        return dataSourceServer.addClientAndLogin(clientLoginInfoReq)
    }

    override suspend fun autoLogin(
        loginType: LoginType,
        connectionConfig: ConnectionConfig?
    ): Flow<ClientLoginInfoState> {
        loading = true
        Log.i("=====", "开始登录.............")
        return dataSourceServer.autoLogin(loginType, connectionConfig)
    }

    /**
     * 获得资源地址
     */
    override suspend fun getResources(
        clientLoginInfoReq: ClientLoginInfoReq
    ): List<ResourceData> {
        return try {
            dataSourceServer.getResources(clientLoginInfoReq)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得服务器资源失败", e)
            throw ServiceException(message = getString(Res.string.get_server_resources_failed))
        }
    }

    /**
     * 获得OkHttpClient
     */
    override fun getHttpClient(): HttpClient {
        return dataSourceServer.getHttpClient()
    }

    /**
     * 获得连接id
     */
    override fun getConnectionId(): Long {
        return dataSourceServer.getConnectionId()
    }

    /**
     * 获得连接地址
     */
    override fun getConnectionAddress(): String {
        return dataSourceServer.getConnectionAddress()
    }

    /**
     * 更新连接设置
     */
    override suspend fun updateConnectionConfig(connectionConfig: ConnectionConfig) {
        dataSourceServer.updateConnectionConfig(connectionConfig)
    }

    /**
     * 更新媒体库id
     */
    override suspend fun updateLibraryId(libraryIds: List<String>?, connectionId: Long) {
        return dataSourceServer.updateLibraryId(libraryIds, connectionId)
    }

    /**
     * 获得专辑,艺术家,音频,歌单数量
     */
    override suspend fun getDataInfoCount(connectionId: Long) {
        dataSourceServer.getDataInfoCount(connectionId)
    }


    /**
     * 初始化收藏数据
     */
    override suspend fun initFavoriteData(connectionId: Long) {
        return dataSourceServer.initFavoriteData(connectionId)
    }

    /**
     * 获得专辑数据
     */
    override fun selectAlbumFlowList(
        sort: Sort
    ): Flow<PagingData<XyAlbum>> {
        return dataSourceServer.selectAlbumFlowList(sort)
    }

    /**
     * 获得音乐数据
     */
    override fun selectMusicFlowList(
        sort: Sort
    ): Flow<PagingData<XyMusic>> {
        return dataSourceServer.selectMusicFlowList(sort)
    }

    /**
     * 获得艺术家
     */
    override fun selectArtistFlowList(): Flow<PagingData<XyArtistExt>> {
        return dataSourceServer.selectArtistFlowList()
    }

    /**
     * 搜索音乐,艺术家,专辑
     */
    override suspend fun searchAll(search: String): SearchData {
        return dataSourceServer.searchAll(search)
    }

    /**
     * 获得最近播放音乐
     */
    override suspend fun playRecordMusicOrAlbumList(pageSize: Int) {
        try {
            dataSourceServer.playRecordMusicOrAlbumList(pageSize)
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
        return dataSourceServer.getPlayRecordMusicList(pageSize)
    }

    /**
     * 获得最多播放
     */
    override suspend fun getMostPlayerMusicList() {
        try {
            dataSourceServer.getMostPlayerMusicList()
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得最多播放专辑失败", e)
        }
    }

    /**
     * 获得最新专辑
     */
    override suspend fun getNewestAlbumList() {
        try {
            dataSourceServer.getNewestAlbumList()
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
            dataSourceServer.selectAlbumInfoById(albumId, dataType)
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
            dataSourceServer.selectLocalAlbumInfoById(albumId)
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
            dataSourceServer.selectServerAlbumInfoById(albumId, dataType)
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
            dataSourceServer.selectMusicInfoById(itemId)
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
        return dataSourceServer.selectMusicListByParentId(
            itemId = itemId,
            dataType = dataType,
            sort = sort
        )
    }

    /**
     * 根据艺术家获得专辑列表
     */
    override fun selectAlbumListByArtistId(artistId: String): Flow<PagingData<XyAlbum>> {
        return dataSourceServer.selectAlbumListByArtistId(artistId)
    }

    /**
     * 根据艺术家获得音乐列表
     */
    override fun selectMusicListByArtistId(
        artistId: String,
        artistName: String
    ): Flow<PagingData<XyMusic>> {
        return dataSourceServer.selectMusicListByArtistId(artistId, artistName)
    }

    /**
     * 获得歌曲列表
     */
    override suspend fun getMusicList(
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return try {
            dataSourceServer.getMusicList(pageSize, pageNum)
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
            dataSourceServer.getMusicListByAlbumId(albumId, pageSize, pageNum)
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
            dataSourceServer.getMusicListByArtistId(artistId, pageSize, pageNum)
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
            dataSourceServer.getMusicListByArtistIds(artistIds, pageSize)
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
            dataSourceServer.getMusicListByFavorite(pageSize, pageNum)
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
        return dataSourceServer.getRemoteServerMusicListByAlbumOrPlaylist(
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
            dataSourceServer.getRandomMusicList(pageSize, pageNum)
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
            dataSourceServer.getRandomMusicExtendList(pageSize, pageNum)
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
            dataSourceServer.getMusicLyricList(itemId)?.takeIf { it.isNotEmpty() }
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
        return try {
            dataSourceServer.getPlaylists()
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
            dataSourceServer.addPlaylist(name)
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
                dataSourceServer.importPlaylist(playlistData, playlistId)
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
                dataSourceServer.editPlaylistName(id, name)
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
                dataSourceServer.removePlaylist(id)
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
                dataSourceServer.saveMusicPlaylist(playlistId, musicIds)
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
                dataSourceServer.removeMusicPlaylist(playlistId, musicIds)
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
        return dataSourceServer.selectArtistInfoByIds(artistIds)
    }

    /**
     * 获得艺术家信息
     */
    override suspend fun selectArtistInfoById(artistId: String): XyArtist? {
        return try {
            dataSourceServer.selectArtistInfoById(artistId)
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
            dataSourceServer.selectServerArtistInfo(artistId)
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
    ): List<XyMusicExtend> {
        return try {
            dataSourceServer.getArtistPopularMusicList(artistId, artistName)
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
            dataSourceServer.getSimilarArtistsRemotely(artistId, startIndex, pageSize)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得歌手热门歌曲列表失败", e)
            null
        } ?: emptyList()
    }

    /**
     * 获得流派列表
     */
    override suspend fun selectGenresPage(): Flow<PagingData<XyGenre>> {
        return dataSourceServer.selectGenresPage()
    }

    /**
     * 获得流派详情
     */
    override suspend fun getGenreById(genreId: String): XyGenre? {
        return try {
            return dataSourceServer.getGenreById(genreId)
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
        return dataSourceServer.selectAlbumListByGenreId(genreId)
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
            dataSourceServer.selectMusicListByGenreIds(genreIds, pageSize)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得流派内音乐列表失败", e)
            null
        }
    }

    /**
     * 获得收藏歌曲列表
     */
    override fun selectFavoriteMusicFlowList(): Flow<PagingData<XyMusic>> {
        return dataSourceServer.selectFavoriteMusicFlowList()
    }

    /**
     * 将项目标记为收藏
     * @param [itemId] 专辑/音乐id
     */
    override suspend fun markFavoriteItem(itemId: String, dataType: MusicTypeEnum): Boolean {
        return try {
            dataSourceServer.markFavoriteItem(itemId, dataType)
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
            dataSourceServer.unmarkFavoriteItem(itemId, dataType)
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
    override fun getMusicPlayUrl(
        musicId: String,
        static: Boolean,
        audioCodec: AudioCodecEnum?,
        audioBitRate: Int?,
        session: String?
    ): String {
        return try {
            dataSourceServer.getMusicPlayUrl(
                musicId,
                static,
                audioCodec,
                audioBitRate,
                session
            )
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取播放连接失败", e)
            ""
        }
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
            dataSourceServer.reportPlaying(musicId, playSessionId, isPaused, positionTicks)
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
            dataSourceServer.reportProgress(musicId, playSessionId, positionTicks)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "播放上报失败", e)
        }
    }

    /**
     * 取消上报播放进度
     */
    override suspend fun cancelReportProgress(musicId: String) {
        try {
            dataSourceServer.cancelReportProgress(musicId)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "取消播放上报失败", e)
        }
    }

    /**
     * 获得相似歌曲列表
     */
    override suspend fun getSimilarMusicList(musicId: String): List<XyMusicExtend> {
        return try {
            dataSourceServer.getSimilarMusicList(musicId) ?: emptyList()
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
            dataSourceServer.getTranscodingType()
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取转码类型失败", e)
            emptyList()
        }
    }

    /**
     * 获得是否可以下载
     */
    fun getCanDownload(): Boolean {
        return dataSourceServer.getCanDownload()
    }

    fun getCanDelete(): Boolean {
        return dataSourceServer.getCanDelete()
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
        return dataSourceServer.setFavoriteData(
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
            val bool = dataSourceServer.removeById(musicId)
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
                dataSourceServer.removeByIds(musicIds)
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
        dataSourceServer.updateDataSourceRemoteKey(remoteCurrentId)
    }


    fun dataSourceScope() = scope

    fun release() {
        dataSourceServer.close()
        dataSourceServerFlow.value = null
        dataSourceType = null
    }

    override fun close() {
        super.close()
        versionApiClient.release()
        release()
    }
}
