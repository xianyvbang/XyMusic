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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.ResourcesUtils.readPaletteColor
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.image.rememberPlayMusicCoverUrls
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.extension.playProgress
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.PlayerModeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XySmallImage
import cn.xybbz.viewmodel.SnackBarPlayerViewModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
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
import xymusic_kmp.composeapp.generated.resources.heart_broken_24px
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
import xymusic_kmp.composeapp.generated.resources.unfavorite
import cn.xybbz.ui.xy.XyIconButton as IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnackBarPlayerComponent(
    modifier: Modifier = Modifier,
    snackBarPlayerViewModel: SnackBarPlayerViewModel = koinViewModel<SnackBarPlayerViewModel>(),
    onClick: () -> Unit
) {
    val mainViewModel = LocalMainViewModel.current
    val navigator = LocalNavigator.current
    var musicListState by remember {
        mutableStateOf(false)
    }
    val coroutineScope = rememberCoroutineScope()
    val selectUiState by snackBarPlayerViewModel.selectControl.uiState.collectAsStateWithLifecycle()
    val musicInfo by snackBarPlayerViewModel.musicController.musicInfoFlow.collectAsStateWithLifecycle()
    val picByte by snackBarPlayerViewModel.musicController.picByteFlow.collectAsStateWithLifecycle()
    val curOriginIndex by snackBarPlayerViewModel.musicController.curOriginIndexFlow.collectAsStateWithLifecycle()
    val originMusicList by snackBarPlayerViewModel.musicController.originMusicListFlow.collectAsStateWithLifecycle()

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

    musicInfo?.let {
        MusicPlayerComponent(
            music = it,
            picByte,
            sheetStateR = playerSheetState,
            toNext = {
                Log.i("=====", "数据调用SnackBarPlayerComponent")
                snackBarPlayerViewModel.musicController.seekToNext()
            }, backNext = {
                snackBarPlayerViewModel.musicController.seekToPrevious()
            }, onSetState = { musicListState = it })
    }


    MusicListComponent(
        musicListState = musicListState,
        curOriginIndex = curOriginIndex,
        originMusicList = originMusicList,
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
                if (originMusicList.isEmpty()) {
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
            .zIndex(Float.MAX_VALUE)
            .padding(horizontal = XyTheme.dimens.innerHorizontalPadding)
            .clip(
                shape = RoundedCornerShape(
                    topStart = XyTheme.dimens.corner,
                    bottomStart = XyTheme.dimens.corner,
                    topEnd = 25.dp,
                    bottomEnd = 25.dp
                )
            )
            .background(defaultSnackBarColor)
            .drawBehind {
                drawRect(animatedColor)
            }
    ) {
        AnimatedContent(
            targetState = selectUiState.isOpen,
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
                        .fillMaxWidth()
                        .height(XyTheme.dimens.snackBarPlayerHeight),
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
                        }, enabled = selectUiState.ifEnableButton) {
                            Icon(
                                painter = painterResource(Res.drawable.delete_24px),
                                contentDescription = if (selectUiState.ifLocal) stringResource(
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
                    }, enabled = selectUiState.ifEnableButton) {
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
                    }, enabled = selectUiState.ifEnableButton) {
                        Icon(
                            painter = painterResource(Res.drawable.playlist_add_24px),
                            contentDescription = stringResource(Res.string.add_to_playlist)
                        )
                    }
                    if (selectUiState.ifPlaylist)
                        IconButton(onClick = {
                            if (snackBarPlayerViewModel.selectControl.ifSelectEmpty()) {
                                MessageUtils.sendPopTip(pleaseSelect)
                            } else
                                snackBarPlayerViewModel.selectControl.onRemovePlaylistMusic(
                                    snackBarPlayerViewModel.dataSourceManager,
                                    coroutineScope
                                )
                        }, enabled = selectUiState.ifEnableButton) {
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
                    }, enabled = selectUiState.ifEnableButton) {
                        Icon(
                            painter = painterResource(Res.drawable.heart_broken_24px),
                            contentDescription = stringResource(Res.string.unfavorite)
                        )
                    }

                    if (!selectUiState.ifLocal && snackBarPlayerViewModel.dataSourceManager.getCanDownload())
                        IconButton(onClick = {
                            if (snackBarPlayerViewModel.selectControl.ifSelectEmpty()) {
                                MessageUtils.sendPopTip(pleaseSelect)
                            } else {
                                permissionState.launchMultiplePermissionRequest()
                            }

                        }, enabled = selectUiState.ifEnableButton) {
                            Icon(
                                painter = painterResource(Res.drawable.download_24px),
                                contentDescription = stringResource(Res.string.download_list)
                            )
                        }
                }
            } else {
                Row(
                    modifier = modifier
                        .debounceClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (musicInfo != null) {
                                onClick()
                            }
                        },
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Start
                ) {

                    ImageCover(
                        musicController = snackBarPlayerViewModel.musicController,
                        isDarkTheme = isDarkTheme,
                        onSetColor = {
                            Log.i("=====", "加载图片成功1 ${it}")
                            colorPurple = it
                                ?.takeUnless { color -> color.alpha == 0f }
                                ?: defaultSnackBarColor
                        }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(XyTheme.dimens.snackBarPlayerHeight),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //需要实现左右滑动

                        HorizontalPagerSnackBar(
                            modifier = Modifier
                                .weight(1f),
                            toNext = {
                                snackBarPlayerViewModel.musicController.seekToNext()
                            },
                            backNext = {
                                snackBarPlayerViewModel.musicController.seekToPrevious()
                            },
                            musicController = snackBarPlayerViewModel.musicController
                        )

                        CircularProgressIndicatorComp(
                            musicController = snackBarPlayerViewModel.musicController
                        )
                        Icon(
                            painter = painterResource(Res.drawable.queue_music_24px),
                            contentDescription = stringResource(Res.string.music_list),
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .debounceClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (originMusicList.isNotEmpty()) {
                                        musicListState = true
                                    }
                                }
                        )
                    }
                }
            }
        }
    }


}

