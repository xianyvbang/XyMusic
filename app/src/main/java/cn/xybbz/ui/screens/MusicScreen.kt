package cn.xybbz.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistAddCheck
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.R
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.entity.data.SelectControl
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.SelectSortBottomSheetComponent
import cn.xybbz.ui.components.SwipeRefreshListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.XySelectAllComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.MusicViewModel
import kotlinx.coroutines.launch

@Composable
fun MusicScreen(
    musicViewModel: MusicViewModel = hiltViewModel<MusicViewModel>(),
) {

    val coroutineScope = rememberCoroutineScope()
    val mainViewModel = LocalMainViewModel.current
    val homeMusicPager = musicViewModel.homeMusicPager.collectAsLazyPagingItems()
    val favoriteSet by musicViewModel.favoriteRepository.favoriteSet.collectAsState()
    val sortBy by musicViewModel.sortBy.collectAsState()

    XyColumnScreen(
        modifier = Modifier
            .brashColor(
                topVerticalColor = musicViewModel.backgroundConfig.musicBrash[0],
                bottomVerticalColor = musicViewModel.backgroundConfig.musicBrash[1]
            )
    ) {
        MusicSelectTopBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = stringResource(R.string.music),
            musicViewModel = musicViewModel,

            onRandomPlayerClick = {
                coroutineScope.launch {
                    musicViewModel.musicPlayContext.randomMusic(
                        onMusicPlayParameter = OnMusicPlayParameter(musicId = "")
                    )
                }
            },
            onSelectAll = {
                coroutineScope.launch {
                    musicViewModel.getMusicInfoByIds(homeMusicPager.itemSnapshotList.items.map { it.musicId })
                }
            },
            selectControl = musicViewModel.selectControl,
            sortOrFilterContent = {
                SelectSortBottomSheetComponent(
                    onIfYearFilter = { musicViewModel.dataSourceManager.dataSourceType?.ifYearFilter },
                    onIfSelectOneYear = { musicViewModel.dataSourceManager.dataSourceType?.ifMusicSelectOneYear },
                    onIfStartEndYear = { musicViewModel.dataSourceManager.dataSourceType?.ifStartEndYear },
                    onIfSort = { musicViewModel.dataSourceManager.dataSourceType?.ifMusicSort },
                    onIfFavoriteFilter = { musicViewModel.dataSourceManager.dataSourceType?.ifMusicFavoriteFilter },
                    onSortTypeClick = {
                        musicViewModel.setSortedData(it, { homeMusicPager.refresh() })
                    },
                    onSortType = { sortBy.sortType },
                    onFilterEraTypeList = { mainViewModel.eraItemList },
                    onFilterEraTypeClick = {
                        musicViewModel.setFilterEraType(
                            it,
                            { homeMusicPager.refresh() })
                    },
                    onIfFavorite = { sortBy.isFavorite == true },
                    setFavorite = { musicViewModel.setFavorite(it, { homeMusicPager.refresh() }) },
                    onYearSet = { mainViewModel.yearSet },
                    onSelectYear = { sortBy.yearList?.get(0) },
                    onSetSelectYear = { year ->
                        year?.let {
                            musicViewModel.setFilterYear(
                                listOf(it),
                                { homeMusicPager.refresh() })
                        }
                    },
                    onSelectRangeYear = { sortBy.yearList },
                    onSetSelectRangeYear = { years ->
                        musicViewModel.setFilterYear(
                            years.mapNotNull { it },
                            { homeMusicPager.refresh() })
                    }
                )
            }
        )

        SwipeRefreshListComponent(
            collectAsLazyPagingItems = homeMusicPager
        ) { page ->
            items(
                page.itemCount,
                page.itemKey { it.musicId },
                page.itemContentType { MusicTypeEnum.MUSIC.code }) { index ->
                // 加上remember就可以防止重组
                page[index]?.let { musicParent ->
                    MusicItemComponent(
                        itemId = musicParent.musicId,
                        name = musicParent.name,
                        album = musicParent.album,
                        artists = musicParent.artists,
                        pic = musicParent.pic,
                        codec = musicParent.codec,
                        bitRate = musicParent.bitRate,
                        onIfFavorite = {
                            musicParent.musicId in favoriteSet
                        },
                        onMusicPlay = {
                            musicViewModel.musicPlayContext.music(
                                onMusicPlayParameter = it,
                                index = index,
                                type = MusicPlayTypeEnum.FOUNDATION
                            )
                        },
                        backgroundColor = Color.Transparent,
                        textColor = if (musicViewModel.musicController.musicInfo?.itemId == musicParent.musicId)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        ifSelect = musicViewModel.selectControl.ifOpenSelect,
                        ifSelectCheckBox = {
                            musicViewModel.selectControl.selectMusicIdList.any { it == musicParent.musicId }
                        },
                        trailingOnSelectClick = { select ->
                            Log.i("======", "数据是否一起变化${select}")
                            musicViewModel.selectControl.toggleSelection(musicParent.musicId, onIsSelectAll = {
                                musicViewModel.selectControl.selectMusicIdList.containsAll(
                                    homeMusicPager.itemSnapshotList.items.map { it.musicId }
                                )
                            })
                        },
                        trailingOnClick = {
                            coroutineScope.launch {
                                musicViewModel.getMusicInfoById(musicParent.musicId)?.show()
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
fun MusicSelectTopBarComponent(
    modifier: Modifier,
    title: String,
    musicViewModel: MusicViewModel,
//    onIfOpenSelect: () -> Boolean,
//    onSetIfOpenSelect: (Boolean) -> Unit,
//    onIfAllSelect: () -> Boolean,
    onRandomPlayerClick: () -> Unit,
//    musicListCount: () -> Int,
    onSelectAll: () -> Unit,
    selectControl: SelectControl,
    sortOrFilterContent: @Composable () -> Unit
) {
    val navController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()

    /*val selectListSize by remember {
        derivedStateOf {
            selectControl.selectMusicDataList.size
        }
    }*/

    /*LaunchedEffect(selectListSize) {
        snapshotFlow { selectListSize }.collect {
            onSetIfAllSelect(musicListCount() == selectListSize && musicListCount() > 0)
        }
    }*/
    TopAppBarComponent(
        modifier = modifier,
        title = {
            if (selectControl.ifOpenSelect) {

                XySelectAllComponent(
                    isSelectAll = selectControl.isSelectAll,
                    onSelectAll = onSelectAll
                )

                /*if (onIfAllSelect()) {
                    TextButton(onClick = {
                        onSetIfAllSelect(false)
                        selectControl.clearSelectMusicList()
                    }) {
                        Text(text = stringResource(R.string.deselect_all))
                    }
                } else {
                    TextButton(onClick = {
                        onSetIfAllSelect(true)
                        coroutineScope.launch {
                            val musicList = musicViewModel.getMusicMusic(musicListCount())
                            if (!musicList.isNullOrEmpty())
                                selectControl.setSelectMusicListAllData(musicList)
                        }
                    }) {
                        Text(text = stringResource(R.string.select_all))
                    }
                }*/
            } else {
                Text(
                    text = title,
                    fontWeight = FontWeight.W900
                )
            }
        }, navigationIcon = {
            IconButton(
                onClick = {
                    if (selectControl.ifOpenSelect) {
                        selectControl.dismiss()
                    }
                    navController.popBackStack()
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.return_home)
                )
            }


        }, actions = {
            if (selectControl.ifOpenSelect) {
                IconButton(onClick = {
                    selectControl.dismiss()
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cancel_selection)
                    )
                }
            } else {
                IconButton(onClick = composeClick {
                    onRandomPlayerClick()
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = stringResource(R.string.random_play)
                    )
                }

                IconButton(onClick = {
                    musicViewModel.selectControl.show(ifOpenSelect = true)
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.PlaylistAddCheck,
                        contentDescription = stringResource(R.string.open_selection_function)
                    )
                }
                sortOrFilterContent()
            }


        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}