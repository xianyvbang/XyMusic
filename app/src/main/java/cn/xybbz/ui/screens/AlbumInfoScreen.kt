package cn.xybbz.ui.screens


import android.util.Log
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.PlaylistAddCheck
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.R
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.music.MusicController
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.entity.data.Sort
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.extension.isSticking
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.progress.Progress
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.LazyListComponent
import cn.xybbz.ui.components.MusicItemIndexComponent
import cn.xybbz.ui.components.SelectSortBottomSheetComponent
import cn.xybbz.ui.components.SwipeRefreshListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.XySelectAllComponent
import cn.xybbz.ui.components.getExportPlaylistsAlertDialogObject
import cn.xybbz.ui.components.importPlaylistsCompose
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.popup.XyDropdownMenu
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyItemSwitcherNotPadding
import cn.xybbz.ui.xy.XyItemTextHorizontal
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.viewmodel.AlbumInfoViewModel
import kotlinx.coroutines.launch
import kotlin.io.encoding.ExperimentalEncodingApi


internal val DefaultAlbumInfoHeight = 124.dp

/**
 * 专辑详情,通过分类或者其他跳转
 * [dataType] 数据类型 0专辑,1歌单
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlbumInfoScreen(
    itemId: String,
    dataType: MusicDataTypeEnum,
    albumInfoViewModel: AlbumInfoViewModel = hiltViewModel<AlbumInfoViewModel, AlbumInfoViewModel.Factory>(
        creationCallback = { factory ->
            factory.create(
                itemId = itemId,
                dataType = dataType
            )
        })
) {

    SideEffect {
        Log.d("=====", "MusicAudiobookInfoScreen重组一次")
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val navHostController = LocalNavController.current
    val isSticking by remember(lazyListState) { lazyListState.isSticking(1) }

    val favoriteList by albumInfoViewModel.favoriteRepository.favoriteMap.collectAsState()

    val musicListPage =
        albumInfoViewModel.xyMusicList.collectAsLazyPagingItems()
    val sortBy by albumInfoViewModel.sortBy.collectAsState()

    var ifShowMenu by remember {
        mutableStateOf(false)
    }
    var playlistName by remember {
        mutableStateOf(albumInfoViewModel.xyAlbumInfoData?.name ?: "")
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                return if (delta > 0) Offset.Zero else {
                    val y =
                        -lazyListState.dispatchRawDelta(-delta)
                    Offset(
                        x = 0f,
                        y = y
                    )
                }
            }
        }
    }

    BoxWithConstraints {
        val maxHeight = this.maxHeight
        XyColumnScreen(
            modifier = Modifier.brashColor(
                topVerticalColor = albumInfoViewModel.backgroundConfig.albumInfoBrash[0],
                bottomVerticalColor = albumInfoViewModel.backgroundConfig.albumInfoBrash[1]
            )
        ) {
            TopAppBarComponent(
                modifier = Modifier.statusBarsPadding(),
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
                                    stringResource(R.string.playlist)
                                else
                                    stringResource(
                                        R.string.album
                                    ),
                                modifier = Modifier.basicMarquee(
                                    iterations = Int.MAX_VALUE
                                )
                            )
                        }

                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            albumInfoViewModel.selectControl.dismiss()
                            navHostController.popBackStack()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.return_album_page)
                        )
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
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = stringResource(R.string.open_operation_menu)
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
                                        title = context.getString(R.string.import_playlist),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.Login,
                                                contentDescription = stringResource(R.string.import_playlist)
                                            )
                                        },
                                        onClick = importPlaylistsCompose(onCreatePlaylist = {
                                            AlertDialogObject(
                                                title = R.string.import_playlist,
                                                content = {
                                                    XyItemTextHorizontal(
                                                        text = stringResource(R.string.import_playlist_hint)
                                                    )
                                                },
                                                onConfirmation = {
                                                    coroutineScope.launch {
                                                        albumInfoViewModel.importPlaylist(it)
                                                    }
                                                },
                                                confirmText = R.string.import_info,
                                                onDismissRequest = {}).show()

                                        }) {
                                            ifShowMenu = false
                                        }),
                                    MenuItemDefaultData(
                                        title = stringResource(R.string.export_playlist),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.Logout,
                                                contentDescription = stringResource(R.string.export_playlist)
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
                                        title = stringResource(R.string.rename_playlist),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Edit,
                                                contentDescription = stringResource(R.string.rename_playlist)
                                            )
                                        },
                                        onClick = {
                                            ifShowMenu = false
                                            AlertDialogObject(
                                                title = R.string.modify_playlist_name,
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
                                        title = stringResource(R.string.delete_playlist),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Delete,
                                                contentDescription = stringResource(R.string.delete_playlist),
                                                tint = Color.Red
                                            )
                                        },
                                        onClick = {
                                            ifShowMenu = false
                                            AlertDialogObject(
                                                title = R.string.delete_playlist,
                                                content = {
                                                    XyItemTextHorizontal(
                                                        text = stringResource(
                                                            R.string.confirm_delete_playlist,
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
                                                        navHostController.popBackStack()
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
                }
            )

            SwipeRefreshListComponent(
                collectAsLazyPagingItems = musicListPage,
                lazyState = lazyListState,
                lazyColumBottom = null,
                bottomItem = null,
                modifier = Modifier
                    .nestedScroll(nestedScrollConnection)
            ) { list ->
                item {
                    MusicAlbumInfoComponent(
                        onData = {
                            albumInfoViewModel.xyAlbumInfoData
                        },
                        onIfFavorite = { albumInfoViewModel.ifFavorite },
                        onSetIfFavorite = { albumInfoViewModel.updateIfFavorite(it) },
                        onIfSavePlaybackHistory = { albumInfoViewModel.ifSavePlaybackHistory },
                        onSetIfSavePlaybackHistory = {
                            albumInfoViewModel.setIfSavePlaybackHistoryData(
                                itemId,
                                it
                            )
                        },
                        dataSourceManager = albumInfoViewModel.dataSourceManager,
                        musicController = albumInfoViewModel.musicController
                    )
                }
                stickyHeader(key = 2) { headerIndex ->
                    StickyHeaderOperationParent(
                        onMusicListPage = { musicListPage },
                        albumInfoViewModel = albumInfoViewModel,
                        musicListPage = musicListPage,
                        sortBy = sortBy
                    )
                }

                item {
                    LazyListComponent(
                        modifier = Modifier.height(
                            maxHeight - DefaultAlbumInfoHeight - /*XyTheme.dimens.contentPadding -*/ TopAppBarDefaults.TopAppBarExpandedHeight - WindowInsets.statusBars.asPaddingValues()
                                .calculateTopPadding() + XyTheme.dimens.snackBarPlayerHeight + WindowInsets.navigationBars.asPaddingValues()
                                .calculateBottomPadding()
                        ),
                        collectAsLazyPagingItems = musicListPage
                    ) {
                        items(
                            list.itemCount,
                            key = list.itemKey { item -> item.itemId },
                            contentType = list.itemContentType { MusicTypeEnum.MUSIC }
                        ) { index ->

                            list[index]?.let { music ->
                                MusicItemIndexComponent(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = Color.Transparent,
                                    onMusicData = { music },
                                    onIfFavorite = {
                                        if (favoriteList.containsKey(music.itemId)) {
                                            favoriteList.getOrDefault(music.itemId, false)
                                        } else {
                                            music.ifFavoriteStatus
                                        }
                                    },
                                    index = index + 1,
                                    textColor = if (albumInfoViewModel.musicController.musicInfo?.itemId == music.itemId)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    subordination =
                                        if (albumInfoViewModel.albumPlayerHistoryProgressMap.containsKey(
                                                music.itemId
                                            )
                                        ) "已播: ${albumInfoViewModel.albumPlayerHistoryProgressMap[music.itemId]}%" else music.artists
                                            ?: "",
                                    onMusicPlay = { parameter ->
                                        coroutineScope.launch {
                                            albumInfoViewModel.musicPlayContext.album(
                                                parameter
                                            )
                                        }
                                    },
                                    ifSelect = albumInfoViewModel.selectControl.ifOpenSelect,
                                    ifSelectCheckBox = { albumInfoViewModel.selectControl.selectMusicDataList.any { it.itemId == music.itemId } },
                                    trailingOnClick = { select ->
                                        albumInfoViewModel.selectControl.toggleSelection(
                                            music,
                                            onIsSelectAll = {
                                                albumInfoViewModel.selectControl.selectMusicDataList.containsAll(
                                                    musicListPage.itemSnapshotList.items
                                                )
                                            }
                                        )
                                    }
                                )
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
private fun MusicListOperation(
    onAlbumPlayerHistoryProgress: () -> Progress?,
    onPlayAlbumId: () -> String,
    onMusicAlbum: () -> XyAlbum?,
    musicPlayContext: MusicPlayContext,
    onPlayState: () -> PlayStateEnum,
    onRemovePlayerHistory: (String) -> Unit,
    onMusicPlanOrPause: () -> Unit,
    albumInfoViewModel: AlbumInfoViewModel,
    onSelectAll: () -> Unit,
    sortContent: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val iconImageVector by remember {
        derivedStateOf {
            if (albumInfoViewModel.selectControl.ifOpenSelect) null else if (onMusicAlbum()?.itemId == onPlayAlbumId())
                if (onPlayState() == PlayStateEnum.Playing)
                    Icons.Rounded.PauseCircle
                else
                    Icons.Rounded.PlayCircle
            else
                Icons.Rounded.PlayCircle
        }
    }

    val textInfo by remember {
        derivedStateOf {
            if (onMusicAlbum()?.itemId == onPlayAlbumId()) {
                if (onPlayState() == PlayStateEnum.Playing)
                    context.getString(R.string.pause_playback)
                else
                    context.getString(R.string.resume_playback)
            } else if (onAlbumPlayerHistoryProgress() != null)
                "${context.getString(R.string.resume_playback)}:${onAlbumPlayerHistoryProgress()?.musicName}"
            else
                context.getString(R.string.start_playback)
        }
    }

    XyRow(
        modifier = Modifier
            .debounceClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (albumInfoViewModel.selectControl.ifOpenSelect) {
                    onSelectAll()
                } else {
                    coroutineScope.launch {
                        if (onMusicAlbum()?.itemId == onPlayAlbumId()) {
                            onMusicPlanOrPause()
                        } else {
                            musicPlayContext.album(
                                OnMusicPlayParameter(
                                    musicId = onAlbumPlayerHistoryProgress()?.musicId
                                        ?: "",
                                    albumId = onMusicAlbum()?.itemId ?: ""
                                )
                            )
                        }
                    }
                }
            }
            .height(XyTheme.dimens.itemHeight),
        paddingValues = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding
        ),
    ) {


        if (albumInfoViewModel.selectControl.ifOpenSelect) {
            XySelectAllComponent(
                isSelectAll = albumInfoViewModel.selectControl.isSelectAll,
                onSelectAll = onSelectAll
            )
            IconButton(onClick = {
                albumInfoViewModel.selectControl.dismiss()
            }) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.close_selection)
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                iconImageVector?.let { imageVector ->
                    Icon(
                        imageVector = imageVector,
                        contentDescription = textInfo
                    )
                }
                Text(text = textInfo)
            }
            if (onMusicAlbum()?.itemId != onPlayAlbumId() && onAlbumPlayerHistoryProgress() != null) {
                IconButton(onClick = {
                    onAlbumPlayerHistoryProgress()?.let {
                        onRemovePlayerHistory(it.musicId)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.delete_playback_history)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    sortContent()

                    IconButton(onClick = {
                        albumInfoViewModel.show()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.PlaylistAddCheck,
                            contentDescription = stringResource(R.string.select)
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
private fun MusicAlbumInfoComponent(
    onData: () -> XyAlbum?,
    onIfFavorite: () -> Boolean,
    onSetIfFavorite: (Boolean) -> Unit,
    onIfSavePlaybackHistory: () -> Boolean,
    onSetIfSavePlaybackHistory: (Boolean) -> Unit,
    dataSourceManager: IDataSourceManager,
    musicController: MusicController,
    ifShowPlaybackHistory: Boolean = true
) {
    val coroutineScope = rememberCoroutineScope()

    XyColumn(
        backgroundColor = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(DefaultAlbumInfoHeight),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start
        ) {
            XyImage(
                model = onData()?.pic,
                contentDescription = stringResource(R.string.album_cover),
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(5.dp)
                    )
                    .aspectRatio(1F)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xff3b82f6), Color(0xff8b5cf6)),
                            start = Offset(x = Float.POSITIVE_INFINITY, y = 0f), // 右上角
                            end = Offset(x = 0f, y = Float.POSITIVE_INFINITY)   // 左下角
                        )
                    ),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.music_xy_placeholder_foreground),
                error = painterResource(id = R.drawable.music_xy_placeholder_foreground),
                fallback = painterResource(id = R.drawable.music_xy_placeholder_foreground),
            )
            Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
            Column(
                modifier = Modifier
                    .fillMaxHeight(),
            ) {
                Text(
                    text = onData()?.name ?: "",
                    fontWeight = FontWeight.W700,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(XyTheme.dimens.contentPadding))
                Text(
                    text = onData()?.artists ?: "",
                    maxLines = 3,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.W500,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        }
        if (ifShowPlaybackHistory)
        //修改为切换开启播放历史
            XyRow(paddingValues = PaddingValues(top = XyTheme.dimens.outerVerticalPadding)) {
                XyItemSwitcherNotPadding(
                    state = onIfSavePlaybackHistory(),
                    onChange = { onSetIfSavePlaybackHistory(it) },
                    text = stringResource(R.string.enable_playback_history)
                )

                IconButton(onClick = composeClick {
                    coroutineScope.launch {
                        val ifFavoriteData = dataSourceManager.setFavoriteData(
                            type = MusicTypeEnum.ALBUM,
                            itemId = onData()?.itemId ?: "",
                            musicController = musicController,
                            ifFavorite = onIfFavorite()
                        )
                        onSetIfFavorite(ifFavoriteData)
                    }
                }) {
                    Icon(
                        imageVector = if (onIfFavorite()) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (onIfFavorite()) stringResource(R.string.favorite_added) else stringResource(
                            R.string.favorite_removed
                        ),
                        tint = if (onIfFavorite()) Color.Red else LocalContentColor.current
                    )
                }
            }
    }

}


@Composable
private fun StickyHeaderOperation(
    onMusicListPage: () -> LazyPagingItems<XyMusic>,
    albumInfoViewModel: AlbumInfoViewModel,
    sortContent: @Composable () -> Unit
) {
    MusicListOperation(
        onAlbumPlayerHistoryProgress = { albumInfoViewModel.albumPlayerHistoryProgress },
        onPlayAlbumId = {
            albumInfoViewModel.musicController.musicInfo?.album
                ?: ""
        },
        onMusicAlbum = { albumInfoViewModel.xyAlbumInfoData },
        musicPlayContext = albumInfoViewModel.musicPlayContext,
        onPlayState = { albumInfoViewModel.musicController.state },
        onRemovePlayerHistory = {
            albumInfoViewModel.removeAlbumPlayerHistoryProgress(
                it
            )
        },
        onMusicPlanOrPause = {
            if (albumInfoViewModel.musicController.state != PlayStateEnum.Pause) {
                albumInfoViewModel.musicController.pause()
            } else {
                albumInfoViewModel.musicController.resume()
            }
        },
        albumInfoViewModel = albumInfoViewModel,
        onSelectAll = {
            albumInfoViewModel.selectControl.toggleSelectionAll(onMusicListPage().itemSnapshotList.items)
        },
        sortContent = sortContent
    )
}

@Composable
private fun StickyHeaderOperationParent(
    onMusicListPage: () -> LazyPagingItems<XyMusic>,
    albumInfoViewModel: AlbumInfoViewModel,
    musicListPage: LazyPagingItems<XyMusic>,
    sortBy: Sort,
) {

    val mainViewModel = LocalMainViewModel.current
    StickyHeaderOperation(
        onMusicListPage = onMusicListPage,
        albumInfoViewModel = albumInfoViewModel,
        sortContent = {
            SelectSortBottomSheetComponent(
                onIfYearFilter = { albumInfoViewModel.dataSourceManager.dataSourceType?.ifYearFilter },
                onIfSelectOneYear = { albumInfoViewModel.dataSourceManager.dataSourceType?.ifMusicSelectOneYear },
                onIfStartEndYear = { albumInfoViewModel.dataSourceManager.dataSourceType?.ifStartEndYear },
                onIfSort = { albumInfoViewModel.dataSourceManager.dataSourceType?.ifMusicSort },
                onIfFavoriteFilter = { albumInfoViewModel.dataSourceManager.dataSourceType?.ifMusicFavoriteFilter },
                onSortTypeClick = {
                    albumInfoViewModel.setSortedData(it, { musicListPage.refresh() })
                },
                onSortType = { sortBy.sortType },
                onFilterEraTypeList = { mainViewModel.eraItemList },
                onFilterEraTypeClick = {
                    albumInfoViewModel.setFilterEraType(
                        it,
                        { musicListPage.refresh() })
                },
                onIfFavorite = { sortBy.isFavorite == true },
                setFavorite = {
                    albumInfoViewModel.setFavorite(
                        it,
                        { musicListPage.refresh() })
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
                            listOf(it), { musicListPage.refresh() }
                        )
                    }
                },
                onSelectRangeYear = { sortBy.yearList },
                onSetSelectRangeYear = { years ->
                    albumInfoViewModel.setFilterYear(
                        years.mapNotNull { it },
                        { musicListPage.refresh() })
                }
            )
        }
    )


}