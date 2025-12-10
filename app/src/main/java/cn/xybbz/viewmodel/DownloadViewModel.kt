package cn.xybbz.viewmodel

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.data.music.XyMusic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("UnsafeOptInUsageError")
@HiltViewModel
class DownloadViewModel @Inject constructor(
    val favoriteRepository: FavoriteRepository,
    val db: DatabaseClient,
    private val downLoadManager: DownLoadManager,
    private val datasourceServer: IDataSourceManager,
    val backgroundConfig: BackgroundConfig,
) : ViewModel() {


    val musicDownloadInfo: StateFlow<List<XyDownload>> = db.downloadDao.getAllMusicTasksFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    var isMultiSelectMode by mutableStateOf(false)
        private set

    var isSelectAll by mutableStateOf(false)
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
        isSelectAll = false
        selectedTaskIds.clear()
    }

    fun toggleSelection(taskId: Long) {
        if (selectedTaskIds.contains(taskId)) {
            isSelectAll = false
            selectedTaskIds.remove(taskId)
        } else {
            //判断是否已经全选
            val downloadIds = musicDownloadInfo.value.map { it.id }
            selectedTaskIds.add(taskId)
            if (selectedTaskIds.containsAll(downloadIds)){
                isSelectAll = true
            }
        }
        // 如果取消了所有选择，则自动退出多选模式
        if (selectedTaskIds.isEmpty()) {
            exitMultiSelectMode()
        }
    }

    fun toggleSelectionAll() {
        viewModelScope.launch {
            val downloadIds = musicDownloadInfo.value.map { it.id }
            if (isSelectAll){
                selectedTaskIds.clear()
                isSelectAll = false
            }else {
                isSelectAll = true
                selectedTaskIds.addAll(downloadIds)
            }
        }
    }


    suspend fun getMusicInfoById(itemId: String): XyMusic? {
        return datasourceServer.selectMusicInfoById(itemId)
    }


    fun performBatchPause() {
        val idsToPause = selectedTaskIds
        if (idsToPause.isNotEmpty()) {
            downLoadManager.pause(*idsToPause.toLongArray())
        }
        exitMultiSelectMode()
    }

    fun performBatchResume() {
        val idsToResume = selectedTaskIds
        if (idsToResume.isNotEmpty()) {
            downLoadManager.resume(*idsToResume.toLongArray())
        }
        exitMultiSelectMode()
    }

    fun performBatchCancel() {
        val idsToCancel = selectedTaskIds
        if (idsToCancel.isNotEmpty()) {
            downLoadManager.cancel(*idsToCancel.toLongArray())
        }
        exitMultiSelectMode()
    }

    fun performBatchDelete() {
        val idsToCancel = selectedTaskIds
        if (idsToCancel.isNotEmpty()) {
            downLoadManager.delete(*idsToCancel.toLongArray())
        }
        exitMultiSelectMode()
    }

}