package cn.xybbz.viewmodel

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.DataSourceChangeUtils
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.recommender.DailyRecommender
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.enums.DownloadStatus
import cn.xybbz.localdata.enums.DownloadTypes
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val db: DatabaseClient,
    val dataSourceManager: DataSourceManager,
    val connectionConfigServer: ConnectionConfigServer,
    private val _musicPlayContext: MusicPlayContext,
    private val musicController: MusicController,
    val backgroundConfig: BackgroundConfig,
    private val dailyRecommender: DailyRecommender
) : ViewModel() {

    val musicPlayContext = _musicPlayContext

    var isRefreshing by mutableStateOf(false)
        private set

    /**
     * 最新专辑
     */
    var newAlbumList by mutableStateOf<List<XyAlbum>>(emptyList())
        private set

    /**
     * 最近播放的音乐
     */
    var musicRecentlyList by mutableStateOf<List<XyMusicExtend>>(emptyList())
        private set

    /**
     * 最近播放的专辑
     */
    var albumRecentlyList by mutableStateOf<List<XyAlbum>>(emptyList())
        private set

    /**
     * 最多播放的音乐
     */
    var mostPlayerMusicList by mutableStateOf<List<XyMusicExtend>>(emptyList())
        private set

    /**
     * 最多播放的专辑
     */
    var mostPlayerAlbumList by mutableStateOf<List<XyAlbum>>(emptyList())
        private set

    /**
     * 推荐音乐
     */
    var recommendedMusicList by mutableStateOf<List<XyMusicExtend>>(emptyList())
        private set


    /**
     * 歌单列表
     */
    var playlists by mutableStateOf(listOf<XyAlbum>())
        private set

    /**
     * 音乐数量
     */
    var musicCount by mutableStateOf<String?>(null)
        private set

    /**
     * 专辑数量
     */
    var albumCount by mutableStateOf<String?>(null)
        private set

    /**
     * 艺术家数量
     */
    var artistCount by mutableStateOf<String?>(null)
        private set

    /**
     * 歌单数量
     */
    var playlistCount by mutableStateOf<String?>(null)
        private set

    /**
     * 流派数量
     */
    var genreCount by mutableStateOf<String?>(null)
        private set

    /**
     * 收藏数量
     */
    var favoriteCount by mutableStateOf<String?>(null)
        private set

    /**
     * 本地音乐数量
     */
    var localCount by mutableStateOf<String?>(null)
        private set

    /**
     * 下载中的任务的数量
     */
    var downloadCount by mutableStateOf<Int>(0)
        private set


    /**
     * 所有链接信息
     */
    var connectionList by mutableStateOf<List<ConnectionConfig>>(emptyList())
        private set

    /**
     * 歌单是否加载中
     */
    var isLoadingPlaylist by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            val cacheTimeout =
                TimeUnit.MILLISECONDS.convert(Constants.HOME_PAGE_TIME_FAILURE, TimeUnit.MINUTES)
            val remoteCurrent = db.remoteCurrentDao.remoteKeyById(RemoteIdConstants.HOME_REFRESH)
            if (System.currentTimeMillis() - (remoteCurrent?.createTime
                    ?: 0) >= cacheTimeout
            ) {
                initGetData({
                    viewModelScope.launch {
                        db.remoteCurrentDao.deleteById(RemoteIdConstants.HOME_REFRESH)
                        db.remoteCurrentDao.insertOrReplace(
                            RemoteCurrent(
                                id = RemoteIdConstants.HOME_REFRESH,
                                nextKey = 0,
                                total = 0,
                                connectionId = connectionConfigServer.getConnectionId()
                            )
                        )
                    }
                }, true)
            }
        }
        getPlaylists()
        getMostPlayerMusicList()
        getMostPlayerAlbumList()
        getNewAlbumList()
        getPlayRecordMusicList()
        getPlayRecordAlbumList()
        getRecommendedMusicList()
        //获取数据数量
        getDataCount()
        //获得本地音乐数量
        getLocalMusicCount()
        //下载中的任务数量
        getDownloadCount()
        getConnectionListData()
    }


    /**
     * 获得最多播放音乐列表
     */
    private fun getMostPlayerMusicList() {
        viewModelScope.launch {
            connectionConfigServer.loginStateFlow.collect { bool ->
                if (bool) {
                    db.musicDao.selectLimitMusicListFlow(MusicDataTypeEnum.MAXIMUM_PLAY, 20)
                        .distinctUntilChanged()
                        .collect {
                            mostPlayerMusicList = it
                        }
                }
            }
        }
    }

    /**
     * 获得最多播放专辑列表
     */
    private fun getMostPlayerAlbumList() {
        viewModelScope.launch {
            connectionConfigServer.loginStateFlow.collect { bool ->
                if (bool) {

                    db.albumDao.selectMaximumPlayAlbumListFlow(20).distinctUntilChanged()
                        .collect {
                            mostPlayerAlbumList = it
                        }
                }
            }
        }
    }

    /**
     * 获得最新专辑列表
     */
    private fun getNewAlbumList() {
        viewModelScope.launch {
            connectionConfigServer.loginStateFlow.collect { bool ->
                if (bool) {
                    db.albumDao.selectNewestListPageFlow(20)
                        .distinctUntilChanged()
                        .collect {
                            newAlbumList = it
                        }
                }

            }

        }
    }

    /**
     * 获得歌单信息
     */
    private fun getPlaylists() {
        viewModelScope.launch {
            connectionConfigServer.loginStateFlow.collect { bool ->
                if (bool) {

                    db.albumDao.selectPlaylistFlow().collect {
                        playlists = it
                        isLoadingPlaylist = false
                    }
                }
            }

        }
    }

    /**
     * 保存歌单
     * @param [name] 歌单名称
     */
    suspend fun savePlaylist(name: String) {
        dataSourceManager.addPlaylist(name)
    }


    /**
     * 修改歌单名称
     * @param [id] ID
     * @param [name] 名称
     */
    fun editPlaylistName(id: String, name: String) {
        viewModelScope.launch {
            dataSourceManager.editPlaylistName(id, name)
        }
    }

    /**
     * 删除歌单
     * @param [id] ID
     */
    fun removePlaylist(id: String) {
        viewModelScope.launch {
            dataSourceManager.removePlaylist(id)
        }
    }

    /**
     * 获得最近播放音乐
     */
    fun getPlayRecordMusicList() {
        viewModelScope.launch {
            connectionConfigServer.loginStateFlow.collect { bool ->
                if (bool) {
                    db.musicDao.selectLimitMusicListFlow(MusicDataTypeEnum.PLAY_HISTORY, 20)
                        .distinctUntilChanged()
                        .collect {
                            musicRecentlyList = it
                        }
                }
            }
        }
    }

    /**
     * 获得最近播放专辑
     */
    fun getPlayRecordAlbumList() {
        viewModelScope.launch {
            connectionConfigServer.loginStateFlow.collect { bool ->
                if (bool) {
                    db.albumDao.selectPlayHistoryAlbumListFlow(20)
                        .distinctUntilChanged()
                        .collect {
                            albumRecentlyList = it
                        }
                }
            }
        }
    }

    /**
     * 获得推荐音乐列表
     */

    fun getRecommendedMusicList() {
        viewModelScope.launch {
            connectionConfigServer.loginStateFlow.collect { bool ->
                if (bool) {
                    db.musicDao.selectRecommendedMusicExtendListFlow(20)
                        .distinctUntilChanged()
                        .collect {
                            recommendedMusicList = it
                        }
                }
            }
        }
    }

    /**
     * 获得服务端歌单
     */
    suspend fun getServerPlaylists() {
        Log.i("=====", "获得歌单数据")
        dataSourceManager.getPlaylists()
    }


    /**
     * 初始化获得数据
     */
    suspend fun initGetData(onEnd: ((Boolean) -> Unit)? = null, isRefresh: Boolean = false) {
        connectionConfigServer.loginStateFlow.collect { bool ->
            if (bool) {
                refreshDataAll(onEnd, isRefresh)
            }
        }
    }

    /**
     * 刷新数据
     */
    fun refreshDataAll(onEnd: ((Boolean) -> Unit)? = null, isRefresh: Boolean = false) {
        isRefreshing = true
        viewModelScope.launch {
            if (dataSourceManager.ifLoginError) {
                if (isRefresh) {
                    connectionConfigServer.updateLoginStates(false)
                    dataSourceManager.initDataSource()
                }
            } else {
                val mostPlayerMusicAsync = async {
                    dataSourceManager.getMostPlayerMusicList()
                }
                val newestAlbumAsync = async {
                    dataSourceManager.getNewestAlbumList()
                }
                val playRecordMusicOrAlbumAsync = async {
                    dataSourceManager.playRecordMusicOrAlbumList()
                }

                val playlistsAsync = async {
                    getServerPlaylists()
                }
                val dataCountAsync = async {
                    getServerDataCount()
                }

                if (isRefresh)
                    async {
                        generateRecommendedMusicList()
                    }

                awaitAll(
                    mostPlayerMusicAsync,
                    newestAlbumAsync,
                    playRecordMusicOrAlbumAsync,
                    playlistsAsync,
                    dataCountAsync
                )
            }
            isRefreshing = false
            onEnd?.invoke(false)
        }
    }

    /**
     * 获取数据数量
     */
    private fun getDataCount() {
        if (dataSourceManager.dataSourceType?.ifShowCount == true)
            viewModelScope.launch {
                connectionConfigServer.loginStateFlow.collect { bool ->
                    if (bool) {
                        db.dataCountDao.selectOneFlow().collect {
                            if (it != null) {
                                musicCount = it.musicCount.toString()
                                albumCount = it.albumCount.toString()
                                artistCount = it.artistCount.toString()
                                playlistCount = it.playlistCount.toString()
                                genreCount = it.genreCount.toString()
                                favoriteCount = it.favoriteCount.toString()
                            } else {
                                musicCount = null
                                albumCount = null
                                artistCount = null
                                playlistCount = null
                                genreCount = null
                                favoriteCount = null
                            }
                        }
                    }
                }
            }
    }

    /**
     * 获得本地音乐数量
     */
    private fun getLocalMusicCount() {
        viewModelScope.launch {
            connectionConfigServer.loginStateFlow.collect { bool ->
                if (bool) {
                    db.downloadDao.getAllMusicTasksCountFlow(status = DownloadStatus.COMPLETED)
                        .collect {
                            if (it == 0) {
                                localCount = null
                            } else {
                                localCount = it.toString()
                            }
                        }
                }
            }
        }
    }

    /**
     * 获得下载列表数量
     */
    private fun getDownloadCount() {
        viewModelScope.launch {
            connectionConfigServer.loginStateFlow.collect { bool ->
                if (bool) {
                    db.downloadDao.getAllMusicTasksDownloadCountFlow(
                        status = listOf(
                            DownloadStatus.QUEUED,
                            DownloadStatus.DOWNLOADING
                        )
                    )
                        .collect {
                            downloadCount = it
                        }
                }
            }
        }
    }

    /**
     * 获取连接信息
     */
    private fun getConnectionListData() {
        viewModelScope.launch {
            db.connectionConfigDao.selectAllDataFlow().collect {
                connectionList = it
            }
        }
    }


    /**
     * 切换连接服务
     */
    suspend fun changeDataSource(connectionConfig: ConnectionConfig) {
        DataSourceChangeUtils.changeDataSource(connectionConfig, dataSourceManager, musicController)
    }

    /**
     * 获得数据数量
     */
    private suspend fun getServerDataCount() {
        dataSourceManager.getDataInfoCount(connectionConfigServer.getConnectionId())
    }

    private suspend fun generateRecommendedMusicList() {
        try {
            dailyRecommender.generate()
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "生成每日推荐错误", e)
        }
    }


    /**
     * 播放音乐列表
     * @param [musicList] 音乐清单
     * @param [onMusicPlayParameter] 关于音乐播放参数
     * @param [playerTypeEnum] 播放顺序类型
     */
    fun musicList(
        onMusicPlayParameter: OnMusicPlayParameter,
        musicList: List<XyMusicExtend>,
        playerTypeEnum: PlayerTypeEnum? = null
    ) {
        viewModelScope.launch {
            musicPlayContext.musicList(
                onMusicPlayParameter,
                musicList.map { it.toPlayMusic() },
                playerTypeEnum
            )
        }
    }

    fun saveTmpDownload() {
        viewModelScope.launch {
            db.downloadDao.insert(
                XyDownload(
                    url = "https://www.baidu.com",
                    fileName = "test",
                    filePath = "/storage/emulated/0/Download/XyMusic/说好的幸福呢 - 周杰伦.mp3",
                    fileSize = 0,
                    tempFilePath = "test",
                    typeData = DownloadTypes.JELLYFIN,
                    progress = 100.0f,
                    status = DownloadStatus.COMPLETED,
                    uid = UUID.randomUUID().toString(),
                    music = XyMusic(
                        itemId = UUID.randomUUID().toString(),
                        name = "test",
                        downloadUrl = "",
                        connectionId = connectionConfigServer.getConnectionId(),
                        container = "mp3",
                    ),
                    connectionId = connectionConfigServer.getConnectionId()
                )
            )
        }
    }
}