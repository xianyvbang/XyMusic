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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.ResourcesUtils.readPaletteColor
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.image.rememberPlayMusicCoverUrls
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.PlayerModeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XySmallImage
import cn.xybbz.ui.xy.XyText
import cn.xybbz.viewmodel.MusicBottomMenuViewModel
import cn.xybbz.viewmodel.SnackBarPlayerViewModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.add_to_playlist
import xymusic_kmp.composeapp.generated.resources.delete_24px
import xymusic_kmp.composeapp.generated.resources.delete_local_permanently
import xymusic_kmp.composeapp.generated.resources.delete_permanently
import xymusic_kmp.composeapp.generated.resources.download_24px
import xymusic_kmp.composeapp.generated.resources.download_list
import xymusic_kmp.composeapp.generated.resources.favorite_24px
import xymusic_kmp.composeapp.generated.resources.favorite_border_24px
import xymusic_kmp.composeapp.generated.resources.favorite_button
import xymusic_kmp.composeapp.generated.resources.heart_broken_24px
import xymusic_kmp.composeapp.generated.resources.info_24px
import xymusic_kmp.composeapp.generated.resources.list_loop
import xymusic_kmp.composeapp.generated.resources.music_cover
import xymusic_kmp.composeapp.generated.resources.music_list
import xymusic_kmp.composeapp.generated.resources.music_remove_from_playlist
import xymusic_kmp.composeapp.generated.resources.music_xy_placeholder_foreground
import xymusic_kmp.composeapp.generated.resources.pause
import xymusic_kmp.composeapp.generated.resources.pause_24px
import xymusic_kmp.composeapp.generated.resources.play_arrow_24px
import xymusic_kmp.composeapp.generated.resources.play_selected
import xymusic_kmp.composeapp.generated.resources.playing
import xymusic_kmp.composeapp.generated.resources.playlist_add_24px
import xymusic_kmp.composeapp.generated.resources.playlist_play_24px
import xymusic_kmp.composeapp.generated.resources.playlist_remove_24px
import xymusic_kmp.composeapp.generated.resources.please_select
import xymusic_kmp.composeapp.generated.resources.queue_music_24px
import xymusic_kmp.composeapp.generated.resources.repeat_24px
import xymusic_kmp.composeapp.generated.resources.repeat_one_24px
import xymusic_kmp.composeapp.generated.resources.shuffle_24px
import xymusic_kmp.composeapp.generated.resources.shuffle_play
import xymusic_kmp.composeapp.generated.resources.single_loop
import xymusic_kmp.composeapp.generated.resources.skip_next_24px
import xymusic_kmp.composeapp.generated.resources.skip_previous_24px
import xymusic_kmp.composeapp.generated.resources.song_info
import xymusic_kmp.composeapp.generated.resources.unfavorite
import xymusic_kmp.composeapp.generated.resources.volume_up_24px
import xymusic_kmp.composeapp.generated.resources.volume_value_setting
import kotlin.math.roundToInt
import cn.xybbz.ui.xy.XyIconButton as IconButton

