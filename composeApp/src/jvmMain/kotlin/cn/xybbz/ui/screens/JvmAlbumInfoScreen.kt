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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import cn.xybbz.api.client.FavoriteCoordinator
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.DateUtil
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.image.rememberAlbumCoverUrls
import cn.xybbz.entity.data.Sort
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.extension.isSticking
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.PlayerModeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.FavoriteIconButton
import cn.xybbz.ui.components.JvmCompactSwitch
import cn.xybbz.ui.components.JvmLazyListComponent
import cn.xybbz.ui.components.JvmMusicSelectionActionBar
import cn.xybbz.ui.components.SelectSortBottomSheetComponent
import cn.xybbz.ui.components.SongTableColumns
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.XyDialogIcon
import cn.xybbz.ui.components.XyDialogTone
import cn.xybbz.ui.components.getExportPlaylistsAlertDialogObject
import cn.xybbz.ui.components.importPlaylistsCompose
import cn.xybbz.ui.components.rememberMusicArtistClickHandler
import cn.xybbz.ui.components.rememberMusicArtistInfoLoader
import cn.xybbz.ui.components.show
import cn.xybbz.ui.components.songTableItems
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.popup.XyDropdownMenu
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopTooltipBox
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyTextButton
import cn.xybbz.ui.xy.rememberXyTextHoverColor
import cn.xybbz.viewmodel.AlbumInfoViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.album
import xymusic.composeapp.generated.resources.album_cover
import xymusic.composeapp.generated.resources.check_24px
import xymusic.composeapp.generated.resources.close_24px
import xymusic.composeapp.generated.resources.confirm_delete_playlist
import xymusic.composeapp.generated.resources.delete_24px
import xymusic.composeapp.generated.resources.delete_playlist
import xymusic.composeapp.generated.resources.dialog_kicker_danger
import xymusic.composeapp.generated.resources.dialog_kicker_import
import xymusic.composeapp.generated.resources.dialog_kicker_rename
import xymusic.composeapp.generated.resources.edit_24px
import xymusic.composeapp.generated.resources.export_playlist
import xymusic.composeapp.generated.resources.import_info
import xymusic.composeapp.generated.resources.import_playlist
import xymusic.composeapp.generated.resources.import_playlist_hint
import xymusic.composeapp.generated.resources.login_24px
import xymusic.composeapp.generated.resources.modify_playlist_name
import xymusic.composeapp.generated.resources.more_vert_24px
import xymusic.composeapp.generated.resources.music_xy_placeholder_foreground
import xymusic.composeapp.generated.resources.open_operation_menu
import xymusic.composeapp.generated.resources.open_sort_and_filter_menu
import xymusic.composeapp.generated.resources.pause_24px
import xymusic.composeapp.generated.resources.pause_playback
import xymusic.composeapp.generated.resources.play_all
import xymusic.composeapp.generated.resources.play_arrow_24px
import xymusic.composeapp.generated.resources.playing
import xymusic.composeapp.generated.resources.playlist
import xymusic.composeapp.generated.resources.playlist_add_check_24px
import xymusic.composeapp.generated.resources.random_play
import xymusic.composeapp.generated.resources.rename_playlist
import xymusic.composeapp.generated.resources.resume_playback
import xymusic.composeapp.generated.resources.select
import xymusic.composeapp.generated.resources.selection_done
import xymusic.composeapp.generated.resources.shuffle_24px
import xymusic.composeapp.generated.resources.songs_count_suffix
import xymusic.composeapp.generated.resources.start_playback
import xymusic.composeapp.generated.resources.jvm_album_info_text_02
import xymusic.composeapp.generated.resources.jvm_album_info_text_03

/**
 * 专辑详情页主视觉方形封面直径。
 */
private val JvmAlbumInfoHeroCoverSize = 188.dp

/**
 * 主视觉黑胶唱片直径，略大于封面，向左露出版面。
 */
private val JvmAlbumInfoVinylSize = 200.dp

/**
 * 主视觉整体宽度：唱片左出部分 + 方形封面。
 */
