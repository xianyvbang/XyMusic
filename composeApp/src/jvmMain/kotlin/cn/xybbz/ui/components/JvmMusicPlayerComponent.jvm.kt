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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.client.FavoriteCoordinator
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.compositionLocal.LocalDesktopWindowChromeController
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.config.image.rememberPlayMusicCoverUrls
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.PlayerModeEnum
import cn.xybbz.ui.components.lrc.LrcViewNewCompose
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.album_cover
import xymusic_kmp.composeapp.generated.resources.close_player_screen
import xymusic_kmp.composeapp.generated.resources.disc_placeholder
import xymusic_kmp.composeapp.generated.resources.favorite_24px
import xymusic_kmp.composeapp.generated.resources.favorite_border_24px
import xymusic_kmp.composeapp.generated.resources.favorite_button
import xymusic_kmp.composeapp.generated.resources.keyboard_arrow_down_24px
import xymusic_kmp.composeapp.generated.resources.more_vert_24px
import xymusic_kmp.composeapp.generated.resources.music_list
import xymusic_kmp.composeapp.generated.resources.next_track
import xymusic_kmp.composeapp.generated.resources.other_operations_button_suffix
import xymusic_kmp.composeapp.generated.resources.pause
import xymusic_kmp.composeapp.generated.resources.pause_24px
import xymusic_kmp.composeapp.generated.resources.play_arrow_24px
import xymusic_kmp.composeapp.generated.resources.playing
import xymusic_kmp.composeapp.generated.resources.previous_track
import xymusic_kmp.composeapp.generated.resources.queue_music_24px
import xymusic_kmp.composeapp.generated.resources.skip_next_24px
import xymusic_kmp.composeapp.generated.resources.skip_previous_24px
import xymusic_kmp.composeapp.generated.resources.unknown_artist
import kotlin.math.min
import kotlin.math.roundToInt
import cn.xybbz.ui.xy.XyIconButton as IconButton

