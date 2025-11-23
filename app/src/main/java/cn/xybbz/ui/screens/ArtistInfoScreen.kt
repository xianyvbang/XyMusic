package cn.xybbz.ui.screens

import android.util.Log
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.viewmodel.ArtistInfoViewModel
import kotlinx.coroutines.launch
import kotlin.math.min

internal val DefaultImageHeight = 350.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ArtistInfoScreen(
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
    val favoriteSet by artistInfoViewModel.favoriteRepository.favoriteSet.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val lazyListState1 = rememberLazyListState()
    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()
    val parentState = rememberLazyListState()

    //渐变高度占图片高度的比例
    val gradientHeight = 0.4f
    val imageOffsetDp =
        DefaultImageHeight
    val density = LocalDensity.current

    val current by remember {
        derivedStateOf {
            val maxScrollPx = with(density) { imageOffsetDp.toPx() } // 最大滚动距离
            val scrollOffset = min(
                parentState.firstVisibleItemIndex * 1000 + parentState.firstVisibleItemScrollOffset.toFloat(),
                maxScrollPx
            )
            (scrollOffset / maxScrollPx).coerceIn(0f, 1f)
        }
    }

    // 使用 animateFloatAsState 实现平滑过渡
    val topBarAlpha by animateFloatAsState(
        targetValue = current,
        animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing)
    )

    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                return if (available.y > 0) Offset.Zero else Offset(
                    x = 0f,
                    y = -lazyListState1.dispatchRawDelta(
                        -available.y
                    )
                )
            }
        }
    }

    val parentScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                return if (available.y > 0) Offset.Zero else Offset(
                    x = 0f,
                    y = -parentState.dispatchRawDelta(
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
    ) {
        val maxHeight =
            this.maxHeight - XyTheme.dimens.itemHeight - TopAppBarDefaults.TopAppBarExpandedHeight - WindowInsets.statusBars.asPaddingValues()
                .calculateTopPadding() /*- (DefaultImageHeight.times(0.2f))*/

        val parentMaxHeight = this.maxHeight

        /*Box(
            modifier = Modifier
                .fillMaxSize()
                .brashColor(
                    topVerticalColor = artistInfoViewModel.backgroundConfig.artistInfoBrash[0],
                    bottomVerticalColor = artistInfoViewModel.backgroundConfig.artistInfoBrash[1]
                )
        )*/

        Box(
            modifier = Modifier
                .height(
                    DefaultImageHeight
                )
        ) {
            XyImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .height(DefaultImageHeight),
                model = artistInfoViewModel.artistInfoData?.pic,
                contentDescription = stringResource(R.string.artist_cover),
                fallback = painterResource(R.drawable.artrist_info),
                placeholder = painterResource(R.drawable.artrist_info),
                error = painterResource(R.drawable.artrist_info),
//                alpha = (topBarAlpha - 1) * -1
            )

        }

        XyColumnScreen {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(parentScrollConnection),
                state = parentState,
            ) {
                stickyHeader {
                    TopAppBarComponent(
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
                                        Color.White.copy(alpha = topBarAlpha)
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
                                            artistInfoViewModel.updateFavorite(ifFavorite)
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (artistInfoViewModel.ifFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                            contentDescription = if (artistInfoViewModel.ifFavorite) stringResource(
                                                R.string.favorite_added
                                            ) else stringResource(R.string.favorite_removed),
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
                                        contentDescription = stringResource(R.string.return_home)
                                    )
                                }
                            }


                        },
//                scrollBehavior = scrollBehavior
                    )
                }
                item {
                    Spacer(
                        modifier = Modifier.padding(
                            top =
                                (DefaultImageHeight.times(1 - gradientHeight)) - TopAppBarDefaults.TopAppBarExpandedHeight - WindowInsets.statusBars.asPaddingValues()
                                    .calculateTopPadding()
                        )
                    )
                }

                item {
                    Box(modifier = Modifier.height(DefaultImageHeight.times(gradientHeight))) {

                        Spacer(
                            modifier = Modifier
                                .align(alignment = Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(DefaultImageHeight.times(gradientHeight))
                                .drawBehind {
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                artistInfoViewModel.backgroundConfig.artistInfoBrash[0]
                                            ),
                                            startY = 0f,
                                            endY = size.height
                                        ),
                                        blendMode = BlendMode.SrcOver
                                    )

                                })
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(DefaultImageHeight.times(0.3f))
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = XyTheme.dimens.outerHorizontalPadding)
                        ) {

                            BasicText(
                                text = artistInfoViewModel.artistInfoData?.name ?: "",
                                modifier = Modifier.basicMarquee(
                                    iterations = Int.MAX_VALUE
                                ),
                                color = {
                                    Color.White.copy(alpha = (topBarAlpha - 1) * -1)
                                },
                                style = LocalTextStyle.current.copy(fontSize = 30.sp)

                            )
                            Spacer(modifier = Modifier.height(XyTheme.dimens.corner))
                            BasicText(
                                text = artistInfoViewModel.artistInfoData?.describe
                                    ?: stringResource(R.string.no_description),
                                modifier = Modifier,
                                color = {
                                    Color.White.copy(alpha = (topBarAlpha - 1) * -1)
                                },
                                style = MaterialTheme.typography.titleSmall.copy(lineHeight = 15.sp),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,

                                )
                        }
                    }

                }
                item(key = 2) {
                    LazyColumn(
                        state = lazyListState1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                parentMaxHeight - TopAppBarDefaults.TopAppBarExpandedHeight - WindowInsets.statusBars.asPaddingValues()
                                    .calculateTopPadding()
                            )
                            .nestedScroll(scrollConnection)
                            .brashColor(
                                topVerticalColor = artistInfoViewModel.backgroundConfig.artistInfoBrash[0],
                                bottomVerticalColor = artistInfoViewModel.backgroundConfig.artistInfoBrash[1]
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
                                                text = stringResource(it.message),
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
                                                        itemId = music.itemId,
                                                        name = music.name,
                                                        album = music.album,
                                                        artists = music.artists,
                                                        pic = music.pic,
                                                        codec = music.codec,
                                                        bitRate = music.bitRate,
                                                        onIfFavorite = {
                                                            music.itemId in favoriteSet
                                                        },
                                                        textColor = if (artistInfoViewModel.musicController.musicInfo?.itemId == music.itemId)
                                                            MaterialTheme.colorScheme.primary
                                                        else
                                                            MaterialTheme.colorScheme.onSurface,
                                                        backgroundColor = Color.Transparent,
                                                        trailingOnClick = {
                                                            music.show()
                                                        },
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


    }


}