private val JvmAlbumInfoHeroWidth = 240.dp

/**
 * 吸顶操作栏高度。
 */
private val JvmAlbumStickyOperationHeight = 64.dp

/**
 * 专辑详情页曲目表列配置。
 */
private val JvmAlbumMusicTableColumns = SongTableColumns(
    showFavoriteColumn = true,
    showInlineActions = true,
    showAlbumColumn = false,
    showMetaColumn = false,
)

/**
 * 专辑详情，通过分类或者其他跳转进入。
 *
 * @param [itemId] 专辑/歌单 id
 * @param [dataType] 数据类型：0 专辑，1 歌单
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
)
@Composable
fun JvmAlbumInfoScreen(
    itemId: String,
    dataType: MusicDataTypeEnum,
    albumInfoViewModel: AlbumInfoViewModel = koinViewModel<AlbumInfoViewModel> {
        parametersOf(
            itemId,
            dataType
        )
    },
) {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val navigator = LocalNavigator.current

    // 专辑头部的艺术家文本点击使用统一处理器，兼容多个专辑艺术家。
    val artistInfoLoader = rememberMusicArtistInfoLoader(albumInfoViewModel.dataSourceManager)
    val artistClickHandler = rememberMusicArtistClickHandler(
        artistList = artistInfoLoader.artistList,
        onLoadArtistInfos = artistInfoLoader.loadArtistInfos,
    )
    // 吸顶后标题栏由类型文案切换为专辑名称
    val isSticking by remember(lazyListState) { lazyListState.isSticking(1) }

    val favoriteSet by albumInfoViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    // 已下载完成的音乐 ID 集合，用于行内展示已下载图标并禁止重复下载
    val downloadMusicIds by albumInfoViewModel.downloadMusicIdsFlow.collectAsStateWithLifecycle(emptyList())
    val selectUiState by albumInfoViewModel.selectControl.uiState.collectAsStateWithLifecycle()
    val musicInfo by albumInfoViewModel.musicController.musicInfoFlow.collectAsStateWithLifecycle()
    val playState by albumInfoViewModel.musicController.stateFlow.collectAsStateWithLifecycle()
    val currentPlayingMusicIdFlow = remember(albumInfoViewModel) {
        albumInfoViewModel.musicController.musicInfoFlow.map { music ->
            music?.itemId
        }
    }

    val musicListPage = albumInfoViewModel.listPage.collectAsLazyPagingItems()
    val sortBy by albumInfoViewModel.sortBy.collectAsStateWithLifecycle()

    var ifShowMenu by remember {
        mutableStateOf(false)
    }
    var playlistName by remember {
        mutableStateOf(albumInfoViewModel.xyAlbumInfoData?.name ?: "")
    }

    val album = albumInfoViewModel.xyAlbumInfoData
    // 封面地址按当前数据源解析，兼顾主封面与备用封面
    val coverUrls = rememberAlbumCoverUrls(album, albumInfoViewModel.albumPic)
    val albumId = album?.itemId.orEmpty()
    // 当前播放队列是否属于本专辑，决定页头与吸顶播放按钮的状态
    val isCurrentAlbum = albumId.isNotBlank() && musicInfo?.album == albumId
    val isPlayingCurrentAlbum = isCurrentAlbum && playState == PlayStateEnum.Playing
    val albumPlayText = when {
        isPlayingCurrentAlbum -> stringResource(Res.string.pause_playback)
        isCurrentAlbum -> stringResource(Res.string.resume_playback)
        else -> stringResource(Res.string.play_all)
    }
    val albumPlayIcon = if (isPlayingCurrentAlbum) {
        // 暂停使用纯暂停条，避免与圆形按钮底色叠加成双层圆
        Res.drawable.pause_24px
    } else {
        Res.drawable.play_arrow_24px
    }
    val typeTitle = if (dataType == MusicDataTypeEnum.PLAYLIST) {
        stringResource(Res.string.playlist)
    } else {
        stringResource(Res.string.album)
    }

    // 播放全部：正在播放本专辑时切换暂停/继续，否则以专辑为上下文开始播放
    val playAlbum: () -> Unit = {
        coroutineScope.launch {
            if (isCurrentAlbum) {
                if (playState == PlayStateEnum.Pause) {
                    albumInfoViewModel.musicController.resume()
                } else {
                    albumInfoViewModel.musicController.pause()
                }
            } else {
                albumInfoViewModel.musicPlayContext.album(
                    OnMusicPlayParameter(
                        musicId = "",
                        albumId = itemId
                    )
                )
            }
        }
    }
    // 随机播放：先切换为随机模式，再以本专辑为上下文开始播放
    val shuffleAlbum: () -> Unit = {
        albumInfoViewModel.musicController.setPlayTypeData(PlayerModeEnum.RANDOM_PLAY)
        coroutineScope.launch {
            albumInfoViewModel.musicPlayContext.album(
                OnMusicPlayParameter(
                    musicId = "",
                    albumId = itemId
                )
            )
        }
    }
    // 收藏切换：专辑收藏统一走收藏协调器，歌单不展示收藏入口
    val toggleAlbumFavorite: () -> Unit = {
        coroutineScope.launch {
            val ifFavoriteData = FavoriteCoordinator.setFavoriteData(
                dataSourceManager = albumInfoViewModel.dataSourceManager,
                type = MusicTypeEnum.ALBUM,
                itemId = albumId,
                ifFavorite = albumInfoViewModel.ifFavorite,
                musicController = albumInfoViewModel.musicController
            )
            albumInfoViewModel.updateIfFavorite(ifFavoriteData)
        }
    }
    // 吸顶操作栏播放按钮：属于本专辑时切换播放状态，否则直接开始播放本专辑
    val togglePlayback: () -> Unit = {
        if (isCurrentAlbum) {
            coroutineScope.launch {
                if (playState == PlayStateEnum.Pause) {
                    albumInfoViewModel.musicController.resume()
                } else {
                    albumInfoViewModel.musicController.pause()
                }
            }
        } else {
            playAlbum()
        }
    }
    // 吸顶操作栏文案：优先展示当前曲目，其次展示播放进度来源
    val playingText = stringResource(Res.string.playing)
    val resumeText = stringResource(Res.string.resume_playback)
    val albumPlayLabel = when {
        isPlayingCurrentAlbum -> "$playingText · ${musicInfo?.name.orEmpty()}"
        isCurrentAlbum -> "$resumeText · ${musicInfo?.name.orEmpty()}"
        albumInfoViewModel.albumPlayerHistoryProgress != null ->
            "$resumeText: ${albumInfoViewModel.albumPlayerHistoryProgress?.musicName.orEmpty()}"

        else -> stringResource(Res.string.start_playback)
    }
    // 选择模式：本页使用列表内的选择操作栏，底部播放栏不再重复展示
    val toggleSelection: () -> Unit = {
        if (selectUiState.isOpen) {
            albumInfoViewModel.selectControl.dismiss()
        } else {
            albumInfoViewModel.selectControl.show(
                ifOpenSelect = true,
                playlistId = itemId,
                ifPlaylist = dataType == MusicDataTypeEnum.PLAYLIST,
                useListActionBar = true,
            )
        }
    }

    // 已加载曲目的音质标签，专辑档案本身不提供该信息
    val loadedSongs = musicListPage.itemSnapshotList.items
    val qualityText = loadedSongs.firstOrNull()?.qualityLabel()

    val removePlaylistAlertTitle = stringResource(Res.string.delete_playlist)
    val editPlaylistAlertTitle = stringResource(Res.string.modify_playlist_name)
    val importPlaylistAlertTitle = stringResource(Res.string.import_playlist)
    // 弹窗都在菜单点击回调里创建，正文必须先在 composable 作用域解析
    val importPlaylistHint = stringResource(Res.string.import_playlist_hint)
    val confirmDeletePlaylistText = stringResource(
        Res.string.confirm_delete_playlist,
        albumInfoViewModel.xyAlbumInfoData?.name ?: ""
    )

    XyColumnScreen {
        TopAppBarComponent(
            title = {
                AnimatedContent(
                    targetState = isSticking,
                    label = "album_info_title",
                    transitionSpec = {
                        (fadeIn() togetherWith fadeOut()).using(
                            SizeTransform(clip = false)
                        )
                    }
                ) { sticking ->
                    Text(
                        text = if (sticking) album?.name ?: "" else typeTitle,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    )
                }
            },
            actions = {
                // 歌单在顶栏保留管理菜单；专辑的收藏入口放在页面主视觉操作区
                if (dataType == MusicDataTypeEnum.PLAYLIST) {
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
                                            body = importPlaylistHint,
                                            icon = XyDialogIcon.Download,
                                            kicker = Res.string.dialog_kicker_import,
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
                                        coroutineScope = coroutineScope,
                                        playlistName = albumInfoViewModel.xyAlbumInfoData?.name,
                                        onPlayTrackList = { exportFailedTip ->
                                            albumInfoViewModel.exportPlaylist(exportFailedTip)
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
                                                        ?: "",
                                                    onChange = {
                                                        playlistName = it
                                                    },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            },
                                            icon = XyDialogIcon.Edit,
                                            kicker = Res.string.dialog_kicker_rename,
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
                                            tint = XyTheme.extendedColors.danger
                                        )
                                    },
                                    onClick = {
                                        ifShowMenu = false
                                        AlertDialogObject(
                                            title = removePlaylistAlertTitle,
                                            body = confirmDeletePlaylistText,
                                            tone = XyDialogTone.Danger,
                                            icon = XyDialogIcon.Trash,
                                            kicker = Res.string.dialog_kicker_danger,
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
                                    colors = { MenuDefaults.itemColors(textColor = XyTheme.extendedColors.danger) })
                            )
                        )
                    }
                }
            }
        )

        JvmLazyListComponent(
            lazyState = lazyListState,
            pagingItems = musicListPage,
        ) {
            item(key = "album_hero") {
                JvmAlbumDesktopHero(
                    album = album,
                    coverUrl = coverUrls.primaryUrl,
                    fallbackCoverUrl = coverUrls.fallbackUrl,
                    playText = albumPlayText,
                    playIcon = albumPlayIcon,
                    qualityText = qualityText,
                    ifSavePlaybackHistory = albumInfoViewModel.ifSavePlaybackHistory,
                    ifFavorite = albumInfoViewModel.ifFavorite,
                    showFavorite = dataType != MusicDataTypeEnum.PLAYLIST,
                    onIfSavePlaybackHistory = {
                        albumInfoViewModel.setIfSavePlaybackHistoryData(itemId, it)
                    },
                    onOpenArtists = {
                        artistClickHandler.openAlbumArtists(albumInfoViewModel.xyAlbumInfoData)
                    },
                    onPlayAll = playAlbum,
                    onShuffle = shuffleAlbum,
                    onToggleFavorite = toggleAlbumFavorite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = XyTheme.dimens.outerHorizontalPadding,
                            end = XyTheme.dimens.outerHorizontalPadding,
                            top = XyTheme.dimens.outerVerticalPadding,
                            bottom = XyTheme.dimens.innerVerticalPadding,
                        )
                )
            }

            stickyHeader(key = 2) {
                JvmAlbumStickyOperationBar(
                    ifOpenSelect = selectUiState.isOpen,
                    playIcon = if (selectUiState.isOpen) null else albumPlayIcon,
                    playLabel = albumPlayLabel,
                    onPlayOrPause = togglePlayback,
                    onToggleSelect = toggleSelection,
                    sortContent = {
                        JvmAlbumSortContent(
                            albumInfoViewModel = albumInfoViewModel,
                            musicListPage = musicListPage,
                            sortBy = sortBy,
                        )
                    }
                )
            }

            // 选择模式展开的批量操作栏，紧跟在吸顶操作栏下方
            item(key = "album_selection_bar") {
                AnimatedVisibility(
                    visible = selectUiState.isOpen,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    JvmMusicSelectionActionBar(
                        selectControl = albumInfoViewModel.selectControl,
                        allMusicIds = {
                            musicListPage.itemSnapshotList.items.map { it.itemId }
                        },
                        dataSourceManager = albumInfoViewModel.dataSourceManager,
                        musicPlayContext = albumInfoViewModel.musicPlayContext,
                        db = albumInfoViewModel.db,
                        downloadDb = albumInfoViewModel.downloadDb,
                        musicController = albumInfoViewModel.musicController,
                        downloaderManager = albumInfoViewModel.downloaderManager,
                        modifier = Modifier.padding(
                            start = XyTheme.dimens.outerHorizontalPadding,
                            end = XyTheme.dimens.outerHorizontalPadding,
                            bottom = XyTheme.dimens.innerVerticalPadding,
                        ),
                    )
                }
            }

            songTableItems(
                tableKey = "album_info_music",
                pagingItems = musicListPage,
                columns = JvmAlbumMusicTableColumns.copy(
                    showInlineActions = !selectUiState.isOpen,
                    showSelectionColumn = selectUiState.isOpen,
                ),
                ifFavorite = { music -> music.itemId in favoriteSet },
                ifDownloaded = { music -> music.itemId in downloadMusicIds },
                currentPlayingMusicIdFlow = currentPlayingMusicIdFlow,
                isSelected = { music -> music.itemId in selectUiState.selectedMusicIds },
                onSongClick = { _, music ->
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
                showViewAlbumMenuItem = false,
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
                onDownloadClick = { music ->
                    albumInfoViewModel.downloadMusic(music)
                },
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
        }
    }
}

/**
 * 专辑详情页头：黑胶出鞘式主视觉 + 专辑档案 + 播放操作。
 *
 * @param [album] 专辑信息
 * @param [coverUrl] 主封面地址
 * @param [fallbackCoverUrl] 备用封面地址
 * @param [playText] 播放按钮文案
 * @param [playIcon] 播放按钮图标
 * @param [qualityText] 已加载曲目的音质标签
 * @param [ifSavePlaybackHistory] 是否记录播放历史
 * @param [ifFavorite] 是否已收藏
 * @param [showFavorite] 是否展示收藏入口
 * @param [onIfSavePlaybackHistory] 切换播放历史记录
 * @param [onOpenArtists] 打开专辑艺术家
 * @param [onPlayAll] 播放全部
 * @param [onShuffle] 随机播放
 * @param [onToggleFavorite] 切换收藏
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JvmAlbumDesktopHero(
    album: XyAlbum?,
    coverUrl: String?,
    fallbackCoverUrl: String?,
    playText: String,
    playIcon: DrawableResource,
    qualityText: String?,
    ifSavePlaybackHistory: Boolean,
    ifFavorite: Boolean,
    showFavorite: Boolean,
    onIfSavePlaybackHistory: (Boolean) -> Unit,
    onOpenArtists: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 专辑艺术家文本由结构化列表按需拼接，避免名称中的斜杠参与数据拆分
    val albumArtistsText = album?.artists?.takeIf { it.isNotEmpty() }
        ?.joinToString(separator = Constants.SLASH_DELIMITER)
        .orEmpty()
    val songsCountText = album?.musicCount?.takeIf { it > 0 }?.let {
        "$it${stringResource(Res.string.songs_count_suffix)}"
    }
    // 发行日期优先使用完整发行时间，缺失时退回年份
    val releaseText = album?.premiereDate?.let { date ->
        DateUtil.run { date.toDateStr("yyyy-MM-dd") }
    } ?: album?.year?.toString()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JvmAlbumVinylCover(
                coverUrl = coverUrl,
                fallbackCoverUrl = fallbackCoverUrl,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Text(
                    text = album?.name ?: "",
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (albumArtistsText.isNotBlank()) {
                    XyTextButton(
                        text = albumArtistsText,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        onClick = onOpenArtists,
                    )
                }
                // 档案标签：发行日期、曲目数量与音质
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    releaseText?.takeIf { it.isNotBlank() }?.let { JvmAlbumMetaChip(it) }
                    songsCountText?.let { JvmAlbumMetaChip(it) }
                    qualityText?.takeIf { it.isNotBlank() }?.let {
                        JvmAlbumMetaChip(it, emphasized = true)
                    }
                }
                // 主操作区：播放全部为主按钮，随机播放为次按钮，收藏为图标按钮
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Button(
                        onClick = onPlayAll,
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                        shape = RoundedCornerShape(XyTheme.dimens.corner),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Icon(
                            painter = painterResource(playIcon),
                            contentDescription = playText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = playText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    OutlinedButton(
                        onClick = onShuffle,
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                        shape = RoundedCornerShape(XyTheme.dimens.corner),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.shuffle_24px),
                            contentDescription = stringResource(Res.string.random_play),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = stringResource(Res.string.random_play),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (showFavorite) {
                        FavoriteIconButton(
                            isFavorite = ifFavorite,
                            onClick = onToggleFavorite,
                            normalTint = XyTheme.extendedColors.favorite,
                        )
                    }
                }
                // 播放历史开关采用设计稿的紧凑开关行：标签 + 说明文案
                JvmAlbumPlaybackHistorySwitch(
                    checked = ifSavePlaybackHistory,
                    onCheckedChange = onIfSavePlaybackHistory,
                )
            }
        }
    }
}

/**
 * 播放历史开关行：紧凑胶囊开关 + 标签 + 说明文案，对齐设计稿的开关样式。
 *
 * @param [checked] 当前是否开启播放历史
 * @param [onCheckedChange] 开关状态变化回调
 */
