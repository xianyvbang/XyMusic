package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.enums.DownloadStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LocalViewModel @Inject constructor(
    db: DatabaseClient,
    val favoriteRepository: FavoriteRepository,
    val musicController: MusicController,
    val musicPlayContext: MusicPlayContext,
) : ViewModel() {

    val musicDownloadInfo: StateFlow<List<XyDownload>> =
        db.apkDownloadDao.getAllMusicTasksFlow(status = DownloadStatus.COMPLETED)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}