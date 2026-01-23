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
import cn.xybbz.common.enums.HomeRefreshReason
import cn.xybbz.common.enums.LoginType
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.DataSourceChangeUtils
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.HomeDataRepository
import cn.xybbz.config.recommender.DailyRecommender
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.enums.DownloadStatus
import cn.xybbz.localdata.enums.PlayerTypeEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val db: DatabaseClient,
    val dataSourceManager: DataSourceManager,
    val musicPlayContext: MusicPlayContext,
    private val musicController: MusicController,
    val backgroundConfig: BackgroundConfig,
    private val dailyRecommender: DailyRecommender,
    val homeDataRepository: HomeDataRepository
) : ViewModel() {


    var isRefreshing by mutableStateOf(false)
        private set

    /**
     * 本地音乐数量
     */
    var localCount by mutableStateOf<String?>(null)
        private set

    /**
     * 下载中的任务的数量
     */
    var downloadCount by mutableStateOf(0)
        private set


    /**
     * 所有链接信息
     */
    var connectionList by mutableStateOf<List<ConnectionConfig>>(emptyList())
        private set

    private var localMusicCountJob: Job? = null
    private var downloadCountJob: Job? = null


    init {
        observeLoginSuccess()
        observeLoginSuccessRoomData()
        getConnectionListData()

        initLoginChangeMonitor()
    }

    /**
     * 监听登录状态加载数据
     */
    private fun initLoginChangeMonitor(){
        viewModelScope.launch {
            homeDataRepository.initLoginChangeMonitor()
        }
    }

    private fun observeLoginSuccessRoomData() {
        viewModelScope.launch {
            startHomeDataObservers()
        }
    }

    private fun startHomeDataObservers() {
        startDownloadDataObservers()
    }

    /**
     * 获得本地音乐数量
     */
    private fun startDownloadDataObservers() {
        // 取消之前的 Job，避免重复监听
        localMusicCountJob?.cancel()
        downloadCountJob?.cancel()

        // 本地音乐数量
        localMusicCountJob = viewModelScope.launch {
            db.downloadDao
                .getAllMusicTasksCountFlow(status = DownloadStatus.COMPLETED)
                .collect { count ->
                    localCount = if (count == 0) null else count.toString()
                }
        }

        // 下载列表数量
        downloadCountJob = viewModelScope.launch {
            db.downloadDao
                .getAllMusicTasksDownloadCountFlow(
                    status = listOf(DownloadStatus.QUEUED, DownloadStatus.DOWNLOADING)
                )
                .collect { count ->
                    downloadCount = count
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
     * 获得服务端歌单
     */
    suspend fun getServerPlaylists() {
        Log.i("=====", "获得歌单数据")
        dataSourceManager.getPlaylists()
    }

    /**
     * 手动刷新
     */
    fun onManualRefresh() {
        viewModelScope.launch {
            tryRefreshHome(
                reason = HomeRefreshReason.Manual
            )
        }
    }

    /**
     * 登陆成功后监听一次
     */
    private fun observeLoginSuccess() {
        viewModelScope.launch {
            dataSourceManager.getLoginStateFlow().collect {
                tryRefreshHome(
                    isRefresh = true,
                    reason = HomeRefreshReason.Login
                )
            }
        }
    }

    /**
     * 初始化获得数据
     */
    suspend fun tryRefreshHome(
        isRefresh: Boolean = false,
        reason: HomeRefreshReason,
        onEnd: ((Boolean) -> Unit)? = null,
    ) {
        if (!shouldRefresh(reason)) {
            onEnd?.invoke(false)
            return
        }
        refreshDataAll(onEnd, isRefresh)
        updateHomeRefreshTime()
    }

    /**
     * 判断是否可以刷新
     */
    private suspend fun shouldRefresh(reason: HomeRefreshReason): Boolean {
        // 手动刷新：永远刷新
        if (reason == HomeRefreshReason.Manual) return true

        val remoteCurrent = db.remoteCurrentDao
            .remoteKeyById(RemoteIdConstants.HOME_REFRESH)

        val lastTime = remoteCurrent?.createTime ?: 0L
        val now = System.currentTimeMillis()

        return now - lastTime >= Constants.HOME_PAGE_TIME_FAILURE * 60_000
    }

    /**
     * 更新刷新时间
     */
    private suspend fun updateHomeRefreshTime() {
        db.remoteCurrentDao.insertOrReplace(
            RemoteCurrent(
                id = RemoteIdConstants.HOME_REFRESH,
                connectionId = dataSourceManager.getConnectionId(),
                createTime = System.currentTimeMillis()
            )
        )
    }

    /**
     * 刷新数据
     */
    fun refreshDataAll(onEnd: ((Boolean) -> Unit)? = null, isRefresh: Boolean = false) {
        isRefreshing = true
        viewModelScope.launch {
            if (dataSourceManager.ifLoginError) {
                if (isRefresh) {
                    autoLogin()
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
        dataSourceManager.getDataInfoCount(dataSourceManager.getConnectionId())
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

    fun autoLogin(){
        viewModelScope.launch {
            dataSourceManager.serverLogin(LoginType.API, null)
        }
    }
}