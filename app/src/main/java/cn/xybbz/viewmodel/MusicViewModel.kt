package cn.xybbz.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.IDataSourceManager
import cn.xybbz.config.SettingsConfig
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.entity.data.SelectControl
import cn.xybbz.entity.data.Sort
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.era.XyEraItem
import cn.xybbz.localdata.data.music.XyMusic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MusicViewModel @Inject constructor(
    private val _dataSourceManager: IDataSourceManager,
    private val db: DatabaseClient,
    private val _settingsConfig: SettingsConfig,
    private val _musicPlayContext: MusicPlayContext,
    private val _musicController: MusicController,
    private val connectionConfigServer: ConnectionConfigServer,
    private val _selectControl: SelectControl,
    private val _favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _sortType = MutableStateFlow(Sort())

    val sortBy: StateFlow<Sort> = _sortType

    val settingsConfig = _settingsConfig
    val dataSourceManager = _dataSourceManager
    val musicPlayContext = _musicPlayContext
    val musicController = _musicController
    val selectControl = _selectControl
    val favoriteRepository = _favoriteRepository

    //选中音乐列表
//    val selectMusicData by mutableStateOf(SelectListData())

    var homeMusicPager = connectionConfigServer.loginStateFlow.flatMapLatest { bool ->
        if (bool)
            _sortType.flatMapLatest { sortBy ->
                _dataSourceManager.selectMusicFlowList(
                    sortBy.sortType,
                    sortBy.isFavorite,
                    sortBy.eraItem?.years
                ).distinctUntilChanged()
            }.cachedIn(viewModelScope)
        else flow { }
    }.cachedIn(viewModelScope)

    /**
     * 设置排序类型
     */
    fun setSortedData(sortType: SortTypeEnum?) {
        this._sortType.update {
            it.sortType = sortType
            it.copy()
        }
    }

    /**
     * 设置过滤类型
     */
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

    /**
     * 设置值查询收藏
     */
    fun setFavorite(isFavorite: Boolean) {
        Log.i("=====", "数据变化$isFavorite")
        this._sortType.update {
            it.isFavorite = isFavorite
            it.copy()
        }
    }

    /**
     * 获得音乐数据
     * @param [musicId] 音乐id
     */
    suspend fun getMusicInfo(
        musicId: String,
    ): XyMusic? {
        return dataSourceManager.selectMusicInfoById(musicId)
    }

    /**
     * 获得当前加载的列表
     */
    suspend fun getMusicMusic(limit:Int):List<XyMusic>?{
        return db.musicDao.selectHomeMusicList(limit,0)
    }

}