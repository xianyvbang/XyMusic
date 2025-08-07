package cn.xybbz.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.room.Transaction
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.PlaylistFileUtils
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.IDataSourceManager
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.entity.data.SelectControl
import cn.xybbz.entity.data.Sort
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.era.XyEraItem
import cn.xybbz.localdata.data.progress.EnableProgress
import cn.xybbz.localdata.data.progress.Progress
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = AlbumInfoViewModel.Factory::class)
class AlbumInfoViewModel @AssistedInject constructor(
    @Assisted private val itemId: String,
    @Assisted private val dataType: MusicDataTypeEnum,
    private val _dataSourceManager: IDataSourceManager,
    private val db: DatabaseClient,
    private val _musicPlayContext: MusicPlayContext,
    private val _musicController: MusicController,
    private val connectionConfigServer: ConnectionConfigServer,
    private val _selectControl: SelectControl,
    private val _favoriteRepository: FavoriteRepository
) : ViewModel() {

    /**
     * 创建方法
     * [dataType] 0专辑,1歌单
     */
    @AssistedFactory
    interface Factory {
        fun create(itemId: String, dataType: MusicDataTypeEnum): AlbumInfoViewModel
    }

    val musicPlayContext = _musicPlayContext
    val musicController = _musicController
    val dataSourceManager = _dataSourceManager
    val selectControl = _selectControl
    val favoriteRepository = _favoriteRepository


    //排序
    private val _sortType =
        MutableStateFlow(Sort())

    val sortBy: StateFlow<Sort> = _sortType.asStateFlow()

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

    //选中音乐列表
//    val selectMusicData by mutableStateOf<SelectControl>(SelectControl())

    //OutputStream的key
    var outputStreamKey: String by mutableStateOf("")
        private set

    /**
     * 是否开启记录播放历史
     */
    var ifSavePlaybackHistory by mutableStateOf(false)

    init {
        getPlayerHistoryProgressList()
        getAlbumInfoData()
    }

    var xyMusicList = connectionConfigServer.loginStateFlow.flatMapLatest { bool ->
        if (bool)
            _sortType.flatMapLatest { sortBy ->
                _dataSourceManager.selectMusicListByParentId(
                    sortBy.sortType,
                    sortBy.isFavorite,
                    sortBy.eraItem?.years,
                    itemId,
                    dataType = dataType
                ).distinctUntilChanged()
            }.cachedIn(viewModelScope)
        else flow { }
    }.cachedIn(viewModelScope)

    /**
     * 获得专辑详情
     */
    fun getAlbumInfoData() {
        viewModelScope.launch {
            val artistExtend = _dataSourceManager.selectAlbumInfoById(itemId, dataType)
            if (artistExtend != null) {
                xyAlbumInfoData = artistExtend
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
     * 设置排序类型
     */
    fun setSortedData(sortType: SortTypeEnum? = null) {
        this._sortType.update {
            it.sortType = sortType
            it.copy()
        }
    }

    fun setFilterEraType(eraItem: XyEraItem) {
        this._sortType.update {
            if (it.eraItem == eraItem) {
                it.eraItem = null
            } else {
                it.eraItem = eraItem
            }
            it.copy()
        }
    }

    fun setFavorite(isFavorite: Boolean) {
        Log.i("=====", "数据变化$isFavorite")
        this._sortType.update {
            it.isFavorite = isFavorite
            it.copy()
        }
    }


    /**
     * 创建json数据,导出歌单
     */
    suspend fun createJsonStr(applicationContext: Context) {
        outputStreamKey = UUID.randomUUID().toString()
        PlaylistFileUtils.createJsonStr(applicationContext, db, outputStreamKey)
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
    fun show(onOpenChange: (Boolean) -> Unit) {
        selectControl.show(
            true,
            dataType == MusicDataTypeEnum.PLAYLIST,
            onOpenChange = onOpenChange
        )
    }

}