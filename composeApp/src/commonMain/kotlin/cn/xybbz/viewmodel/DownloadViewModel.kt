/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.enums.DownloadTypes
import cn.xybbz.download.DownloaderManager
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.localdata.data.music.XyMusic
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class DownloadViewModel(
    val downloadDb: DownloadDatabaseClient,
    private val datasourceServer: DataSourceManager,
    private val downloaderManager: DownloaderManager
) : ViewModel() {


    @OptIn(FlowPreview::class)
    val musicDownloadInfo: StateFlow<List<XyDownload>> =
        downloadDb.downloadDao.getAllMusicTasksFlow(
            notTypeData = DownloadTypes.APK.toString(),
            mediaLibraryId = datasourceServer.getConnectionId().toString()
        )
            .sample(200)
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

    fun pauseDownload(id: Long) = downloaderManager.pause(id)
    fun resumeDownload(id: Long) = downloaderManager.resume(id)
    fun cancelDownload(id: Long) = downloaderManager.cancel(id)
    fun deleteDownload(id: Long) = downloaderManager.delete(id)


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
            if (selectedTaskIds.containsAll(downloadIds)) {
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
            if (isSelectAll) {
                selectedTaskIds.clear()
                isSelectAll = false
            } else {
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
            downloaderManager.pause(*idsToPause.toLongArray())
        }
        exitMultiSelectMode()
    }

    fun performBatchResume() {
        val idsToResume = selectedTaskIds
        if (idsToResume.isNotEmpty()) {
            downloaderManager.resume(*idsToResume.toLongArray())
        }
        exitMultiSelectMode()
    }

    fun performBatchCancel() {
        val idsToCancel = selectedTaskIds
        if (idsToCancel.isNotEmpty()) {
            downloaderManager.cancel(*idsToCancel.toLongArray())
        }
        exitMultiSelectMode()
    }

    fun performBatchDelete() {
        val idsToCancel = selectedTaskIds
        if (idsToCancel.isNotEmpty()) {
            downloaderManager.delete(*idsToCancel.toLongArray())
        }
        exitMultiSelectMode()
    }

}