package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.localdata.data.music.XyMusic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val dataSourceManager: IDataSourceManager,
    private val _musicPlayContext: MusicPlayContext,
    private val connectionConfigServer: ConnectionConfigServer,
    private val _musicController: MusicController,
    private val _favoriteRepository: FavoriteRepository,
    private val _backgroundConfig: BackgroundConfig
) : ViewModel() {

    val musicPlayContext = _musicPlayContext
    val musicController = _musicController
    val favoriteRepository = _favoriteRepository
    val backgroundConfig = _backgroundConfig

    @OptIn(ExperimentalCoroutinesApi::class)
    val favoriteMusicList =
        connectionConfigServer.loginStateFlow.flatMapLatest { bool ->
            if (bool) {
                dataSourceManager.selectFavoriteMusicFlowList().distinctUntilChanged()
            } else {
                flow { }
            }
        }.cachedIn(viewModelScope)


    /**
     * 获得音乐数据
     * @param [musicId] 音乐id
     */
    suspend fun getMusicInfo(
        musicId: String,
    ): XyMusic? {
        return dataSourceManager.selectMusicInfoById(musicId)
    }
}