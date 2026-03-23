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


import android.util.Log
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.HeartBroken
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistRemove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.scale
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import xymusic_kmp.composeapp.generated.resources.Res
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.ResourcesUtils.drawableToBitmap
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.image.rememberPlayMusicCoverUrls
import cn.xybbz.extension.playProgress
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.viewmodel.SnackBarPlayerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch
import xymusic_kmp.composeapp.generated.resources.add_to_playlist
import xymusic_kmp.composeapp.generated.resources.delete_local_permanently
import xymusic_kmp.composeapp.generated.resources.delete_permanently
import xymusic_kmp.composeapp.generated.resources.download_list
import xymusic_kmp.composeapp.generated.resources.music_cover
import xymusic_kmp.composeapp.generated.resources.music_list
import xymusic_kmp.composeapp.generated.resources.music_remove_from_playlist
import xymusic_kmp.composeapp.generated.resources.pause
import xymusic_kmp.composeapp.generated.resources.play_selected
import xymusic_kmp.composeapp.generated.resources.playing
import xymusic_kmp.composeapp.generated.resources.please_select
import xymusic_kmp.composeapp.generated.resources.unfavorite
import cn.xybbz.ui.xy.XyIconButton as IconButton

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SnackBarPlayerComponent(
    modifier: Modifier = Modifier,
    snackBarPlayerViewModel: SnackBarPlayerViewModel = hiltViewModel<SnackBarPlayerViewModel>(),
    onClick: () -> Unit
) {
    val mainViewModel = LocalMainViewModel.current
    val navigator = LocalNavigator.current
    var musicListState by remember {
        mutableStateOf(false)
    }
    val coroutineScope = rememberCoroutineScope()
    val ifOpenSelect by snackBarPlayerViewModel.selectControl.uiState.collectAsStateWithLifecycle()

    var colorPurple by remember {
        mutableStateOf(Color.Transparent)
    }
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

    snackBarPlayerViewModel.musicController.musicInfo?.let {
        MusicPlayerComponent(
            music = it,
            snackBarPlayerViewModel.musicController.picByte,
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
        curOriginIndex = snackBarPlayerViewModel.musicController.curOriginIndex,
        originMusicList = snackBarPlayerViewModel.musicController.originMusicList,
        onSetState = { musicListState = it },
        onClearPlayerList = {
            coroutineScope.launch {
                snackBarPlayerViewModel.clearPlayer()

            }.invokeOnCompletion {
                snackBarPlayerViewModel.musicController.clearPlayerList()
                colorPurple = Color.Transparent
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
                    colorPurple = Color.Transparent
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
            .clip(shape = RoundedCornerShape(topEnd = 25.dp, bottomEnd = 25.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
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
                                snackBarPlayerViewModel.selectControl.onRemoveSelectListResource.invoke(
                                    snackBarPlayerViewModel.dataSourceManager,
                                    coroutineScope
                                )
                            }
                        }, enabled = snackBarPlayerViewModel.selectControl.ifEnableButton) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
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
                                    snackBarPlayerViewModel.db
                                )
                            }
                        }
                    }, enabled = snackBarPlayerViewModel.selectControl.ifEnableButton) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.PlaylistPlay,
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
                            imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
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
                                imageVector = Icons.Rounded.PlaylistRemove,
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
                            imageVector = Icons.Rounded.HeartBroken,
                            contentDescription = stringResource(Res.string.unfavorite)
                        )
                    }

                    if (!snackBarPlayerViewModel.selectControl.ifLocal && snackBarPlayerViewModel.dataSourceManager.getCanDownload())
                        IconButton(onClick = {
                            if (snackBarPlayerViewModel.selectControl.ifSelectEmpty()) {
                                MessageUtils.sendPopTip(pleaseSelect)
                            } else {
                                permissionState?.launchMultiplePermissionRequest()
                            }

                        }, enabled = snackBarPlayerViewModel.selectControl.ifEnableButton) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
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
                            if (snackBarPlayerViewModel.musicController.musicInfo != null) {
                                onClick()
                            }
                        },
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Start
                ) {

                    ImageCover(
                        musicController = snackBarPlayerViewModel.musicController,
                        onSetColor = {
                            colorPurple = it ?: Color.Transparent
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
                            imageVector = Icons.AutoMirrored.Outlined.QueueMusic,
                            contentDescription = stringResource(Res.string.music_list),
                            tint = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .debounceClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (snackBarPlayerViewModel.musicController.originMusicList.isNotEmpty()) {
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
    musicController: MusicController
) {
    val originMusicListIsEmpty by remember {
        derivedStateOf {
            musicController.originMusicList
        }
    }

    if (originMusicListIsEmpty.isNotEmpty()) {
        val basePage by remember {
            derivedStateOf {
                val mid = Int.MAX_VALUE / 2
                val size = musicController.originMusicList.size
                if (size == 0) 0 else mid - (mid % size) + (musicController.curOriginIndex % size)
            }

        }


        val horPagerState = rememberPagerState(
            initialPage = basePage
        ) {
            Int.MAX_VALUE
        }

        LaunchedEffect(originMusicListIsEmpty) {
            snapshotFlow { originMusicListIsEmpty }.collect {
                horPagerState.scrollToPage(basePage)
            }
        }

        val mainViewModel = LocalMainViewModel.current

        SideEffect {
            Log.d("=====", "HorizontalPagerSnackBar重组一次${horPagerState.currentPage}")
        }
        val intState = remember {
            derivedStateOf {
                musicController.curOriginIndex
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
                    && musicController.curOriginIndex != Constants.MINUS_ONE_INT
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
                originMusicListIsEmpty,
                musicController.curOriginIndex,
                musicController.playType
            ) {
                derivedStateOf {
                    if (originMusicListIsEmpty.isEmpty()) {
                        0
                    } else {
                        val listSize = originMusicListIsEmpty.size
                        val currentIndex = musicController.curOriginIndex
                            .takeIf { it in originMusicListIsEmpty.indices }
                            ?: 0
                        val fallbackIndex =
                            (page % listSize + listSize) % listSize

                        if (musicController.playType != PlayerTypeEnum.RANDOM_PLAY) {
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
                text = if (originMusicListIsEmpty.isNotEmpty()) originMusicListIsEmpty[index].name else "",
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

@Composable
private fun CircularProgressIndicatorComp(musicController: MusicController) {

    val progress by playProgress(musicController.duration, musicController.progressStateFlow)

    Box(
        modifier = Modifier
            .padding(end = 5.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (musicController.state == PlayStateEnum.Loading) {
            //这个放在一个单独的组件里
            CircularProgressIndicator(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {

            CircularProgressIndicator(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape),
                color = Color.White,
                trackColor = Color.White,
                progress = {
                    0.0f
                },
                strokeWidth = 1.dp
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
            imageVector = if (musicController.state == PlayStateEnum.Playing || musicController.state == PlayStateEnum.Loading) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = if (musicController.state == PlayStateEnum.Playing) stringResource(
                Res.string.playing
            ) else stringResource(Res.string.pause),
            modifier = Modifier
                .size(25.dp)
                .clip(CircleShape)
                .debounceClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (musicController.state != PlayStateEnum.Pause) {
                        musicController.pause()
                    } else {
                        musicController.resume()
                    }

                },
            tint = Color.White,
        )
    }
}

@Composable
private fun ImageCover(
    musicController: MusicController,
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

    XyImage(
        modifier = Modifier
            .size(XyTheme.dimens.snackBarPlayerHeight),
        model = activeCoverModel,
        backModel = backupCoverModel,
        placeholder = painterResource(Res.drawable.music_xy_placeholder_foreground),
        error = painterResource(Res.drawable.music_xy_placeholder_foreground),
        fallback = painterResource(Res.drawable.music_xy_placeholder_foreground),
        contentDescription = stringResource(Res.string.music_cover),
        onSuccess = {
            val drawable = it.result.drawable
            val bitmap = drawableToBitmap(drawable)
            bitmap?.let {
                val scaledBitmap = bitmap.scale(200, 200, false)
                Palette.from(scaledBitmap).generate { palette ->
                    val colorValue = palette?.darkMutedSwatch?.rgb
                        ?: palette?.mutedSwatch?.rgb
                        ?: palette?.darkVibrantSwatch?.rgb
                        ?: palette?.vibrantSwatch?.rgb
                        ?: palette?.dominantSwatch?.rgb
                    onSetColor.invoke(colorValue?.let { Color(it) } ?: Color.Transparent)
                }
            }
        },
        onError = {
            onSetColor.invoke(Color.Transparent)
        },
    )
}