@Composable
private fun JvmAlbumPlaybackHistorySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    // 悬停整行时文字切换为主题色，与其它可点击文字项保持一致的移入反馈
    val textColorState = rememberXyTextHoverColor(
        interactionSource = interactionSource,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            // 整行可点击切换，与开关保持同一交互结果
            .jvmHoverDebounceClickable(
                interactionSource = interactionSource,
                onClick = { onCheckedChange(!checked) },
            )
            .padding(vertical = 2.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        JvmCompactSwitch(
            checked = checked,
            enabled = true,
            onCheckedChange = onCheckedChange,
        )
        Text(
            text = stringResource(Res.string.jvm_album_info_text_02),
            color = textColorState.color,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        Text(
            text = stringResource(Res.string.jvm_album_info_text_03),
            color = if (textColorState.hovered) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
        )
    }
}

/**
 * 黑胶出鞘式封面：左侧唱片纹路，右侧方形专辑封面。
 */
@Composable
private fun JvmAlbumVinylCover(
    coverUrl: String?,
    fallbackCoverUrl: String?,
) {
    val discColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val ringColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)

    Box(
        modifier = Modifier
            .width(JvmAlbumInfoHeroWidth)
            .height(JvmAlbumInfoHeroCoverSize + 12.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        // 唱片纹路：从外缘向内逐圈绘制，保持设计稿的黑胶质感
        Canvas(
            modifier = Modifier
                .size(JvmAlbumInfoVinylSize)
                .align(Alignment.CenterStart)
        ) {
            val centerRadius = size.minDimension / 2
            drawCircle(color = discColor, radius = centerRadius)
            var ringRadius = centerRadius - 1.dp.toPx()
            val innerRadius = size.minDimension * 0.30f
            val step = 5.dp.toPx()
            while (ringRadius > innerRadius) {
                drawCircle(
                    color = ringColor,
                    radius = ringRadius,
                    style = Stroke(width = 1f)
                )
                ringRadius -= step
            }
        }
        XyImage(
            modifier = Modifier
                .size(JvmAlbumInfoHeroCoverSize)
                .clip(RoundedCornerShape(XyTheme.dimens.corner))
                .background(
                    Brush.linearGradient(
                        // 专辑封面占位渐变从主题扩展颜色读取
                        colors = listOf(
                            XyTheme.extendedColors.mediaCoverGradientStart,
                            XyTheme.extendedColors.mediaCoverGradientEnd,
                        ),
                        start = Offset(x = Float.POSITIVE_INFINITY, y = 0f),
                        end = Offset(x = 0f, y = Float.POSITIVE_INFINITY)
                    )
                ),
            model = coverUrl,
            backModel = fallbackCoverUrl,
            placeholder = Res.drawable.music_xy_placeholder_foreground,
            error = Res.drawable.music_xy_placeholder_foreground,
            fallback = Res.drawable.music_xy_placeholder_foreground,
            contentDescription = stringResource(Res.string.album_cover),
        )
    }
}

/**
 * 专辑档案标签，主色描边样式用于音质等需要强调的信息。
 */
@Composable
private fun JvmAlbumMetaChip(
    text: String,
    emphasized: Boolean = false,
) {
    val contentColor = if (emphasized) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val borderColor = if (emphasized) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (emphasized) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            Color.Transparent
        },
        border = BorderStroke(1.dp, borderColor),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}

