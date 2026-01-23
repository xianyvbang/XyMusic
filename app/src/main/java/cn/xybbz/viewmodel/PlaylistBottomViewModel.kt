package cn.xybbz.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 底部弹窗歌单ViewModel
 */
@HiltViewModel
class PlaylistBottomViewModel @Inject constructor(
    private val db: DatabaseClient,
    val dataSourceManager: DataSourceManager

    ) : ViewModel() {

    /**
     * 歌单列表
     */
    var playlists by mutableStateOf(emptyList<XyAlbum>())
        private set

    var isInit by mutableStateOf(false)
        private set

    private var playlistJob: Job? = null


    init {
        observeLoginSuccessForPlaylist()
        isInit = true
    }

    private fun observeLoginSuccessForPlaylist() {
        viewModelScope.launch {
            dataSourceManager.getLoginStateFlow().collect {
                startPlaylistObserver()
            }
        }
    }
    /**
     * 获得歌单数据
     */
    private fun startPlaylistObserver() {
        // 取消之前的 Job，避免重复订阅
        playlistJob?.cancel()

        playlistJob = viewModelScope.launch {
            db.albumDao
                .selectPlaylistFlow()
                .distinctUntilChanged()
                .collect { list ->
                    if (list.isNotEmpty()) {
                        playlists = list
                    }
                }
        }
    }


    suspend fun getServerPlaylists() {
        Log.i("=====","获得歌单数据")
        dataSourceManager.getPlaylists()
    }
}