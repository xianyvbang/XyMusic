package cn.xybbz.viewmodel

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.room.Transaction
import cn.xybbz.common.music.CacheController
import cn.xybbz.common.music.MusicController
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.entity.data.SelectControl
import cn.xybbz.localdata.config.DatabaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(UnstableApi::class)
class SnackBarPlayerViewModel @Inject constructor(
    private val _musicController: MusicController,
    private val db: DatabaseClient,
    private val cacheController: CacheController,
    private val _datasourceManager:IDataSourceManager,
    private val _selectControl: SelectControl
) : ViewModel() {

    val musicController = _musicController
    val datasourceManager = _datasourceManager
    val selectControl = _selectControl


    /**
     * 清空播放列表
     */
    @Transaction
    suspend fun clearPlayer() {
        db.playerDao.removeByDatasource()
        db.musicDao.removePlayQueueMusic()
        viewModelScope.launch {
            cacheController.clearCache()
        }
    }
}