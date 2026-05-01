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

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.TabListEnum
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.image.rememberArtistCoverUrls
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.config.select.SelectControl
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.components.LazyListComponent
import cn.xybbz.ui.components.LazyLoadingAndStatus
import cn.xybbz.ui.components.LazyVerticalGridComponent
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.MusicArtistCardComponent
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.VerticalGridListComponent
import cn.xybbz.ui.components.XySelectAllComponent
import cn.xybbz.ui.components.rememberMusicArtistClickHandler
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnParentComponent
import cn.xybbz.ui.xy.ModalBottomSheetExtendComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.viewmodel.ArtistInfoViewModel
import com.github.panpf.sketch.ability.bindPauseLoadWhenScrolling
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.album
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.artist_cover
import xymusic_kmp.composeapp.generated.resources.artrist_info
import xymusic_kmp.composeapp.generated.resources.close_24px
import xymusic_kmp.composeapp.generated.resources.close_selection
import xymusic_kmp.composeapp.generated.resources.favorite_24px
import xymusic_kmp.composeapp.generated.resources.favorite_added
import xymusic_kmp.composeapp.generated.resources.favorite_border_24px
import xymusic_kmp.composeapp.generated.resources.favorite_removed
import xymusic_kmp.composeapp.generated.resources.no_description
import xymusic_kmp.composeapp.generated.resources.play_circle_24px
import xymusic_kmp.composeapp.generated.resources.playlist_add_check_24px
import xymusic_kmp.composeapp.generated.resources.reached_bottom
import xymusic_kmp.composeapp.generated.resources.return_home
import xymusic_kmp.composeapp.generated.resources.select
import xymusic_kmp.composeapp.generated.resources.songs_count_suffix
import xymusic_kmp.composeapp.generated.resources.start_playback
import cn.xybbz.ui.xy.XyIconButton as IconButton

