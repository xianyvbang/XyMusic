package cn.xybbz.entity.data

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import cn.xybbz.R
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.common.utils.OperationTipUtils
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.ui.components.AddPlaylistBottomData
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.show
import cn.xybbz.ui.xy.XyItemTextHorizontal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


/**
 * 选择列表数据
 * @author 刘梦龙
 * @date 2025/01/18
 * @constructor 创建[SelectControl]
 */
@Immutable
class SelectControl {


    //选中音乐列表
    val selectMusicDataList = mutableStateSetOf<XyMusic>()

    var isSelectAll by mutableStateOf(false)
        private set

    var musicList by mutableStateOf<List<XyMusic>?>(null)
        private set

    //是否显示按钮
    var ifOpenSelect by mutableStateOf(false)
        private set

    //是否启用按钮
    var ifEnableButton by mutableStateOf(false)
        private set

    var itemId: String? by mutableStateOf(null)
        private set

    //是否在歌单中操作
    var ifPlaylist by mutableStateOf(false)
        private set

    val scope = CoroutineScopeUtils.getIo("SelectControl")

    //永久删除所选音乐资源-从硬盘上删除
    val onRemoveSelectListResource: ((IDataSourceManager, CoroutineScope) -> Unit) =
        { dataSourceManager, viewModelScope ->
            AlertDialogObject(
                title = R.string.delete_permanently,
                content = {
                    XyItemTextHorizontal(
                        text = stringResource(R.string.delete_warning)
                    )
                },
                ifWarning = true,
                onConfirmation = {
                    removeSelectListResource(dataSourceManager, viewModelScope)
                }, onDismissRequest = {}).show()
        }

    //增加选中音乐到播放列表
    val onAddPlaySelect: (MusicController) -> Unit = {
        addPlayerList(it)
    }

    //增加选中音乐到歌单
    val onAddPlaylistSelect: () -> Unit = {
        AddPlaylistBottomData(
            ifShow = true,
            musicInfoList = selectMusicDataList.map { it },
            onItemClick = {
                dismiss()
            }).show()
    }

    //从歌单中删除选中音乐
    val onRemovePlaylistMusic: (IDataSourceManager, CoroutineScope) -> Unit =
        { dataSourceManager, viewModelScope ->
            removePlaylistMusic(dataSourceManager, viewModelScope)
        }

    //取消收藏
    val onRemoveFavorite: (IDataSourceManager, CoroutineScope, MusicController) -> Unit =
        { dataSourceManager, viewModelScope, musicController ->
            viewModelScope.launch {
                OperationTipUtils.operationTipProgress() { loadingObject ->
                    loadingObject.updateProgress(0.0f, 0)
                    selectMusicDataList.filter { item -> item.ifFavoriteStatus }
                        .forEachIndexed { index, music ->
                            dataSourceManager.setFavoriteData(
                                MusicTypeEnum.MUSIC,
                                music.itemId,
                                musicController,
                                true
                            )
                            loadingObject.updateProgress(
                                (index * 1.0f + 1.0f) / selectMusicDataList.size,
                                index + 1
                            )
                        }
                }
                dismiss()
            }
        }

    //状态变化
    var onOpenChange: ((Boolean) -> Unit)? = null

    constructor(ifOpenSelect: Boolean) : this() {
        this.ifOpenSelect = ifOpenSelect
    }

    constructor(ifOpenSelect: Boolean, itemId: String) : this() {
        this.ifOpenSelect = ifOpenSelect
        this.itemId = itemId
    }

    constructor()

    fun show(ifOpenSelect: Boolean, ifPlaylist: Boolean = false, onOpenChange: ((Boolean) -> Unit)? = null) {
        setData(ifOpenSelect, false, ifPlaylist)
        this.onOpenChange = onOpenChange
    }

    fun dismiss() {
        clearData()
    }

    fun setData(ifOpenSelect: Boolean, ifEnableButton: Boolean, ifPlaylist: Boolean) {
        this.ifOpenSelect = ifOpenSelect
        this.ifEnableButton = ifEnableButton
        this.ifPlaylist = ifPlaylist
    }

    fun clearData() {
        scope.launch {
            selectMusicDataList.clear()
        }
        isSelectAll = false
        ifOpenSelect = false
        ifEnableButton = false
        this.ifPlaylist = false
        onOpenChange?.invoke(false)
    }

    /**
     * 设置选择音乐列表数据
     * @param [music] 音乐
     */
    fun setSelectMusicListData(music: XyMusic) {
        if (!selectMusicDataList.any { it.itemId == music.itemId }) {
            selectMusicDataList.add(music)
            ifEnableButton = true
        }
    }
    /**
     * 删除选中数据
     */
    fun removeSelectListResource(
        dataSourceManager: IDataSourceManager,
        viewModelScope: CoroutineScope
    ) {
        if (selectMusicDataList.isNotEmpty()) {
            viewModelScope.launch {
                dataSourceManager.removeMusicByIds(selectMusicDataList.map { it.itemId })
            }.invokeOnCompletion {
                dismiss()
            }
        }

    }


    /**
     * 删除选择音乐 ID
     * @param [itemId] 商品编号
     */
    fun removeSelectMusicId(itemId: String) {
        selectMusicDataList.removeIf { it.itemId == itemId }
        if (selectMusicDataList.isEmpty()) {
            ifEnableButton = false
        }
    }


    /**
     * 播放选中列表
     */
    fun addPlayerList(musicController: MusicController) {
        musicController.addMusicList(
            musicList = selectMusicDataList.toList(),
            isPlayer = true,
        )
        clearData()
    }

    fun toggleSelectionAll(musicList: List<XyMusic>? = null) {
        if (isSelectAll) {
            selectMusicDataList.clear()
            isSelectAll = false
            ifEnableButton = false
        } else {
            if (!musicList.isNullOrEmpty()){
                ifEnableButton = true
                isSelectAll = true
                selectMusicDataList.addAll(musicList)
            }
        }
    }


    /**
     * 从歌单中移除相应音乐
     */
    fun removePlaylistMusic(
        dataSourceManager: IDataSourceManager,
        viewModelScope: CoroutineScope
    ) {
        viewModelScope.launch {
            if (selectMusicDataList.isNotEmpty()) {
                itemId?.let {
                    dataSourceManager.removeMusicPlaylist(
                        it,
                        selectMusicDataList.map { it.itemId }
                    )
                }
            }
        }.invokeOnCompletion {
            dismiss()
        }
    }

    //判断是否选择列表是否为空
    fun ifSelectEmpty(): Boolean {
        return selectMusicDataList.isEmpty()
    }
}