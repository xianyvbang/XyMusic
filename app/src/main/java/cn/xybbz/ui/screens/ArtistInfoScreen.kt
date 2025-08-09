package cn.xybbz.ui.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.R
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.TabListEnum
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.RouterConstants
import cn.xybbz.ui.components.LazyListComponent
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.VerticalGridListComponent
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnNotPaddingScreen
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.viewmodel.ArtistInfoViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal val DefaultImageHeight = 350.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ArtistInfoScreen(
    paddingValues: PaddingValues,
    artistId: () -> String = { "" },
    artistInfoViewModel: ArtistInfoViewModel = hiltViewModel<ArtistInfoViewModel, ArtistInfoViewModel.Factory>(
        creationCallback = { factory ->
            factory.create(
                artistId = artistId(),
            )
        }
    )
) {

    val horPagerState =
        rememberPagerState(initialPage = 0) {
            2
        }
    val musicPage =
        artistInfoViewModel.musicList.collectAsLazyPagingItems()
    val albumPageList =
        artistInfoViewModel.albumList.collectAsLazyPagingItems()
    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val lazyListState1 = rememberLazyListState()
    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()
    val scrollOffset = remember { mutableStateOf(0f) }
    val favoriteList by artistInfoViewModel.favoriteRepository.favoriteMap.collectAsState()

    val topBarAlpha by remember {
        derivedStateOf {
            val offset = lazyListState1.firstVisibleItemScrollOffset
            val itemIndex = lazyListState1.firstVisibleItemIndex
            ((if (itemIndex >= 1 && offset > 400) 1f else if (itemIndex >= 1) offset / 400f else 0f) - 1) * -1
        }
    }

    // 监听滚动偏移量
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = scrollOffset.value + delta
                scrollOffset.value = newOffset.coerceIn(-400f, 0f) // 控制头图最大滑动范围
                return Offset.Zero
            }
        }
    }

    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                return if (available.y > 0) Offset.Zero else Offset(
                    x = 0f,
                    y = if (horPagerState.currentPage == 1) -lazyGridState.dispatchRawDelta(-available.y) else -lazyListState1.dispatchRawDelta(
                        -available.y
                    )
                )
            }
        }
    }

    SideEffect {
        Log.d("=====", "MusicAudiobookInfoScreen重组一次")
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .brashColor(
                topVerticalColor = Color(0xFF113460),
                bottomVerticalColor = Color(0xFF0B424B)
            )
            .nestedScroll(nestedScrollConnection)
    ) {
        val maxHeight =
            this.maxHeight - XyTheme.dimens.itemHeight - WindowInsets.statusBars.asPaddingValues()
                .calculateTopPadding() - TopAppBarDefaults.TopAppBarExpandedHeight

        Box(
            modifier = Modifier
                .height(
                    DefaultImageHeight
                )
                .align(Alignment.TopCenter)
                .offset { IntOffset(x = 0, y = scrollOffset.value.roundToInt()) }
        ) {
            XyImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .height(DefaultImageHeight),
                model = artistInfoViewModel.artistInfoData?.pic,
                contentDescription = "专辑封面",
                fallback = painterResource(R.drawable.image_placeholder),
                placeholder = painterResource(R.drawable.image_placeholder),
                error = painterResource(R.drawable.image_placeholder),
                alpha = topBarAlpha
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = XyTheme.dimens.outerHorizontalPadding)
            ) {

                BasicText(
                    text = artistInfoViewModel.artistInfoData?.name ?: "",
                    modifier = Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE
                    ),
                    color = {
                        Color.White.copy(alpha = topBarAlpha)
                    },
                    style = LocalTextStyle.current.copy(fontSize = 30.sp)

                )
                Spacer(modifier = Modifier.height(XyTheme.dimens.corner))
                BasicText(
                    text = artistInfoViewModel.artistInfoData?.describe
                        ?: "暂无简介",
                    modifier = Modifier,
                    color = {
                        Color.White.copy(alpha = topBarAlpha)
                    },
                    style = MaterialTheme.typography.titleSmall

                )
                Spacer(modifier = Modifier.height(XyTheme.dimens.corner))
            }
        }

        XyColumnNotPaddingScreen {
            TopAppBarComponent(
                modifier = Modifier,
                title = {
                    Box(
                        modifier = Modifier
                            .padding(
                                top = WindowInsets.statusBars.asPaddingValues()
                                    .calculateTopPadding()
                            )
                            .height(TopAppBarDefaults.TopAppBarExpandedHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicText(
                            text = artistInfoViewModel.artistInfoData?.name ?: "",
                            modifier = Modifier.basicMarquee(
                                iterations = Int.MAX_VALUE
                            ),
                            color = {
                                Color.White.copy(alpha = (topBarAlpha - 1) * -1)
                            },
                            style = LocalTextStyle.current

                        )
                    }

                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(
                                top = WindowInsets.statusBars.asPaddingValues()
                                    .calculateTopPadding()
                            )
                            .height(TopAppBarDefaults.TopAppBarExpandedHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        if (artistInfoViewModel.dataSourceManager.dataSourceType?.ifArtistFavorite == true)
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    val ifFavorite =
                                        artistInfoViewModel.dataSourceManager.setFavoriteData(
                                            type = MusicTypeEnum.ARTIST,
                                            itemId = artistId(),
                                            ifFavorite = artistInfoViewModel.ifFavorite
                                        )
                                    Log.i("=====", "收藏数据 $ifFavorite")
                                }
                            }) {
                                Icon(
                                    imageVector = if (artistInfoViewModel.ifFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = if (artistInfoViewModel.ifFavorite) "已收藏-可取消收藏" else "未收藏-可以收藏",
                                    tint = Color.Red
                                )
                            }

                    }

                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(
                                top = WindowInsets.statusBars.asPaddingValues()
                                    .calculateTopPadding()
                            )
                            .height(TopAppBarDefaults.TopAppBarExpandedHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回广播"
                            )
                        }
                    }


                },
