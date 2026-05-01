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


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import cn.xybbz.api.client.FavoriteCoordinator
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.image.rememberAlbumCoverUrls
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.config.select.SelectControl
import cn.xybbz.entity.data.Sort
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.extension.isSticking
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.progress.Progress
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.ErrorContent
import cn.xybbz.ui.components.ErrorMoreRetryItem
import cn.xybbz.ui.components.LazyLoadingAndStatus
import cn.xybbz.ui.components.SelectSortBottomSheetComponent
import cn.xybbz.ui.components.SongTableColumns
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.XySelectAllComponent
import cn.xybbz.ui.components.getExportPlaylistsAlertDialogObject
import cn.xybbz.ui.components.importPlaylistsCompose
import cn.xybbz.ui.components.rememberMusicArtistClickHandler
import cn.xybbz.ui.components.show
import cn.xybbz.ui.components.songTableItems
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.popup.XyDropdownMenu
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopTooltipBox
import cn.xybbz.ui.windows.DesktopTooltipIconButton
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyItemSwitcher
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.AlbumInfoViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.album
import xymusic_kmp.composeapp.generated.resources.album_cover
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.close_24px
import xymusic_kmp.composeapp.generated.resources.close_selection
import xymusic_kmp.composeapp.generated.resources.confirm_delete_playlist
import xymusic_kmp.composeapp.generated.resources.delete_24px
import xymusic_kmp.composeapp.generated.resources.delete_playback_history
import xymusic_kmp.composeapp.generated.resources.delete_playlist
import xymusic_kmp.composeapp.generated.resources.edit_24px
import xymusic_kmp.composeapp.generated.resources.empty_info
import xymusic_kmp.composeapp.generated.resources.enable_playback_history
import xymusic_kmp.composeapp.generated.resources.export_playlist
import xymusic_kmp.composeapp.generated.resources.favorite_24px
import xymusic_kmp.composeapp.generated.resources.favorite_added
import xymusic_kmp.composeapp.generated.resources.favorite_border_24px
import xymusic_kmp.composeapp.generated.resources.favorite_removed
import xymusic_kmp.composeapp.generated.resources.import_info
import xymusic_kmp.composeapp.generated.resources.import_playlist
import xymusic_kmp.composeapp.generated.resources.import_playlist_hint
import xymusic_kmp.composeapp.generated.resources.login_24px
import xymusic_kmp.composeapp.generated.resources.loading
import xymusic_kmp.composeapp.generated.resources.modify_playlist_name
import xymusic_kmp.composeapp.generated.resources.more_vert_24px
import xymusic_kmp.composeapp.generated.resources.music_xy_placeholder_foreground
import xymusic_kmp.composeapp.generated.resources.open_operation_menu
import xymusic_kmp.composeapp.generated.resources.open_sort_and_filter_menu
import xymusic_kmp.composeapp.generated.resources.pause_circle_24px
import xymusic_kmp.composeapp.generated.resources.pause_playback
import xymusic_kmp.composeapp.generated.resources.play_circle_24px
import xymusic_kmp.composeapp.generated.resources.playlist
import xymusic_kmp.composeapp.generated.resources.playlist_add_check_24px
import xymusic_kmp.composeapp.generated.resources.reached_bottom
import xymusic_kmp.composeapp.generated.resources.rename_playlist
import xymusic_kmp.composeapp.generated.resources.resume_playback
import xymusic_kmp.composeapp.generated.resources.return_album_page
import xymusic_kmp.composeapp.generated.resources.select
import xymusic_kmp.composeapp.generated.resources.songs_count_suffix
import xymusic_kmp.composeapp.generated.resources.start_playback
import kotlin.io.encoding.ExperimentalEncodingApi
import cn.xybbz.ui.xy.XyIconButton as IconButton


internal val JvmDefaultAlbumInfoHeight = 220.dp

private val JvmAlbumInfoCoverSize = 188.dp
private val JvmAlbumStickyOperationHeight = 72.dp