internal val JvmMusicPlayerSharedCoverTargetSize = 300.dp
private const val JvmMusicPlayerSharedCoverDurationMillis = 920
private const val JvmMusicPlayerDialogEnterDurationMillis = 260


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmMusicPlayerComponent(
    music: XyPlayMusic,
    picByte: ByteArray? = null,
    sharedCoverSourceBoundsOnScreen: Rect? = null,
    sheetStateR: SheetState,
    toNext: () -> Unit,
    backNext: () -> Unit,
    onSetState: (Boolean) -> Unit
) {
    val mainViewModel = LocalMainViewModel.current
    val chromeController = LocalDesktopWindowChromeController.current
    val coroutineScope = rememberCoroutineScope()
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
            3
        }
    val listState = rememberLazyListState()
    val similarPopularListState = rememberLazyListState()
    LaunchedEffect(mainViewModel.sheetState) {
        overlayVisibleState.targetState = mainViewModel.sheetState
    }
    DisposableEffect(mainViewModel.sheetState, chromeController) {
        chromeController.setTitleBarHitTestEnabled(!mainViewModel.sheetState)
        onDispose {
            chromeController.setTitleBarHitTestEnabled(true)
        }
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
                scrimColor= Color.Transparent
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
                        onSeekToNext = toNext,
                        onSeekBack = backNext,
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


/**
 * 播放页面弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmMusicPlayerScreen(
    musicDetail: XyPlayMusic,
    picByte: ByteArray? = null,
    musicPlayerViewModel: MusicPlayerViewModel = koinViewModel<MusicPlayerViewModel>(),
    sharedCoverSourceBoundsOnScreen: Rect? = null,
    sharedCoverProgress: Float = 1f,
    onCloseSheet: () -> Unit,
    onSeekToNext: () -> Unit,
    onSeekBack: () -> Unit,
    onSetState: (Boolean) -> Unit,
    lrcListState: LazyListState = rememberLazyListState(),
    similarPopularListState: LazyListState = rememberLazyListState(),
    horPagerState: PagerState
) {


    val cacheScheduleData by musicPlayerViewModel.downloadCacheController.cacheSchedule.collectAsStateWithLifecycle()

    val favoriteList by musicPlayerViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val coverUrls = rememberPlayMusicCoverUrls(
        musicDetail,
        musicPlayerViewModel.musicController.coverRefreshVersion
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
    val showSharedCoverOverlay =
        sharedCoverSourceBoundsOnScreen != null &&
            playerRootBoundsOnScreen != null &&
            sharedCoverTargetBoundsOnScreen != null &&
            sharedCoverProgress > 0f &&
            sharedCoverProgress < 1f

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
            requestSize = sharedCoverRequestSize
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
            TopAppBarComponent(
                modifier = Modifier,
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        musicPlayerViewModel.dataList.forEachIndexed { index, item ->

                            TextButton(onClick = {
                                coroutineScope
                                    .launch {
                                        horPagerState.animateScrollToPage(index)
                                    }
                            }) {
                                Text(
                                    text = stringResource(item),
                                    maxLines = 2,
                                    color = if (horPagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onCloseSheet()
                        },
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.keyboard_arrow_down_24px),
                            contentDescription = stringResource(Res.string.close_player_screen)
                        )
                    }
                }, actions = {
                    IconButton(
                        onClick = {
                        },
                    ) {
                        // todo 加个隐藏按钮操作,打开开发者页面
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HorizontalPager(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(4f),
                    state = horPagerState,
                    // 在底部弹窗里关闭 Pager 的边缘回弹，避免翻页落位后再次左右抖动
                    overscrollEffect = null
                ) { page ->
                    when (page) {
                        0 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxSize()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(300.dp)
                                        .aspectRatio(1F)
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

                        }

                        1 -> {
                            LrcViewNewCompose(
                                listState = lrcListState,
                                onSetLrcOffset = { offsetMs ->
                                    coroutineScope.launch {
                                        musicPlayerViewModel.lrcServer.updateLrcConfig(offsetMs)
                                    }
                                }
                            )
                        }

                        else -> {
                            MusicPlayerSimilarPopularComponent(
                                listState = similarPopularListState,
                                onFavoriteSet = { emptySet() },
                                onDownloadMusicIds = { emptyList() },
                                playMusicList = musicPlayerViewModel.musicController.originMusicList,
                                onAddPlayMusic = { musicPlayerViewModel.addNextPlayer(it) }
                            )
                        }
                    }
                }


                XyColumn(
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                    /*.weight(2.8f)*/,
                    paddingValues = PaddingValues(0.dp),
                    clipSize = 0.dp,
                    backgroundColor = Color.Transparent
                ) {
//                    Spacer(modifier = Modifier.height(XyTheme.dimens.corner))
                    XyRow {
                        Column(
                            modifier = Modifier
                                .width(200.dp)
                        ) {
                            Text(
                                text = musicDetail.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.W700,
                                lineHeight = 31.86.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Visible,
                                modifier = Modifier
                                    .basicMarquee(iterations = Int.MAX_VALUE)
                            )

                            Text(
                                text = if (musicDetail.artists.isNullOrEmpty()) stringResource(Res.string.unknown_artist) else musicDetail.artists?.joinToString()
                                    ?: "",
                                color = Color(0xff7B7B8B),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.W400,
                                lineHeight = 15.93.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.basicMarquee(
                                    iterations = 0
                                )
                            )
                        }

                        JvmFavoriteMusicIconComponent(
                            musicDetail = musicDetail,
                            dataSourceManager = musicPlayerViewModel.dataSourceManager,
                            musicController = musicPlayerViewModel.musicController,
                            onFavoriteMusicIdSet = { favoriteList }
                        )
                        IconButton(
                            modifier = Modifier.offset(x = (10).dp),
                            onClick = {
                                coroutineScope.launch {
                                    val itemId =
                                        musicPlayerViewModel.musicController.musicInfo?.itemId
                                    itemId?.let {
                                        musicPlayerViewModel.dataSourceManager.selectMusicInfoById(
                                            it
                                        )?.show()
                                    }
                                }
                            },
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.more_vert_24px),
                                contentDescription = "${musicPlayerViewModel.musicController.musicInfo?.name}${
                                    stringResource(
                                        Res.string.other_operations_button_suffix
                                    )
                                }"
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = !musicPlayerViewModel.lrcServer.lrcText.isNullOrBlank() && horPagerState.currentPage == 0
                    ) {
                        XyRow(modifier = Modifier/*.height(30.dp)*/) {
                            LrcViewOneComponent(lrcText = musicPlayerViewModel.lrcServer.lrcText)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        JvmPlayerCurrentPosition(
                            musicController = musicPlayerViewModel.musicController,
                            onCacheProgress = {
                                cacheScheduleData
                            })
                        Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
                        XyRow(modifier = Modifier/*.weight(1f)*/) {
                            JvmPlayerTypeComponent(musicController = musicPlayerViewModel.musicController)
                            Icon(
                                painter = painterResource(Res.drawable.skip_previous_24px),
                                contentDescription = stringResource(Res.string.previous_track),
                                modifier = Modifier
                                    .size(30.dp, 35.dp)
                                    .debounceClickable {
                                        coroutineScope.launch {
                                            onSeekBack()
                                        }
                                    }
                            )

                            JvmPlayerStateComponent(musicController = musicPlayerViewModel.musicController)

                            Icon(
                                painter = painterResource(Res.drawable.skip_next_24px),
                                contentDescription = stringResource(Res.string.next_track),
                                modifier = Modifier
                                    .size(30.dp, 35.dp)
                                    .debounceClickable {
                                        coroutineScope.launch {
                                            onSeekToNext()
                                        }
                                    }
                            )

                            IconButton(
                                onClick = {
                                    if (musicPlayerViewModel.musicController.originMusicList.isNotEmpty()) {
                                        onSetState(true)
                                    }
                                },
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.queue_music_24px),
                                    contentDescription = stringResource(Res.string.music_list)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding * 4))
                    }
                }

            }

        }
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
    requestSize: IntSize
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

    XyImage(
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
            .clip(RoundedCornerShape(animatedCorner)),
        model = model,
        backModel = backModel,
        requestSize = requestSize,
        placeholder = Res.drawable.disc_placeholder,
        error = Res.drawable.disc_placeholder,
        fallback = Res.drawable.disc_placeholder,
        contentDescription = stringResource(Res.string.album_cover),
    )
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

