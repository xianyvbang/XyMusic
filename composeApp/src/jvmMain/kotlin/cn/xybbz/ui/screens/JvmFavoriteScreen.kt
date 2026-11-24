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


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.components.JvmLazyListComponent
import cn.xybbz.ui.components.JvmMusicSelectionPanel
import cn.xybbz.ui.components.JvmPageTitleBar
import cn.xybbz.ui.components.JvmSelectionToggleButton
import cn.xybbz.ui.components.SongTableColumns
import cn.xybbz.ui.components.rememberMusicArtistInfoLoader
import cn.xybbz.ui.components.rememberMusicArtistClickHandler
import cn.xybbz.ui.components.show
import cn.xybbz.ui.components.songTableItems
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.FavoriteViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.my_favorites

private val JvmFavoriteMusicTableColumns = SongTableColumns(
    showFavoriteColumn = true,
    showInlineActions = true,
    showAlbumColumn = true,
    showMetaColumn = false,
)

/**
 * 收藏界面
 */
@Composable
fun JvmFavoriteScreen(
    favoriteViewModel: FavoriteViewModel = koinViewModel<FavoriteViewModel>(),
    dataSourceManager: DataSourceManager = koinInject(),
) {
    val favoriteMusicListPage =
        favoriteViewModel.favoriteMusicList.collectAsLazyPagingItems()
    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    val artistInfoLoader = rememberMusicArtistInfoLoader(dataSourceManager)
    val artistClickHandler = rememberMusicArtistClickHandler(
        artistList = artistInfoLoader.artistList,
        onLoadArtistInfos = artistInfoLoader.loadArtistInfos,
    )
    val favoriteList by favoriteViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    // 已下载完成的音乐 ID 集合，用于行内展示已下载图标并禁止重复下载
    val downloadMusicIds by favoriteViewModel.downloadMusicIdsFlow.collectAsStateWithLifecycle(emptyList())
    // 批量选择状态：决定选择入口按钮文案与列表行内选择列
    val selectUiState by favoriteViewModel.selectControl.uiState.collectAsStateWithLifecycle()
    val currentPlayingMusicIdFlow = remember(favoriteViewModel) {
        favoriteViewModel.musicController.musicInfoFlow.map { musicInfo ->
            musicInfo?.itemId
        }
    }


    XyColumnScreen {
        // 与设置页、连接管理页共用统一页头，右侧提供批量选择入口
        JvmPageTitleBar(
            title = stringResource(Res.string.my_favorites),
            trailing = {
                JvmSelectionToggleButton(
                    isSelecting = selectUiState.isOpen,
                    onClick = {
                        if (selectUiState.isOpen) {
                            favoriteViewModel.selectControl.dismiss()
                        } else {
                            // 批量操作由列表内的选择操作栏承载
                            favoriteViewModel.selectControl.show(ifOpenSelect = true)
                        }
                    },
                )
            },
        )
        JvmLazyListComponent(
            pagingItems = favoriteMusicListPage,
        ) {
            // 选择模式展开的批量操作栏，紧跟在页头下方
            item(key = "favorite_selection_bar") {
                JvmMusicSelectionPanel(
                    visible = selectUiState.isOpen,
                    selectControl = favoriteViewModel.selectControl,
                    allMusicIds = {
                        favoriteMusicListPage.itemSnapshotList.items.map { it.itemId }
                    },
                    dataSourceManager = favoriteViewModel.dataSourceManager,
                    musicPlayContext = favoriteViewModel.musicPlayContext,
                    db = favoriteViewModel.db,
                    downloadDb = favoriteViewModel.downloadDb,
                    musicController = favoriteViewModel.musicController,
                    downloaderManager = favoriteViewModel.downloaderManager,
                )
            }
            songTableItems(
                tableKey = "favorite_music",
                pagingItems = favoriteMusicListPage,
                columns = JvmFavoriteMusicTableColumns.copy(
                    showInlineActions = !selectUiState.isOpen,
                    showSelectionColumn = selectUiState.isOpen,
                ),
                ifFavorite = { music -> favoriteList.contains(music.itemId) },
                ifDownloaded = { music -> music.itemId in downloadMusicIds },
                currentPlayingMusicIdFlow = currentPlayingMusicIdFlow,
                isSelected = { music -> music.itemId in selectUiState.selectedMusicIds },
                onSongClick = { index, music ->
                    if (selectUiState.isOpen) {
                        // 选择模式下点击整行等价于勾选/取消勾选当前歌曲
                        favoriteViewModel.selectControl.toggleSelection(
                            music.itemId,
                            onIsSelectAll = {
                                selectUiState.selectedMusicIds.containsAll(
                                    favoriteMusicListPage.itemSnapshotList.items.map { it.itemId }
                                )
                            }
                        )
                    } else {
                        favoriteViewModel.musicPlayContext.favorite(
                            OnMusicPlayParameter(
                                musicId = music.itemId,
                                albumId = music.album,
                            ),
                            index = index,
                        )
                    }
                },
                onOpenArtist = artistClickHandler::openMusicArtists,
                onOpenAlbum = { music ->
                    if (music.album.isNotBlank()) {
                        navigator.navigate(
                            AlbumInfo(
                                music.album,
                                MusicDataTypeEnum.ALBUM,
                            )
                        )
                    }
                },
                onFavoriteClick = { music ->
                    favoriteViewModel.musicController.invokingOnFavorite(music.itemId)
                },
                onDownloadClick = { music ->
                    favoriteViewModel.downloadMusic(music)
                },
                onMoreClick = { music ->
                    coroutineScope.launch {
                        favoriteViewModel.getMusicInfo(music.itemId)?.show()
                    }
                },
                // 点击行尾勾选框同样切换选中状态
                onSelectionClick = { musicId ->
                    favoriteViewModel.selectControl.toggleSelection(
                        musicId,
                        onIsSelectAll = {
                            selectUiState.selectedMusicIds.containsAll(
                                favoriteMusicListPage.itemSnapshotList.items.map { it.itemId }
                            )
                        }
                    )
                },
            )
        }
    }
}