@Composable
fun RowScope.HorizontalPagerSnackBar(
    modifier: Modifier,
    toNext: (Int) -> Unit,
    backNext: () -> Unit,
    musicController: MusicCommonController
) {
    val originMusicList by musicController.originMusicListFlow.collectAsStateWithLifecycle()
    val curOriginIndex by musicController.curOriginIndexFlow.collectAsStateWithLifecycle()
    val playMode by musicController.playModeFlow.collectAsStateWithLifecycle()

    if (originMusicList.isNotEmpty()) {
        val basePage by remember {
            derivedStateOf {
                val mid = Int.MAX_VALUE / 2
                val size = originMusicList.size
                if (size == 0) 0 else mid - (mid % size) + (curOriginIndex % size)
            }

        }


        val horPagerState = rememberPagerState(
            initialPage = basePage
        ) {
            Int.MAX_VALUE
        }

        LaunchedEffect(originMusicList) {
            snapshotFlow { originMusicList }.collect {
                horPagerState.scrollToPage(basePage)
            }
        }

        val mainViewModel = LocalMainViewModel.current

        val intState = remember {
            derivedStateOf {
                curOriginIndex
            }
        }
        LaunchedEffect(intState) {
            snapshotFlow { intState.value }.collect {
                if (basePage != horPagerState.currentPage) {
                    horPagerState.scrollToPage(basePage)
                }
            }
        }

        //是否应该加载更多的状态
        val shouldLoadMore = remember {
            //由另一个状态计算派生
            derivedStateOf {
                horPagerState.isScrollInProgress
            }
        }

        LaunchedEffect(shouldLoadMore) {
            //详见文档：https://developer.android.com/jetpack/compose/side-effects#snapshotFlow
            snapshotFlow { shouldLoadMore.value }.collect {
                if (!it && basePage != horPagerState.currentPage
                    && curOriginIndex != Constants.MINUS_ONE_INT
                ) {
                    if (basePage > horPagerState.currentPage) {
                        Log.d("Pager", "向右滑动 ->")
                        backNext()
                    } else if (basePage < horPagerState.currentPage) {
                        toNext(horPagerState.currentPage)
                        Log.d("Pager", "向左滑动 <-")
                    }

                    mainViewModel.putIterations(1)
                }
            }

        }

        HorizontalPager(
            state = horPagerState,
            modifier = modifier
        ) { page ->
            val index by remember(
                page,
                basePage,
                originMusicList,
                curOriginIndex,
                playMode
            ) {
                derivedStateOf {
                    if (originMusicList.isEmpty()) {
                        0
                    } else {
                        val listSize = originMusicList.size
                        val currentIndex = curOriginIndex
                            .takeIf { it in originMusicList.indices }
                            ?: 0
                        val fallbackIndex =
                            (page % listSize + listSize) % listSize

                        if (playMode != PlayerModeEnum.RANDOM_PLAY) {
                            fallbackIndex
                        } else {
                            when (page - basePage) {
                                0 -> currentIndex
                                1 -> musicController.getNextPlayableIndex()
                                    ?: ((currentIndex + 1) % listSize)

                                -1 -> musicController.getPreviousPlayableIndex()
                                    ?: ((currentIndex - 1 + listSize) % listSize)

                                else -> fallbackIndex
                            }
                        }
                    }
                }
            }
            Text(
                text = if (originMusicList.isNotEmpty()) originMusicList[index].name else "",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
                    .basicMarquee(
                        iterations = mainViewModel.iterations
                    ),
                fontSize = 12.sp,
                fontWeight = FontWeight.W700,
                lineHeight = 17.38.sp,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )

        }
    } else {
        Spacer(modifier.weight(1f))
    }

}

