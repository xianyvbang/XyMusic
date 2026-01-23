package cn.xybbz.config

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.count.XyDataCount
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class HomeDataRepository(
    private val db: DatabaseClient,
    private val dataSourceManager: DataSourceManager
) {

    private val _mostPlayedMusic = MutableStateFlow<List<XyMusicExtend>>(emptyList())
    private val _newestAlbums = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _recentMusic = MutableStateFlow<List<XyMusicExtend>>(emptyList())
    private val _recentAlbums = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _mostPlayedAlbums = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _recommendedMusic = MutableStateFlow<List<XyMusicExtend>>(emptyList())
    private val _playlists = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _dataCount = MutableStateFlow<XyDataCount?>(null)

    val mostPlayedMusic: StateFlow<List<XyMusicExtend>> get() = _mostPlayedMusic
    val newestAlbums: StateFlow<List<XyAlbum>> get() = _newestAlbums
    val recentMusic: StateFlow<List<XyMusicExtend>> get() = _recentMusic
    val recentAlbums: StateFlow<List<XyAlbum>> get() = _recentAlbums
    val mostPlayedAlbums: StateFlow<List<XyAlbum>> get() = _mostPlayedAlbums
    val recommendedMusic: StateFlow<List<XyMusicExtend>> get() = _recommendedMusic
    val playlists: StateFlow<List<XyAlbum>> get() = _playlists
    val dataCount: StateFlow<XyDataCount?> get() = _dataCount


    private var collectJobs = mutableListOf<Job>()

   suspend fun initData(){
        loadOnce()
    }

    /**
     * 执行登录状态数据监听
     */
    suspend fun initLoginChangeMonitor(){
        dataSourceManager.getLoginStateFlow().collect { user ->
            //切用户：取消旧监听
            collectJobs.forEach { it.cancel() }
            collectJobs.clear()

            //清空旧缓存（防串数据）
            clearAll()

            //首次加载（不阻塞 UI）
            loadOnce()

            //监听数据库变化
            observeFlows()
        }
    }


    private suspend fun loadOnce() = coroutineScope {
        launch {
            _mostPlayedMusic.value =
                db.musicDao.selectMaximumPlayMusicExtendList(20)
        }
        launch {
            _newestAlbums.value =
                db.albumDao.selectNewestList(20)
        }
        launch {
            _recentMusic.value =
                db.musicDao.selectPlayHistoryMusicExtendList(20)
        }
        launch {
            _recentAlbums.value =
                db.albumDao.selectPlayHistoryAlbumList(20)
        }
        launch {
            _mostPlayedAlbums.value =
                db.albumDao.selectMaximumPlayAlbumList(20)
        }
        launch {
            _recommendedMusic.value =
                db.musicDao.selectRecommendedMusicExtendList(20)
        }
        launch {
            _playlists.value =
                db.albumDao.selectPlaylist()
        }
        launch {
            _dataCount.value =
                db.dataCountDao.selectOne()
        }
    }

    private suspend fun observeFlows() =coroutineScope{
        collectJobs += launch {
            db.musicDao
                .selectLimitMusicListFlow(MusicDataTypeEnum.MAXIMUM_PLAY, 20)
                .distinctUntilChanged()
                .collect { _mostPlayedMusic.value = it }
        }

        collectJobs += launch {
            db.albumDao
                .selectNewestListFlow(20)
                .distinctUntilChanged()
                .collect { _newestAlbums.value = it }
        }

        collectJobs += launch {
            db.musicDao
                .selectLimitMusicListFlow(MusicDataTypeEnum.PLAY_HISTORY, 20)
                .distinctUntilChanged()
                .collect { _recentMusic.value = it }
        }

        collectJobs += launch {
            db.albumDao
                .selectPlayHistoryAlbumListFlow(20)
                .distinctUntilChanged()
                .collect { _recentAlbums.value = it }
        }

        collectJobs += launch {
            db.albumDao
                .selectMaximumPlayAlbumListFlow(20)
                .distinctUntilChanged()
                .collect { _mostPlayedAlbums.value = it }
        }

        collectJobs += launch {
            db.musicDao
                .selectRecommendedMusicExtendListFlow(20)
                .distinctUntilChanged()
                .collect { _recommendedMusic.value = it }
        }

        collectJobs += launch {
            db.albumDao
                .selectPlaylistFlow()
                .distinctUntilChanged()
                .collect { _playlists.value = it }
        }

        collectJobs += launch {
            db.dataCountDao
                .selectOneFlow()
                .distinctUntilChanged()
                .collect { _dataCount.value = it }
        }
    }

    private fun clearAll() {
        _mostPlayedMusic.value = emptyList()
        _newestAlbums.value = emptyList()
        _recentMusic.value = emptyList()
        _recentAlbums.value = emptyList()
        _mostPlayedAlbums.value = emptyList()
        _recommendedMusic.value = emptyList()
        _playlists.value = emptyList()
        _dataCount.value = null
    }
}