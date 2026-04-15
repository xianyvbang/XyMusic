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
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.select.SelectControl
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.SelectSortBottomSheetComponent
import cn.xybbz.ui.components.SwipeRefreshListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.XySelectAllComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.MusicViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.close_24px
import xymusic_kmp.composeapp.generated.resources.close_selection
import xymusic_kmp.composeapp.generated.resources.music
import xymusic_kmp.composeapp.generated.resources.open_selection_function
import xymusic_kmp.composeapp.generated.resources.playlist_add_check_24px
import xymusic_kmp.composeapp.generated.resources.random_play
import xymusic_kmp.composeapp.generated.resources.return_home
import xymusic_kmp.composeapp.generated.resources.shuffle_24px
import cn.xybbz.ui.xy.XyIconButton as IconButton

@Composable
fun JvmMusicScreen(
    musicViewModel: MusicViewModel = koinViewModel<MusicViewModel>(),
) {

    val coroutineScope = rememberCoroutineScope()
    val mainViewModel = LocalMainViewModel.current
    val homeMusicPager = musicViewModel.listPage.collectAsLazyPagingItems()
    val favoriteSet by musicViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val downloadMusicIds by musicViewModel.downloadMusicIdsFlow.collectAsStateWithLifecycle(
        emptyList()
    )
    val sortBy by musicViewModel.sortBy.collectAsStateWithLifecycle()
    val ifOpenSelect by musicViewModel.selectControl.uiState.collectAsStateWithLifecycle()

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
            ifOpenSelect = ifOpenSelect,
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

        SwipeRefreshListComponent(
            collectAsLazyPagingItems = homeMusicPager
        ) { page ->
            items(
                page.itemCount,
                page.itemKey { it.itemId },
                page.itemContentType { MusicTypeEnum.MUSIC.code }) { index ->
                // 加上remember就可以防止重组
                page[index]?.let { music ->
                    MusicItemComponent(
                        music = music,
                        onIfFavorite = {
                            music.itemId in favoriteSet
                        },
                        ifDownload = music.itemId in downloadMusicIds,
                        ifPlay = musicViewModel.musicController.musicInfo?.itemId == music.itemId,
                        onMusicPlay = {
                            musicViewModel.musicPlayContext.music(
                                onMusicPlayParameter = it,
                                index = index,
                                type = MusicPlayTypeEnum.FOUNDATION
                            )
                        },
                        backgroundColor = Color.Transparent,
                        ifSelect = ifOpenSelect,
                        ifSelectCheckBox = {
                            music.itemId in musicViewModel.selectControl.selectMusicIdList
                        },
                        trailingOnSelectClick = { select ->
                            Log.i("======", "数据是否一起变化${select}")
                            musicViewModel.selectControl.toggleSelection(
                                music.itemId,
                                onIsSelectAll = {
                                    musicViewModel.selectControl.selectMusicIdList.containsAll(
                                        homeMusicPager.itemSnapshotList.items.map { it.itemId }
                                    )
                                })
                        },
                        trailingOnClick = {
                            coroutineScope.launch {
                                musicViewModel.getMusicInfoById(music.itemId)?.show()
                            }
                        }
                    )
                }
            }
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
    selectControl: SelectControl,
    sortOrFilterContent: @Composable () -> Unit
) {
    val navigator = LocalNavigator.current

    TopAppBarComponent(
        modifier = modifier,
        title = {
            if (ifOpenSelect) {

                XySelectAllComponent(
                    isSelectAll = selectControl.isSelectAll,
                    onSelectAll = onSelectAll
                )
            } else {
                TopAppBarTitle(
                    title = title
                )
            }
        }, navigationIcon = {
            IconButton(
                onClick = {
                    navigator.goBack()
                },
            ) {
                Icon(
                    painter = painterResource(Res.drawable.arrow_back_24px),
                    contentDescription = stringResource(Res.string.return_home)
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