// 底部播放栏里相对固定的尺寸统一收敛在这里，避免散落魔法值。
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmSnackBarPlayerComponent(
    modifier: Modifier = Modifier,
    snackBarPlayerViewModel: SnackBarPlayerViewModel = koinViewModel<SnackBarPlayerViewModel>(),
    musicBottomMenuViewModel: MusicBottomMenuViewModel = koinViewModel<MusicBottomMenuViewModel>(),
    onClick: () -> Unit
) {
    val mainViewModel = LocalMainViewModel.current
    val navigator = LocalNavigator.current
    var musicListState by remember {
        mutableStateOf(false)
    }
    var currentMusicInfo by remember {
        mutableStateOf<XyMusic?>(null)
    }
    var showMusicInfo by remember {
        mutableStateOf(false)
    }
    val coroutineScope = rememberCoroutineScope()
    val ifOpenSelect by snackBarPlayerViewModel.selectControl.uiState.collectAsStateWithLifecycle()
    val favoriteSet by mainViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())

    val defaultSnackBarColor = MaterialTheme.colorScheme.surfaceContainerLowest

    var colorPurple by remember {
        mutableStateOf(defaultSnackBarColor)
    }
    val isDarkTheme = XyTheme.configs.isDarkTheme
    val animatedColor by animateColorAsState(
        targetValue = colorPurple,
        animationSpec = tween(durationMillis = 800),
        label = "backgroundColorAnim"
    )

    val permissionState = downloadPermission {
        snackBarPlayerViewModel.downloadMusics()
    }

    val playerSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val pleaseSelect = stringResource(Res.string.please_select)

    LaunchedEffect(Unit) {
        musicBottomMenuViewModel.refreshVolume()
    }

    MusicBottomMenuComponent(
        onAlbumRouter = { albumId ->
            navigator.navigate(AlbumInfo(albumId, MusicDataTypeEnum.ALBUM))
        }, onPlayerSheetClose = {
            coroutineScope.launch {
                playerSheetState.hide()
            }.invokeOnCompletion {
                mainViewModel.putIterations(1)
                mainViewModel.putSheetState(false)
            }
        })

    currentMusicInfo?.let { musicInfo ->
        MusicInfoBottomComponent(
            musicInfo = musicInfo,
            onIfShowMusicInfo = { showMusicInfo },
            onSetShowMusicInfo = { showMusicInfo = it },
            dataSourceType = snackBarPlayerViewModel.dataSourceManager.dataSourceType
        )
    }

    snackBarPlayerViewModel.musicController.musicInfo?.let {
        MusicPlayerComponent(
            music = it,
            snackBarPlayerViewModel.musicController.picByte,
            sheetStateR = playerSheetState,
            toNext = {
                Log.i("=====", "数据调用JvmSnackBarPlayerComponent")
                snackBarPlayerViewModel.musicController.seekToNext()
            }, backNext = {
                snackBarPlayerViewModel.musicController.seekToPrevious()
            }, onSetState = { musicListState = it })
    }

    MusicListComponent(
        musicListState = musicListState,
        curOriginIndex = snackBarPlayerViewModel.musicController.curOriginIndex,
        originMusicList = snackBarPlayerViewModel.musicController.originMusicList,
        onSetState = { musicListState = it },
        onClearPlayerList = {
            coroutineScope.launch {
                snackBarPlayerViewModel.clearPlayer()

            }.invokeOnCompletion {
                snackBarPlayerViewModel.musicController.clearPlayerList()
                colorPurple = defaultSnackBarColor
            }
        },
        onSeekToIndex = {
            snackBarPlayerViewModel.musicController.seekToIndex(it)
        },
        onRemovePlayerMusicItem = {
            try {
                snackBarPlayerViewModel.musicController.removeItem(it)
                if (snackBarPlayerViewModel.musicController.originMusicList.isEmpty()) {
                    mainViewModel.putSheetState(false)
                    coroutineScope.launch {
                        mainViewModel.db.playerDao.removeByDatasource()
                    }
                    colorPurple = defaultSnackBarColor
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(XyTheme.dimens.snackBarPlayerHeight)
            .zIndex(Float.MAX_VALUE)
            .background(defaultSnackBarColor)
            .drawBehind {
                drawRect(animatedColor)
            }
    ) {
        AnimatedContent(
            targetState = ifOpenSelect,
            transitionSpec = {
                if (targetState > initialState) {
                    fadeIn() togetherWith fadeOut()
                } else {
                    fadeIn() togetherWith fadeOut()
                }.using(
                    SizeTransform(clip = false)
                )
            }, label = "animated content"
        ) { select ->
            if (select) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (snackBarPlayerViewModel.dataSourceManager.getCanDelete())
                        IconButton(onClick = {

                            if (snackBarPlayerViewModel.selectControl.ifSelectEmpty()) {
                                MessageUtils.sendPopTip(pleaseSelect)
                            } else {
                                coroutineScope.launch {
                                    snackBarPlayerViewModel.selectControl.onRemoveSelectListResource.invoke(
                                        snackBarPlayerViewModel.dataSourceManager,
                                        coroutineScope
                                    )
                                }

                            }
                        }, enabled = snackBarPlayerViewModel.selectControl.ifEnableButton) {
                            Icon(
                                painter = painterResource(Res.drawable.delete_24px),
                                contentDescription = if (snackBarPlayerViewModel.selectControl.ifLocal) stringResource(
                                    Res.string.delete_local_permanently
                                ) else stringResource(Res.string.delete_permanently)
                            )
                        }

                    IconButton(onClick = {
                        if (snackBarPlayerViewModel.selectControl.ifSelectEmpty()) {
                            MessageUtils.sendPopTip(pleaseSelect)
                        } else {
                            coroutineScope.launch {
                                snackBarPlayerViewModel.selectControl.onAddPlaySelect(
                                    snackBarPlayerViewModel.musicController,
                                    snackBarPlayerViewModel.db,
                                    snackBarPlayerViewModel.downloadDb
                                )
                            }
                        }
                    }, enabled = snackBarPlayerViewModel.selectControl.ifEnableButton) {
                        Icon(
                            painter = painterResource(Res.drawable.playlist_play_24px),
                            contentDescription = stringResource(Res.string.play_selected)
                        )
                    }

                    IconButton(onClick = {
                        if (snackBarPlayerViewModel.selectControl.ifSelectEmpty()) {
                            MessageUtils.sendPopTip(pleaseSelect)
                        } else {
                            snackBarPlayerViewModel.selectControl.onAddPlaylistSelect()
                        }
                    }, enabled = snackBarPlayerViewModel.selectControl.ifEnableButton) {
                        Icon(
                            painter = painterResource(Res.drawable.playlist_add_24px),
                            contentDescription = stringResource(Res.string.add_to_playlist)
                        )
                    }
                    if (snackBarPlayerViewModel.selectControl.ifPlaylist)
                        IconButton(onClick = {
                            if (snackBarPlayerViewModel.selectControl.ifSelectEmpty()) {
                                MessageUtils.sendPopTip(pleaseSelect)
                            } else
                                snackBarPlayerViewModel.selectControl.onRemovePlaylistMusic(
                                    snackBarPlayerViewModel.dataSourceManager,
                                    coroutineScope
                                )
                        }, enabled = snackBarPlayerViewModel.selectControl.ifEnableButton) {
                            Icon(
                                painter = painterResource(Res.drawable.playlist_remove_24px),
                                contentDescription = stringResource(Res.string.music_remove_from_playlist)
                            )
                        }

                    IconButton(onClick = {
                        if (snackBarPlayerViewModel.selectControl.ifSelectEmpty()) {
                            MessageUtils.sendPopTip(pleaseSelect)
                        } else
                            snackBarPlayerViewModel.selectControl.onRemoveFavorite(
                                snackBarPlayerViewModel.dataSourceManager,
                                coroutineScope,
                                snackBarPlayerViewModel.musicController
                            )
                    }, enabled = snackBarPlayerViewModel.selectControl.ifEnableButton) {
                        Icon(
                            painter = painterResource(Res.drawable.heart_broken_24px),
                            contentDescription = stringResource(Res.string.unfavorite)
                        )
                    }

                    if (!snackBarPlayerViewModel.selectControl.ifLocal && snackBarPlayerViewModel.dataSourceManager.getCanDownload())
                        IconButton(onClick = {
                            if (snackBarPlayerViewModel.selectControl.ifSelectEmpty()) {
                                MessageUtils.sendPopTip(pleaseSelect)
                            } else {
                                permissionState.launchMultiplePermissionRequest()
                            }

                        }, enabled = snackBarPlayerViewModel.selectControl.ifEnableButton) {
                            Icon(
                                painter = painterResource(Res.drawable.download_24px),
                                contentDescription = stringResource(Res.string.download_list)
                            )
                        }
                }
            } else {
                JvmSnackBarPlaybackBar(
                    modifier = modifier,
                    snackBarPlayerViewModel = snackBarPlayerViewModel,
                    musicBottomMenuViewModel = musicBottomMenuViewModel,
                    favoriteSet = favoriteSet,
                    isDarkTheme = isDarkTheme,
                    defaultSnackBarColor = defaultSnackBarColor,
                    onSetColor = {
                        Log.i("=====", "加载图片成功1 $it")
                        colorPurple = it
                            ?.takeUnless { color -> color.alpha == 0f }
                            ?: defaultSnackBarColor
                    },
                    onShowPlayer = onClick,
                    onShowPlaylist = {
                        musicListState = true
                    },
                    onShowMusicInfo = { musicInfo ->
                        currentMusicInfo = musicInfo
                        showMusicInfo = true
                    }
                )
            }
        }
    }
}

