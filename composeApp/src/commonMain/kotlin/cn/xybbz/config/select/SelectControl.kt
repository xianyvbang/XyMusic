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

package cn.xybbz.config.select

import cn.xybbz.assembler.MusicPlayAssembler
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.client.FavoriteCoordinator
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.utils.OperationTipUtils
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.ui.components.AddPlaylistBottomData
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.show
import cn.xybbz.ui.xy.XyTextSubSmall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.delete_permanently
import xymusic_kmp.composeapp.generated.resources.delete_warning

/**
 * 选择模式下的界面状态。
 */
data class SelectUiState(
    // 是否打开选择模式
    val isOpen: Boolean = false,
    // 选中音乐列表 id
    val selectedMusicIds: Set<String> = emptySet(),
    // 是否已经全选
    val isSelectAll: Boolean = false,
    // 是否为本地页面操作
    val ifLocal: Boolean = false,
    // 是否启用按钮
    val ifEnableButton: Boolean = false,
    // 当前操作的歌单 id
    val playlistId: String? = null,
    // 是否在歌单中操作
    val ifPlaylist: Boolean = false
)

/**
 * 选择列表数据
 * @author 刘梦龙
 * @date 2025/01/18
 * @constructor 创建[SelectControl]
 */
class SelectControl(
    private val dataSourceManager: DataSourceManager
) {

    // 选择状态的唯一响应式来源
    private val _uiState = MutableStateFlow(SelectUiState())
    val uiState = _uiState.asStateFlow()

    // 当前选中的音乐 id 集合
    val selectMusicIdList: Set<String>
        get() = uiState.value.selectedMusicIds

    // 当前是否处于全选状态
    val isSelectAll: Boolean
        get() = uiState.value.isSelectAll

    // 当前是否在本地页面操作
    val ifLocal: Boolean
        get() = uiState.value.ifLocal

    // 当前批量操作按钮是否可用
    val ifEnableButton: Boolean
        get() = uiState.value.ifEnableButton

    // 当前操作的歌单 id
    val playlistId: String?
        get() = uiState.value.playlistId

    // 当前是否在歌单场景中操作
    val ifPlaylist: Boolean
        get() = uiState.value.ifPlaylist

    // 永久删除所选音乐资源，从硬盘上删除
    val onRemoveSelectListResource: (suspend (DataSourceManager, CoroutineScope) -> Unit) =
        { dataSourceManager, viewModelScope ->
            AlertDialogObject(
                title = getString(Res.string.delete_permanently),
                content = {
                    XyTextSubSmall(
                        text = stringResource(Res.string.delete_warning)
                    )
                },
                ifWarning = true,
                onConfirmation = {
                    removeSelectListResource(dataSourceManager, viewModelScope)
                },
                onDismissRequest = {}
            ).show()
        }

    // 增加选中音乐到播放列表
    val onAddPlaySelect: suspend (MusicCommonController, LocalDatabaseClient, DownloadDatabaseClient) -> Unit =
        { musicController, db, downloadDb ->
            addPlayerList(musicController, db, downloadDb)
        }

    // 增加选中音乐到歌单
    val onAddPlaylistSelect: () -> Unit = {
        AddPlaylistBottomData(
            ifShow = true,
            musicInfoList = selectMusicIdList.toList(),
            onItemClick = {
                dismiss()
            }
        ).show()
    }

    // 从歌单中删除选中音乐
    val onRemovePlaylistMusic: (DataSourceManager, CoroutineScope) -> Unit =
        { dataSourceManager, viewModelScope ->
            removePlaylistMusic(dataSourceManager, viewModelScope)
        }

    // 取消收藏
    val onRemoveFavorite: (DataSourceManager, CoroutineScope, MusicCommonController) -> Unit =
        { dataSourceManager, viewModelScope, musicController ->
            viewModelScope.launch {
                OperationTipUtils.operationTipProgress() { loadingObject ->
                    loadingObject.updateProgress(0.0f, 0)
                    selectMusicIdList.forEachIndexed { index, musicId ->
                        FavoriteCoordinator.setFavoriteData(
                            dataSourceManager = dataSourceManager,
                            type = MusicTypeEnum.MUSIC,
                            itemId = musicId,
                            ifFavorite = true,
                            musicController = musicController
                        )
                        loadingObject.updateProgress(
                            (index * 1.0f + 1.0f) / selectMusicIdList.size,
                            index + 1
                        )
                    }
                }
                dismiss()
            }
        }

    // 状态变化
    var onOpenChange: ((Boolean) -> Unit)? = null

    /**
     * 打开选择模式并初始化上下文。
     */
    fun show(
        ifOpenSelect: Boolean,
        playlistId: String? = null,
        ifPlaylist: Boolean = false,
        ifLocal: Boolean = false,
        onOpenChange: ((Boolean) -> Unit)? = null
    ) {
        setData(ifOpenSelect, playlistId, false, ifPlaylist, ifLocal)
        this.onOpenChange = onOpenChange
    }

    /**
     * 关闭选择模式。
     */
    fun dismiss() {
        clearData()
    }

    /**
     * 设置选择音乐列表数据
     */
    private fun setData(
        ifOpenSelect: Boolean,
        playlistId: String? = null,
        ifEnableButton: Boolean,
        ifPlaylist: Boolean,
        ifLocal: Boolean = false
    ) {
        _uiState.update {
            it.copy(
                isOpen = ifOpenSelect,
                playlistId = playlistId.takeIf { ifPlaylist },
                ifEnableButton = ifEnableButton,
                ifPlaylist = ifPlaylist,
                ifLocal = ifLocal
            )
        }
    }

    /**
     * 删除选中数据
     */
    fun clearData() {
        _uiState.update {
            it.copy(
                isOpen = false,
                selectedMusicIds = emptySet(),
                isSelectAll = false,
                ifEnableButton = false,
                playlistId = null,
                ifPlaylist = false,
                ifLocal = false
            )
        }
        onOpenChange?.invoke(false)
    }

    /**
     * 切换单首歌曲的选中状态。
     */
    fun toggleSelection(musicId: String, onIsSelectAll: () -> Boolean) {
        _uiState.update { current ->
            val nextIds = current.selectedMusicIds.toMutableSet()
            val nextIsSelectAll: Boolean
            if (nextIds.contains(musicId)) {
                nextIds.remove(musicId)
                nextIsSelectAll = false
            } else {
                nextIds.add(musicId)
                nextIsSelectAll = onIsSelectAll()
            }

            current.copy(
                selectedMusicIds = nextIds,
                ifEnableButton = nextIds.isNotEmpty(),
                isSelectAll = nextIsSelectAll
            )
        }
    }

    /**
     * 永久删除当前选中的本地资源。
     */
    fun removeSelectListResource(
        dataSourceManager: DataSourceManager,
        viewModelScope: CoroutineScope
    ) {
        if (selectMusicIdList.isNotEmpty()) {
            viewModelScope.launch {
                dataSourceManager.removeMusicByIds(selectMusicIdList.toList())
            }.invokeOnCompletion {
                dismiss()
            }
        }
    }

    /**
     * 播放选中列表
     */
    suspend fun addPlayerList(
        musicController: MusicCommonController,
        db: LocalDatabaseClient,
        downloadDb: DownloadDatabaseClient
    ) {
        val xyMusics = MusicPlayAssembler.attachFilePath(
            playMusicList = db.musicDao.selectExtendByIds(selectMusicIdList.toList()),
            downloadDb = downloadDb,
            mediaLibraryId = dataSourceManager.getConnectionId().toString()
        ) ?: emptyList()
        musicController.addMusicList(
            musicList = xyMusics,
            isPlayer = true,
        )
        clearData()
    }

    /**
     * 切换全选状态。
     */
    fun toggleSelectionAll(musicIdList: List<String>? = null) {
        _uiState.update { current ->
            if (current.isSelectAll) {
                current.copy(
                    selectedMusicIds = emptySet(),
                    isSelectAll = false,
                    ifEnableButton = false
                )
            } else if (!musicIdList.isNullOrEmpty()) {
                current.copy(
                    selectedMusicIds = musicIdList.toSet(),
                    isSelectAll = true,
                    ifEnableButton = true
                )
            } else {
                current
            }
        }
    }

    /**
     * 从歌单中移除相应音乐
     */
    fun removePlaylistMusic(
        dataSourceManager: DataSourceManager,
        viewModelScope: CoroutineScope
    ) {
        viewModelScope.launch {
            if (selectMusicIdList.isNotEmpty()) {
                playlistId?.let {
                    dataSourceManager.removeMusicPlaylist(
                        it,
                        selectMusicIdList.toList()
                    )
                }
            }
        }.invokeOnCompletion {
            dismiss()
        }
    }

    // 判断选择列表是否为空
    fun ifSelectEmpty(): Boolean {
        return selectMusicIdList.isEmpty()
    }
}
