package cn.xybbz.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
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
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnNotPaddingScreen
import cn.xybbz.viewmodel.MusicViewModel
import kotlinx.coroutines.launch

@Composable
fun MusicScreen(
    paddingValues: PaddingValues,
    musicViewModel: MusicViewModel = hiltViewModel<MusicViewModel>(),
) {

    val coroutineScope = rememberCoroutineScope()
    val mainViewModel = LocalMainViewModel.current
    val homeMusicPager = musicViewModel.homeMusicPager.collectAsLazyPagingItems()
    val favoriteList by musicViewModel.favoriteRepository.favoriteMap.collectAsState()


    var isSortBottomSheet by remember {
        mutableStateOf(false)
    }

    //是否打开选择
    var ifOpenSelect by remember {
        mutableStateOf(false)
    }
    //是否全选
    var ifAllSelect by remember {
        mutableStateOf(false)
    }

    SelectSortBottomSheetComponent(
        ifDisplay = { isSortBottomSheet },
        onSetDisplay = { isSortBottomSheet = it },
        onSortTypeClick = {
            musicViewModel.setSortedData(it)
        },
        onRefresh = {
            homeMusicPager.refresh()
            Log.i("=====", "刷新")
        },
        onSortType = { musicViewModel.sortBy.value.sortType },
        onFilterEraTypeList = { mainViewModel.eraItemList },
        onFilterEraType = { musicViewModel.sortBy.value.eraItem?.id ?: 0 },
        onFilterEraTypeClick = { musicViewModel.setFilterEraType(it) },
        onIfFavorite = { musicViewModel.sortBy.value.isFavorite == true },
        setFavorite = { musicViewModel.setFavorite(it) }
    )


    XyColumnNotPaddingScreen(
        modifier = Modifier
            .brashColor(topVerticalColor = Color(0xFF234B77))
    ) {
        MusicSelectTopBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = "音乐",
            musicViewModel = musicViewModel,
            onIfOpenSelect = { ifOpenSelect },
            onSetIfOpenSelect = {
                ifOpenSelect = it
            },
            onIfAllSelect = {
                ifAllSelect
            },
            onSetIfAllSelect = { ifAllSelect = it },
            onRandomPlayerClick = {
                coroutineScope.launch {
                    musicViewModel.musicPlayContext.randomMusic(
                        onMusicPlayParameter = OnMusicPlayParameter(musicId = "")
                    )
                }
            },
            onSortClick = {
                isSortBottomSheet = true
            },
            onSelectClick = {
                ifOpenSelect = true

            },
            musicListCount = { homeMusicPager.itemCount },
            selectControl = musicViewModel.selectControl
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
                        onMusicData = {
                            music
                        },
                        onIfFavorite = {
                            if (favoriteList.containsKey(music.itemId)) {
                                favoriteList.getOrDefault(music.itemId, false)
                            } else {
                                music.ifFavoriteStatus
                            }
                        },
                        onMusicPlay = {
                            musicViewModel.musicPlayContext.music(
                                onMusicPlayParameter = it,
                                index = index,
                                type = MusicPlayTypeEnum.FOUNDATION
                            )
                        },
                        backgroundColor = Color.Transparent,
                        textColor = if (musicViewModel.musicController.musicInfo?.itemId == music.itemId)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        ifSelect = ifOpenSelect,
                        ifSelectCheckBox = {
                            musicViewModel.selectControl.selectMusicDataList.any { it.itemId == music.itemId }
                        },
                        trailingOnClick = { select ->
                            Log.i("======", "数据是否一起变化${select}")
                            if (select) {
                                musicViewModel.selectControl.removeSelectMusicId(music.itemId)
                            } else {
                                musicViewModel.selectControl.setSelectMusicListData(music)
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
    onIfOpenSelect: () -> Boolean,
    onSetIfOpenSelect: (Boolean) -> Unit,
    onIfAllSelect: () -> Boolean,
    onSetIfAllSelect: (Boolean) -> Unit,
    onRandomPlayerClick: () -> Unit,
    onSortClick: () -> Unit,
    onSelectClick: () -> Unit,
    musicListCount: () -> Int,
    selectControl: SelectControl
) {
    val navController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()

    val selectListSize by remember {
        derivedStateOf {
            selectControl.selectMusicDataList.size
        }
    }

    LaunchedEffect(selectListSize) {
        snapshotFlow { selectListSize }.collect {
            onSetIfAllSelect(musicListCount() == selectListSize && musicListCount() > 0)
        }
    }
    TopAppBarComponent(
        modifier = modifier,
        title = {
            if (onIfOpenSelect()) {
                if (onIfAllSelect()) {
                    TextButton(onClick = {
                        onSetIfAllSelect(false)
                        selectControl.clearSelectMusicList()
                    }) {
                        Text(text = "取消全选")
                    }
                } else {
                    TextButton(onClick = {
                        onSetIfAllSelect(true)
                        coroutineScope.launch {
                            val musicList = musicViewModel.getMusicMusic(musicListCount())
                            if (!musicList.isNullOrEmpty())
                                selectControl.setSelectMusicListAllData(musicList)
                        }
                        Log.i("=====", "点击全选")
                    }) {
                        Text(text = "全选")
                    }
                }
            } else {
                Text(
                    text = title,
                    fontWeight = FontWeight.W900
                )
            }
        }, navigationIcon = {
            if (onIfOpenSelect()) {
                IconButton(
                    onClick = {
                        selectControl.dismiss()
                        navController.popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回首页"
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回首页"
                    )
                }
            }

        }, actions = {
            if (onIfOpenSelect()) {
                IconButton(onClick = {
                    onSetIfAllSelect(false)
                    musicViewModel.selectControl.dismiss()
                    onSetIfOpenSelect(false)
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "取消选择"
                    )
                }
            } else {
                IconButton(onClick = composeClick {
                    onRandomPlayerClick()
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "随机播放"
                    )
                }


                IconButton(onClick = {
                    onSortClick()
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.Sort, contentDescription = "排序")
                }

                IconButton(onClick = {
                    onSelectClick()
                    musicViewModel.selectControl.show(ifOpenSelect = true, onOpenChange = {
                        onSetIfOpenSelect(it)
                    })
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.PlaylistAddCheck,
                        contentDescription = "选中"
                    )
                }
            }


        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}