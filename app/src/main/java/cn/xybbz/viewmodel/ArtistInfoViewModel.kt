package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.localdata.data.artist.XyArtist
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * 艺术家详情ViewModel
 */
@HiltViewModel(assistedFactory = ArtistInfoViewModel.Factory::class)
class ArtistInfoViewModel @AssistedInject constructor(
    @Assisted private val artistId: String,
    private val _dataSourceManager: IDataSourceManager,
    private val _musicPlayContext: MusicPlayContext,
    private val _connectionConfigServer: ConnectionConfigServer,
    private val _musicController: MusicController,
    private val _favoriteRepository: FavoriteRepository,
    private val _backgroundConfig: BackgroundConfig
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(artistId: String): ArtistInfoViewModel
    }

    val musicPlayContext = _musicPlayContext
    val dataSourceManager = _dataSourceManager
    val musicController = _musicController
    val connectionConfigServer = _connectionConfigServer
    val favoriteRepository = _favoriteRepository
    val backgroundConfig = _backgroundConfig


    /**
     * 艺术家信息
     */
    var artistInfoData by mutableStateOf<XyArtist?>(null)
        private set

    /**
     * 是否收藏
     */
    var ifFavorite by mutableStateOf(false)
        private set

    //艺术家的音乐列表
    val musicList = _dataSourceManager.selectMusicListByArtistId(artistId).distinctUntilChanged()
        .cachedIn(viewModelScope)

    //艺术家的专辑列表
    val albumList = _dataSourceManager.selectAlbumListByArtistId(artistId).distinctUntilChanged()
        .cachedIn(viewModelScope)

    init {
        getArtistInfoData()
    }

    /**
     * 获得艺术家信息
     */
    private fun getArtistInfoData() {
        viewModelScope.launch {
            val artistInfoTmp = dataSourceManager.selectArtistInfoById(artistId)
            if (artistInfoTmp != null) {
                artistInfoData = artistInfoTmp
                ifFavorite = artistInfoTmp.ifFavorite
            }
        }
    }
    /**
     * 更新收藏信息
     */
    fun updateFavorite(ifFavorite: Boolean){
        this.ifFavorite = ifFavorite
    }
}