private val JvmArtistInfoDesktopCoverSize = 232.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun JvmArtistInfoScreen(
    artistId: String,
    artistName: String,
    artistInfoViewModel: ArtistInfoViewModel = koinViewModel<ArtistInfoViewModel> {
        parametersOf(
            artistId,
            artistName
        )
    }
) {

    val horPagerState =
        rememberPagerState(initialPage = 0) {
            3
        }
    val musicPage =
        artistInfoViewModel.musicList.collectAsLazyPagingItems()
    val albumPageList =
        artistInfoViewModel.albumList.collectAsLazyPagingItems()
    val favoriteSet by artistInfoViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val downloadMusicIds by artistInfoViewModel.downloadMusicIdsFlow.collectAsStateWithLifecycle(
        emptyList()
    )
    val selectUiState by artistInfoViewModel.selectControl.uiState.collectAsStateWithLifecycle()
    val playbackState by artistInfoViewModel.musicController.playbackStateFlow.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current

    // 相似艺术家列表复用统一的艺术家打开逻辑。
    val artistClickHandler = rememberMusicArtistClickHandler()
    val lazyListState1 = rememberLazyListState()
    val lazyListState = rememberLazyListState()
    val parentState = rememberLazyListState()

    bindPauseLoadWhenScrolling(lazyListState1)
    bindPauseLoadWhenScrolling(lazyListState)
    bindPauseLoadWhenScrolling(parentState)

    val density = LocalDensity.current

    val tabHeightDp = XyTheme.dimens.itemHeight * 0.6f
    val surface = MaterialTheme.colorScheme.onSurface
    val artistHeaderHeight = JvmArtistInfoDesktopCoverSize + XyTheme.dimens.outerVerticalPadding * 2
    val topBarBottomPx = with(density) {
        (
                TopAppBarDefaults.TopAppBarExpandedHeight +
                        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                ).toPx()
    }
    val collapseRangePx = with(density) {
        (artistHeaderHeight.toPx() - topBarBottomPx).coerceAtLeast(1f)
    }

    val isParentAtTop by remember {
        derivedStateOf { isLazyListAtTop(parentState) }
    }
    val isCurrentListAtTop by remember {
        derivedStateOf { isLazyListAtTop(lazyListState1) }
    }
    val distanceToTopBarBottomPx by remember {
        derivedStateOf {
            val item2OffsetPx =
                parentState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == 2 }?.offset?.toFloat()
            when {
                item2OffsetPx != null -> item2OffsetPx - topBarBottomPx
                parentState.firstVisibleItemIndex > 2 -> 0f
                else -> collapseRangePx + 1f
            }
        }
    }

    val rawCollapseProgress by remember {
        derivedStateOf {
            ((collapseRangePx - distanceToTopBarBottomPx) / collapseRangePx).coerceIn(0f, 1f)
        }
    }
    val current by remember {
        derivedStateOf {
            if (isParentAtTop && !parentState.isScrollInProgress) {
                0f
            } else {
                rawCollapseProgress
            }
        }
    }
    //是否文本描述超过最大行数
    var isOverflow by remember { mutableStateOf(false) }
    //是否打开描述信息页面
    var ifOpenDescribe by remember { mutableStateOf(false) }

    // 使用 animateFloatAsState 实现平滑过渡
    val collapseProgress by animateFloatAsState(
        targetValue = current,
        animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing),
        label = "artist_collapse_progress"
    )
    val topBarAlpha by animateFloatAsState(
        targetValue = rangeProgress(collapseProgress, start = 0.55f, end = 1f),
        animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing),
        label = "artist_topbar_alpha"
    )

    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source == NestedScrollSource.UserInput &&
                    available.y > 0 &&
                    isParentAtTop &&
                    isCurrentListAtTop
                ) {
                    return Offset(x = 0f, y = available.y)
                }

                return if (available.y > 0) {
                    Offset.Zero
                } else {
                    Offset(
                        x = 0f,
                        y = -lazyListState1.dispatchRawDelta(
                            -available.y
                        )
                    )
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return Velocity.Zero
            }
        }
    }

    val parentScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source == NestedScrollSource.UserInput &&
                    available.y > 0 &&
                    isParentAtTop &&
                    isCurrentListAtTop
                ) {
                    return Offset(x = 0f, y = available.y)
                }

                return if (available.y > 0) {
                    Offset.Zero
                } else {
                    Offset(
                        x = 0f,
                        y = -parentState.dispatchRawDelta(
                            -available.y
                        )
                    )
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return Velocity.Zero
            }
        }
    }
    ModalBottomSheetExtendComponent(
        modifier = Modifier,
        onIfDisplay = { ifOpenDescribe },
        onClose = { bool -> ifOpenDescribe = bool },
        titleText = artistName,
        dragHandle = { BottomSheetDefaults.DragHandle(height = 2.dp) }
    ) {
        LazyColumnParentComponent(
            verticalArrangement = Arrangement.Top,
            contentPadding = PaddingValues(
                horizontal = XyTheme.dimens.outerHorizontalPadding
            ),
        ) {
            item {
                BasicText(
                    text = artistInfoViewModel.artistDescribe ?: "",
                    modifier = Modifier,
                    style = MaterialTheme.typography.titleSmall.copy(lineHeight = 15.sp),
                    overflow = TextOverflow.Visible,

                    )
            }
        }

    }


    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val artistHeaderHeight =
            JvmArtistInfoDesktopCoverSize + XyTheme.dimens.outerVerticalPadding * 2
        val maxHeight =
            (this.maxHeight - artistHeaderHeight - tabHeightDp -
                    TopAppBarDefaults.TopAppBarExpandedHeight -
                    WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .coerceAtLeast(1.dp)

        val parentMaxHeight = this.maxHeight
        val artistCoverUrls = rememberArtistCoverUrls(artistInfoViewModel.artistInfoData)

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
                                    text = artistName,
                                    modifier = Modifier.basicMarquee(
                                        iterations = Int.MAX_VALUE
                                    ),
                                    color = {
                                        surface.copy(alpha = topBarAlpha)
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
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        val ifFavorite =
                                            artistInfoViewModel.dataSourceManager.setFavoriteData(
                                                type = MusicTypeEnum.ARTIST,
                                                itemId = artistId,
                                                ifFavorite = artistInfoViewModel.ifFavorite
                                            )
                                        artistInfoViewModel.updateFavorite(ifFavorite)
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(if (artistInfoViewModel.ifFavorite) Res.drawable.favorite_24px else Res.drawable.favorite_border_24px),
                                        contentDescription = if (artistInfoViewModel.ifFavorite) stringResource(
                                            Res.string.favorite_added
                                        ) else stringResource(Res.string.favorite_removed),
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
                                        navigator.goBack()
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.arrow_back_24px),
                                        contentDescription = stringResource(Res.string.return_home)
                                    )
                                }
                            }


                        },
