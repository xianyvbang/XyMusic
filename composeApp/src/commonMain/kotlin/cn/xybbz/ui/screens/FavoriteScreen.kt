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

package cn.xybbz.ui.screens


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.SwipeRefreshListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.XySelectAllComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.FavoriteViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.arrow_back_24px
import xymusic.composeapp.generated.resources.close_24px
import xymusic.composeapp.generated.resources.close_selection
import xymusic.composeapp.generated.resources.my_favorites
import xymusic.composeapp.generated.resources.open_selection_function
import xymusic.composeapp.generated.resources.playlist_add_check_24px
import xymusic.composeapp.generated.resources.return_home
import cn.xybbz.ui.xy.XyIconButton as IconButton

/**
 * 收藏界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    favoriteViewModel: FavoriteViewModel = koinViewModel<FavoriteViewModel>(),
) {
    val favoriteMusicListPage =
        favoriteViewModel.favoriteMusicList.collectAsLazyPagingItems()
    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    val musicInfo by favoriteViewModel.musicController.musicInfoFlow.collectAsStateWithLifecycle()
    val favoriteList by favoriteViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val downloadMusicIdList by favoriteViewModel.downloadMusicIdsFlow.collectAsStateWithLifecycle(
        emptyList()
    )
    // 批量选择状态：决定顶部栏形态与列表行的勾选展示
    val selectUiState by favoriteViewModel.selectControl.uiState.collectAsStateWithLifecycle()


    XyColumnScreen {
        TopAppBarComponent(
            title = {
                if (selectUiState.isOpen) {
                    // 选择模式下标题区域切换为全选控件，与音乐列表页保持一致
                    XySelectAllComponent(
                        isSelectAll = selectUiState.isSelectAll,
                        onSelectAll = {
                            favoriteViewModel.selectControl.toggleSelectionAll(
                                favoriteMusicListPage.itemSnapshotList.items.map { it.itemId }
                            )
                        }
                    )
                } else {
                    TopAppBarTitle(
                        title = stringResource(Res.string.my_favorites)
                    )
                }
            }, navigationIcon = {
                IconButton(onClick = composeClick { navigator.goBack() }) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_back_24px),
                        contentDescription = stringResource(Res.string.return_home)
                    )
                }
            }, actions = {
                if (selectUiState.isOpen) {
                    // 选择模式下右侧只保留退出选择按钮
                    IconButton(onClick = {
                        favoriteViewModel.selectControl.dismiss()
                    }) {
                        Icon(
                            painter = painterResource(Res.drawable.close_24px),
                            contentDescription = stringResource(Res.string.close_selection)
                        )
                    }
                } else {
                    // 普通模式下提供批量选择入口
                    IconButton(onClick = {
                        favoriteViewModel.selectControl.show(ifOpenSelect = true)
                    }) {
                        Icon(
                            painter = painterResource(Res.drawable.playlist_add_check_24px),
                            contentDescription = stringResource(Res.string.open_selection_function)
                        )
                    }
                }
            })
        SwipeRefreshListComponent(
            collectAsLazyPagingItems = favoriteMusicListPage,
            listContent = { collectAsLazyPagingItems ->
                items(
                    collectAsLazyPagingItems.itemCount,
                    key = collectAsLazyPagingItems.itemKey { item -> item.itemId },
                    contentType = collectAsLazyPagingItems.itemContentType { MusicTypeEnum.MUSIC }
                ) { index ->
                    collectAsLazyPagingItems[index]?.let { music ->
                        MusicItemComponent(
                            music = music,
                            onIfFavorite = {
                                favoriteList.contains(music.itemId)
                            },
                            ifDownload = music.itemId in downloadMusicIdList,
                            ifPlay = musicInfo?.itemId == music.itemId,
                            backgroundColor = Color.Transparent,
                            // 选择模式下展示勾选框，并仅在未选择时响应播放点击
                            ifSelect = selectUiState.isOpen,
                            ifSelectCheckBox = {
                                music.itemId in selectUiState.selectedMusicIds
                            },
                            trailingOnSelectClick = {
                                favoriteViewModel.selectControl.toggleSelection(
                                    music.itemId,
                                    onIsSelectAll = {
                                        selectUiState.selectedMusicIds.containsAll(
                                            favoriteMusicListPage.itemSnapshotList.items.map { it.itemId }
                                        )
                                    }
                                )
                            },
                            onMusicPlay = {

                                favoriteViewModel.musicPlayContext.favorite(
                                    it,
                                    index = index
                                )

                            },
                            trailingOnClick = {
                                coroutineScope.launch {
                                    favoriteViewModel.getMusicInfo(music.itemId)?.show()
                                }
                            }
                        )
                    }
                }
            }
        )
    }
}


