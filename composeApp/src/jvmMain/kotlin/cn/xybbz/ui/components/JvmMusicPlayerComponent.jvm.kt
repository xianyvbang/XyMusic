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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.utils.DateUtil.toSecondMs
import cn.xybbz.compositionLocal.LocalPlayerChromeState
import cn.xybbz.config.image.rememberPlayMusicCoverUrls
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.ui.components.lrc.LrcViewNewCompose
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopInteractiveHitTestOwner
import cn.xybbz.ui.windows.DesktopWindowTitleCenterBar
import cn.xybbz.ui.windows.LocalDesktopTitleBarHitTestOwner
import cn.xybbz.ui.windows.LocalDesktopWindowDecorators
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.album_cover
import xymusic.composeapp.generated.resources.backward_offset
import xymusic.composeapp.generated.resources.close_player_screen
import xymusic.composeapp.generated.resources.confirm
import xymusic.composeapp.generated.resources.forward_offset
import xymusic.composeapp.generated.resources.keyboard_arrow_down_24px
import xymusic.composeapp.generated.resources.offset
import xymusic.composeapp.generated.resources.recommend
import xymusic.composeapp.generated.resources.reset
import xymusic.composeapp.generated.resources.song_tab
import kotlin.math.min
import kotlin.math.roundToInt
import cn.xybbz.ui.xy.XyIconButton as IconButton

internal val JvmMusicPlayerSharedCoverTargetSize = 720.dp
private val JvmMusicPlayerSharedCoverDisplayMaxSize = 720.dp
private const val JvmMusicPlayerSharedCoverDurationMillis = 520
private const val JvmMusicPlayerDialogEnterDurationMillis = 260
private const val JvmMusicPlayerCoverRotationDurationMillis = 18_000
private const val JvmMusicPlayerCoverRotationReadyProgress = 0.999f

