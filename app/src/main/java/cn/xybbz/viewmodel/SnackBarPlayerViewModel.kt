package cn.xybbz.viewmodel

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.room.Transaction
import cn.xybbz.R
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.music.CacheController
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.download.core.DownloadRequest
import cn.xybbz.entity.data.SelectControl
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.enums.DownloadTypes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(UnstableApi::class)
class SnackBarPlayerViewModel @Inject constructor(
    val musicController: MusicController,
    val db: DatabaseClient,
    private val cacheController: CacheController,
    val dataSourceManager: IDataSourceManager,
    val selectControl: SelectControl,
    private val downloadManager: DownLoadManager,
    val connectionConfigServer: ConnectionConfigServer
) : ViewModel() {


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

    fun downloadMusics() {
        viewModelScope.launch {
            val downloadTypes =
                dataSourceManager.dataSourceType?.getDownloadType() ?: DownloadTypes.APK
            val itemIds = selectControl.selectMusicIdList.toList()
            val musicList = db.musicDao.selectByIds(itemIds)
            val requests = musicList.map { musicData ->
                DownloadRequest(
                    url = musicData.downloadUrl,
                    fileName = musicData.name + "." + musicData.container,
                    fileSize = musicData.size ?: 0,
                    uid = musicData.itemId,
                    title = musicData.name,
                    type = downloadTypes,
                    cover = musicData.pic,
                    duration = musicData.runTimeTicks,
                    connectionId = connectionConfigServer.getConnectionId(),
                    music = musicData
                )
            }
            if (requests.isNotEmpty()) {
                downloadManager.enqueue(
                    *requests.toTypedArray()
                )
                MessageUtils.sendPopTip(R.string.add_download_list)
            }

        }.invokeOnCompletion { selectControl.dismiss() }

    }
}