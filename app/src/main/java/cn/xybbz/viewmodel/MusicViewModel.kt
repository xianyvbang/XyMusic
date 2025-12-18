package cn.xybbz.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.config.download.DownloadRepository
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.config.select.SelectControl
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.HomeMusic
import cn.xybbz.localdata.data.music.XyMusic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MusicViewModel @Inject constructor(
    val dataSourceManager: DataSourceManager,
    private val db: DatabaseClient,
    val settingsManager: SettingsManager,
    val musicPlayContext: MusicPlayContext,
    val musicController: MusicController,
    val connectionConfigServer: ConnectionConfigServer,
    val selectControl: SelectControl,
    val favoriteRepository: FavoriteRepository,
    val downloadRepository: DownloadRepository,
    val backgroundConfig: BackgroundConfig
) : PageListViewModel() {


    // 单例Pager Flow
    // 暴露一个 Flow<PagingData<XyMusic>>
    val musicListPage: Flow<PagingData<HomeMusic>> =
        connectionConfigServer.loginSuccessEvent
            .flatMapLatest {
                _sortType.flatMapLatest { sort ->
                    dataSourceManager.selectMusicFlowList(sort)
                }
            }
            .cachedIn(viewModelScope) // 只调用一次


    suspend fun getMusicInfoById(musicId: String): XyMusic? = db.musicDao.selectById(musicId)

}