private val logger = KotlinLogging.logger("CircularProgressIndicatorComp")

@Composable
private fun CircularProgressIndicatorComp(musicController: MusicCommonController) {
    val duration by musicController.durationFlow.collectAsStateWithLifecycle()
    val state by musicController.stateFlow.collectAsStateWithLifecycle()
    val progress by playProgress(duration, musicController.progressStateFlow)

    Box(
        modifier = Modifier
            .padding(end = 5.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (state == PlayStateEnum.Loading) {
            //这个放在一个单独的组件里
            CircularProgressIndicator(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape),
                strokeWidth = 2.dp
            )
        } else {

            CircularProgressIndicator(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape),
                progress = {
                    0.0f
                },
                strokeWidth = 1.5.dp
            )
            CircularProgressIndicator(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape),
                color = Color.White,
                trackColor = Color.Transparent,
                progress = {
                    progress
                },
                strokeWidth = 2.dp,
                gapSize = 0.dp
            )
        }

        Icon(
            painter = painterResource(if (state == PlayStateEnum.Playing || state == PlayStateEnum.Loading) Res.drawable.pause_24px else Res.drawable.play_arrow_24px),
            contentDescription = if (state == PlayStateEnum.Playing) stringResource(
                Res.string.playing
            ) else stringResource(Res.string.pause),
            modifier = Modifier
                .size(25.dp)
                .clip(CircleShape)
                .debounceClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (state != PlayStateEnum.Pause) {
                        musicController.pause()
                    } else {
                        musicController.resume()
                    }

                }
        )
    }
}

@Composable
private fun ImageCover(
    musicController: MusicCommonController,
    isDarkTheme: Boolean,
    onSetColor: (Color?) -> Unit
) {
    val musicInfo by musicController.musicInfoFlow.collectAsStateWithLifecycle()
    val coverRefreshVersion by musicController.coverRefreshVersionFlow.collectAsStateWithLifecycle()
    val byteCoverModel by musicController.picByteFlow.collectAsStateWithLifecycle()
    val coverUrls = rememberPlayMusicCoverUrls(
        musicInfo,
        coverRefreshVersion
    )
    val primaryCoverModel = coverUrls.primaryUrl
    val fallbackCoverModel = coverUrls.fallbackUrl
    val activeCoverModel = primaryCoverModel ?: fallbackCoverModel ?: byteCoverModel
    val backupCoverModel = if (activeCoverModel == byteCoverModel) null else byteCoverModel
    val defaultSnackBarColor = MaterialTheme.colorScheme.surfaceContainerLowest

    XySmallImage(
        modifier = Modifier
            .size(XyTheme.dimens.snackBarPlayerHeight),
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


