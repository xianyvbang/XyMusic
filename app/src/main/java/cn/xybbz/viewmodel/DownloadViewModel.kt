package cn.xybbz.viewmodel

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.download.XyDownload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@SuppressLint("UnsafeOptInUsageError")
@HiltViewModel
class DownloadViewModel @Inject constructor(
    val favoriteRepository: FavoriteRepository,
    private val db: DatabaseClient,
    private val downLoadManager: DownLoadManager
) : ViewModel() {


    val musicDownloadInfo: StateFlow<List<XyDownload>> = db.apkDownloadDao.getAllMusicTasksFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    var isMultiSelectMode by mutableStateOf(false)
        private set

    val selectedTaskIds = mutableStateSetOf<Long>()

    fun pauseDownload(id: Long) = downLoadManager.pause(id)
    fun resumeDownload(id: Long) = downLoadManager.resume(id)
    fun cancelDownload(id: Long) = downLoadManager.cancel(id)
    fun deleteDownload(id: Long) = downLoadManager.delete(id)


    fun enterMultiSelectMode() {
        isMultiSelectMode = true
    }

    fun exitMultiSelectMode() {
        isMultiSelectMode = false
        selectedTaskIds.clear()
    }

    fun toggleSelection(taskId: Long) {
        if (selectedTaskIds.contains(taskId)) {
            selectedTaskIds - taskId
        } else {
            selectedTaskIds + taskId
        }
        // 如果取消了所有选择，则自动退出多选模式
        if (selectedTaskIds.isEmpty()) {
            exitMultiSelectMode()
        }
    }



}