//private val JvmMusicPlayerPrimaryPageMaxWidth = 1320.dp
private const val JvmMusicPlayerPrimaryPageGapWeight = 0.18f
private const val JvmMusicPlayerCoverWidthRatio = 0.84f
private const val JvmMusicPlayerCoverHeightRatio = 0.72f
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
    volumeValue: Float,
    onVolumeChanged: (Float) -> Unit,
    onRefreshVolume: () -> Unit,
    onSetState: (Boolean) -> Unit
) {
    // 桌面完整播放器页通过播放器外壳状态控制弹层显隐和关闭后的标题跑马灯。
    val playerChromeState = LocalPlayerChromeState.current
    // 收集完整播放器页显隐状态，驱动桌面弹层和共享封面动画。
    val isPlayerSheetVisible by playerChromeState.isPlayerSheetVisibleFlow.collectAsStateWithLifecycle()
    val playerHitTestOwner = remember { DesktopInteractiveHitTestOwner() }
    val coroutineScope = rememberCoroutineScope()
    val desktopTabs = remember {
        listOf(Res.string.song_tab, Res.string.recommend)
    }
    val overlayVisibleState = remember {
        MutableTransitionState(false)
    }
    // 共享封面动画进度跟随完整播放器显隐状态变化。
    val sharedCoverProgress by animateFloatAsState(
        targetValue = if (isPlayerSheetVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = JvmMusicPlayerSharedCoverDurationMillis,
            easing = LinearEasing
        ),
        label = "JvmMusicPlayerSharedCoverProgress"
    )
    val horPagerState =
        rememberPagerState {
            desktopTabs.size
        }
    val listState = rememberLazyListState()
    val similarPopularListState = rememberLazyListState()
    // 桌面 Dialog 通过过渡状态保留退场动画，不直接在隐藏时移除节点。
    LaunchedEffect(isPlayerSheetVisible) {
        overlayVisibleState.targetState = isPlayerSheetVisible
    }
    if (overlayVisibleState.currentState || overlayVisibleState.targetState) {
        Dialog(
            onDismissRequest = {
                // 系统关闭 Dialog 时恢复迷你播放条标题滚动，并同步隐藏完整播放器。
                playerChromeState.putMarqueeIterations(1)
                playerChromeState.hidePlayerSheet()
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
                            volumeValue = volumeValue,
                            onVolumeChanged = onVolumeChanged,
                            onRefreshVolume = onRefreshVolume,
                            onCloseSheet = {
                                coroutineScope.launch {
                                    runCatching {
                                        sheetStateR.hide()
                                    }
                                }.invokeOnCompletion {
                                    // 点击桌面播放器内关闭按钮后恢复迷你播放条标题滚动，并同步隐藏完整播放器。
                                    playerChromeState.putMarqueeIterations(1)
                                    playerChromeState.hidePlayerSheet()
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun JvmMusicPlayerScreen(
    musicDetail: XyPlayMusic,
    picByte: ByteArray? = null,
    musicPlayerViewModel: MusicPlayerViewModel = koinViewModel<MusicPlayerViewModel>(),
    sharedCoverSourceBoundsOnScreen: Rect? = null,
    sharedCoverProgress: Float = 1f,
    volumeValue: Float,
    onVolumeChanged: (Float) -> Unit,
    onRefreshVolume: () -> Unit,
    onCloseSheet: () -> Unit,
    onSetState: (Boolean) -> Unit,
    lrcListState: LazyListState = rememberLazyListState(),
    similarPopularListState: LazyListState = rememberLazyListState(),
    horPagerState: PagerState
) {

    val decorators = LocalDesktopWindowDecorators.current
    val titleBarHitTestOwner = LocalDesktopTitleBarHitTestOwner.current
    val cacheScheduleData by musicPlayerViewModel.downloadCacheController.cacheSchedule.collectAsStateWithLifecycle()
    val coverRefreshVersion by musicPlayerViewModel.musicController.coverRefreshVersionFlow.collectAsStateWithLifecycle()
    val state by musicPlayerViewModel.musicController.stateFlow.collectAsStateWithLifecycle()
    val originMusicList by musicPlayerViewModel.musicController.originMusicListFlow.collectAsStateWithLifecycle()
    val lrcState by musicPlayerViewModel.lrcStateFlow.collectAsStateWithLifecycle()
    val favoriteList by musicPlayerViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val coverUrls = rememberPlayMusicCoverUrls(
        musicDetail,
        coverRefreshVersion
    )
    val coverModels = resolvePlayerCoverModels(coverUrls, picByte)
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
    val forwardOffsetText = stringResource(Res.string.forward_offset)
    val backwardOffsetText = stringResource(Res.string.backward_offset)
    val offsetText = stringResource(Res.string.offset)
    val resetText = stringResource(Res.string.reset)
    val confirmText = stringResource(Res.string.confirm)
    val artistLabel = remember(musicDetail.artists) {
        musicDetail.artistLabel()
    }
    val isCoverPlaybackActive = state == PlayStateEnum.Playing ||
            state == PlayStateEnum.Loading
    val isPlayerFullyOpened = sharedCoverProgress >= JvmMusicPlayerCoverRotationReadyProgress
    val shouldRotateCover = isCoverPlaybackActive && isPlayerFullyOpened
    val coverRotation = remember {
        Animatable(0f)
    }
    val showSharedCoverOverlay =
        sharedCoverSourceBoundsOnScreen != null &&
                playerRootBoundsOnScreen != null &&
                sharedCoverTargetBoundsOnScreen != null &&
                sharedCoverProgress > 0f &&
                sharedCoverProgress < 1f

    LaunchedEffect(musicDetail.itemId) {
        dismissJvmRightClickDropdownMenus()
        // 切歌时先用当前歌曲已保存的偏移初始化右键菜单预览值。
        musicPlayerViewModel.resetLyricsPreviewOffset(musicDetail.itemId)
        coverRotation.snapTo(0f)
    }
    LaunchedEffect(musicDetail.itemId, lrcState.lrcConfig?.itemId, lrcState.lrcConfig?.lrcOffsetMs) {
        // 歌词配置可能在真实歌词加载完成后才进入状态流，匹配当前歌曲后再同步预览值。
        if (lrcState.lrcConfig?.itemId == musicDetail.itemId) {
            musicPlayerViewModel.resetLyricsPreviewOffset(musicDetail.itemId)
        }
    }
    LaunchedEffect(Unit) {
        onRefreshVolume()
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
            PlayerCoverImage(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(),
                model = coverModels.model,
                backModel = coverModels.backModel,
                requestSize = sharedCoverRequestSize,
                alpha = 0.2f,
                showPlaceholder = false,
                contentDescription = stringResource(Res.string.album_cover),
            )
            JvmSharedCoverOverlay(
                sourceBoundsOnScreen = sharedCoverSourceBoundsOnScreen,
                targetBoundsOnScreen = sharedCoverTargetBoundsOnScreen,
                rootBoundsOnScreen = playerRootBoundsOnScreen,
                progress = sharedCoverProgress,
                model = coverModels.model,
                backModel = coverModels.backModel,
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
                DesktopWindowTitleCenterBar(
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
                                    modifier = Modifier
                                        .jvmPlayerTitleBarHitTarget(
                                            hitTestOwner,
                                            JvmMusicPlayerTitleBarHitTarget.Tab(index)
                                        )
                                        .jvmHoverDebounceClickable(),
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
                                        minOf(
                                            coverColumnWidth * JvmMusicPlayerCoverWidthRatio,
                                            maxHeight * JvmMusicPlayerCoverHeightRatio
                                        )
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
                                                PlayerCoverImage(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .graphicsLayer {
                                                            rotationZ = coverRotation.value
                                                        }
                                                        .clip(CircleShape),
                                                    model = coverModels.model,
                                                    backModel = coverModels.backModel,
                                                    requestSize = sharedCoverRequestSize,
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
                                                    // 桌面端用右键菜单承载歌词偏移预览和确认，避免修改通用歌词组件。
                                                    .jvmRightClickDropdownMenu(
                                                        dismissOnItemClick = false,
                                                        onShowRequest = {
                                                            musicPlayerViewModel.resetLyricsPreviewOffset(
                                                                musicDetail.itemId
                                                            )
                                                        },
                                                        onCloseRequest = {
                                                            musicPlayerViewModel.resetLyricsPreviewOffset(
                                                                musicDetail.itemId
                                                            )
                                                        },
                                                        itemDataList = {
                                                            listOf(
                                                                // 顶部只读展示当前预览偏移，右键菜单保持打开时会随预览值实时刷新。
                                                                MenuItemDefaultData(
                                                                    title = "$offsetText: ${
                                                                        formatJvmLyricsOffsetSeconds(
                                                                            musicPlayerViewModel.lyricsPreviewOffsetMs
                                                                        )
                                                                    }",
                                                                    enabled = false,
                                                                    dismissOnClick = false,
                                                                    onClick = {}
                                                                ),
                                                                MenuItemDefaultData(
                                                                    title = forwardOffsetText,
                                                                    onClick = {
                                                                        musicPlayerViewModel.adjustLyricsPreviewOffset(
                                                                            500L
                                                                        )
                                                                    }
                                                                ),
                                                                MenuItemDefaultData(
                                                                    title = backwardOffsetText,
                                                                    onClick = {
                                                                        musicPlayerViewModel.adjustLyricsPreviewOffset(
                                                                            -500L
                                                                        )
                                                                    }
                                                                ),
                                                                MenuItemDefaultData(
                                                                    title = resetText,
                                                                    onClick = {
                                                                        musicPlayerViewModel.resetLyricsPreviewOffsetToZero()
                                                                    }
                                                                ),
                                                                MenuItemDefaultData(
                                                                    title = confirmText,
                                                                    onClick = {
                                                                        // 只有确认后才把预览偏移写回歌词配置。
                                                                        musicPlayerViewModel.confirmLyricsPreviewOffset(
                                                                            musicDetail.itemId
                                                                        )
                                                                        dismissJvmRightClickDropdownMenus()
                                                                    }
                                                                )
                                                            )
                                                        }
                                                    ),
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
                                                        .jvmPlayerInteractiveHitTarget(
                                                            titleBarHitTestOwner,
                                                            JvmMusicPlayerHitTarget.Lyrics
                                                        )
                                                ) {
                                                    LrcViewNewCompose(
                                                        modifier = Modifier.fillMaxSize(),
                                                        listState = lrcListState,
                                                        // 不再传入 mock 数据，歌词组件按自身逻辑读取真实 LrcServer 数据。
                                                        externalOffsetMillis = musicPlayerViewModel.lyricsPreviewOffsetMs,
                                                        currentLineTopInset = JvmMusicPlayerLyricsItemHeight,
                                                        highlightScaleEnabled = false,
                                                        showConfigButton = false,
                                                        primaryFontSize = JvmMusicPlayerLyricsFontSize
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
                            volume = volumeValue,
                            onVolumeChanged = onVolumeChanged,
                            favoriteSet = favoriteList,
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
                                musicPlayerViewModel.musicController.invokingOnFavorite(playMusic.itemId)
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

private sealed class JvmMusicPlayerHitTarget(val id: String) {
    data object Lyrics : JvmMusicPlayerHitTarget("Lyrics")
}

// 将控件窗口坐标同步给原生命中测试层，命中这些区域时不触发窗口拖拽。
private fun Modifier.jvmPlayerTitleBarHitTarget(
    owner: DesktopInteractiveHitTestOwner,
    target: JvmMusicPlayerTitleBarHitTarget,
): Modifier = onGloballyPositioned { coordinates ->
    owner.updateBounds(target.id, coordinates.boundsInWindow())
}

private fun Modifier.jvmPlayerInteractiveHitTarget(
    owner: DesktopInteractiveHitTestOwner,
    target: JvmMusicPlayerHitTarget,
): Modifier = onGloballyPositioned { coordinates ->
    owner.updateBounds(target.id, coordinates.boundsInWindow())
}

private fun XyPlayMusic.artistLabel(): String {
    return artists?.joinToString().takeUnless { it.isNullOrBlank() } ?: "Artist"
}

// 右键菜单需要显式展示正负号，方便用户判断歌词相对播放进度的调整方向。
private fun formatJvmLyricsOffsetSeconds(offsetMs: Long): String {
    if (offsetMs == 0L) return "0s"

    // 保留整数秒的 .0，例如 +1.0s / -1.0s，避免偏移展示精度前后不一致。
    val secondsText = offsetMs.toSecondMs().toString()
    val sign = if (offsetMs > 0) "+" else ""
    return "${sign}${secondsText}s"
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
        PlayerCoverImage(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotationDegrees },
            model = model,
            backModel = backModel,
            requestSize = requestSize,
            contentDescription = stringResource(Res.string.album_cover),
        )
    }
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}
