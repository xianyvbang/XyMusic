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

package cn.xybbz.ui.components


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.api.client.FavoriteCoordinator
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.config.image.rememberPlayMusicCoverUrls
import cn.xybbz.entity.data.LrcEntryData
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.ui.components.lrc.LrcViewNewCompose
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopInteractiveHitTestOwner
import cn.xybbz.ui.windows.DesktopWindowTitleBar as UiDesktopWindowTitleBar
import cn.xybbz.ui.windows.LocalDesktopTitleBarHitTestOwner
import cn.xybbz.ui.windows.LocalDesktopWindowDecorators
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.MusicBottomMenuViewModel
import cn.xybbz.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.album_cover
import xymusic_kmp.composeapp.generated.resources.backward_offset
import xymusic_kmp.composeapp.generated.resources.close_player_screen
import xymusic_kmp.composeapp.generated.resources.confirm
import xymusic_kmp.composeapp.generated.resources.disc_placeholder
import xymusic_kmp.composeapp.generated.resources.forward_offset
import xymusic_kmp.composeapp.generated.resources.keyboard_arrow_down_24px
import xymusic_kmp.composeapp.generated.resources.recommend
import xymusic_kmp.composeapp.generated.resources.reset
import xymusic_kmp.composeapp.generated.resources.song_tab
import kotlin.math.min
import kotlin.math.roundToInt
import cn.xybbz.ui.xy.XyIconButton as IconButton

internal val JvmMusicPlayerSharedCoverTargetSize = 650.dp
private val JvmMusicPlayerSharedCoverDisplayMaxSize = 650.dp
private const val JvmMusicPlayerSharedCoverDurationMillis = 920
private const val JvmMusicPlayerDialogEnterDurationMillis = 260
private const val JvmMusicPlayerCoverRotationDurationMillis = 18_000
private const val JvmMusicPlayerCoverRotationReadyProgress = 0.999f

//private val JvmMusicPlayerPrimaryPageMaxWidth = 1320.dp
private const val JvmMusicPlayerPrimaryPageGapWeight = 0.18f
private val JvmMusicPlayerLyricsMaxWidth = 480.dp

// JVM 播放页歌词使用略小的主歌词字号，避免桌面布局里文字显得过大。
private val JvmMusicPlayerLyricsFontSize = 18.sp

// 当前歌词锚点只按主歌词单行高度计算，不包含翻译歌词高度。
private val JvmMusicPlayerLyricsItemHeight = 26.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmMusicPlayerComponent(
    music: XyPlayMusic,
    picByte: ByteArray? = null,
    sharedCoverSourceBoundsOnScreen: Rect? = null,
    sheetStateR: SheetState,
    onSetState: (Boolean) -> Unit
) {
    val mainViewModel = LocalMainViewModel.current
    val playerHitTestOwner = remember { DesktopInteractiveHitTestOwner() }
    val coroutineScope = rememberCoroutineScope()
    val desktopTabs = remember {
        listOf(Res.string.song_tab, Res.string.recommend)
    }
    val overlayVisibleState = remember {
        MutableTransitionState(false)
    }
    val sharedCoverProgress by animateFloatAsState(
        targetValue = if (mainViewModel.sheetState) 1f else 0f,
        animationSpec = tween(
            durationMillis = JvmMusicPlayerSharedCoverDurationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "JvmMusicPlayerSharedCoverProgress"
    )
    val horPagerState =
        rememberPagerState {
            desktopTabs.size
        }
    val listState = rememberLazyListState()
    val similarPopularListState = rememberLazyListState()
    LaunchedEffect(mainViewModel.sheetState) {
        overlayVisibleState.targetState = mainViewModel.sheetState
    }
    if (overlayVisibleState.currentState || overlayVisibleState.targetState) {
        Dialog(
            onDismissRequest = {
                mainViewModel.putIterations(1)
                mainViewModel.putSheetState(false)
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
                usePlatformInsets = false,
                scrimColor = Color.Transparent
            )
        ) {
            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxSize(),
                visibleState = overlayVisibleState,
                enter = fadeIn(animationSpec = tween(durationMillis = JvmMusicPlayerDialogEnterDurationMillis)) +
                        scaleIn(
                            animationSpec = tween(durationMillis = JvmMusicPlayerDialogEnterDurationMillis),
                            initialScale = 0.98f
                        ),
                exit = fadeOut(animationSpec = tween(durationMillis = JvmMusicPlayerSharedCoverDurationMillis)) +
                        scaleOut(
                            animationSpec = tween(durationMillis = JvmMusicPlayerSharedCoverDurationMillis),
                            targetScale = 0.98f
                        )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // 向播放器内容下发当前弹层的命中测试记录器，供按钮和 Tab 排除拖拽命中。
                    CompositionLocalProvider(LocalDesktopTitleBarHitTestOwner provides playerHitTestOwner) {
                        JvmMusicPlayerScreen(
                            musicDetail = music,
                            picByte = picByte,
                            sharedCoverSourceBoundsOnScreen = sharedCoverSourceBoundsOnScreen,
                            sharedCoverProgress = sharedCoverProgress,
                            onCloseSheet = {
                                coroutineScope.launch {
                                    runCatching {
                                        sheetStateR.hide()
                                    }
                                }.invokeOnCompletion {
                                    mainViewModel.putIterations(1)
                                    mainViewModel.putSheetState(false)
                                }
                            },
                            onSetState = onSetState,
                            lrcListState = listState,
                            similarPopularListState = similarPopularListState,
                            horPagerState = horPagerState
                        )
                    }
                }
            }
        }
    }
}


