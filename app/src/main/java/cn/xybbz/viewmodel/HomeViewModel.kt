package cn.xybbz.viewmodel

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.DataSourceChangeUtils
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val db: DatabaseClient,
    private val _dataSourceManager: IDataSourceManager,
    private val connectionConfigServer: ConnectionConfigServer,
    private val _musicPlayContext: MusicPlayContext,
    private val musicController: MusicController,
    private val _backgroundConfig: BackgroundConfig
) : ViewModel() {

    val musicPlayContext = _musicPlayContext
    val dataSourceManager = _dataSourceManager
    val backgroundConfig = _backgroundConfig

    /**
     * 最新专辑
     */
    var newAlbumList by mutableStateOf<List<XyAlbum>>(emptyList())
        private set

    /**
     * 最近播放的音乐
     */
    var musicRecentlyList by mutableStateOf<List<XyMusic>>(emptyList())
        private set

    /**
     * 最近播放的专辑
     */
    var albumRecentlyList by mutableStateOf<List<XyAlbum>>(emptyList())
        private set

    /**
     * 最多播放的音乐
     */
    var mostPlayerMusicList by mutableStateOf<List<XyMusic>>(emptyList())
        private set

    /**
     * 最多播放的专辑
     */
    var mostPlayerAlbumList by mutableStateOf<List<XyAlbum>>(emptyList())
        private set

    /**
     * 歌单列表
     */
    var playlists by mutableStateOf(listOf<XyAlbum>())
        private set

    /**
     * 音乐数量
     */
    var musicCount by mutableIntStateOf(0)
        private set

    /**
     * 专辑数量
     */
    var albumCount by mutableIntStateOf(0)
        private set

    /**
     * 艺术家数量
     */
    var artistCount by mutableIntStateOf(0)
        private set

    /**
     * 歌单数量
     */
    var playlistCount by mutableIntStateOf(0)
        private set

    /**
     * 流派数量
     */
    var genreCount by mutableIntStateOf(0)
        private set

    /**
     * 收藏数量
     */
    var favoriteCount by mutableIntStateOf(0)
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
                })
            }
        }
        getPlaylists()
        getMostPlayerMusicList()
        getMostPlayerAlbumList()
        getNewAlbumList()
        getPlayRecordMusicList()
        getPlayRecordAlbumList()
        //获取数据数量
        getDataCount()
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
     * 播放音乐列表
     * @param [musicList] 音乐清单
     * @param [onMusicPlayParameter] 关于音乐播放参数
     * @param [playerTypeEnum] 播放顺序类型
     */
    fun playMusicList(
        musicList: List<XyMusic>,
        onMusicPlayParameter: OnMusicPlayParameter,
        playerTypeEnum: PlayerTypeEnum
    ) {
        _musicPlayContext.musicList(onMusicPlayParameter, musicList, playerTypeEnum)
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
     * 获得服务端歌单
     */
    suspend fun getServerPlaylists() {
        Log.i("=====","获得歌单数据")
        dataSourceManager.getPlaylists()
    }


    /**
     * 初始化获得数据
     */
    suspend fun initGetData(onEnd: (Boolean) -> Unit, isRefresh: Boolean = false) {
        connectionConfigServer.loginStateFlow.collect { bool ->
            if (bool) {
                refreshDataAll(onEnd, isRefresh)
            }
        }
    }

    /**
     * 刷新数据
     */
    fun refreshDataAll(onEnd: (Boolean) -> Unit, isRefresh: Boolean = false) {
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

                awaitAll(
                    mostPlayerMusicAsync,
                    newestAlbumAsync,
                    playRecordMusicOrAlbumAsync,
                    playlistsAsync,
                    dataCountAsync
                )
            }
            onEnd(false)
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
                            musicCount = it?.musicCount ?: 0
                            albumCount = it?.albumCount ?: 0
                            artistCount = it?.artistCount ?: 0
                            playlistCount = it?.playlistCount ?: 0
                            genreCount = it?.genreCount ?: 0
                            favoriteCount = it?.favoriteCount ?: 0
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
}