private val JvmAlbumMusicTableColumns = SongTableColumns(
    showFavoriteColumn = true,
    showInlineActions = true,
    showAlbumColumn = false,
    showMetaColumn = false,
)

/**
 * 专辑详情,通过分类或者其他跳转
 * [dataType] 数据类型 0专辑,1歌单
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun JvmAlbumInfoScreen(
    itemId: String,
    dataType: MusicDataTypeEnum,
    albumInfoViewModel: AlbumInfoViewModel = koinViewModel<AlbumInfoViewModel> {
        parametersOf(
            itemId,
            dataType
        )
    }
) {

    SideEffect {
        Log.d("=====", "MusicAudiobookInfoScreen recomposed")
    }

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val navigator = LocalNavigator.current

    // 专辑头部的艺术家文本点击使用统一处理器，兼容多个专辑艺术家。
    val artistClickHandler = rememberMusicArtistClickHandler()
    val isSticking by remember(lazyListState) { lazyListState.isSticking(1) }

    val favoriteSet by albumInfoViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val selectUiState by albumInfoViewModel.selectControl.uiState.collectAsStateWithLifecycle()
    val playbackState by albumInfoViewModel.musicController.playbackStateFlow.collectAsStateWithLifecycle()

    val musicListPage =
        albumInfoViewModel.listPage.collectAsLazyPagingItems()
    val sortBy by albumInfoViewModel.sortBy.collectAsStateWithLifecycle()

    var ifShowMenu by remember {
        mutableStateOf(false)
    }
    var playlistName by remember {
        mutableStateOf(albumInfoViewModel.xyAlbumInfoData?.name ?: "")
    }


    val removePlaylistAlertTitle = stringResource(Res.string.delete_playlist)
    val editPlaylistAlertTitle = stringResource(Res.string.modify_playlist_name)
    val importPlaylistAlertTitle = stringResource(Res.string.import_playlist)

    XyColumnScreen {
            TopAppBarComponent(
                title = {

                    AnimatedContent(
                        targetState = isSticking,
                        label = "animated content",
                        transitionSpec = {
                            if (targetState > initialState) {
                                fadeIn() togetherWith fadeOut()
                            } else {
                                fadeIn() togetherWith fadeOut()
                            }.using(
                                SizeTransform(clip = false)
                            )
                        }
                    ) { targetCount ->
                        if (targetCount) {
                            // Make sure to use `targetCount`, not `count`.
                            Text(
                                text = albumInfoViewModel.xyAlbumInfoData?.name ?: "",
                                modifier = Modifier.basicMarquee(
                                    iterations = Int.MAX_VALUE
                                )
                            )
                        } else {
                            Text(
                                text = if (dataType == MusicDataTypeEnum.PLAYLIST)
                                    stringResource(Res.string.playlist)
                                else
                                    stringResource(
                                        Res.string.album
                                    ),
                                modifier = Modifier.basicMarquee(
                                    iterations = Int.MAX_VALUE
                                )
                            )
                        }

                    }
                },
                actions = {
                    if (dataType == MusicDataTypeEnum.PLAYLIST)
                        Box {
                            IconButton(
                                onClick = {
                                    ifShowMenu = true
                                },
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.more_vert_24px),
                                    contentDescription = stringResource(Res.string.open_operation_menu)
                                )
                            }
                            XyDropdownMenu(
                                offset = DpOffset(
                                    x = 0.dp,
                                    y = XyTheme.dimens.outerVerticalPadding
                                ),
                                onIfShowMenu = { ifShowMenu },
                                onSetIfShowMenu = { ifShowMenu = it },
                                modifier = Modifier.width(200.dp),
                                itemDataList = listOf(
                                    MenuItemDefaultData(
                                        title = importPlaylistAlertTitle,
                                        trailingIcon = {
                                            Icon(
                                                painter = painterResource(Res.drawable.login_24px),
                                                contentDescription = importPlaylistAlertTitle
                                            )
                                        },
                                        onClick = importPlaylistsCompose(onCreatePlaylist = {
                                            AlertDialogObject(
                                                title = importPlaylistAlertTitle,
                                                content = {
                                                    XyTextSubSmall(
                                                        text = stringResource(Res.string.import_playlist_hint)
                                                    )
                                                },
                                                onConfirmation = {
                                                    coroutineScope.launch {
                                                        albumInfoViewModel.importPlaylist(it)
                                                    }
                                                },
                                                confirmText = Res.string.import_info,
                                                onDismissRequest = {}).show()

                                        }) {
                                            ifShowMenu = false
                                        }),
                                    MenuItemDefaultData(
                                        title = stringResource(Res.string.export_playlist),
                                        trailingIcon = {
                                            Icon(
                                                painter = painterResource(Res.drawable.login_24px),
                                                contentDescription = stringResource(Res.string.export_playlist)
                                            )
                                        },
                                        onClick = getExportPlaylistsAlertDialogObject(
                                            onPlayTrackList = {
                                                albumInfoViewModel.exportPlaylist()
                                            },
                                            onClick = {
                                                ifShowMenu = false
                                            })
                                    ),
                                    MenuItemDefaultData(
                                        title = stringResource(Res.string.rename_playlist),
                                        trailingIcon = {
                                            Icon(
                                                painter = painterResource(Res.drawable.edit_24px),
                                                contentDescription = stringResource(Res.string.rename_playlist)
                                            )
                                        },
                                        onClick = {
                                            ifShowMenu = false
                                            AlertDialogObject(
                                                title = editPlaylistAlertTitle,
                                                content = {
                                                    XyEdit(
                                                        text = albumInfoViewModel.xyAlbumInfoData?.name
                                                            ?: "", onChange = {
                                                            playlistName = it
                                                        })
                                                },
                                                onConfirmation = {
                                                    albumInfoViewModel.editPlaylistName(
                                                        itemId,
                                                        playlistName
                                                    )
                                                    coroutineScope.launch {
                                                        albumInfoViewModel.getAlbumInfoData()
                                                    }
                                                },
                                                onDismissRequest = {
                                                    playlistName = ""
                                                }
                                            ).show()
                                        }),
                                    MenuItemDefaultData(
                                        title = removePlaylistAlertTitle,
                                        trailingIcon = {
                                            Icon(
                                                painter = painterResource(Res.drawable.delete_24px),
                                                contentDescription = removePlaylistAlertTitle,
                                                tint = Color.Red
                                            )
                                        },
                                        onClick = {
                                            ifShowMenu = false
                                            AlertDialogObject(
                                                title = removePlaylistAlertTitle,
                                                content = {
                                                    XyTextSubSmall(
                                                        text = stringResource(
                                                            Res.string.confirm_delete_playlist,
                                                            albumInfoViewModel.xyAlbumInfoData?.name
                                                                ?: ""
                                                        )
                                                    )
                                                },
                                                ifWarning = true,
                                                onConfirmation = {
                                                    coroutineScope.launch {
                                                        albumInfoViewModel.removePlaylist(itemId)
                                                    }.invokeOnCompletion {
                                                        navigator.goBack()
                                                    }
                                                },
                                                onDismissRequest = {

                                                }
                                            ).show()
                                        },
                                        colors = { MenuDefaults.itemColors(textColor = Color.Red) })
                                )
                            )
                        }
                    else {
                        val favoriteTooltipText =
                            if (albumInfoViewModel.ifFavorite) stringResource(
                                Res.string.favorite_added
                            ) else stringResource(
                                Res.string.favorite_removed
                            )
                        DesktopTooltipIconButton(
                            tooltip = favoriteTooltipText,
                            onClick = composeClick {
                                coroutineScope.launch {
                                    val ifFavoriteData =
                                        FavoriteCoordinator.setFavoriteData(
                                            dataSourceManager = albumInfoViewModel.dataSourceManager,
                                            type = MusicTypeEnum.ALBUM,
                                            itemId = albumInfoViewModel.xyAlbumInfoData?.itemId
                                                ?: "",
                                            ifFavorite = albumInfoViewModel.ifFavorite,
                                            musicController = albumInfoViewModel.musicController
                                        )
                                    albumInfoViewModel.updateIfFavorite(ifFavoriteData)
                                }
                            },
                        ) {
                            Icon(
                                painter = painterResource(if (albumInfoViewModel.ifFavorite) Res.drawable.favorite_border_24px else Res.drawable.favorite_24px),
                                contentDescription = favoriteTooltipText,
                                tint = if (albumInfoViewModel.ifFavorite) Color.Red else LocalContentColor.current
                            )
                        }
                    }
                }
            )

            LazyColumnNotComponent(
                state = lazyListState
            ) {
                item {
                    JvmMusicAlbumInfoComponent(
                        onData = {
                            albumInfoViewModel.xyAlbumInfoData
                        },
                        albumPic = albumInfoViewModel.albumPic,
                        onIfSavePlaybackHistory = { albumInfoViewModel.ifSavePlaybackHistory },
                        onSetIfSavePlaybackHistory = {
                            albumInfoViewModel.setIfSavePlaybackHistoryData(
                                itemId,
                                it
                            )
                        },
                        onOpenArtists = {
                            artistClickHandler.openAlbumArtists(albumInfoViewModel.xyAlbumInfoData)
                        }
                    )
                }
                stickyHeader(key = 2) {
                    JvmStickyHeaderOperationParent(
                        albumInfoViewModel = albumInfoViewModel,
                        musicListPage = musicListPage,
                        ifOpenSelect = selectUiState.isOpen,
                        isSelectAll = selectUiState.isSelectAll,
                        currentPlayAlbumId = playbackState.musicInfo?.album.orEmpty(),
                        playState = playbackState.state,
                        sortBy = sortBy
                    )
                }

                songTableItems(
                    tableKey = "album_info_music",
                    pagingItems = musicListPage,
                    columns = JvmAlbumMusicTableColumns.copy(
                        showInlineActions = !selectUiState.isOpen,
                        showSelectionColumn = selectUiState.isOpen,
                    ),
                    ifFavorite = { music -> music.itemId in favoriteSet },
                    ifPlay = { music -> playbackState.musicInfo?.itemId == music.itemId },
                    isSelected = { music -> music.itemId in selectUiState.selectedMusicIds },
                    onSongClick = { music ->
                        if (selectUiState.isOpen) {
                            albumInfoViewModel.selectControl.toggleSelection(
                                music.itemId,
                                onIsSelectAll = {
                                    selectUiState.selectedMusicIds.containsAll(
                                        musicListPage.itemSnapshotList.items.map { it.itemId }
                                    )
                                }
                            )
                        } else {
                            coroutineScope.launch {
                                albumInfoViewModel.musicPlayContext.album(
                                    OnMusicPlayParameter(
                                        musicId = music.itemId,
                                        albumId = music.album,
                                    )
                                )
                            }
                        }
                    },
                    onOpenArtist = artistClickHandler::openMusicArtists,
                    onOpenAlbum = { music ->
                        if (music.album.isNotBlank()) {
                            navigator.navigate(
                                AlbumInfo(
                                    music.album,
                                    MusicDataTypeEnum.ALBUM,
                                )
                            )
                        }
                    },
                    onFavoriteClick = { music ->
                        albumInfoViewModel.musicController.invokingOnFavorite(music.itemId)
                    },
                    onDownloadClick = {},
                    onMoreClick = { music ->
                        coroutineScope.launch {
                            music.show()
                        }
                    },
                    onSelectionClick = { musicId ->
                        albumInfoViewModel.selectControl.toggleSelection(
                            musicId,
                            onIsSelectAll = {
                                selectUiState.selectedMusicIds.containsAll(
                                    musicListPage.itemSnapshotList.items.map { it.itemId }
                                )
                            }
                        )
                    }
                )

                val refreshState = musicListPage.loadState.refresh
                val appendState = musicListPage.loadState.append
                val hasData = musicListPage.itemCount > 0
                val isInitialLoading = refreshState is LoadState.Loading && !hasData
                val isAppending = appendState is LoadState.Loading && hasData
                val isAppendEndReached =
                    hasData && appendState is LoadState.NotLoading && appendState.endOfPaginationReached
                val isEmpty =
                    !hasData && refreshState is LoadState.NotLoading && appendState is LoadState.NotLoading
                val appendError = appendState is LoadState.Error && hasData
                val refreshError = refreshState is LoadState.Error
                item {
                    LazyLoadingAndStatus(
                        text = stringResource(
                            when {
                                isEmpty -> Res.string.reached_bottom
                                isInitialLoading || isAppending -> Res.string.loading
                                isAppendEndReached -> Res.string.reached_bottom
                                else -> Res.string.empty_info
                            }
                        ),
                        ifLoading = isInitialLoading || isAppending
                    )
                }

                when {
                    appendError -> {
                        item(key = 8888) {
                            ErrorMoreRetryItem {
                                musicListPage.retry()
                            }
                        }
                    }

                    refreshError -> {
                        item(key = 7777) {
                            if (!hasData) {
                                ErrorContent {
                                    musicListPage.retry()
                                }
                            } else {
                                ErrorMoreRetryItem {
                                    musicListPage.retry()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

/**
 * 音乐列表操作栏
 */
