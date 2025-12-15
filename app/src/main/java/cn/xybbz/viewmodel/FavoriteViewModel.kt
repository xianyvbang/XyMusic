package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.download.DownloadRepository
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.localdata.data.music.XyMusic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val dataSourceManager: DataSourceManager,
    val musicPlayContext: MusicPlayContext,
    val connectionConfigServer: ConnectionConfigServer,
    val musicController: MusicController,
    val favoriteRepository: FavoriteRepository,
    val downloadRepository: DownloadRepository,
    val backgroundConfig: BackgroundConfig
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val favoriteMusicList =
        connectionConfigServer.loginSuccessEvent
            .flatMapLatest {
                dataSourceManager.selectFavoriteMusicFlowList()
            }
            .cachedIn(viewModelScope)



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