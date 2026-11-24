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

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import cn.xybbz.ui.components.JvmMusicSelectionPanel
import cn.xybbz.ui.components.JvmPageTitleBar
import cn.xybbz.ui.components.JvmSelectionToggleButton
import cn.xybbz.ui.components.SelectSortBottomSheetComponent
import cn.xybbz.ui.components.SongTableColumns
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
import xymusic.composeapp.generated.resources.music
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
    // 已下载完成的音乐 ID 集合，用于行内展示已下载图标并禁止重复下载
    val downloadMusicIds by musicViewModel.downloadMusicIdsFlow.collectAsStateWithLifecycle(emptyList())
    val sortBy by musicViewModel.sortBy.collectAsStateWithLifecycle()
    val selectUiState by musicViewModel.selectControl.uiState.collectAsStateWithLifecycle()
    val currentPlayingMusicIdFlow = remember(musicViewModel) {
        musicViewModel.musicController.musicInfoFlow.map { musicInfo ->
            musicInfo?.itemId
        }
    }

    val musicListState = rememberLazyListState()

    // 顶部标题普通模式下随列表滚动并做折叠动画，选择模式下则需要固定在页面顶部，
    // 提取成同一个 Composable 便于两处复用。
    val musicTopBar: @Composable (Modifier) -> Unit = { topBarModifier ->
        MusicSelectTopBarComponent(
            modifier = topBarModifier,
            title = stringResource(Res.string.music),
            onIfShowMusicDropdownMenu = { musicViewModel.dataSourceManager.dataSourceType?.ifShowMusicDropdownMenu == true },
            onRandomPlayerClick = {
                coroutineScope.launch {
                    musicViewModel.musicPlayContext.randomMusic(
                        onMusicPlayParameter = OnMusicPlayParameter(musicId = "")
                    )
                }
            },
            selectControl = musicViewModel.selectControl,
            ifOpenSelect = selectUiState.isOpen,
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
    }

    XyColumnScreen {
        // 标题固定在列表外，列表内容在普通态与选择态下保持同一套项结构，
        // 避免开关选择时 LazyColumn 的滚动锚点在“标题/表头”之间切换导致标题被顶出视口
        musicTopBar(Modifier)
        // 选择操作栏固定在页头下方，随页面一起展示而不会跟随列表滚动被顶出视口；
        // 非选择态不占高度，底部播放栏始终展示播放控制。
        JvmMusicSelectionPanel(
            visible = selectUiState.isOpen,
            selectControl = musicViewModel.selectControl,
            allMusicIds = {
                homeMusicPager.itemSnapshotList.items.map { it.itemId }
            },
            dataSourceManager = musicViewModel.dataSourceManager,
            musicPlayContext = musicViewModel.musicPlayContext,
            db = musicViewModel.db,
            downloadDb = musicViewModel.downloadDb,
            musicController = musicViewModel.musicController,
            downloaderManager = musicViewModel.downloaderManager,
        )

        JvmLazyListComponent(
            lazyState = musicListState,
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
                ifDownloaded = { music -> music.itemId in downloadMusicIds },
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
                },
                // 歌曲表头吸附固定在列表顶部，滚动时保持可见
                stickyTableHeader = true,
            )
        }
    }
}

@Composable
private fun MusicSelectTopBarComponent(
    modifier: Modifier,
    title: String,
    onIfShowMusicDropdownMenu: () -> Boolean,
    onRandomPlayerClick: () -> Unit,
    ifOpenSelect: Boolean,
    selectControl: SelectControl,
    sortOrFilterContent: @Composable () -> Unit
) {
    if (ifOpenSelect) {
        // 选择模式下与艺术家/专辑详情页保持一致：页头保留标题，右侧只保留「完成」，
        // 全选与批量操作统一由列表内的选择操作栏承载。
        JvmPageTitleBar(
            title = title,
            modifier = modifier,
            trailing = {
                JvmSelectionToggleButton(
                    isSelecting = true,
                    onClick = {
                        selectControl.dismiss()
                    },
                )
            },
        )
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

                JvmSelectionToggleButton(
                    isSelecting = false,
                    onClick = {
                        // 批量操作由列表内的选择操作栏承载
                        selectControl.show(ifOpenSelect = true)
                    },
                )
                if (onIfShowMusicDropdownMenu()) {
                    sortOrFilterContent()
                }
            },
        )
    }
}

