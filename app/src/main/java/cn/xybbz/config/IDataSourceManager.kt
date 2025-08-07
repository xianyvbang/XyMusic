package cn.xybbz.config

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.toColorInt
import androidx.media3.common.util.UnstableApi
import androidx.paging.PagingData
import androidx.room.Transaction
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.IDataSourceServer
import cn.xybbz.api.client.emby.EmbyDatasourceServer
import cn.xybbz.api.client.jellyfin.JellyfinDatasourceServer
import cn.xybbz.api.client.jellyfin.data.ClientLoginInfoReq
import cn.xybbz.api.client.navidrome.NavidromeDatasourceServer
import cn.xybbz.api.client.subsonic.SubsonicDatasourceServer
import cn.xybbz.api.exception.ServiceException
import cn.xybbz.api.state.ClientLoginInfoState
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.OperationTipUtils
import cn.xybbz.config.alarm.AlarmConfig
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.entity.data.SearchData
import cn.xybbz.entity.data.file.backup.ExportPlaylistData
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.ui.components.LrcEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.net.SocketTimeoutException

/**
 * 本地数据源管理类
 * @author xybbz
 * @date 2024/06/12
 */
@OptIn(UnstableApi::class)
class IDataSourceManager(
    private val application: Context,
    private val db: DatabaseClient,
    private val jellyfinDatasourceServer: JellyfinDatasourceServer,
    private val subsonicDatasourceServer: SubsonicDatasourceServer,
    private val navidromeDatasourceServer: NavidromeDatasourceServer,
    private val embyDatasourceServer: EmbyDatasourceServer,
    private val connectionConfigServer: ConnectionConfigServer,
    private val alarmConfig: AlarmConfig,
    private val favoriteRepository: FavoriteRepository
) : IDataSourceServer {

    var dataSourceType by mutableStateOf<DataSourceType?>(null)
        private set

    private val datasourceCoroutineScope = CoroutineScopeUtils.getIo(this.javaClass.name)

    /**
     * 用户数据源数据信息服务类
     */
    lateinit var dataSourceServer: IDataSourceParentServer
        private set

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
    var errorMessage by mutableStateOf("")

    /**
     * 初始化对象信息
     */
    fun initDataSource() {
        if (connectionConfigServer.connectionConfig != null) {
            connectionConfigServer.connectionConfig?.let {
                setDataSourceTypeFun(it.type)
                serverLogin()

            }

        }
    }

    /**
     * 登陆服务端
     */
    fun serverLogin(){
        datasourceCoroutineScope.launch {
            autoLogin()?.onEach { loginState ->
                loginStatus = loginState
                ifLoginError = false
                when (loginState) {
                    is ClientLoginInfoState.Connected -> {

                        Log.i("=====","连接中")
                    }

                    is ClientLoginInfoState.ConnectionSuccess -> {
                        //路由跳转,根据id跳转
                        Log.i("=====","服务端连接成功")
                    }

                    is ClientLoginInfoState.ConnectError -> {
                        MessageUtils.sendPopTipDismiss()
                        errorMessage = "服务端连接错误"
                        Log.i("=====","服务端连接错误")
                        loading = false
                        ifLoginError = true
                    }

                    ClientLoginInfoState.ServiceTimeOutState -> {
                        MessageUtils.sendPopTipDismiss()
                        errorMessage = "服务端连接超时"
                        Log.i("=====","服务端连接超时")
                        loading = false
                        ifLoginError = true
                    }

                    is ClientLoginInfoState.ErrorState -> {
                        MessageUtils.sendPopTipDismiss()
                        errorMessage = loginState.error.message.toString()
                        Log.i("=====",loginState.error.message.toString())
                        loading = false
                        ifLoginError = true
                    }

                    ClientLoginInfoState.SelectServer -> {
                        MessageUtils.sendPopTipDismiss()
                        errorMessage = "未选择连接"
                        Log.i("=====","未选择连接")
                        loading = false
                        ifLoginError = true
                    }

                    ClientLoginInfoState.UnauthorizedErrorState -> {
                        MessageUtils.sendPopTipDismiss()
                        errorMessage = "权限报错"
                        Log.i("=====","权限报错")
                        loading = false
                        ifLoginError = true
                    }

                    ClientLoginInfoState.UserLoginSuccess -> {
                        Log.i("=====","登陆成功")
                        loading = false
                        ifLoginError = false
                    }
                }
            }?.launchIn(datasourceCoroutineScope)
        }
    }

    /**
     * 变更数据源
     */
    fun changeDataSource() {
        connectionConfigServer.updateLoginStates(false)
        initDataSource()
    }

    /**
     * 切换数据源
     * @param [dataSourceType] 数据源类型
     */
    @OptIn(UnstableApi::class)
    fun switchDataSource(
        dataSourceType: DataSourceType? = null
    ) {

        Log.i("=====", "数据源开始切换")
        when (dataSourceType) {
            DataSourceType.JELLYFIN -> {
                dataSourceServer = jellyfinDatasourceServer
            }

            DataSourceType.SUBSONIC -> {
                dataSourceServer = subsonicDatasourceServer
            }

            null -> {

            }

            DataSourceType.NAVIDROME -> {
                dataSourceServer = navidromeDatasourceServer
            }

            DataSourceType.EMBY -> {
                dataSourceServer = embyDatasourceServer
            }
        }
        Log.i("=====", "数据源切换完成")
    }

    fun setDataSourceTypeFun(type: DataSourceType?) {
        this.dataSourceType = type
        switchDataSource(dataSourceType = type)
    }

    /**
     * 切换连接服务
     */
    suspend fun changeDataSource(connectionConfig: ConnectionConfig) {
        //更新原本的数据源,关闭ifEnable

        val config = db.connectionConfigDao.selectConnectionConfig()
        if (config != null) {
            db.connectionConfigDao.update(config.copy(ifEnable = false))
        }
        db.connectionConfigDao.update(connectionConfig.copy(ifEnable = true))
        release()
        connectionConfigServer.setConnectionConfigData(connectionConfig)
        changeDataSource()
    }


    /**
     * 绑定地址
     */
    override suspend fun addClientAndLogin(clientLoginInfoReq: ClientLoginInfoReq): Flow<ClientLoginInfoState>? {
        return dataSourceServer.addClientAndLogin(clientLoginInfoReq)
    }

    override suspend fun autoLogin(): Flow<ClientLoginInfoState>? {
        MessageUtils.sendPopTip(
            "正在登录中...",
            backgroundColor = "#E6A23C".toColorInt(),
            delay = 5000
        )
        loading = true
        Log.i("=====", "开始登录.............")
        return dataSourceServer.autoLogin()
    }

    /**
     * 获得专辑数据
     */
    override fun selectAlbumFlowList(
        sortType: SortTypeEnum?,
        ifFavorite: Boolean?,
        years: String?
    ): Flow<PagingData<XyAlbum>> {
        return dataSourceServer.selectAlbumFlowList(sortType, ifFavorite, years)
    }

    /**
     * 获得音乐数据
     */
    override fun selectMusicFlowList(
        sortType: SortTypeEnum?,
        ifFavorite: Boolean?,
        years: String?
    ): Flow<PagingData<XyMusic>> {
        return dataSourceServer.selectMusicFlowList(sortType, ifFavorite, years)
    }

    /**
     * 获得艺术家
     */
    override fun selectArtistFlowList(
        ifFavorite: Boolean?,
        selectChat: String?
    ): Flow<PagingData<XyArtist>> {
        return dataSourceServer.selectArtistFlowList(ifFavorite, selectChat)
    }

    /**
     * 搜索音乐,艺术家,专辑
     */
    override suspend fun searchAll(search: String): SearchData {
        return dataSourceServer.searchAll(search)
    }

    /**
     * 获得专辑内音乐列表
     * @param [itemId] 专辑id
     */
    override fun selectMusicListByParentId(
        sortType: SortTypeEnum?,
        ifFavorite: Boolean?,
        years: String?,
        itemId: String,
        dataType: MusicDataTypeEnum
    ): Flow<PagingData<XyMusic>> {
        return dataSourceServer.selectMusicListByParentId(
            sortType = sortType,
            ifFavorite = ifFavorite,
            years = years,
            itemId = itemId,
            dataType = dataType
        )
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
     * 获得专辑,艺术家,音频,歌单数量
     */
    override suspend fun getDataInfoCount(connectionId: Long) {
        dataSourceServer.getDataInfoCount(connectionId)
    }

    /**
     * 删除数据
     * @param [musicId] 需要删除数据的id
     * @return true->删除成功,false->删除失败
     */
    override suspend fun removeById(musicId: String): Boolean {
        return try {
            dataSourceServer.removeById(musicId)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 批量删除数据
     * 按 ID 删除
     * @param [musicIds] 需要删除数据的
     * @return [Boolean?]
     */
    override suspend fun removeByIds(musicIds: List<String>): Boolean {
        return try {
            dataSourceServer.removeByIds(musicIds)
        } catch (e: Exception) {
            e.printStackTrace()
            false
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
     * 按 ID 选择音乐信息
     * @param [itemId] 音乐唯一标识
     * @return [MusicArtistExtend?]
     */
    override suspend fun selectMusicInfoById(itemId: String): XyMusic? {
        return try {
            dataSourceServer.selectMusicInfoById(itemId)
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取歌曲信息超时", e)
            return null
        } catch (e: ServiceException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取歌曲信息失败", e)
            return null
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取歌曲信息未知异常失败", e)
            return null
        }

    }

    /**
     * 根据音乐获得歌词信息
     * @param [music] 音乐id
     * @return 返回歌词列表
     */
    override suspend fun getMusicLyricList(music: XyMusic): List<LrcEntry>? {
        return try {
            dataSourceServer.getMusicLyricList(music)
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得歌词超时", e)
            return null
        } catch (e: ServiceException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得歌词失败", e)
            return null
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得歌词未知异常失败", e)
            return null
        }
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
    override fun selectMusicListByArtistId(artistId: String): Flow<PagingData<XyMusic>> {
        return dataSourceServer.selectMusicListByArtistId(artistId)
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
     * 释放
     */
    override suspend fun release() {
        dataSourceServer.release()
        //取消定时任务
        alarmConfig.cancelAllAlarm()
        connectionConfigServer.setConnectionConfigData(null)
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
            loadingMessage = "正在创建...",
            successMessage = "创建成功",
            errorMessage = "创建失败"
        ) {
            dataSourceServer.addPlaylist(name)
        }
    }

    /**
     * 新增或修改歌单
     */
    override suspend fun importPlaylist(playlistData: ExportPlaylistData): Boolean {
        return OperationTipUtils.operationTipNotToBlock(
            loadingMessage = "正在导入...",
            successMessage = "导入成功",
            errorMessage = "导入失败"
        ) {
            try {
                dataSourceServer.importPlaylist(playlistData)
            } catch (e: Exception) {
                Log.e(Constants.LOG_ERROR_PREFIX, "导入失败", e)
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
            loadingMessage = "正在修改歌单名称...",
            successMessage = "歌单名称修改成功",
            errorMessage = "歌单名称修改失败"
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
            loadingMessage = "正在删除歌单...",
            successMessage = "删除歌单成功",
            errorMessage = "删除歌单失败"
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
     * @param [pic] 照片
     */
    override suspend fun saveMusicPlaylist(
        playlistId: String,
        musicIds: List<String>,
        pic: String?
    ): Boolean {
        return OperationTipUtils.operationTipNotToBlock(
            loadingMessage = "正在添加音乐到歌单...",
            successMessage = "添加音乐到歌单成功",
            errorMessage = "添加音乐到歌单失败"
        ) {
            try {
                dataSourceServer.saveMusicPlaylist(playlistId, musicIds, pic)
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
            loadingMessage = "正在删除歌单中的音乐...",
            successMessage = "删除歌单中的音乐成功",
            errorMessage = "删除歌单中的音乐失败"
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
     * 获得媒体库列表
     */
    override suspend fun selectMediaLibrary() {
        try {
            dataSourceServer.selectMediaLibrary()
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得媒体库列表失败", e)
        }
    }

    /**
     * 获得最近播放音乐
     */
    override suspend fun playRecordMusicOrAlbumList() {
        try {
            dataSourceServer.playRecordMusicOrAlbumList()
        } catch (e: SocketTimeoutException) {
            Log.e(Constants.LOG_ERROR_PREFIX, "音乐播放历史更新超时", e)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "音乐播放历史更新失败", e)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "音乐播放历史更新未知错误失败", e)
        }
    }

    /**
     * 获得艺术家信息
     */
    override suspend fun selectArtistInfoByIds(artistIds: List<String>): List<XyArtist>? {
        return dataSourceServer.selectArtistInfoByIds(artistIds)
    }

    /**
     * 根据id获得艺术家信息
     * @param [artistId] 艺术家id
     * @return [List<ArtistItem>?] 艺术家信息
     */
    override suspend fun selectArtistInfoById(artistId: String): XyArtist? {
        return try {
            dataSourceServer.selectArtistInfoById(artistId)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "根据id获得艺术家信息失败", e)
            null
        }
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
     * 获得收藏歌曲列表
     */
    override fun selectFavoriteMusicFlowList(): Flow<PagingData<XyMusic>> {
        return dataSourceServer.selectFavoriteMusicFlowList()
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
     * 获得歌曲列表
     */
    override suspend fun getMusicList(
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
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
    ): List<XyMusic>? {
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
    ): List<XyMusic>? {
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
     * 获得收藏歌曲列表
     */
    override suspend fun getMusicListByFavorite(
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
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
     * 获得OkHttpClient
     */
    override fun getOkhttpClient(): OkHttpClient {
        return dataSourceServer.getOkhttpClient()
    }

    /**
     * 设置token
     */
    override fun setToken() {
        dataSourceServer.setToken()
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
        dataSourceServer.reportProgress(musicId, playSessionId, positionTicks)
    }

    /**
     * 获得播放连接
     */
    override suspend fun getMusicPlayUrl(musicId: String): String {
        return try {
            dataSourceServer.getMusicPlayUrl(musicId)
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取播放连接失败", e)
            ""
        }
    }

    /**
     * 设置收藏音乐信息
     */
    @Transaction
    suspend fun setFavoriteData(
        type: MusicTypeEnum,
        itemId: String,
        musicController: MusicController? = null,
        ifFavorite: Boolean
    ): Boolean {

        val favorite = if (ifFavorite) {
            unmarkFavoriteItem(itemId = itemId, type)
        } else {
            markFavoriteItem(itemId = itemId, type)
        }

        when (type) {
            MusicTypeEnum.MUSIC -> {
                favoriteRepository.toggleBoolean(itemId, favorite)
                if (musicController?.musicInfo?.itemId == itemId) {
                    musicController.updateCurrentFavorite(favorite)
                }
            }

            MusicTypeEnum.ALBUM -> {
            }

            MusicTypeEnum.ARTIST -> {
            }
        }

        return favorite
    }


    /**
     * 根据musicId删除音乐数据
     * @param [musicId] 音乐id
     */
    @Transaction
    suspend fun removeMusicById(musicId: String) {
        OperationTipUtils.operationTipNotToBlock {
            val bool = removeById(musicId)
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
            val bool = removeByIds(musicIds)
            db.musicDao.removeByItemIds(musicIds)
            bool
        }
    }
}