/**
 * JVM 底部播放栏的普通播放态内容。
 * 整体拆成左侧歌曲信息、中间播放控制、右侧播放列表入口三块区域。
 */
@Composable
private fun JvmSnackBarPlaybackBar(
    modifier: Modifier,
    snackBarPlayerViewModel: SnackBarPlayerViewModel,
    musicBottomMenuViewModel: MusicBottomMenuViewModel,
    favoriteSet: List<String>,
    isDarkTheme: Boolean,
    defaultSnackBarColor: Color,
    onSetColor: (Color?) -> Unit,
    onShowPlayer: () -> Unit,
    onShowPlaylist: () -> Unit,
    onShowMusicInfo: (XyMusic) -> Unit
) {
    val mainViewModel = LocalMainViewModel.current
    val coroutineScope = rememberCoroutineScope()
    val musicController = snackBarPlayerViewModel.musicController
    val currentMusic = musicController.musicInfo
    val snackBarTitle = currentMusic.snackBarTitleAnnotatedString(
        subColor = MaterialTheme.colorScheme.onSurfaceVariant,
        spanStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ).toSpanStyle(

        )
    )

    // 桌面端底栏固定拆成左侧信息、中间控制、右侧播放列表三段。
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
    ) {
        val sectionWidth = maxWidth / 3

        XyRow(
            paddingValues = PaddingValues(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧主体仍然负责打开完整播放页，底部只保留收藏和歌曲信息两个快捷操作。
            ListItem(
                modifier = Modifier
                    .width(sectionWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(XyTheme.dimens.corner))
                    .debounceClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (currentMusic != null) {
                            onShowPlayer()
                        }
                    },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = {
                    JvmImageCover(
                        modifier = Modifier
                            .size(JvmSnackBarCoverSize)
                            .clip(RoundedCornerShape(XyTheme.dimens.corner)),
                        musicController = musicController,
                        isDarkTheme = isDarkTheme,
                        onSetColor = {
                            onSetColor(it ?: defaultSnackBarColor)
                        }
                    )
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
                                    snackBarPlayerViewModel.toggleFavorite(
                                        itemId = currentMusic.itemId,
                                        ifFavorite = currentMusic.itemId in favoriteSet
                                    )
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
                                    snackBarPlayerViewModel.loadMusicInfo(currentMusic.itemId)
                                        ?.let {
                                            onShowMusicInfo(it)
                                        }
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
                mainViewModel = mainViewModel,
                musicController = musicController,
                musicBottomMenuViewModel = musicBottomMenuViewModel
            )

            Row(
                modifier = Modifier
                    .width(sectionWidth)
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 右侧只保留播放列表入口，避免和中间控制区混在一起。
                JvmSnackBarIconButton(
                    iconRes = Res.drawable.queue_music_24px,
                    contentDescription = stringResource(Res.string.music_list),
                    enabled = musicController.originMusicList.isNotEmpty(),
                    onClick = onShowPlaylist
                )
            }
        }
    }
}