/**
 * JVM 完整播放页主体。
 *
 * 上半部分承载封面、歌词和推荐页内容，
 * 底部区域复用 `JvmSnackBarPlaybackBar`，让完整播放页和底部悬浮播放条保持一致的控制体验。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmMusicPlayerScreen(
    musicDetail: XyPlayMusic,
    picByte: ByteArray? = null,
    musicBottomMenuViewModel: MusicBottomMenuViewModel = koinViewModel<MusicBottomMenuViewModel>(),
    musicPlayerViewModel: MusicPlayerViewModel = koinViewModel<MusicPlayerViewModel>(),
    sharedCoverSourceBoundsOnScreen: Rect? = null,
    sharedCoverProgress: Float = 1f,
    onCloseSheet: () -> Unit,
    onSetState: (Boolean) -> Unit,
    lrcListState: LazyListState = rememberLazyListState(),
    similarPopularListState: LazyListState = rememberLazyListState(),
    horPagerState: PagerState
) {

    val decorators = LocalDesktopWindowDecorators.current
    val titleBarHitTestOwner = LocalDesktopTitleBarHitTestOwner.current
    val cacheScheduleData by musicPlayerViewModel.downloadCacheController.cacheSchedule.collectAsStateWithLifecycle()
    val playbackState by musicPlayerViewModel.musicController.playbackStateFlow.collectAsStateWithLifecycle()
    val originMusicList by musicPlayerViewModel.musicController.originMusicListFlow.collectAsStateWithLifecycle()
    val favoriteList by musicPlayerViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val coverUrls = rememberPlayMusicCoverUrls(
        musicDetail,
        playbackState.coverRefreshVersion
    )
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val sharedCoverRequestSize = remember(density) {
        with(density) {
            IntSize(
                width = JvmMusicPlayerSharedCoverTargetSize.roundToPx(),
                height = JvmMusicPlayerSharedCoverTargetSize.roundToPx()
            )
        }
    }
    var playerRootBoundsOnScreen by remember {
        mutableStateOf<Rect?>(null)
    }
    var sharedCoverTargetBoundsOnScreen by remember {
        mutableStateOf<Rect?>(null)
    }
    val desktopTabs = remember {
        listOf(Res.string.song_tab, Res.string.recommend)
    }
    var lyricsMenuExpanded by remember(musicDetail.itemId) {
        mutableStateOf(false)
    }
    var lyricsMenuOffset by remember {
        mutableStateOf(DpOffset(0.dp, 0.dp))
    }
    var lyricsPreviewOffsetMs by remember(musicDetail.itemId) {
        mutableLongStateOf(0L)
    }
    val mockLyricsEntries = remember(
        musicDetail.itemId,
        musicDetail.name,
        musicDetail.artists
    ) {
        buildJvmMockLyricsEntries(musicDetail)
    }
    val artistLabel = remember(musicDetail.artists) {
        musicDetail.artistLabel()
    }
    val isCoverPlaybackActive = playbackState.state == PlayStateEnum.Playing ||
            playbackState.state == PlayStateEnum.Loading
    val isPlayerFullyOpened = sharedCoverProgress >= JvmMusicPlayerCoverRotationReadyProgress
    val shouldRotateCover = isCoverPlaybackActive && isPlayerFullyOpened
    val coverRotation = remember {
        Animatable(0f)
    }
    val mockLyricsLoopDuration = remember(mockLyricsEntries) {
        (mockLyricsEntries.lastOrNull()?.startTime ?: 0L) + 3_000L
    }
    var mockLyricsCurrentTimeMillis by remember(musicDetail.itemId) {
        mutableLongStateOf(0L)
    }
    val showSharedCoverOverlay =
        sharedCoverSourceBoundsOnScreen != null &&
                playerRootBoundsOnScreen != null &&
                sharedCoverTargetBoundsOnScreen != null &&
                sharedCoverProgress > 0f &&
                sharedCoverProgress < 1f

    fun persistedLyricsOffsetMs(): Long {
        // TODO 接入真实歌词配置后，从 lrcServer 读取并返回已保存的歌词偏移量。
        return 0L
    }

    fun resetLyricsPreviewOffset() {
        lyricsPreviewOffsetMs = persistedLyricsOffsetMs()
    }

    LaunchedEffect(musicDetail.itemId) {
        lyricsMenuExpanded = false
        resetLyricsPreviewOffset()
        coverRotation.snapTo(0f)
    }
    LaunchedEffect(Unit) {
        musicBottomMenuViewModel.refreshVolume()
    }
    LaunchedEffect(shouldRotateCover, musicDetail.itemId) {
        if (!shouldRotateCover) return@LaunchedEffect

        while (true) {
            val startRotation = coverRotation.value % 360f
            if (startRotation != coverRotation.value) {
                coverRotation.snapTo(startRotation)
            }
            coverRotation.animateTo(
                targetValue = startRotation + 360f,
                animationSpec = tween(
                    durationMillis = JvmMusicPlayerCoverRotationDurationMillis,
                    easing = LinearEasing
                )
            )
        }
    }
    LaunchedEffect(musicDetail.itemId, mockLyricsLoopDuration) {
        mockLyricsCurrentTimeMillis = 0L
        while (true) {
            delay(90L)
            mockLyricsCurrentTimeMillis =
                (mockLyricsCurrentTimeMillis + 90L) % mockLyricsLoopDuration
        }
    }
    val content: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    playerRootBoundsOnScreen = Rect(
                        offset = coordinates.positionOnScreen(),
                        size = Size(
                            width = coordinates.size.width.toFloat(),
                            height = coordinates.size.height.toFloat()
                        )
                    )
                }
        ) {
            XyImage(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(),
                model = coverUrls.primaryUrl ?: picByte,
                backModel = coverUrls.fallbackUrl ?: picByte,
                requestSize = sharedCoverRequestSize,
                alpha = 0.2f,
                contentDescription = stringResource(Res.string.album_cover),
            )
            JvmSharedCoverOverlay(
                sourceBoundsOnScreen = sharedCoverSourceBoundsOnScreen,
                targetBoundsOnScreen = sharedCoverTargetBoundsOnScreen,
                rootBoundsOnScreen = playerRootBoundsOnScreen,
                progress = sharedCoverProgress,
                model = coverUrls.primaryUrl ?: picByte,
                backModel = coverUrls.fallbackUrl ?: picByte,
                requestSize = sharedCoverRequestSize,
                rotationDegrees = coverRotation.value
            )
            XyColumnScreen(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues()
                            .calculateTopPadding()
                    ),
                background = Color.Transparent
            ) {
                UiDesktopWindowTitleBar(
                    hitTestOwner = titleBarHitTestOwner,
                    backgroundColor = Color.Transparent,
                    front = { hitTestOwner ->
                        IconButton(
                            modifier = Modifier.jvmPlayerTitleBarHitTarget(
                                hitTestOwner,
                                JvmMusicPlayerTitleBarHitTarget.CloseButton
                            ),
                            onClick = {
                                onCloseSheet()
                            },
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.keyboard_arrow_down_24px),
                                contentDescription = stringResource(Res.string.close_player_screen)
                            )
                        }
                    },
                    middle = { hitTestOwner ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            desktopTabs.forEachIndexed { index, item ->
                                TextButton(
                                    modifier = Modifier.jvmPlayerTitleBarHitTarget(
                                        hitTestOwner,
                                        JvmMusicPlayerTitleBarHitTarget.Tab(index)
                                    ),
                                    onClick = {
                                        coroutineScope
                                            .launch {
                                                horPagerState.animateScrollToPage(index)
                                            }
                                    }
                                ) {
                                    XyText(
                                        text = stringResource(item),
                                        fontWeight = null,
                                        maxLines = 2,
                                        style = LocalTextStyle.current,
                                        color = if (horPagerState.currentPage == index) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    HorizontalPager(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = horPagerState,
                        // 在底部弹窗里关闭 Pager 的边缘回弹，避免翻页落位后再次左右抖动
                        overscrollEffect = null
                    ) { page ->
                        when (page) {
                            0 -> {
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = XyTheme.dimens.outerHorizontalPadding)
//                                    .widthIn(max = JvmMusicPlayerPrimaryPageMaxWidth)
                                ) {
                                    val coverColumnWidth =
                                        maxWidth * (1f / (2f + JvmMusicPlayerPrimaryPageGapWeight))
                                    val coverSize =
                                        minOf(coverColumnWidth * 0.76f, maxHeight * 0.6f)
                                            .coerceAtMost(JvmMusicPlayerSharedCoverDisplayMaxSize)
                                    val primaryPageSideInset =
                                        (coverColumnWidth - coverSize).coerceAtLeast(0.dp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(coverSize),
                                            contentAlignment = Alignment.TopEnd
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .requiredSize(coverSize)
                                                    .onGloballyPositioned { coordinates ->
                                                        sharedCoverTargetBoundsOnScreen = Rect(
                                                            offset = coordinates.positionOnScreen(),
                                                            size = Size(
                                                                width = coordinates.size.width.toFloat(),
                                                                height = coordinates.size.height.toFloat()
                                                            )
                                                        )
                                                    }
                                            ) {
                                                XyImage(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .graphicsLayer {
                                                            rotationZ = coverRotation.value
                                                        }
                                                        .clip(CircleShape),
                                                    model = coverUrls.primaryUrl ?: picByte,
                                                    backModel = coverUrls.fallbackUrl ?: picByte,
                                                    requestSize = sharedCoverRequestSize,
                                                    placeholder = Res.drawable.disc_placeholder,
                                                    error = Res.drawable.disc_placeholder,
                                                    fallback = Res.drawable.disc_placeholder,
                                                    alpha = if (showSharedCoverOverlay) 0f else 1f,
                                                    contentDescription = stringResource(Res.string.album_cover),
                                                )
                                            }
                                        }

                                        Spacer(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(JvmMusicPlayerPrimaryPageGapWeight)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(coverSize)
                                                .padding(end = primaryPageSideInset),
                                            contentAlignment = Alignment.TopStart
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .widthIn(max = JvmMusicPlayerLyricsMaxWidth)
                                                    .fillMaxHeight()
                                                    .pointerInput(musicDetail.itemId) {
                                                        awaitPointerEventScope {
                                                            while (true) {
                                                                val event = awaitPointerEvent()
                                                                val change =
                                                                    event.changes.firstOrNull()
                                                                        ?: continue
                                                                if (
                                                                    event.type == PointerEventType.Press &&
                                                                    event.buttons.isSecondaryPressed
                                                                ) {
                                                                    resetLyricsPreviewOffset()
                                                                    lyricsMenuOffset =
                                                                        with(density) {
                                                                            DpOffset(
                                                                                change.position.x.toDp(),
                                                                                change.position.y.toDp()
                                                                            )
                                                                        }
                                                                    lyricsMenuExpanded = true
                                                                }
                                                            }
                                                        }
                                                    },
                                                verticalArrangement = Arrangement.Top
                                            ) {
                                                JvmMusicPlayerLyricsHeader(
                                                    title = musicDetail.name,
                                                    artist = artistLabel
                                                )
//                                        Spacer(modifier = Modifier.height(JvmMusicPlayerLyricsHeaderBottomGap))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(1f)
                                                ) {
                                                    LrcViewNewCompose(
                                                        modifier = Modifier.fillMaxSize(),
                                                        listState = lrcListState,
                                                        externalOffsetMillis = lyricsPreviewOffsetMs,
                                                        currentLineTopInset = JvmMusicPlayerLyricsItemHeight,
                                                        highlightScaleEnabled = false,
                                                        primaryFontSize = JvmMusicPlayerLyricsFontSize,
                                                        previewEntries = mockLyricsEntries,
                                                        previewCurrentTimeMillis = mockLyricsCurrentTimeMillis,
                                                        // TODO 接入真实歌词数据后，移除 previewEntries / previewCurrentTimeMillis，改回真实歌词流。
                                                        onSetLrcOffset = { }
                                                    )
                                                    JvmLyricsConfigDropdownMenu(
                                                        expanded = lyricsMenuExpanded,
                                                        offset = lyricsMenuOffset,
                                                        onDismissRequest = {
                                                            lyricsMenuExpanded = false
                                                            resetLyricsPreviewOffset()
                                                        },
                                                        onPreviewOffsetChange = {
                                                            lyricsPreviewOffsetMs = it
                                                        },
                                                        onConfirm = {
                                                            // TODO 接入真实歌词配置后，在这里持久化 lyricsPreviewOffsetMs 到 lrcServer。
                                                            lyricsMenuExpanded = false
                                                        },
                                                        currentPreviewOffsetMs = lyricsPreviewOffsetMs
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {
                                MusicPlayerSimilarPopularComponent(
                                    listState = similarPopularListState,
                                    onFavoriteSet = { emptySet() },
                                    onDownloadMusicIds = { emptyList() },
                                    playMusicList = originMusicList,
                                    onAddPlayMusic = { musicPlayerViewModel.addNextPlayer(it) }
                                )
                            }
                        }
                    }


                    XyColumn(
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxWidth(),
                        paddingValues = PaddingValues(0.dp),
                        clipSize = 0.dp,
                        backgroundColor = Color.Transparent
                    ) {
                        // 完整播放页底部直接复用桌面端共享播放栏，避免两套控制区长期分叉。
                        JvmSnackBarPlaybackBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(XyTheme.dimens.snackBarPlayerHeight),
                            musicController = musicPlayerViewModel.musicController,
                            musicBottomMenuViewModel = musicBottomMenuViewModel,
                            favoriteSet = favoriteList,
                            isDarkTheme = XyTheme.configs.isDarkTheme,
                            defaultContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            sharedCoverRequestSize = sharedCoverRequestSize,
                            showCover = false,
                            cacheProgress = cacheScheduleData,
                            desktopDragHitTestOwner = titleBarHitTestOwner,
                            onShowPlaylist = {
                                if (originMusicList.isNotEmpty()) {
                                    onSetState(true)
                                }
                            },
                            onToggleFavorite = { playMusic ->
                                FavoriteCoordinator.setFavoriteData(
                                    dataSourceManager = musicPlayerViewModel.dataSourceManager,
                                    type = MusicTypeEnum.MUSIC,
                                    itemId = playMusic.itemId,
                                    ifFavorite = playMusic.itemId in favoriteList,
                                    musicController = musicPlayerViewModel.musicController
                                )
                            },
                            onShowMusicInfo = { playMusic ->
                                musicPlayerViewModel.dataSourceManager.selectMusicInfoById(playMusic.itemId)
                                    ?.show()
                            }
                        )
                    }
                }

            }
        }
    }
    decorators.DraggableArea(
        content = content,
        hitTestOwner = titleBarHitTestOwner
    )
}

@Composable
private fun JvmMusicPlayerLyricsHeader(
    title: String,
    artist: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        XyText(
            text = title,
            modifier = Modifier
                .basicMarquee(iterations = Int.MAX_VALUE),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 28.sp,
                lineHeight = 36.sp
            ),
            overflow = TextOverflow.Visible
        )
        XyTextSub(
            text = artist,
            modifier = Modifier
                .basicMarquee(iterations = Int.MAX_VALUE),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
    }
}

// 播放器顶部栏中需要保留 Compose 点击能力的控件标识。
private sealed class JvmMusicPlayerTitleBarHitTarget(val id: String) {
    data object CloseButton : JvmMusicPlayerTitleBarHitTarget("CloseButton")

    data class Tab(private val index: Int) : JvmMusicPlayerTitleBarHitTarget("Tab$index")
}

// 将控件窗口坐标同步给原生命中测试层，命中这些区域时不触发窗口拖拽。
private fun Modifier.jvmPlayerTitleBarHitTarget(
    owner: DesktopInteractiveHitTestOwner,
    target: JvmMusicPlayerTitleBarHitTarget,
): Modifier = onGloballyPositioned { coordinates ->
    owner.updateBounds(target.id, coordinates.boundsInWindow())
}

private fun buildJvmMockLyricsEntries(musicDetail: XyPlayMusic): List<LrcEntryData> {
    return listOf(
        LrcEntryData(
            startTime = 0L,
            text = "桌面播放器歌词区域正在使用模拟数据",
            secondText = "Mock lyrics are showing in the JVM player for now"
        ),
        LrcEntryData(
            startTime = 2_200L,
            text = musicDetail.name,
            secondText = musicDetail.artistLabel()
        ),
        LrcEntryData(
            startTime = 4_600L,
            text = "这里先验证排版、滚动和高亮节奏",
            secondText = "We are validating layout, scrolling, and highlight rhythm"
        ),
        LrcEntryData(
            startTime = 7_100L,
            text = "真实歌词接口稍后再接入",
            secondText = "Real lyric data will be wired in later"
        ),
        LrcEntryData(
            startTime = 9_600L,
            text = "右键菜单里的偏移预览仍然可以先体验",
            secondText = "Offset preview in the context menu still works"
        ),
        LrcEntryData(
            startTime = 12_200L,
            text = "接入真实数据时，把这里替换回正式歌词组件即可",
            secondText = "Swap this mock block back to the real lyrics component later"
        )
    )
}

private fun XyPlayMusic.artistLabel(): String {
    return artists?.joinToString().takeUnless { it.isNullOrBlank() } ?: "Artist"
}

@Composable
private fun BoxScope.JvmLyricsConfigDropdownMenu(
    expanded: Boolean,
    offset: DpOffset,
    onDismissRequest: () -> Unit,
    onPreviewOffsetChange: (Long) -> Unit,
    onConfirm: () -> Unit,
    currentPreviewOffsetMs: Long
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = offset,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        DropdownMenuItem(
            text = {
                XyText(
                    text = stringResource(Res.string.forward_offset),
                    fontWeight = null,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            onClick = {
                onPreviewOffsetChange(currentPreviewOffsetMs + 500L)
            }
        )
        DropdownMenuItem(
            text = {
                XyText(
                    text = stringResource(Res.string.backward_offset),
                    fontWeight = null,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            onClick = {
                onPreviewOffsetChange(currentPreviewOffsetMs - 500L)
            }
        )
        DropdownMenuItem(
            text = {
                XyText(
                    text = stringResource(Res.string.reset),
                    fontWeight = null,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            onClick = {
                onPreviewOffsetChange(0L)
            }
        )
        DropdownMenuItem(
            text = {
                XyText(
                    text = stringResource(Res.string.confirm),
                    fontWeight = null,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            onClick = onConfirm
        )
    }
}

@Composable
private fun JvmSharedCoverOverlay(
    sourceBoundsOnScreen: Rect?,
    targetBoundsOnScreen: Rect?,
    rootBoundsOnScreen: Rect?,
    progress: Float,
    model: Any?,
    backModel: Any?,
    requestSize: IntSize,
    rotationDegrees: Float
) {
    val sourceBounds = sourceBoundsOnScreen ?: return
    val targetBounds = targetBoundsOnScreen ?: return
    val rootBounds = rootBoundsOnScreen ?: return
    if (progress <= 0f || progress >= 1f) return

    val density = LocalDensity.current
    val easedProgress = FastOutSlowInEasing.transform(progress.coerceIn(0f, 1f))
    val animatedLeft = lerpFloat(sourceBounds.left, targetBounds.left, easedProgress)
    val animatedTop = lerpFloat(sourceBounds.top, targetBounds.top, easedProgress)
    val animatedWidth = lerpFloat(sourceBounds.width, targetBounds.width, easedProgress)
    val animatedHeight = lerpFloat(sourceBounds.height, targetBounds.height, easedProgress)
    val startCornerPx = with(density) { XyTheme.dimens.corner.toPx() }
    val endCornerPx = min(animatedWidth, animatedHeight) / 2f
    val animatedCorner = with(density) {
        lerp(
            start = startCornerPx.toDp(),
            stop = endCornerPx.toDp(),
            fraction = easedProgress
        )
    }
    val animatedShape = RoundedCornerShape(animatedCorner)

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (animatedLeft - rootBounds.left).roundToInt(),
                    y = (animatedTop - rootBounds.top).roundToInt()
                )
            }
            .requiredSize(
                width = with(density) { animatedWidth.toDp() },
                height = with(density) { animatedHeight.toDp() }
            )
            .clip(animatedShape)
    ) {
        XyImage(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotationDegrees },
            model = model,
            backModel = backModel,
            requestSize = requestSize,
            placeholder = Res.drawable.disc_placeholder,
            error = Res.drawable.disc_placeholder,
            fallback = Res.drawable.disc_placeholder,
            contentDescription = stringResource(Res.string.album_cover),
        )
    }
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}
