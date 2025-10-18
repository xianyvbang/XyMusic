package cn.xybbz.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 底部弹窗歌单ViewModel
 */
@HiltViewModel
class PlaylistBottomViewModel @Inject constructor(
    private val db: DatabaseClient,
    val dataSourceManager: IDataSourceManager,
    private val connectionConfigServer: ConnectionConfigServer,

    ) : ViewModel() {

    /**
     * 歌单列表
     */
    var playlists by mutableStateOf(emptyList<XyAlbum>())
        private set

    var isInit by mutableStateOf(false)
        private set

    init {
        initPlaylist()
        isInit = true
    }

    /**
     * 获得歌单数据
     */
    private fun initPlaylist() {
        viewModelScope.launch {
            connectionConfigServer.loginStateFlow.collect { bool ->
                if (bool) {
//                    getServerPlaylists()
                    db.albumDao.selectPlaylistFlow()
                        .distinctUntilChanged()
                        .collect {
                            if (it.isNotEmpty()) {
                                playlists = it
                            }
                        }
                }
            }
        }
    }

    suspend fun getServerPlaylists() {
        Log.i("=====","获得歌单数据")
        dataSourceManager.getPlaylists()
    }
}