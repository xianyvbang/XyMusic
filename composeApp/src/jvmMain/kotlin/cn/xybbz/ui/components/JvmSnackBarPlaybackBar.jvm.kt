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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.config.image.rememberPlayMusicCoverUrls
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.PlayerModeEnum
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopInteractiveHitTestOwner
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XySmallImage
import cn.xybbz.ui.xy.XyText
import cn.xybbz.viewmodel.MusicBottomMenuViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.favorite_24px
import xymusic_kmp.composeapp.generated.resources.favorite_border_24px
import xymusic_kmp.composeapp.generated.resources.favorite_button
import xymusic_kmp.composeapp.generated.resources.info_24px
import xymusic_kmp.composeapp.generated.resources.list_loop
import xymusic_kmp.composeapp.generated.resources.music_cover
import xymusic_kmp.composeapp.generated.resources.music_list
import xymusic_kmp.composeapp.generated.resources.music_xy_placeholder_foreground
import xymusic_kmp.composeapp.generated.resources.next_track
import xymusic_kmp.composeapp.generated.resources.pause
import xymusic_kmp.composeapp.generated.resources.pause_24px
import xymusic_kmp.composeapp.generated.resources.play_arrow_24px
import xymusic_kmp.composeapp.generated.resources.playing
import xymusic_kmp.composeapp.generated.resources.previous_track
import xymusic_kmp.composeapp.generated.resources.queue_music_24px
import xymusic_kmp.composeapp.generated.resources.repeat_24px
import xymusic_kmp.composeapp.generated.resources.repeat_one_24px
import xymusic_kmp.composeapp.generated.resources.shuffle_24px
import xymusic_kmp.composeapp.generated.resources.shuffle_play
import xymusic_kmp.composeapp.generated.resources.single_loop
import xymusic_kmp.composeapp.generated.resources.skip_next_24px
import xymusic_kmp.composeapp.generated.resources.skip_previous_24px
import xymusic_kmp.composeapp.generated.resources.song_info
import xymusic_kmp.composeapp.generated.resources.volume_up_24px
import xymusic_kmp.composeapp.generated.resources.volume_value_setting
import kotlin.math.roundToInt
import cn.xybbz.ui.xy.XyIconButton as IconButton

private val JvmSnackBarCoverSize = 56.dp
private val JvmSnackBarControlMaxWidth = 420.dp
private val JvmSnackBarPlayButtonWidth = 56.dp
private val JvmSnackBarPlayButtonHeight = 32.dp
private val JvmSnackBarPlayIconSize = 40.dp
private val JvmSnackBarIconButtonSize = 32.dp
private val JvmSnackBarVolumePopupWidth = 60.dp
private val JvmSnackBarVolumePopupHeight = 188.dp
private val JvmSnackBarVolumeSliderWidth = 24.dp
private val JvmSnackBarVolumeSliderHeight = 128.dp
private val JvmSnackBarVolumePopupGap = 12.dp
private val JvmSnackBarVolumePopupOffsetX =
    (JvmSnackBarVolumePopupWidth - JvmSnackBarIconButtonSize) / -2
private const val JvmSnackBarVolumeSliderBarWidth = 5f
private const val JvmSnackBarVolumeSliderThumbRadius = 6f

/**
 * JVM 桌面端共用的底部播放栏组件。
 *
 * 这个组件最初来自 `JvmSnackBarPlayerComponent` 的普通播放态底栏，
 * 现在被抽成独立文件，供底部悬浮播放条和完整播放器页面共同复用。
 *
 * 布局职责分为三块：
 * 1. 左侧展示封面、歌曲标题，以及收藏/歌曲信息快捷操作。
 * 2. 中间展示播放模式、上下曲、播放暂停、音量和进度条。
 * 3. 右侧保留播放列表入口，避免和主控制区混在一起。
 *
 * 调用方只需要传入当前播放控制器、音量控制 ViewModel，以及各业务行为回调，
 * 就可以复用统一的桌面端播放栏视觉和交互。
 *
 * 通过 `showCover` 可以控制左侧是否显示封面图，方便完整播放器页在已有大封面时复用同一套底栏。
 */