@Composable
private fun JvmMusicListOperation(
    playbackHistoryProgress: Progress?,
    currentPlayAlbumId: String,
    album: XyAlbum?,
    musicPlayContext: MusicPlayContext,
    playState: PlayStateEnum,
    onRemovePlayerHistory: (String) -> Unit,
    onPlayOrPause: () -> Unit,
    selectControl: SelectControl,
    onSelectAll: () -> Unit,
    ifOpenSelect: Boolean,
    isSelectAll: Boolean,
    sortContent: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pauseText = stringResource(Res.string.pause_playback)
    val resumeText = stringResource(Res.string.resume_playback)
    val startText = stringResource(Res.string.start_playback)
    val albumId = album?.itemId.orEmpty()
    val isCurrentAlbum = albumId == currentPlayAlbumId
    val isPlayingCurrentAlbum = isCurrentAlbum && playState == PlayStateEnum.Playing
    val hasPlaybackHistory = playbackHistoryProgress != null
    val showPlaybackHistoryAction = !isCurrentAlbum && hasPlaybackHistory
    val playIcon = when {
        ifOpenSelect -> null
        isPlayingCurrentAlbum -> Res.drawable.pause_circle_24px
        else -> Res.drawable.play_circle_24px
    }
    val playActionText = when {
        isPlayingCurrentAlbum -> pauseText
        isCurrentAlbum -> resumeText
        hasPlaybackHistory -> "$resumeText:${playbackHistoryProgress.musicName}"
        else -> startText
    }

    XyRow(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .debounceClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (ifOpenSelect) {
                    onSelectAll()
                } else {
                    coroutineScope.launch {
                        if (isCurrentAlbum) {
                            onPlayOrPause()
                        } else {
                            musicPlayContext.album(
                                OnMusicPlayParameter(
                                    musicId = playbackHistoryProgress?.musicId.orEmpty(),
                                    albumId = albumId
                                )
                            )
                        }
                    }
                }
            }
            .height(JvmAlbumStickyOperationHeight),
        paddingValues = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding
        ),
    ) {


        if (ifOpenSelect) {
            XySelectAllComponent(
                isSelectAll = isSelectAll,
                onSelectAll = onSelectAll
            )
            val closeSelectionText = stringResource(Res.string.close_selection)
            DesktopTooltipIconButton(
                tooltip = closeSelectionText,
                onClick = {
                    selectControl.dismiss()
                },
            ) {
                Icon(
                    painter = painterResource(Res.drawable.close_24px),
                    contentDescription = closeSelectionText
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                playIcon?.let { painter ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(painter),
                            contentDescription = playActionText,
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
                Text(
                    text = playActionText,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (showPlaybackHistoryAction) {
                val deletePlaybackHistoryText = stringResource(Res.string.delete_playback_history)
                DesktopTooltipIconButton(
                    tooltip = deletePlaybackHistoryText,
                    onClick = {
                        onRemovePlayerHistory(playbackHistoryProgress.musicId)
                    },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.close_24px),
                        contentDescription = deletePlaybackHistoryText
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    sortContent()

                    val selectText = stringResource(Res.string.select)
                    DesktopTooltipIconButton(
                        tooltip = selectText,
                        onClick = {
                            selectControl.show(true)
                        },
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.playlist_add_check_24px),
                            contentDescription = selectText
                        )
                    }
                }
            }
        }

    }
}

