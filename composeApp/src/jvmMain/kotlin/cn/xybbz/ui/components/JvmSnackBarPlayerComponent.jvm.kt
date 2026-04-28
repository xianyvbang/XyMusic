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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.viewmodel.MusicBottomMenuViewModel
import cn.xybbz.viewmodel.SnackBarPlayerViewModel
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
import xymusic_kmp.composeapp.generated.resources.music_remove_from_playlist
import xymusic_kmp.composeapp.generated.resources.play_selected
import xymusic_kmp.composeapp.generated.resources.playlist_add_24px
import xymusic_kmp.composeapp.generated.resources.playlist_play_24px
import xymusic_kmp.composeapp.generated.resources.playlist_remove_24px
import xymusic_kmp.composeapp.generated.resources.please_select
import xymusic_kmp.composeapp.generated.resources.unfavorite
import cn.xybbz.ui.xy.XyIconButton as IconButton

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
    var sharedCoverSourceBoundsOnScreen by remember {
        mutableStateOf<Rect?>(null)
    }
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
    val selectUiState by snackBarPlayerViewModel.selectControl.uiState.collectAsStateWithLifecycle()
    val playbackState by snackBarPlayerViewModel.musicController.playbackStateFlow.collectAsStateWithLifecycle()
    val originMusicList by snackBarPlayerViewModel.musicController.originMusicListFlow.collectAsStateWithLifecycle()
    val cacheScheduleData by snackBarPlayerViewModel.musicController.downloadCacheController.cacheSchedule.collectAsStateWithLifecycle()
    val favoriteSet by mainViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())

    val defaultSnackBarColor = MaterialTheme.colorScheme.surfaceContainerLowest

    val density = androidx.compose.ui.platform.LocalDensity.current
    val sharedCoverRequestSize = remember(density) {
        with(density) {
            IntSize(
                width = JvmMusicPlayerSharedCoverTargetSize.roundToPx(),
                height = JvmMusicPlayerSharedCoverTargetSize.roundToPx()
            )
        }
    }

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

    playbackState.musicInfo?.let {
        JvmMusicPlayerComponent(
            music = it,
            playbackState.picByte,
            sharedCoverSourceBoundsOnScreen = sharedCoverSourceBoundsOnScreen,
            sheetStateR = playerSheetState,
            onSetState = { musicListState = it }
        )
    }

    MusicListComponent(
        musicListState = musicListState,
        curOriginIndex = playbackState.curOriginIndex,
        originMusicList = originMusicList,
        onSetState = { musicListState = it },
        onClearPlayerList = {
            coroutineScope.launch {
                snackBarPlayerViewModel.clearPlayer()

            }.invokeOnCompletion {
                snackBarPlayerViewModel.musicController.clearPlayerList()
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
                // 悬浮播放条普通态同样复用共享底栏组件，只保留当前页面特有的业务回调。
                JvmSnackBarPlaybackBar(
                    modifier = modifier,
                    musicController = snackBarPlayerViewModel.musicController,
                    musicBottomMenuViewModel = musicBottomMenuViewModel,
                    favoriteSet = favoriteSet,
                    cacheProgress = cacheScheduleData,
                    onSharedCoverBoundsChanged = {
                        sharedCoverSourceBoundsOnScreen = it
                    },
                    sharedCoverRequestSize = sharedCoverRequestSize,
                    onShowPlayer = onClick,
                    onShowPlaylist = {
                        musicListState = true
                    },
                    onToggleFavorite = { playMusic ->
                        snackBarPlayerViewModel.toggleFavorite(
                            itemId = playMusic.itemId,
                            ifFavorite = playMusic.itemId in favoriteSet
                        )
                    },
                    onShowMusicInfo = { playMusic ->
                        snackBarPlayerViewModel.loadMusicInfo(playMusic.itemId)?.let { musicInfo ->
                            currentMusicInfo = musicInfo
                            showMusicInfo = true
                        }
                    }
                )
            }
        }
    }
}