@Composable
internal fun JvmSnackBarPlaybackBar(
    modifier: Modifier = Modifier,
    musicController: MusicCommonController,
    musicBottomMenuViewModel: MusicBottomMenuViewModel,
    favoriteSet: List<String>,
    sharedCoverRequestSize: IntSize,
    showCover: Boolean = true,
    cacheProgress: Float = 0f,
    desktopDragHitTestOwner: DesktopInteractiveHitTestOwner? = null,
    onSharedCoverBoundsChanged: (Rect) -> Unit = {},
    onShowPlayer: (() -> Unit)? = null,
    onShowPlaylist: () -> Unit,
    onToggleFavorite: suspend (XyPlayMusic) -> Unit,
    onShowMusicInfo: suspend (XyPlayMusic) -> Unit
) {
    val mainViewModel = LocalMainViewModel.current
    val coroutineScope = rememberCoroutineScope()
    val playbackState by musicController.playbackStateFlow.collectAsStateWithLifecycle()
    val originMusicList by musicController.originMusicListFlow.collectAsStateWithLifecycle()
    val currentMusic = playbackState.musicInfo
    val snackBarTitle = currentMusic.snackBarTitleAnnotatedString(
        spanStyle = SpanStyle(
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
    val openPlayerModifier = if (onShowPlayer != null) {
        Modifier.debounceClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            if (currentMusic != null) {
                onShowPlayer()
            }
        }
    } else {
        Modifier
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val sectionWidth = maxWidth / 3

        XyRow(
            modifier = Modifier.then(openPlayerModifier),
            paddingValues = PaddingValues(bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ListItem(
                modifier = Modifier
                    .width(sectionWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(XyTheme.dimens.corner))
                    .desktopDragHitTarget(desktopDragHitTestOwner, "SongInfo"),
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = if (showCover) {
                    {
                        JvmImageCover(
                            modifier = Modifier
                                .size(JvmSnackBarCoverSize)
                                .clip(RoundedCornerShape(XyTheme.dimens.corner)),
                            musicController = musicController,
                            onBoundsChanged = onSharedCoverBoundsChanged,
                            requestSize = sharedCoverRequestSize,
                        )
                    }
                } else {
                    null
                },
                headlineContent = {
                    XyText(
                        text = snackBarTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee(iterations = mainViewModel.iterations)
                    )
                },
                supportingContent = {
                    Row(
                        modifier = Modifier.padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding / 3),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        JvmSnackBarIconButton(
                            iconRes = if (currentMusic?.itemId in favoriteSet) {
                                Res.drawable.favorite_border_24px
                            } else {
                                Res.drawable.favorite_24px
                            },
                            contentDescription = stringResource(Res.string.favorite_button),
                            tint = if (currentMusic?.itemId in favoriteSet) {
                                Color.Red
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            enabled = currentMusic != null,
                            onClick = {
                                currentMusic ?: return@JvmSnackBarIconButton
                                coroutineScope.launch {
                                    onToggleFavorite(currentMusic)
                                }
                            }
                        )
                        JvmSnackBarIconButton(
                            iconRes = Res.drawable.info_24px,
                            contentDescription = stringResource(Res.string.song_info),
                            enabled = currentMusic != null,
                            onClick = {
                                currentMusic ?: return@JvmSnackBarIconButton
                                coroutineScope.launch {
                                    onShowMusicInfo(currentMusic)
                                }
                            }
                        )
                    }
                }
            )

            JvmSnackBarControlSection(
                modifier = Modifier
                    .width(sectionWidth)
                    .widthIn(max = JvmSnackBarControlMaxWidth),
                musicController = musicController,
                musicBottomMenuViewModel = musicBottomMenuViewModel,
                cacheProgress = cacheProgress,
                desktopDragHitTestOwner = desktopDragHitTestOwner
            )

            Row(
                modifier = Modifier
                    .width(sectionWidth)
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                JvmSnackBarIconButton(
                    iconRes = Res.drawable.queue_music_24px,
                    contentDescription = stringResource(Res.string.music_list),
                    enabled = originMusicList.isNotEmpty(),
                    desktopDragHitTestOwner = desktopDragHitTestOwner,
                    desktopDragHitTargetId = "PlaylistButton",
                    onClick = onShowPlaylist
                )
            }
        }
    }
}

/**
 * 播放栏中间控制区。
 *
 * 这里统一承载桌面端最常用的播放操作，并把进度条放到第二行，
 * 让按钮区和进度区视觉上更稳定，也便于 `SnackBar` 和完整播放器页共用。
 */
@Composable
private fun JvmSnackBarControlSection(
    modifier: Modifier,
    musicController: MusicCommonController,
    musicBottomMenuViewModel: MusicBottomMenuViewModel,
    cacheProgress: Float,
    desktopDragHitTestOwner: DesktopInteractiveHitTestOwner?
) {
    val mainViewModel = LocalMainViewModel.current
    val currentProgress by musicController.progressStateFlow.collectAsStateWithLifecycle()
    val playbackState by musicController.playbackStateFlow.collectAsStateWithLifecycle()
    var showVolumePopup by remember {
        mutableStateOf(false)
    }
    val playModeIcon = playbackState.playMode.toPlayModeIcon()
    val playModeDescription = when (playbackState.playMode) {
        PlayerModeEnum.SINGLE_LOOP -> stringResource(Res.string.single_loop)
        PlayerModeEnum.SEQUENTIAL_PLAYBACK -> stringResource(Res.string.list_loop)
        PlayerModeEnum.RANDOM_PLAY -> stringResource(Res.string.shuffle_play)
    }
    val isPlaying =
        playbackState.state == PlayStateEnum.Playing || playbackState.state == PlayStateEnum.Loading

    XyColumn(
        modifier = modifier.fillMaxHeight(),
        paddingValues = PaddingValues(),
        backgroundColor = Color.Transparent,
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JvmSnackBarIconButton(
                iconRes = playModeIcon,
                contentDescription = playModeDescription,
                enabled = playbackState.musicInfo != null,
                desktopDragHitTestOwner = desktopDragHitTestOwner,
                desktopDragHitTargetId = "PlayModeButton",
                onClick = mainViewModel::setNowPlayerTypeData
            )
            JvmSnackBarIconButton(
                iconRes = Res.drawable.skip_previous_24px,
                contentDescription = stringResource(Res.string.previous_track),
                enabled = playbackState.musicInfo != null,
                desktopDragHitTestOwner = desktopDragHitTestOwner,
                desktopDragHitTargetId = "PreviousButton",
                onClick = musicController::seekToPrevious
            )
            IconButton(
                onClick = {
                    if (isPlaying) {
                        musicController.pause()
                    } else {
                        musicController.resume()
                    }
                },
                enabled = playbackState.musicInfo != null,
                modifier = Modifier
                    .desktopDragHitTarget(desktopDragHitTestOwner, "PlayPauseButton")
                    .clip(RoundedCornerShape(JvmSnackBarPlayButtonHeight))
                    .width(JvmSnackBarPlayButtonWidth)
                    .height(JvmSnackBarPlayButtonHeight)
                    .background(MaterialTheme.colorScheme.onSurface)
            ) {
                Icon(
                    painter = painterResource(
                        if (isPlaying) Res.drawable.pause_24px else Res.drawable.play_arrow_24px
                    ),
                    contentDescription = if (isPlaying) {
                        stringResource(Res.string.pause)
                    } else {
                        stringResource(Res.string.playing)
                    },
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(JvmSnackBarPlayIconSize)
                )
            }
            JvmSnackBarIconButton(
                iconRes = Res.drawable.skip_next_24px,
                contentDescription = stringResource(Res.string.next_track),
                enabled = playbackState.musicInfo != null,
                desktopDragHitTestOwner = desktopDragHitTestOwner,
                desktopDragHitTargetId = "NextButton",
                onClick = musicController::seekToNext
            )
            Box(
                contentAlignment = Alignment.Center
            ) {
                JvmSnackBarIconButton(
                    iconRes = Res.drawable.volume_up_24px,
                    contentDescription = stringResource(Res.string.volume_value_setting),
                    enabled = playbackState.musicInfo != null,
                    desktopDragHitTestOwner = desktopDragHitTestOwner,
                    desktopDragHitTargetId = "VolumeButton",
                    onClick = {
                        showVolumePopup = !showVolumePopup
                    }
                )

                JvmSnackBarVolumePopup(
                    expanded = showVolumePopup,
                    volume = musicBottomMenuViewModel.volumeValue,
                    onDismissRequest = { showVolumePopup = false },
                    onVolumeChanged = musicBottomMenuViewModel::updateVolume
                )
            }
        }

        MusicProgressBarHorizontal(
            currentTime = currentProgress,
            progressStateFlow = musicController.progressStateFlow,
            totalTime = playbackState.duration,
            cacheProgress = cacheProgress,
            onProgressChanged = { progress ->
                musicController.seekTo((playbackState.duration * progress).toLong())
            },
            modifier = Modifier
                .fillMaxWidth()
                .desktopDragHitTarget(desktopDragHitTestOwner, "ProgressBar"),
            progressBarColor = MaterialTheme.colorScheme.onSurface,
            cacheProgressBarColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            backgroundBarColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            barHeight = 3f,
            thumbRadius = 4f,
            timeTextStyle = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

/**
 * 音量悬浮面板。
 *
 * 组件会锚定在音量按钮上方，内部使用竖向滑条，交互方向和系统音量习惯保持一致。
 */
@Composable
private fun JvmSnackBarVolumePopup(
    expanded: Boolean,
    volume: Float,
    onDismissRequest: () -> Unit,
    onVolumeChanged: (Float) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(
            x = JvmSnackBarVolumePopupOffsetX,
            y = -(JvmSnackBarVolumePopupHeight + JvmSnackBarIconButtonSize + JvmSnackBarVolumePopupGap + XyTheme.dimens.outerVerticalPadding * 2)
        ),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = Modifier.width(JvmSnackBarVolumePopupWidth)
    ) {
        Column(
            modifier = Modifier
                .width(JvmSnackBarVolumePopupWidth)
                .padding(vertical = XyTheme.dimens.contentPadding / 2),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            JvmSnackBarVerticalSlider(
                progress = volume,
                onProgressChanged = onVolumeChanged,
                modifier = Modifier
                    .width(JvmSnackBarVolumeSliderWidth)
                    .height(JvmSnackBarVolumeSliderHeight)
            )
            Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
            Text(
                text = "${(volume * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
            Icon(
                painter = painterResource(Res.drawable.volume_up_24px),
                contentDescription = stringResource(Res.string.volume_value_setting)
            )
        }
    }
}

/**
 * JVM 底栏专用竖向滑条。
 *
 * 单独实现点击和拖拽命中逻辑，避免复用横向进度条时产生坐标换算误差。
 */
@Composable
private fun JvmSnackBarVerticalSlider(
    progress: Float,
    onProgressChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val sliderColor = MaterialTheme.colorScheme.onSurface
    val sliderBackgroundColor = sliderColor.copy(alpha = 0.14f)

    var isDragging by remember {
        mutableStateOf(false)
    }
    var updatedProgress by remember(progress) {
        mutableFloatStateOf(progress)
    }
    val animatedProgress by animateFloatAsState(
        targetValue = if (isDragging) updatedProgress else progress,
        label = "JvmSnackBarVolumeProgress"
    )

    fun updateByOffset(offsetY: Float, height: Float) {
        val safeHeight = height.takeIf { it > 0f } ?: return
        val newProgress = (1f - offsetY / safeHeight).coerceIn(0f, 1f)
        updatedProgress = newProgress
        onProgressChanged(newProgress)
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    updateByOffset(offset.y, size.height.toFloat())
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        updateByOffset(offset.y, size.height.toFloat())
                    },
                    onDrag = { change, _ ->
                        updateByOffset(change.position.y, size.height.toFloat())
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false }
                )
            }
    ) {
        val centerX = size.width / 2
        val bottomY = size.height
        val progressY = bottomY - bottomY * animatedProgress

        drawLine(
            color = sliderBackgroundColor,
            start = Offset(centerX, bottomY),
            end = Offset(centerX, 0f),
            strokeWidth = JvmSnackBarVolumeSliderBarWidth.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = sliderColor,
            start = Offset(centerX, bottomY),
            end = Offset(centerX, progressY),
            strokeWidth = JvmSnackBarVolumeSliderBarWidth.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawCircle(
            color = sliderColor,
            radius = JvmSnackBarVolumeSliderThumbRadius.dp.toPx(),
            center = Offset(centerX, progressY)
        )
    }
}

/**
 * 底部播放栏使用的轻量图标按钮。
 *
 * 统一收敛按钮尺寸、着色和交互形式，保证两处复用时细节一致。
 */
@Composable
private fun JvmSnackBarIconButton(
    iconRes: DrawableResource,
    contentDescription: String,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    desktopDragHitTestOwner: DesktopInteractiveHitTestOwner? = null,
    desktopDragHitTargetId: String = contentDescription,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(JvmSnackBarIconButtonSize)
            .desktopDragHitTarget(desktopDragHitTestOwner, desktopDragHitTargetId)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

private fun Modifier.desktopDragHitTarget(
    owner: DesktopInteractiveHitTestOwner?,
    targetId: String,
): Modifier {
    if (owner == null) return this

    // 播放器整页作为原生拖拽热区时，按钮、歌曲信息和进度条需要继续交给 Compose 处理。
    return onGloballyPositioned { coordinates ->
        owner.updateBounds(targetId, coordinates.boundsInWindow())
    }
}

/**
 * 把当前播放歌曲整理成底栏使用的标题文案。
 *
 * 显示形式为“歌曲名 - 艺术家”，其中艺术家部分使用次级文案样式。
 */
private fun XyPlayMusic?.snackBarTitleAnnotatedString(
    spanStyle: SpanStyle
): AnnotatedString {
    val music = this ?: return AnnotatedString("")
    val artistsText = music.artists?.joinToString("/")?.takeIf { it.isNotBlank() }

    return buildAnnotatedString {
        append(music.name)
        if (artistsText != null) {
            withStyle(spanStyle) {
                append(" - ")
                append(artistsText)
            }
        }
    }
}

private fun PlayerModeEnum.toPlayModeIcon(): DrawableResource {
    return when (this) {
        PlayerModeEnum.SINGLE_LOOP -> Res.drawable.repeat_one_24px
        PlayerModeEnum.SEQUENTIAL_PLAYBACK -> Res.drawable.repeat_24px
        PlayerModeEnum.RANDOM_PLAY -> Res.drawable.shuffle_24px
    }
}

/**
 * 播放栏左侧封面组件。
 *
 * 负责加载当前歌曲封面。
 */
@Composable
private fun JvmImageCover(
    modifier: Modifier = Modifier,
    musicController: MusicCommonController,
    onBoundsChanged: (Rect) -> Unit = {},
    requestSize: IntSize,
) {
    val playbackState by musicController.playbackStateFlow.collectAsStateWithLifecycle()
    val coverUrls = rememberPlayMusicCoverUrls(
        playbackState.musicInfo,
        playbackState.coverRefreshVersion
    )
    val primaryCoverModel = coverUrls.primaryUrl
    val fallbackCoverModel = coverUrls.fallbackUrl
    val byteCoverModel = playbackState.picByte
    val activeCoverModel = primaryCoverModel ?: fallbackCoverModel ?: byteCoverModel
    val backupCoverModel = if (activeCoverModel == byteCoverModel) null else byteCoverModel

    XySmallImage(
        modifier = modifier.onGloballyPositioned { coordinates ->
            onBoundsChanged(
                Rect(
                    offset = coordinates.positionOnScreen(),
                    size = Size(
                        width = coordinates.size.width.toFloat(),
                        height = coordinates.size.height.toFloat()
                    )
                )
            )
        },
        model = activeCoverModel,
        backModel = backupCoverModel,
        requestSize = requestSize,
        placeholder = Res.drawable.music_xy_placeholder_foreground,
        error = Res.drawable.music_xy_placeholder_foreground,
        fallback = Res.drawable.music_xy_placeholder_foreground,
        contentDescription = stringResource(Res.string.music_cover),
    )
}