/**
 * 音乐专辑信息组件
 * @param [onData] 专辑和专辑艺术家信息
 * @param [onIfSavePlaybackHistory] 是否保存播放历史记录
 * @param [onSetIfSavePlaybackHistory] 设置是否保存播放历史记录
 */
@OptIn(ExperimentalEncodingApi::class)
@Composable
private fun JvmMusicAlbumInfoComponent(
    onData: () -> XyAlbum?,
    albumPic: String?,
    onIfSavePlaybackHistory: () -> Boolean,
    onSetIfSavePlaybackHistory: (Boolean) -> Unit,
    // 点击专辑艺术家文本时执行，调用方负责传入统一的艺术家打开逻辑。
    onOpenArtists: () -> Unit,
    ifShowPlaybackHistory: Boolean = true
) {
    val album = onData()
    val coverUrls = rememberAlbumCoverUrls(album, albumPic)
    val typeText = if (album?.ifPlaylist == true) {
        stringResource(Res.string.playlist)
    } else {
        stringResource(Res.string.album)
    }
    val songsCountSuffix = stringResource(Res.string.songs_count_suffix)
    val subText = buildList {
        album?.artists?.takeIf { it.isNotBlank() }?.let { add(it) }
        album?.year?.let { add(it.toString()) }
        album?.musicCount?.takeIf { it > 0 }?.let { add("$it$songsCountSuffix") }
    }.joinToString(" • ")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                        MaterialTheme.colorScheme.background,
                    )
                )
            )
            .padding(
                start = XyTheme.dimens.outerHorizontalPadding,
                end = XyTheme.dimens.outerHorizontalPadding,
                top = XyTheme.dimens.outerVerticalPadding,
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(JvmDefaultAlbumInfoHeight),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            XyImage(
                modifier = Modifier
                    .size(JvmAlbumInfoCoverSize)
                    .clip(RoundedCornerShape(XyTheme.dimens.corner))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xff3b82f6), Color(0xff8b5cf6)),
                            start = Offset(x = Float.POSITIVE_INFINITY, y = 0f),
                            end = Offset(x = 0f, y = Float.POSITIVE_INFINITY)
                        )
                    ),
                model = coverUrls.primaryUrl,
                backModel = coverUrls.fallbackUrl,
                placeholder = Res.drawable.music_xy_placeholder_foreground,
                error = Res.drawable.music_xy_placeholder_foreground,
                fallback = Res.drawable.music_xy_placeholder_foreground,
                contentDescription = stringResource(Res.string.album_cover),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = XyTheme.dimens.contentPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = typeText,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = album?.name ?: "",
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subText.isNotBlank()) {
                    XyTextSub(
                        text = subText,
                        maxLines = 2,
                        onClick = if (album?.artists.isNullOrBlank()) null else onOpenArtists,
                    )
                }
            }

        }
        if (ifShowPlaybackHistory)
        //修改为切换开启播放历史
            XyItemSwitcher(
                state = onIfSavePlaybackHistory(),
                onChange = { onSetIfSavePlaybackHistory(it) },
                text = stringResource(Res.string.enable_playback_history),
                paddingValue = PaddingValues()
            )
    }

}


