package cn.xybbz.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.room.Transaction
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.PlaylistFileUtils
import cn.xybbz.common.utils.PlaylistParser
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.entity.data.SelectControl
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.progress.EnableProgress
import cn.xybbz.localdata.data.progress.Progress
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AlbumInfoViewModel.Factory::class)
class AlbumInfoViewModel @AssistedInject constructor(
    @Assisted private val itemId: String,
    @Assisted private val dataType: MusicDataTypeEnum,
    val dataSourceManager: IDataSourceManager,
    private val db: DatabaseClient,
    val musicPlayContext: MusicPlayContext,
    val musicController: MusicController,
    private val connectionConfigServer: ConnectionConfigServer,
    val selectControl: SelectControl,
    val favoriteRepository: FavoriteRepository,
    val backgroundConfig: BackgroundConfig
) : PageListViewModel() {

    /**
     * 创建方法
     * [dataType] 0专辑,1歌单
     */
    @AssistedFactory
    interface Factory {
        fun create(itemId: String, dataType: MusicDataTypeEnum): AlbumInfoViewModel
    }


    /**
     * 播放进度
     */
    var albumPlayerHistoryProgress by mutableStateOf<Progress?>(null)
        private set

    /**
     * 进度信息map
     */
    var albumPlayerHistoryProgressMap = mutableStateMapOf<String, Int>()
        private set

    /**
     * 专辑信息
     */
    var xyAlbumInfoData by mutableStateOf<XyAlbum?>(null)
        private set

    /**
     * 收藏信息
     */
    var ifFavorite by mutableStateOf(false)
        private set

    /**
     * 是否开启记录播放历史
     */
    var ifSavePlaybackHistory by mutableStateOf(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val xyMusicList: Flow<PagingData<XyMusic>> = connectionConfigServer.loginStateFlow
        .flatMapLatest { loggedIn ->
            if (loggedIn) {
                dataSourceManager.selectMusicListByParentId(
                    itemId = itemId,
                    dataType = dataType,
                    sort = sortBy
                ).distinctUntilChanged()
                    .cachedIn(viewModelScope)
            } else {
                emptyFlow()
            }
        }.cachedIn(viewModelScope)

    init {
        getPlayerHistoryProgressList()
        getAlbumInfoData()
    }

    /**
     * 获得专辑详情
     */
    fun getAlbumInfoData() {
        viewModelScope.launch {
            val albumInfo = dataSourceManager.selectAlbumInfoById(itemId, dataType)
            if (albumInfo != null) {
                xyAlbumInfoData = albumInfo
                ifFavorite = albumInfo.ifFavorite
            }

        }

    }


    //region 播放历史进度
    fun getPlayerHistoryProgressList() {
        viewModelScope.launch {
            db.progressDao.selectByAlbumIdFlowOne(albumId = itemId)
                .distinctUntilChanged().collect { data ->
                    albumPlayerHistoryProgress = data
                }
        }


        viewModelScope.launch {
            db.progressDao.selectByAlbumIdFlowMap(itemId).distinctUntilChanged()
                .collect {
                    if (it.isNotEmpty()) {
                        Log.i("=====", "数据进度变化")
                        albumPlayerHistoryProgressMap.clear()
                        albumPlayerHistoryProgressMap.putAll(it)
                    }
                }
        }
        //获取专辑是否开启播放历史记录
        viewModelScope.launch {
            db.enableProgressDao.getAlbumEnableProgressByAlbumId(itemId)
                .distinctUntilChanged().collect {
                    ifSavePlaybackHistory = it == true
                }
        }
    }

    //endregion

    /**
     * 修改保存是否开启播放历史存储
     */
    fun setIfSavePlaybackHistoryData(albumId: String, value: Boolean) {
        this.ifSavePlaybackHistory = value
        viewModelScope.launch {
            saveIfSavePlaybackHistoryData(albumId, value)
        }
    }

    /**
     * 删除专辑播放历史
     */
    fun removeAlbumPlayerHistoryProgress(musicId: String) {
        viewModelScope.launch {
            db.progressDao.removeByMusicId(musicId = musicId)

        }
    }


    /**
     * 更新专辑的播放历史数据
     */
    @Transaction
    suspend fun saveIfSavePlaybackHistoryData(albumId: String, value: Boolean) {
        db.enableProgressDao.save(
            EnableProgress(
                albumId,
                value,
                connectionId = connectionConfigServer.getConnectionId()
            )
        )
        if (!value) {
            //清空播放历史
            db.progressDao.removeByAlbumId(albumId)
        }
    }


    /**
     * 导出歌单
     */
    suspend fun exportPlaylist(): PlaylistParser.Playlist? {
        return PlaylistFileUtils.createTrackList(
            dataSourceManager = dataSourceManager,
            playlistId = itemId
        )
    }

    /**
     * 导入歌单
     */
    suspend fun importPlaylist(playlist: PlaylistParser.Playlist) {
        dataSourceManager.importPlaylist(playlist, itemId)
    }

    /**
     * 删除歌单
     * @param [id] ID
     */
    suspend fun removePlaylist(id: String) {
        dataSourceManager.removePlaylist(id)
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
     * 更新选择功能是否是在歌单中
     */
    fun show(onOpenChange: ((Boolean) -> Unit)? = null) {
        selectControl.show(
            true,
            itemId,
            dataType == MusicDataTypeEnum.PLAYLIST,
            onOpenChange = onOpenChange
        )
    }

    /**
     * 更新收藏状态
     */
    fun updateIfFavorite(ifFavorite: Boolean) {
        this.ifFavorite = ifFavorite
    }

}