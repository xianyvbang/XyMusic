package cn.xybbz.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.SettingsConfig
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.entity.data.SelectControl
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.HomeMusic
import cn.xybbz.localdata.data.music.XyMusic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
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
    private val _favoriteRepository: FavoriteRepository,
    private val _backgroundConfig: BackgroundConfig
) : PageListViewModel() {

    val settingsConfig = _settingsConfig
    val dataSourceManager = _dataSourceManager
    val musicPlayContext = _musicPlayContext
    val musicController = _musicController
    val selectControl = _selectControl
    val favoriteRepository = _favoriteRepository
    val backgroundConfig = _backgroundConfig


    // 单例Pager Flow
    // 暴露一个 Flow<PagingData<XyMusic>>
    val homeMusicPager: Flow<PagingData<HomeMusic>> =
        connectionConfigServer.loginStateFlow
            .flatMapLatest { loggedIn ->
                if (loggedIn) {
                    dataSourceManager
                        .selectMusicFlowList(_sortType) // PagingSource + RemoteMediator
                        .distinctUntilChanged()
                        .cachedIn(viewModelScope)
                } else {
                    emptyFlow()
                }
            }
            .cachedIn(viewModelScope)

    suspend fun getMusicInfoById(musicId: String): XyMusic? = db.musicDao.selectById(musicId)
    suspend fun getMusicInfoByIds(musicIds: List<String>): List<XyMusic> =
        db.musicDao.selectByIds(musicIds)

}