@Composable
private fun JvmStickyHeaderOperation(
    onMusicListPage: () -> LazyPagingItems<XyMusic>,
    albumInfoViewModel: AlbumInfoViewModel,
    currentPlayAlbumId: String,
    playState: PlayStateEnum,
    ifOpenSelect: Boolean,
    isSelectAll: Boolean,
    sortContent: @Composable () -> Unit
) {
    JvmMusicListOperation(
        playbackHistoryProgress = albumInfoViewModel.albumPlayerHistoryProgress,
        currentPlayAlbumId = currentPlayAlbumId,
        album = albumInfoViewModel.xyAlbumInfoData,
        musicPlayContext = albumInfoViewModel.musicPlayContext,
        playState = playState,
        onRemovePlayerHistory = {
            albumInfoViewModel.removeAlbumPlayerHistoryProgress(
                it
            )
        },
        onPlayOrPause = {
            if (playState != PlayStateEnum.Pause) {
                albumInfoViewModel.musicController.pause()
            } else {
                albumInfoViewModel.musicController.resume()
            }
        },
        selectControl = albumInfoViewModel.selectControl,
        onSelectAll = {
            albumInfoViewModel.selectControl.toggleSelectionAll(onMusicListPage().itemSnapshotList.items.map { it.itemId })
        },
        ifOpenSelect = ifOpenSelect,
        isSelectAll = isSelectAll,
        sortContent = sortContent
    )
}

