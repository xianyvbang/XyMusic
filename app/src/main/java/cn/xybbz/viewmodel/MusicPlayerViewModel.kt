package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.R
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.music.CacheController
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.SettingsConfig
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.config.lrc.LrcServer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    private val _musicController: MusicController,
    private val _dataSourceManager: IDataSourceManager,
    private val _settingsConfig: SettingsConfig,
    private val _favoriteRepository: FavoriteRepository,
    private val _cacheController: CacheController
) : ViewModel() {

    val musicController = _musicController
    val dataSourceManager = _dataSourceManager
    val settingsConfig = _settingsConfig
    val favoriteRepository = _favoriteRepository
    val cacheController = _cacheController

    var fontSize by mutableFloatStateOf(1.0f)

    val dataList = listOf(R.string.song_tab, R.string.lyrics_tab)


    init {
        viewModelScope.launch {
            musicController.progressStateFlow.collect { pos ->
                LrcServer.getLrcIndex(pos)
            }
        }

    }
}