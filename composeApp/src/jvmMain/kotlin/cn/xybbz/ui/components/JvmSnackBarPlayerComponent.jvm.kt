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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.compositionLocal.LocalPlayerChromeState
import cn.xybbz.config.volume.VolumeServer
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.viewmodel.SnackBarPlayerViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmSnackBarPlayerComponent(
    modifier: Modifier = Modifier,
    snackBarPlayerViewModel: SnackBarPlayerViewModel = koinViewModel<SnackBarPlayerViewModel>(),
    onClick: () -> Unit
) {
    val mainViewModel = LocalMainViewModel.current
    // 桌面迷你播放条共享播放器外壳状态，用于打开/关闭完整播放器。
    val playerChromeState = LocalPlayerChromeState.current
    val navigator = LocalNavigator.current
    val volumeServer = koinInject<VolumeServer>()
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
    var volumeValue by remember {
        mutableFloatStateOf(0f)
    }
    val coroutineScope = rememberCoroutineScope()
    val refreshVolume = {
        volumeValue = volumeServer.getStreamVolume().toFloat() / volumeServer.getMaxVolume()
    }
    val onVolumeChanged: (Float) -> Unit = { value ->
        coroutineScope.launch {
            volumeServer.updateVolume((value * 100).toInt())
            volumeValue = value
        }
    }
    val musicInfo by snackBarPlayerViewModel.musicController.musicInfoFlow.collectAsStateWithLifecycle()
    val picByte by snackBarPlayerViewModel.musicController.picByteFlow.collectAsStateWithLifecycle()
    val curOriginIndex by snackBarPlayerViewModel.musicController.curOriginIndexFlow.collectAsStateWithLifecycle()
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

    val playerSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )


    LaunchedEffect(volumeServer) {
        refreshVolume()
    }

    MusicBottomMenuComponent(
        onAlbumRouter = { albumId ->
            navigator.navigate(AlbumInfo(albumId, MusicDataTypeEnum.ALBUM))
        }, onPlayerSheetClose = {
            coroutineScope.launch {
                playerSheetState.hide()
            }.invokeOnCompletion {
                // 底部菜单关闭桌面完整播放器后恢复迷你播放条标题滚动。
                playerChromeState.putMarqueeIterations(1)
                playerChromeState.hidePlayerSheet()
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

    musicInfo?.let {
        JvmMusicPlayerComponent(
            music = it,
            picByte,
            sharedCoverSourceBoundsOnScreen = sharedCoverSourceBoundsOnScreen,
            sheetStateR = playerSheetState,
            volumeValue = volumeValue,
            onVolumeChanged = onVolumeChanged,
            onRefreshVolume = refreshVolume,
            onSetState = { musicListState = it }
        )
    }

    MusicListComponent(
        musicListState = musicListState,
        curOriginIndex = curOriginIndex,
        musicController = snackBarPlayerViewModel.musicController,
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
                    // 播放列表为空时关闭桌面完整播放器，避免展示没有歌曲的播放器页。
                    playerChromeState.hidePlayerSheet()
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
        // 选择模式的批量操作已统一由各页面列表内的操作栏承载，底栏始终展示播放控制。
        JvmSnackBarPlaybackBar(
            modifier = modifier,
            musicController = snackBarPlayerViewModel.musicController,
            volume = volumeValue,
            onVolumeChanged = onVolumeChanged,
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
                snackBarPlayerViewModel.musicController.invokingOnFavorite(playMusic.itemId)
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