@Composable
private fun JvmStickyHeaderOperationParent(
    albumInfoViewModel: AlbumInfoViewModel,
    musicListPage: LazyPagingItems<XyMusic>,
    ifOpenSelect: Boolean,
    isSelectAll: Boolean,
    currentPlayAlbumId: String,
    playState: PlayStateEnum,
    sortBy: Sort,
) {

    val mainViewModel = LocalMainViewModel.current
    JvmStickyHeaderOperation(
        onMusicListPage = { musicListPage },
        albumInfoViewModel = albumInfoViewModel,
        currentPlayAlbumId = currentPlayAlbumId,
        playState = playState,
        ifOpenSelect = ifOpenSelect,
        isSelectAll = isSelectAll,
        sortContent = {
            val openSortAndFilterMenuText =
                stringResource(Res.string.open_sort_and_filter_menu)
            DesktopTooltipBox(tooltip = openSortAndFilterMenuText) {
                SelectSortBottomSheetComponent(
                    onIfSelectOneYear = { albumInfoViewModel.dataSourceManager.dataSourceType?.ifAlbumInfoSelectOneYear },
                    onIfStartEndYear = { albumInfoViewModel.dataSourceManager.dataSourceType?.ifAlbumInfoSelectStartEndYear },
                    onIfSort = { albumInfoViewModel.dataSourceManager.dataSourceType?.ifAlbumInfoSort },
                    onIfFavoriteFilter = { albumInfoViewModel.dataSourceManager.dataSourceType?.ifAlbumInfoFavoriteFilter },
                    onSortTypeClick = {
                        albumInfoViewModel.setSortedData(it) { musicListPage.refresh() }
                    },
                    onSortType = { sortBy.sortType },
                    onDefaultSortType = { albumInfoViewModel.defaultSortType },
                    onIfFavorite = { sortBy.isFavorite == true },
                    setFavorite = {
                        albumInfoViewModel.setFavorite(
                            it
                        ) { musicListPage.refresh() }
                    },
                    sortTypeList = listOf(
                        SortTypeEnum.CREATE_TIME_ASC,
                        SortTypeEnum.CREATE_TIME_DESC,
                        SortTypeEnum.MUSIC_NAME_ASC,
                        SortTypeEnum.MUSIC_NAME_DESC
                    ),
                    onYearSet = { mainViewModel.yearSet },
                    onSelectYear = { sortBy.yearList?.get(0) },
                    onSetSelectYear = { year ->
                        year?.let {
                            albumInfoViewModel.setFilterYear(
                                listOf(it)
                            ) { musicListPage.refresh() }
                        }
                    },
                    onSelectRangeYear = { sortBy.yearList },
                    onSetSelectRangeYear = { years ->
                        albumInfoViewModel.setFilterYear(
                            years.mapNotNull { it }
                        ) { musicListPage.refresh() }
                    },
                    onEnabledClearClick = { albumInfoViewModel.isSortChange() }
                ) {
                    albumInfoViewModel.clearFilterOrSort { musicListPage.refresh() }
                }
            }
        }
    )


}



