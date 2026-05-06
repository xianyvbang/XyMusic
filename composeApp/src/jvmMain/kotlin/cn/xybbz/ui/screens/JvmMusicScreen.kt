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

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import cn.xybbz.ui.components.SelectSortBottomSheetComponent
import cn.xybbz.ui.components.SongTableColumns
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.XySelectAllComponent
import cn.xybbz.ui.components.rememberMusicArtistClickHandler
import cn.xybbz.ui.components.show
import cn.xybbz.ui.components.songTableItems
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.MusicViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.close_24px
import xymusic_kmp.composeapp.generated.resources.close_selection
import xymusic_kmp.composeapp.generated.resources.music
import xymusic_kmp.composeapp.generated.resources.open_selection_function
import xymusic_kmp.composeapp.generated.resources.playlist_add_check_24px
import xymusic_kmp.composeapp.generated.resources.random_play
import xymusic_kmp.composeapp.generated.resources.shuffle_24px
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
    val artistClickHandler = rememberMusicArtistClickHandler()
    val homeMusicPager = musicViewModel.listPage.collectAsLazyPagingItems()
    val musicInfo by musicViewModel.musicController.musicInfoFlow.collectAsStateWithLifecycle()
    val favoriteSet by musicViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val sortBy by musicViewModel.sortBy.collectAsStateWithLifecycle()
    val selectUiState by musicViewModel.selectControl.uiState.collectAsStateWithLifecycle()

    XyColumnScreen {
        MusicSelectTopBarComponent(
            modifier = Modifier.statusBarsPadding(),
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
                ifPlay = { music -> musicInfo?.itemId == music.itemId },
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
                onDownloadClick = {},
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

@OptIn(ExperimentalMaterial3Api::class)
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

    TopAppBarComponent(
        modifier = modifier,
        title = {
            if (ifOpenSelect) {

                XySelectAllComponent(
                    isSelectAll = isSelectAll,
                    onSelectAll = onSelectAll
                )
            } else {
                TopAppBarTitle(
                    title = title
                )
            }
        }, actions = {
            if (ifOpenSelect) {
                IconButton(onClick = {
                    selectControl.dismiss()
                }) {
                    Icon(
                        painter = painterResource(Res.drawable.close_24px),
                        contentDescription = stringResource(Res.string.close_selection)
                    )
                }
            } else {
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
                if (onIfShowMusicDropdownMenu())
                    sortOrFilterContent()
            }


        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

