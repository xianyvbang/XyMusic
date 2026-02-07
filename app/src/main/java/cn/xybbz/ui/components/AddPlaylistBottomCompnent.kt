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

package cn.xybbz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.ModalBottomSheetExtendComponent
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.viewmodel.PlaylistBottomViewModel
import kotlinx.coroutines.launch

val playlistObject by mutableStateOf(
    AddPlaylistBottomData(
        ifShow = false,
        musicInfoList = emptyList()
    )
)

@Immutable
class AddPlaylistBottomData() {
    var ifShow: Boolean by mutableStateOf(false)
    var onClose: (suspend (Boolean) -> Unit)? = null
    var onItemClick: (suspend (Boolean) -> Unit)? = null
    var musicIdList: List<String> by mutableStateOf(emptyList())

    constructor(
        ifShow: Boolean = false,
        onClose: (suspend (Boolean) -> Unit)? = null,
        onItemClick: (suspend (Boolean) -> Unit)? = null,
        musicInfoList: List<String>
    ) : this() {
        this.ifShow = ifShow
        this.onClose = onClose
        this.musicIdList = musicInfoList
        this.onItemClick = onItemClick
    }

    /**
     * 将另外一个对象复制到新对象中
     */
    fun thisCopyData(data: AddPlaylistBottomData) {
        this.ifShow = data.ifShow
        this.onClose = data.onClose
        this.musicIdList = data.musicIdList
        this.onItemClick = data.onItemClick
    }

    /**
     * 关闭显示
     */
    fun dismiss() {
        this.ifShow = false
        this.musicIdList = emptyList()
        onClose = null
        onItemClick = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaylistBottomComponent(
    playlistBottomViewModel: PlaylistBottomViewModel = hiltViewModel<PlaylistBottomViewModel>()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    ModalBottomSheetExtendComponent(
        modifier = Modifier.statusBarsPadding(),
        bottomSheetState = sheetState,
        onClose = { bool ->
            coroutineScope.launch {
                playlistObject.onClose?.invoke(bool)
            }.invokeOnCompletion {
                playlistObject.dismiss()
            }
        },
        onIfDisplay = { playlistObject.ifShow },
        titleText = stringResource(R.string.add_to_playlist),
        titleTailContent = {
            IconButton(onClick = {
                var playlistName by mutableStateOf(
                    "${context.getString(R.string.new_playlist)}${playlistBottomViewModel.playlists.size}"
                )
                AlertDialogObject(
                    title = context.getString(R.string.new_playlist),
                    content = {
                        XyEdit(text = playlistName, onChange = {
                            playlistName = it
                        })
                    },
                    onDismissRequest = {
                    },
                    onConfirmation = {
                        //新增歌单
                        coroutineScope.launch {
                            //2025年2月7日 13:42:58 增加歌单,修改为服务端增加
                            playlistBottomViewModel.dataSourceManager.addPlaylist(
                                playlistName
                            )
                        }
                    }
                ).show()
            }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.create_playlist)
                )
            }
        }
    ) {

        val ifLoadPlaylist by remember {
            derivedStateOf {
                playlistObject.ifShow && playlistBottomViewModel.isInit
            }
        }

        LaunchedEffect(ifLoadPlaylist) {
            snapshotFlow { ifLoadPlaylist }.collect {
                if (it) {
                    playlistBottomViewModel.getServerPlaylists()
                }
            }
        }

        LazyColumnNotComponent(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2),
        ) {
            itemsIndexed(
                playlistBottomViewModel.playlists,
                key = { _, item -> item.itemId }) { _, item ->
                //歌单信息
                MusicPlaylistItemComponent(
                    modifier = Modifier.padding(horizontal = XyTheme.dimens.outerHorizontalPadding),
                    name = item.name,
                    subordination = "${item.musicCount}${stringResource(R.string.songs_count_suffix)}",
                    imgUrl = item.pic,
//                    brush = null,
                    backgroundColor = Color.Transparent,
                    onClick = {
                        if (playlistObject.musicIdList.isNotEmpty())
                            coroutineScope.launch {
                                sheetState.hide()
                                playlistBottomViewModel.dataSourceManager.saveMusicPlaylist(
                                    playlistId = item.itemId,
                                    musicIds = playlistObject.musicIdList
                                )
                                playlistObject.onClose?.invoke(false)
                                playlistObject.onItemClick?.invoke(false)
                            }.invokeOnCompletion {
                                playlistObject.dismiss()
                            }
                    }
                )
            }
        }
    }
}

fun AddPlaylistBottomData.show() = apply {
    playlistObject.thisCopyData(this)
}