//                scrollBehavior = scrollBehavior
                    )
                }
                item {
                    JvmArtistDesktopHeader(
                        artistName = artistName,
                        description = artistInfoViewModel.artistDescribe,
                        musicCount = artistInfoViewModel.artistInfoData?.musicCount,
                        albumCount = artistInfoViewModel.artistInfoData?.albumCount,
                        coverUrl = artistCoverUrls.primaryUrl,
                        fallbackCoverUrl = artistCoverUrls.fallbackUrl,
                        descriptionOverflow = isOverflow,
                        onOpenDescription = {
                            ifOpenDescribe = true
                        },
                        onDescriptionOverflow = { overflow ->
                            isOverflow = overflow
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = XyTheme.dimens.outerHorizontalPadding,
                                end = XyTheme.dimens.outerHorizontalPadding,
                                top = XyTheme.dimens.outerVerticalPadding,
                                bottom = XyTheme.dimens.outerVerticalPadding
                            )
                    )
                }
                item(key = 2) {
                    LazyColumn(
                        state = lazyListState1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                parentMaxHeight - TopAppBarDefaults.TopAppBarExpandedHeight -
                                        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                            )
                            .nestedScroll(scrollConnection)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        stickyHeader {
                            PrimaryTabRow(
                                containerColor = MaterialTheme.colorScheme.background,
                                selectedTabIndex = horPagerState.currentPage,
                                divider = {},
                            ) {
                                TabListEnum.entries.forEachIndexed { index, it ->
                                    Tab(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(XyTheme.dimens.corner)),
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
                                .fillMaxWidth()
                                .height(maxHeight)
                                .background(MaterialTheme.colorScheme.background)
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
                                        item {
                                            JvmArtistMusicListOperation(
                                                artistId = artistId,
                                                musicPlayContext = artistInfoViewModel.musicPlayContext,
                                                selectControl = artistInfoViewModel.selectControl,
                                                onSelectAll = {
                                                    artistInfoViewModel.selectControl.toggleSelectionAll(
                                                        musicPage.itemSnapshotList.items.map { it.itemId })
                                                },
                                                ifOpenSelect = selectUiState.isOpen,
                                                isSelectAll = selectUiState.isSelectAll,
                                                sortContent = {}
                                            )
                                        }
                                        items(
                                            list.itemCount,
                                            key = list.itemKey { item -> item.itemId },
                                            contentType = list.itemContentType { MusicTypeEnum.MUSIC }
                                        ) { index ->
                                            list[index]?.let { music ->
                                                MusicItemComponent(
                                                    music = music,
                                                    onIfFavorite = {
                                                        music.itemId in favoriteSet
                                                    },
                                                    ifDownload = music.itemId in downloadMusicIds,
                                                    ifPlay = playbackState.musicInfo?.itemId == music.itemId,
                                                    backgroundColor = Color.Transparent,
                                                    trailingOnClick = {
                                                        music.show()
                                                    },
                                                    onMusicPlay = {
                                                        coroutineScope.launch {
                                                            artistInfoViewModel.musicPlayContext.artist(
                                                                onMusicPlayParameter = it.copy(
                                                                    artistId = artistId
                                                                ),
                                                                index = index,
                                                                artistId = artistId
                                                            )
                                                        }
                                                    },
                                                    ifSelect = selectUiState.isOpen,
                                                    ifSelectCheckBox = { music.itemId in selectUiState.selectedMusicIds },
                                                    trailingOnSelectClick = { _ ->
                                                        artistInfoViewModel.selectControl.toggleSelection(
                                                            music.itemId,
                                                            onIsSelectAll = {
                                                                selectUiState.selectedMusicIds.containsAll(
                                                                    list.itemSnapshotList.items.map { it.itemId }
                                                                )
                                                            }
                                                        )
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
                                                    album = album,
                                                    onRouter = {
                                                        navigator.navigate(
                                                            AlbumInfo(
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

                                TabListEnum.RESEMBLANCE_ARTIST -> {

                                    LazyVerticalGridComponent(
                                        modifier = Modifier.height(maxHeight),
                                    ) {
                                        items(
                                            artistInfoViewModel.resemblanceArtistList,
                                            key = { item -> item.artistId },
                                            contentType = { MusicTypeEnum.ARTIST }
                                        ) { artist ->
                                            MusicArtistCardComponent(
                                                modifier = Modifier,
                                                artist = artist,
                                                enabled = true
                                            ) {
                                                artistClickHandler.openArtist(it, artist.name)
                                            }
                                        }
                                        item(span = { GridItemSpan(maxLineSpan) }) {
                                            LazyLoadingAndStatus(
                                                text = stringResource(Res.string.reached_bottom),
                                                ifLoading = false
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


@Composable
private fun JvmArtistDesktopHeader(
    artistName: String,
    description: String?,
    musicCount: Int?,
    albumCount: Int?,
    coverUrl: String?,
    fallbackCoverUrl: String?,
    descriptionOverflow: Boolean,
    onOpenDescription: () -> Unit,
    onDescriptionOverflow: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val songsCountSuffix = stringResource(Res.string.songs_count_suffix)
    val albumText = stringResource(Res.string.album)
    val descriptionText = description ?: stringResource(Res.string.no_description)
    val subText = buildList {
        musicCount?.takeIf { it > 0 }?.let {
            add("$it$songsCountSuffix")
        }
        albumCount?.takeIf { it > 0 }?.let {
            add("$it$albumText")
        }
    }.joinToString(" • ")

    Row(
        modifier = modifier.height(JvmArtistInfoDesktopCoverSize),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        XyImage(
            modifier = Modifier
                .size(JvmArtistInfoDesktopCoverSize)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xff10b981), Color(0xff06b6d4)),
                        start = Offset(x = Float.POSITIVE_INFINITY, y = 0f),
                        end = Offset(x = 0f, y = Float.POSITIVE_INFINITY),
                    )
                ),
            model = coverUrl,
            backModel = fallbackCoverUrl,
            placeholder = Res.drawable.artrist_info,
            error = Res.drawable.artrist_info,
            fallback = Res.drawable.artrist_info,
            contentDescription = stringResource(Res.string.artist_cover),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = artistName,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (subText.isNotBlank()) {
                Text(
                    text = subText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = descriptionText,
                modifier = Modifier.debounceClickable(enabled = descriptionOverflow) {
                    onOpenDescription()
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResult ->
                    onDescriptionOverflow(textLayoutResult.hasVisualOverflow)
                },
            )
        }
    }
}


/**
 * 音乐列表操作栏
 */
@Composable
private fun JvmArtistMusicListOperation(
    artistId: String,
    musicPlayContext: MusicPlayContext,
    selectControl: SelectControl,
    ifOpenSelect: Boolean,
    isSelectAll: Boolean,
    onSelectAll: () -> Unit,
    sortContent: @Composable () -> Unit
) {
    XyRow(
        modifier = Modifier
            .debounceClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (ifOpenSelect) {
                    onSelectAll()
                } else {
                    musicPlayContext.artist(
                        OnMusicPlayParameter(
                            musicId = "",
                            artistId = artistId
                        ),
                        index = 0,
                        artistId = artistId
                    )
                }
            }
            .height(XyTheme.dimens.itemHeight),
        paddingValues = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding
        )
    ) {


        if (ifOpenSelect) {
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
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.play_circle_24px),
                    contentDescription = stringResource(Res.string.start_playback)
                )
                Text(text = stringResource(Res.string.start_playback))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                sortContent()

                IconButton(onClick = {
                    selectControl.show(
                        true
                    )
                }) {
                    Icon(
                        painter = painterResource(Res.drawable.playlist_add_check_24px),
                        contentDescription = stringResource(Res.string.select)
                    )
                }
            }
        }

    }
}

private fun rangeProgress(value: Float, start: Float, end: Float): Float {
    if (end <= start) return if (value >= end) 1f else 0f
    return ((value - start) / (end - start)).coerceIn(0f, 1f)
}

private fun isLazyListAtTop(state: LazyListState): Boolean {
    return state.firstVisibleItemIndex == 0 && state.firstVisibleItemScrollOffset == 0
}