/**
 * 播放类型
 */
@Composable
private fun JvmPlayerTypeComponent(
    musicController: MusicCommonController
) {
    val mainViewModel = LocalMainViewModel.current
    IconButton(
        onClick = {
            mainViewModel.setNowPlayerTypeData()
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (musicController.playMode.code == PlayerModeEnum.SINGLE_LOOP.code) {
                Text(text = "1", fontSize = 10.sp)
            }
            Icon(
                painter = painterResource(mainViewModel.iconList[musicController.playMode.code].icon),
                contentDescription = stringResource(mainViewModel.iconList[musicController.playMode.code].message),
            )
        }

    }
}

/**
 * 音乐进度
 */
@Composable
private fun JvmPlayerCurrentPosition(
    musicController: MusicCommonController,
    onCacheProgress: () -> Float
) {

    val currentPosition by musicController.progressStateFlow.collectAsStateWithLifecycle()

    XyColumn(
        backgroundColor = Color.Transparent,
        clipSize = 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            MusicProgressBar(
                currentTime = currentPosition,
                progressStateFlow = musicController.progressStateFlow,
                totalTime = musicController.duration,
                cacheProgress = onCacheProgress(),
                onProgressChanged = { newProgress ->
                    musicController.seekTo(
                        (musicController.duration * newProgress).roundToInt().toLong()
                    )
                })
        }
    }
}

/**
 * 音乐状态
 */
@Composable
fun JvmPlayerStateComponent(
    size: Dp = 60.dp,
    musicController: MusicCommonController
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
    ) {

        Icon(
            painter = painterResource(
                if (musicController.state == PlayStateEnum.Playing
                    || musicController.state == PlayStateEnum.Loading
                ) Res.drawable.pause_24px else Res.drawable.play_arrow_24px
            ),
            contentDescription = if (musicController.state == PlayStateEnum.Playing) stringResource(
                Res.string.playing
            ) else stringResource(Res.string.pause),
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .clickable {
                    if (musicController.state != PlayStateEnum.Pause) {
                        musicController.pause()
                    } else {
                        musicController.resume()
                    }

                }
        )
        if (musicController.state == PlayStateEnum.Loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .clip(CircleShape),
                strokeWidth = size
            )
        }
    }
}

/**
 * 音乐收藏
 */
@Composable
private fun JvmFavoriteMusicIconComponent(
    musicDetail: XyPlayMusic,
    dataSourceManager: DataSourceManager,
    musicController: MusicCommonController,
    onFavoriteMusicIdSet: () -> List<String>
) {
    val coroutineScope = rememberCoroutineScope()

    IconButton(
        onClick = {
            coroutineScope.launch {
                FavoriteCoordinator.setFavoriteData(
                    dataSourceManager = dataSourceManager,
                    type = MusicTypeEnum.MUSIC,
                    itemId = musicDetail.itemId,
                    ifFavorite = musicDetail.itemId in onFavoriteMusicIdSet(),
                    musicController = musicController
                )
            }
        },
    ) {
        Icon(
            modifier = Modifier
                .size(60.dp),
            painter =
                painterResource(
                    if (musicDetail.itemId in onFavoriteMusicIdSet())
                        Res.drawable.favorite_border_24px
                    else
                        Res.drawable.favorite_24px
                ),
            contentDescription = stringResource(Res.string.favorite_button),
            tint = if (musicDetail.itemId in onFavoriteMusicIdSet()) Color.Red else LocalContentColor.current
        )
    }

}