//                scrollBehavior = scrollBehavior
            )
            LazyColumn(
                state = lazyListState1,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollConnection),
                contentPadding = PaddingValues(
                    top = DefaultImageHeight - TopAppBarDefaults.TopAppBarExpandedHeight - WindowInsets.statusBars.asPaddingValues()
                        .calculateTopPadding()
                )

            ) {
                stickyHeader {
                    PrimaryTabRow(
                        containerColor = Color.Transparent,
                        selectedTabIndex = horPagerState.currentPage,
                        divider = {},
                        modifier = Modifier.height(XyTheme.dimens.itemHeight)
                    ) {
                        TabListEnum.entries.forEachIndexed { index, it ->
                            Tab(
                                selected = horPagerState.currentPage == index,
                                onClick = {
                                    coroutineScope
                                        .launch {
                                            horPagerState.animateScrollToPage(index)
                                        }
                                },
                                text = {
                                    Text(
                                        text = it.message,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }

                item {
                    HorizontalPager(
                        state = horPagerState,
                        modifier = Modifier
                    ) { page: Int ->
                        when (TabListEnum.entries[page]) {
                            TabListEnum.Music -> {
                                LazyListComponent(
                                    state = lazyListState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .height(maxHeight),
                                    collectAsLazyPagingItems = musicPage
                                ) { list ->
                                    items(
                                        list.itemCount,
                                        key = list.itemKey { item -> item.itemId },
                                        contentType = list.itemContentType { MusicTypeEnum.MUSIC }
                                    ) { index ->
                                        list[index]?.let { music ->
                                            MusicItemComponent(
                                                onMusicData = { music },
                                                onIfFavorite = {
                                                    if (favoriteList.containsKey(music.itemId)) {
                                                        favoriteList.getOrDefault(
                                                            music.itemId,
                                                            false
                                                        )
                                                    } else {
                                                        music.ifFavoriteStatus
                                                    }
                                                },
                                                textColor = if (artistInfoViewModel.musicController.musicInfo?.itemId == music.itemId)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface,
                                                backgroundColor = Color.Transparent,
                                                onMusicPlay = {
                                                    coroutineScope.launch {
                                                        artistInfoViewModel.musicPlayContext.artist(
                                                            onMusicPlayParameter = it.copy(
                                                                artistId = artistId()
                                                            ),
                                                            index = index,
                                                            artistId = artistId()
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                            }

                            TabListEnum.Album -> {

                                VerticalGridListComponent(
                                    modifier = Modifier.height(maxHeight),
                                    collectAsLazyPagingItems = albumPageList,
                                ) {
                                    items(
                                        count = albumPageList.itemCount,
                                        key = albumPageList.itemKey { it.itemId },
                                        contentType = albumPageList.itemContentType { MusicTypeEnum.ALBUM.code }) { index ->
                                        albumPageList[index]?.let { album ->
                                            MusicAlbumCardComponent(
                                                modifier = Modifier,
                                                onItem = { album },
                                                onRouter = {
                                                    navController.navigate(
                                                        RouterConstants.AlbumInfo(
                                                            it,
                                                            MusicDataTypeEnum.ALBUM
                                                        )
                                                    )
                                                }
                                            )

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


    }


}