/**
 * 底部播放栏中间控制区。
 * 第一行放播放模式、切歌、播放暂停和音量，第二行放进度条。
 */
@Composable
private fun JvmSnackBarControlSection(
    modifier: Modifier,
    mainViewModel: cn.xybbz.viewmodel.MainViewModel,
    musicController: MusicCommonController,
    musicBottomMenuViewModel: MusicBottomMenuViewModel
) {
    val currentProgress by musicController.progressStateFlow.collectAsStateWithLifecycle()
    var showVolumePopup by remember {
        mutableStateOf(false)
    }
    val playModeIcon = musicController.playMode.toPlayModeIcon()
    val playModeDescription = when (musicController.playMode) {
        PlayerModeEnum.SINGLE_LOOP -> stringResource(Res.string.single_loop)
        PlayerModeEnum.SEQUENTIAL_PLAYBACK -> stringResource(Res.string.list_loop)
        PlayerModeEnum.RANDOM_PLAY -> stringResource(Res.string.shuffle_play)
    }
    val isPlaying =
        musicController.state == PlayStateEnum.Playing || musicController.state == PlayStateEnum.Loading

    XyColumn(
        modifier = modifier
            .fillMaxHeight(),
        paddingValues = PaddingValues(),
        backgroundColor = Color.Transparent,
        verticalArrangement = Arrangement.Bottom
    ) {
        // 第一行放桌面端高频操作，进度条单独占第二行，避免按钮区过挤。
        Row(
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JvmSnackBarIconButton(
                iconRes = playModeIcon,
                contentDescription = playModeDescription,
                enabled = musicController.musicInfo != null,
                onClick = mainViewModel::setNowPlayerTypeData
            )
            JvmSnackBarIconButton(
                iconRes = Res.drawable.skip_previous_24px,
                contentDescription = stringResource(Res.string.play_selected),
                enabled = musicController.musicInfo != null,
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
                enabled = musicController.musicInfo != null,
                modifier = Modifier
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
                contentDescription = stringResource(Res.string.play_selected),
                enabled = musicController.musicInfo != null,
                onClick = musicController::seekToNext
            )
            Box(
                contentAlignment = Alignment.Center
            ) {
                JvmSnackBarIconButton(
                    iconRes = Res.drawable.volume_up_24px,
                    contentDescription = stringResource(Res.string.volume_value_setting),
                    enabled = musicController.musicInfo != null,
                    onClick = {
                        showVolumePopup = !showVolumePopup
                    }
                )

                // 音量调节改成桌面端悬浮弹层，避免和主控制区横向挤在一起。
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
            totalTime = musicController.duration,
            cacheProgress = 0f,
            onProgressChanged = { progress ->
                musicController.seekTo((musicController.duration * progress).toLong())
            },
            modifier = Modifier
                .fillMaxWidth(),
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
 * 底部播放栏的音量弹层。
 * 点击音量按钮后向上展开，内部放竖向滑条和当前音量百分比。
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
        // 让弹层水平中心对齐音量按钮，避免默认左对齐导致整体偏到一侧。
        offset = DpOffset(
            x = JvmSnackBarVolumePopupOffsetX,
            y = -(JvmSnackBarVolumePopupHeight + JvmSnackBarIconButtonSize + JvmSnackBarVolumePopupGap + XyTheme.dimens.outerVerticalPadding * 2)
        ),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.width(JvmSnackBarVolumePopupWidth)
    ) {
        Column(
            modifier = Modifier
                .width(JvmSnackBarVolumePopupWidth)
                .padding(vertical = XyTheme.dimens.contentPadding / 2),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // 竖向滑条放在弹层主体，保持从下到上对应 0% 到 100% 的直觉。
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
                contentDescription = "音量设置"
            )
        }
    }
}

/**
 * JVM 底部播放栏专用的竖向滑条。
 * 这里单独实现命中区域和拖拽逻辑，避免复用横向滑条时出现点击不准的问题。
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

        // 背景轨道固定铺满整个高度，作为当前音量范围的基准线。
        drawLine(
            color = sliderBackgroundColor,
            start = Offset(centerX, bottomY),
            end = Offset(centerX, 0f),
            strokeWidth = JvmSnackBarVolumeSliderBarWidth.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 前景轨道从底部向上增长，和系统音量的增长方向保持一致。
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
 * 统一桌面端播放栏里各个小按钮的尺寸和着色方式。
 */
@Composable
private fun JvmSnackBarIconButton(
    iconRes: DrawableResource,
    contentDescription: String,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(JvmSnackBarIconButtonSize)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

/**
 * 生成底部播放栏显示的标题富文本。
 * 主标题沿用主文案颜色，艺术家名称使用次级文案颜色。
 */
private fun XyPlayMusic?.snackBarTitleAnnotatedString(
    subColor: Color,
    spanStyle: SpanStyle
): AnnotatedString {
    val music = this ?: return AnnotatedString("")
    val artistsText = music.artists?.joinToString("/")?.takeIf { it.isNotBlank() }

    return buildAnnotatedString {
        append(music.name)
        if (artistsText != null) {
            withStyle(
                spanStyle
            ) {
                append(" - ")
                append(artistsText)
            }
        }
    }
}

/**
 * 根据当前播放模式返回底栏模式按钮对应的图标。
 */
private fun PlayerModeEnum.toPlayModeIcon(): DrawableResource {
    // 底栏播放模式按钮直接根据当前模式切换图标。
    return when (this) {
        PlayerModeEnum.SINGLE_LOOP -> Res.drawable.repeat_one_24px
        PlayerModeEnum.SEQUENTIAL_PLAYBACK -> Res.drawable.repeat_24px
        PlayerModeEnum.RANDOM_PLAY -> Res.drawable.shuffle_24px
    }
}

private val logger = KotlinLogging.logger("JvmSnackBarPlayerComponent")

/**
 * 底部播放栏左侧封面组件。
 * 负责展示当前歌曲封面，并在加载成功后回传主题色用于底栏背景着色。
 */
@Composable
private fun JvmImageCover(
    modifier: Modifier = Modifier,
    musicController: MusicCommonController,
    isDarkTheme: Boolean,
    onSetColor: (Color?) -> Unit
) {
    val coverUrls = rememberPlayMusicCoverUrls(
        musicController.musicInfo,
        musicController.coverRefreshVersion
    )
    val primaryCoverModel = coverUrls.primaryUrl
    val fallbackCoverModel = coverUrls.fallbackUrl
    val byteCoverModel = musicController.picByte
    val activeCoverModel = primaryCoverModel ?: fallbackCoverModel ?: byteCoverModel
    val backupCoverModel = if (activeCoverModel == byteCoverModel) null else byteCoverModel
    val defaultSnackBarColor = MaterialTheme.colorScheme.surfaceContainerLowest

    XySmallImage(
        modifier = modifier,
        model = activeCoverModel,
        backModel = backupCoverModel,
        placeholder = Res.drawable.music_xy_placeholder_foreground,
        error = Res.drawable.music_xy_placeholder_foreground,
        fallback = Res.drawable.music_xy_placeholder_foreground,
        contentDescription = stringResource(Res.string.music_cover),
        onSuccess = {
            readPaletteColor(
                image = it.result.image,
                isDarkTheme = isDarkTheme,
                onColorReady = onSetColor
            )
        },
        onError = {
            logger.info { "加载图片失败" }
            onSetColor.invoke(defaultSnackBarColor)
        },
    )
}