/**
 * 吸顶操作栏：播放控制 + 当前曲目 + 排序筛选 + 选择入口。
 */
@Composable
private fun JvmAlbumStickyOperationBar(
    ifOpenSelect: Boolean,
    playIcon: DrawableResource?,
    playLabel: String,
    onPlayOrPause: () -> Unit,
    onToggleSelect: () -> Unit,
    sortContent: @Composable () -> Unit,
) {
    val selectText = stringResource(Res.string.select)
    val doneText = stringResource(Res.string.selection_done)
    val actionText = if (ifOpenSelect) doneText else selectText

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(JvmAlbumStickyOperationHeight)
                .padding(horizontal = XyTheme.dimens.outerHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (!ifOpenSelect) {
                playIcon?.let { painter ->
                    DesktopTooltipBox(tooltip = playLabel) {
                        IconButton(
                            onClick = onPlayOrPause,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                        ) {
                            Icon(
                                painter = painterResource(painter),
                                contentDescription = playLabel,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                Text(
                    text = playLabel,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            if (!ifOpenSelect) {
                sortContent()
            }
            OutlinedButton(
                onClick = onToggleSelect,
                modifier = Modifier
                    .height(32.dp)
                    .pointerHoverIcon(PointerIcon.Hand),
                shape = RoundedCornerShape(XyTheme.dimens.corner),
                contentPadding = PaddingValues(horizontal = 10.dp),
            ) {
                Icon(
                    // 选择态切换为完成图标，明确当前点击会退出选择模式
                    painter = painterResource(
                        if (ifOpenSelect) {
                            Res.drawable.check_24px
                        } else {
                            Res.drawable.playlist_add_check_24px
                        }
                    ),
                    contentDescription = actionText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = actionText)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/**
 * 专辑曲目排序与筛选入口，复用通用的排序筛选面板。
 */
@Composable
private fun JvmAlbumSortContent(
    albumInfoViewModel: AlbumInfoViewModel,
    musicListPage: LazyPagingItems<XyMusic>,
    sortBy: Sort,
) {
    val mainViewModel = LocalMainViewModel.current
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

/**
 * 从歌曲元数据拼出音质标签（格式 + 位深 + 采样率），供专辑页头与曲目栏展示。
 */
private fun XyMusic.qualityLabel(): String? {
    val containerLabel = container?.trim()?.takeIf { it.isNotBlank() }?.uppercase()
    val bitDepthLabel = bitDepth?.takeIf { it > 0 }?.let { "${it}bit" }
    val sampleRateLabel = sampleRate?.takeIf { it > 0 }?.let { rate ->
        if (rate >= 1000) "${rate / 1000}kHz" else "${rate}Hz"
    }
    return listOfNotNull(containerLabel, bitDepthLabel, sampleRateLabel)
        .joinToString(" ")
        .takeIf { it.isNotBlank() }
}
