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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.select.SelectControl
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.components.JvmLazyListComponent
import cn.xybbz.ui.components.JvmPageTitleBar
import cn.xybbz.ui.components.JvmSettingPageHeaderPadding
import cn.xybbz.ui.components.SelectSortBottomSheetComponent
import cn.xybbz.ui.components.SongTableColumns
import cn.xybbz.ui.components.XySelectAllComponent
import cn.xybbz.ui.components.rememberMusicArtistInfoLoader
import cn.xybbz.ui.components.rememberMusicArtistClickHandler
import cn.xybbz.ui.components.show
import cn.xybbz.ui.components.songTableItems
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.MusicViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.close_24px
import xymusic.composeapp.generated.resources.close_selection
import xymusic.composeapp.generated.resources.music
import xymusic.composeapp.generated.resources.open_selection_function
import xymusic.composeapp.generated.resources.playlist_add_check_24px
import xymusic.composeapp.generated.resources.random_play
import xymusic.composeapp.generated.resources.shuffle_24px
import cn.xybbz.ui.xy.XyIconButton as IconButton

private val JvmMusicTableColumns = SongTableColumns(
    showFavoriteColumn = true,
    showInlineActions = true,
    showAlbumColumn = true,
    showMetaColumn = false,
)

@Composable
fun JvmMusicScreen(
    musicViewModel: MusicViewModel = koinViewModel<MusicViewModel>(),
) {

    val coroutineScope = rememberCoroutineScope()
    val mainViewModel = LocalMainViewModel.current
    val navigator = LocalNavigator.current
    val artistInfoLoader = rememberMusicArtistInfoLoader(musicViewModel.dataSourceManager)
    val artistClickHandler = rememberMusicArtistClickHandler(
        artistList = artistInfoLoader.artistList,
        onLoadArtistInfos = artistInfoLoader.loadArtistInfos,
    )
    val homeMusicPager = musicViewModel.listPage.collectAsLazyPagingItems()
    val favoriteSet by musicViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val sortBy by musicViewModel.sortBy.collectAsStateWithLifecycle()
    val selectUiState by musicViewModel.selectControl.uiState.collectAsStateWithLifecycle()
    val currentPlayingMusicIdFlow = remember(musicViewModel) {
        musicViewModel.musicController.musicInfoFlow.map { musicInfo ->
            musicInfo?.itemId
        }
    }

    XyColumnScreen {
        MusicSelectTopBarComponent(
            modifier = Modifier,
            title = stringResource(Res.string.music),
            musicViewModel = musicViewModel,
            onIfShowMusicDropdownMenu = { musicViewModel.dataSourceManager.dataSourceType?.ifShowMusicDropdownMenu == true },
            onRandomPlayerClick = {
                coroutineScope.launch {
                    musicViewModel.musicPlayContext.randomMusic(
                        onMusicPlayParameter = OnMusicPlayParameter(musicId = "")
                    )
                }
            },
            onSelectAll = {
                coroutineScope.launch {
                    musicViewModel.selectControl.toggleSelectionAll(homeMusicPager.itemSnapshotList.items.map { it.itemId })
                }
            },
            selectControl = musicViewModel.selectControl,
            ifOpenSelect = selectUiState.isOpen,
            isSelectAll = selectUiState.isSelectAll,
            sortOrFilterContent = {
                SelectSortBottomSheetComponent(
                    onIfSelectOneYear = { musicViewModel.dataSourceManager.dataSourceType?.ifMusicSelectOneYear },
                    onIfStartEndYear = { musicViewModel.dataSourceManager.dataSourceType?.ifMusicSelectStartEndYear },
                    onIfSort = { musicViewModel.dataSourceManager.dataSourceType?.ifMusicSort },
                    onIfFavoriteFilter = { musicViewModel.dataSourceManager.dataSourceType?.ifMusicFavoriteFilter },
                    onSortTypeClick = {
                        musicViewModel.setSortedData(it) { homeMusicPager.refresh() }
                    },
                    onSortType = { sortBy.sortType },
                    onDefaultSortType = { musicViewModel.defaultSortType },
                    onIfFavorite = { sortBy.isFavorite == true },
                    setFavorite = { musicViewModel.setFavorite(it) { homeMusicPager.refresh() } },
                    onYearSet = { mainViewModel.yearSet },
                    onSelectYear = { sortBy.yearList?.get(0) },
                    onSetSelectYear = { year ->
                        year?.let {
                            musicViewModel.setFilterYear(
                                listOf(it)
                            ) { homeMusicPager.refresh() }
                        }
                    },
                    onSelectRangeYear = { sortBy.yearList },
                    onSetSelectRangeYear = { years ->
                        musicViewModel.setFilterYear(
                            years.mapNotNull { it }
                        ) { homeMusicPager.refresh() }
                    },
                    onEnabledClearClick = { musicViewModel.isSortChange() }
                ) {
                    musicViewModel.clearFilterOrSort { homeMusicPager.refresh() }
                }
            }
        )

        JvmLazyListComponent(
            pagingItems = homeMusicPager,
        ) {
            songTableItems(
                tableKey = "music_screen",
                pagingItems = homeMusicPager,
                columns = JvmMusicTableColumns.copy(
                    showInlineActions = !selectUiState.isOpen,
                    showSelectionColumn = selectUiState.isOpen,
                ),
                ifFavorite = { music -> music.itemId in favoriteSet },
                currentPlayingMusicIdFlow = currentPlayingMusicIdFlow,
                isSelected = { music -> music.itemId in selectUiState.selectedMusicIds },
                onSongClick = { index, music ->
                    if (selectUiState.isOpen) {
                        musicViewModel.selectControl.toggleSelection(
                            music.itemId,
                            onIsSelectAll = {
                                selectUiState.selectedMusicIds.containsAll(
                                    homeMusicPager.itemSnapshotList.items.map { it.itemId }
                                )
                            }
                        )
                    } else {
                        musicViewModel.musicPlayContext.music(
                            onMusicPlayParameter = OnMusicPlayParameter(musicId = music.itemId),
                            index = index,
                            type = MusicPlayTypeEnum.FOUNDATION
                        )
                    }
                },
                onOpenArtist = { music ->
                    coroutineScope.launch {
                        artistClickHandler.openMusicArtists(
                            musicViewModel.getMusicInfoById(music.itemId) ?: music
                        )
                    }
                },
                onOpenAlbum = { music ->
                    if (music.album.isNotBlank()) {
                        navigator.navigate(
                            AlbumInfo(
                                music.album,
                                MusicDataTypeEnum.ALBUM
                            )
                        )
                    }
                },
                onFavoriteClick = { music ->
                    musicViewModel.musicController.invokingOnFavorite(music.itemId)
                },
                onDownloadClick = { music ->
                    musicViewModel.downloadMusic(music)
                },
                onMoreClick = { music ->
                    coroutineScope.launch {
                        musicViewModel.getMusicInfoById(music.itemId)?.show()
                    }
                },
                onSelectionClick = { musicId ->
                    musicViewModel.selectControl.toggleSelection(
                        musicId,
                        onIsSelectAll = {
                            selectUiState.selectedMusicIds.containsAll(
                                homeMusicPager.itemSnapshotList.items.map { it.itemId }
                            )
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun MusicSelectTopBarComponent(
    modifier: Modifier,
    title: String,
    musicViewModel: MusicViewModel,
    onIfShowMusicDropdownMenu: () -> Boolean,
    onRandomPlayerClick: () -> Unit,
    onSelectAll: () -> Unit,
    ifOpenSelect: Boolean,
    isSelectAll: Boolean,
    selectControl: SelectControl,
    sortOrFilterContent: @Composable () -> Unit
) {
    if (ifOpenSelect) {
        // 选择模式下标题区域切换为全选控件，右侧只保留退出选择按钮，
        // 内边距与统一页头一致，避免切换模式时标题位置发生跳动。
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(JvmSettingPageHeaderPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            XySelectAllComponent(
                isSelectAll = isSelectAll,
                onSelectAll = onSelectAll
            )
            IconButton(onClick = {
                selectControl.dismiss()
            }) {
                Icon(
                    painter = painterResource(Res.drawable.close_24px),
                    contentDescription = stringResource(Res.string.close_selection)
                )
            }
        }
    } else {
        // 普通模式下与设置页、连接管理页共用统一页头，操作按钮放在标题右侧
        JvmPageTitleBar(
            title = title,
            modifier = modifier,
            trailing = {
                IconButton(onClick = composeClick {
                    onRandomPlayerClick()
                }) {
                    Icon(
                        painter = painterResource(Res.drawable.shuffle_24px),
                        contentDescription = stringResource(Res.string.random_play)
                    )
                }

                IconButton(onClick = {
                    musicViewModel.selectControl.show(ifOpenSelect = true)
                }) {
                    Icon(
                        painter = painterResource(Res.drawable.playlist_add_check_24px),
                        contentDescription = stringResource(Res.string.open_selection_function)
                    )
                }
                if (onIfShowMusicDropdownMenu()) {
                    sortOrFilterContent()
                }
            },
        )
    }
}

