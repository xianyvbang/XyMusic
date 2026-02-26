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

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import cn.xybbz.R
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.OperationTipUtils
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.ui.components.AddPlaylistBottomData
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.show
import cn.xybbz.ui.xy.XyTextSubSmall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 选择列表数据
 * @author 刘梦龙
 * @date 2025/01/18
 * @constructor 创建[SelectControl]
 */
class SelectControl(val application: Context) {



    //选中音乐列表id
    val selectMusicIdList = mutableStateSetOf<String>()

    /**
     * 是否已经全选
     */
    var isSelectAll by mutableStateOf(false)
        private set

    //是否显示按钮
    /*var ifOpenSelect by mutableStateOf(false)
        private set*/

    private val _uiState = MutableStateFlow(
        false
    )
    val uiState = _uiState.asStateFlow()

    //是否为本地页面操作
    var ifLocal by mutableStateOf(false)
        private set

    //是否启用按钮
    var ifEnableButton by mutableStateOf(false)
        private set

    var playlistId: String? by mutableStateOf(null)
        private set

    //是否在歌单中操作
    var ifPlaylist by mutableStateOf(false)
        private set

    //永久删除所选音乐资源-从硬盘上删除
    val onRemoveSelectListResource: ((DataSourceManager, CoroutineScope) -> Unit) =
        { dataSourceManager, viewModelScope ->
            AlertDialogObject(
                title = application.getString(R.string.delete_permanently),
                content = {
                    XyTextSubSmall(
                        text = stringResource(R.string.delete_warning)
                    )
                },
                ifWarning = true,
                onConfirmation = {
                    removeSelectListResource(dataSourceManager, viewModelScope)
                }, onDismissRequest = {}).show()
        }

    //增加选中音乐到播放列表
    val onAddPlaySelect: suspend (MusicController, DatabaseClient) -> Unit =
        { musicController, db ->
            addPlayerList(musicController, db)
        }

    //增加选中音乐到歌单
    val onAddPlaylistSelect: () -> Unit = {
        AddPlaylistBottomData(
            ifShow = true,
            musicInfoList = selectMusicIdList.map { it },
            onItemClick = {
                dismiss()
            }).show()
    }

    //从歌单中删除选中音乐
    val onRemovePlaylistMusic: (DataSourceManager, CoroutineScope) -> Unit =
        { dataSourceManager, viewModelScope ->
            removePlaylistMusic(dataSourceManager, viewModelScope)
        }

    //取消收藏
    val onRemoveFavorite: (DataSourceManager, CoroutineScope, MusicController) -> Unit =
        { dataSourceManager, viewModelScope, musicController ->
            viewModelScope.launch {
                OperationTipUtils.operationTipProgress() { loadingObject ->
                    loadingObject.updateProgress(0.0f, 0)
                    selectMusicIdList
                        .forEachIndexed { index, musicId ->
                            dataSourceManager.setFavoriteData(
                                MusicTypeEnum.MUSIC,
                                musicId,
                                musicController,
                                true
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

    //状态变化
    var onOpenChange: ((Boolean) -> Unit)? = null

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

    fun dismiss() {
        clearData()
    }

    private fun setData(
        ifOpenSelect: Boolean,
        playlistId: String? = null,
        ifEnableButton: Boolean,
        ifPlaylist: Boolean,
        ifLocal: Boolean = false
    ) {
//        this.ifOpenSelect = ifOpenSelect

        _uiState.update { ifOpenSelect }
        if (ifPlaylist)
            this.playlistId = playlistId
        else
            this.playlistId = null

        this.ifEnableButton = ifEnableButton
        this.ifPlaylist = ifPlaylist
        this.ifLocal = ifLocal
    }

    fun clearData() {
        selectMusicIdList.clear()
        isSelectAll = false
//        ifOpenSelect = false
        _uiState.update { false }
        ifEnableButton = false
        this.ifPlaylist = false
        onOpenChange?.invoke(false)
    }

    /**
     * 设置选择音乐列表数据
     * @param [music] 音乐
     */
    fun toggleSelection(musicId: String, onIsSelectAll: () -> Boolean) {

        if (selectMusicIdList.contains(musicId)) {
            selectMusicIdList.remove(musicId)
            if (selectMusicIdList.isEmpty()) {
                ifEnableButton = false
            }
            isSelectAll = false
        } else {
            selectMusicIdList.add(musicId)
            ifEnableButton = true
            isSelectAll = onIsSelectAll()
        }
    }

    /**
     * 删除选中数据
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
    suspend fun addPlayerList(musicController: MusicController, db: DatabaseClient) {
        val xyMusics = db.musicDao.selectExtendByIds(selectMusicIdList.toList())
        musicController.addMusicList(
            musicList = xyMusics,
            isPlayer = true,
        )
        clearData()
    }

    fun toggleSelectionAll(musicIdList: List<String>? = null) {
        if (isSelectAll) {
            selectMusicIdList.clear()
            isSelectAll = false
            ifEnableButton = false
        } else {
            if (!musicIdList.isNullOrEmpty()) {
                ifEnableButton = true
                isSelectAll = true
                selectMusicIdList.addAll(musicIdList)
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

    //判断是否选择列表是否为空
    fun ifSelectEmpty(): Boolean {
        return selectMusicIdList.isEmpty()
    }
}