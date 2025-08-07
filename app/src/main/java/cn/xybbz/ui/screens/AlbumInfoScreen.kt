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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.R
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.PlaylistFileUtils
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.config.IDataSourceManager
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.extension.isSticking
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.progress.Progress
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.MusicItemIndexComponent
import cn.xybbz.ui.components.SelectSortBottomSheetComponent
import cn.xybbz.ui.components.SwipeRefreshListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnNotPadding
import cn.xybbz.ui.xy.XyColumnNotPaddingScreen
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyItemBigTitle
import cn.xybbz.ui.xy.XyItemEdit
import cn.xybbz.ui.xy.XyItemSwitcherNotPadding
import cn.xybbz.ui.xy.XyItemTextHorizontal
import cn.xybbz.ui.xy.XyItemTextIconRow
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.viewmodel.AlbumInfoViewModel
import com.kongzue.dialogx.dialogs.WaitDialog
import kotlinx.coroutines.launch
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * 专辑详情,通过分类或者其他跳转
 * [dataType] 数据类型 0专辑,1歌单
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlbumInfoScreen(
    modifier: Modifier = Modifier,
    itemId: String,
    dataType: MusicDataTypeEnum,
    albumInfoViewModel: AlbumInfoViewModel
) {

    SideEffect {
        Log.d("=====", "MusicAudiobookInfoScreen重组一次")
    }

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val mainViewModel = LocalMainViewModel.current
    val navHostController = LocalNavController.current
    val context = LocalContext.current
    val isSticking by remember(lazyListState) { lazyListState.isSticking(1) }

    val favoriteList by albumInfoViewModel.favoriteRepository.favoriteMap.collectAsState()

    //是否打开选择
    var ifOpenSelect by remember {
        mutableStateOf(false)
    }
    //是否全选
    var ifAllSelect by remember {
        mutableStateOf(false)
    }

    var isSortBottomSheet by remember {
        mutableStateOf(false)
    }
    val musicListPage =
        albumInfoViewModel.xyMusicList.collectAsLazyPagingItems()

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

    SelectSortBottomSheetComponent(
        ifDisplay = { isSortBottomSheet },
        onSetDisplay = { isSortBottomSheet = it },
        onSortTypeClick = {
            albumInfoViewModel.setSortedData(it)
        },
        onRefresh = { /*collectAsLazyPagingItems.refresh()*/ },
        onSortType = { albumInfoViewModel.sortBy.value.sortType },
        onFilterEraTypeList = { mainViewModel.eraItemList },
        onFilterEraType = { albumInfoViewModel.sortBy.value.eraItem?.id ?: 0 },
        onFilterEraTypeClick = { albumInfoViewModel.setFilterEraType(it) },
        onIfFavorite = { albumInfoViewModel.sortBy.value.isFavorite == true },
        setFavorite = { albumInfoViewModel.setFavorite(it) },
        sortTypeList = listOf(
            SortTypeEnum.CREATE_TIME_ASC,
            SortTypeEnum.CREATE_TIME_DESC,
            SortTypeEnum.MUSIC_NAME_ASC,
            SortTypeEnum.MUSIC_NAME_DESC
        )
    )

    XyColumnNotPaddingScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = Color(0xFF113460),
            bottomVerticalColor = Color(0xFF0B424B)
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
                            text = if (dataType == MusicDataTypeEnum.PLAYLIST) "歌单" else "专辑",
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
                        contentDescription = "返回专辑列表"
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
                                contentDescription = "打开操作页面"
                            )
                        }
                        DropdownMenu(
                            offset = DpOffset(
                                x = 0.dp,
                                y = XyTheme.dimens.outerVerticalPadding
                            ),
                            expanded = ifShowMenu,
                            onDismissRequest = { ifShowMenu = false },
                            shape = RoundedCornerShape(XyTheme.dimens.corner),
                            modifier = Modifier.width(200.dp)
                        ) {

                            DropdownMenuItem(
                                text = { XyText(text = "导出歌单") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                                        contentDescription = "导出歌单"
                                    )
                                },
                                onClick = {
                                    ifShowMenu = false
                                    //生成文件
                                    MessageUtils.sendWaitDialog(
                                        "歌单导出中...",
                                        onBackPressedListener = {
                                            PlaylistFileUtils.closeOutputStream(
                                                albumInfoViewModel.outputStreamKey
                                            )
                                            WaitDialog.dismiss()
                                            true
                                        })
                                    coroutineScope.launch {
                                        albumInfoViewModel.createJsonStr(context)
                                    }.invokeOnCompletion {
                                        MessageUtils.sendTipDialog(
                                            "导出已完成",
                                            WaitDialog.TYPE.SUCCESS,
                                            1000
                                        )
                                    }
                                }
                            )
                            HorizontalDivider(modifier = Modifier.fillMaxWidth())
                            DropdownMenuItem(
                                text = { XyText(text = "重命名歌单") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "重命名歌单"
                                    )
                                },
                                onClick = {
                                    ifShowMenu = false
                                    AlertDialogObject(
                                        title = {
                                            XyItemBigTitle(text = "修改歌单名称")
                                        },
                                        content = {
                                            XyItemEdit(
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
                                }
                            )
                            HorizontalDivider(modifier = Modifier.fillMaxWidth())
                            DropdownMenuItem(
                                text = { XyText(text = "删除歌单", textColor = Color.Red) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "删除歌单",
                                        tint = Color.Red
                                    )
                                },
                                onClick = {
                                    ifShowMenu = false
                                    AlertDialogObject(
                                        title = {
                                            XyItemBigTitle(text = "删除歌单", color = Color.Red)
                                        },
                                        content = {
                                            XyItemTextHorizontal(
                                                text = "确定要删除 ${albumInfoViewModel.xyAlbumInfoData?.name} 歌单吗?"
                                            )
                                        },
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
                                }
                            )
                        }
                    }
            }
        )

        if (isSticking)
            StickyHeaderOperation(
                dataType = dataType,
                onIfOpenSelect = { ifOpenSelect },
                onSetIfOpenSelect = { ifOpenSelect = it },
                onIfAllSelect = { ifAllSelect },
                onSetIfAllSelect = { ifAllSelect = it },
                onSetIsSortBottomSheet = { isSortBottomSheet = it },
                onMusicListPage = { musicListPage },
                albumInfoViewModel = albumInfoViewModel
            )

        SwipeRefreshListComponent(
            collectAsLazyPagingItems = musicListPage,
            lazyState = lazyListState,
            modifier = Modifier
                .nestedScroll(nestedScrollConnection)
        ) { list ->
            item {
                MusicAlbumInfoComponent(
                    onData = {
                        albumInfoViewModel.xyAlbumInfoData
                    },
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

                if (!isSticking)
                    StickyHeaderOperation(
                        dataType = dataType,
                        onIfOpenSelect = { ifOpenSelect },
                        onSetIfOpenSelect = { ifOpenSelect = it },
                        onIfAllSelect = { ifAllSelect },
                        onSetIfAllSelect = { ifAllSelect = it },
                        onSetIsSortBottomSheet = { isSortBottomSheet = it },
                        onMusicListPage = { musicListPage },
                        albumInfoViewModel = albumInfoViewModel
                    )
            }

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
                        ifSelect = ifOpenSelect,
                        ifSelectCheckBox = { albumInfoViewModel.selectControl.selectMusicDataList.any { it.itemId == music.itemId } },
                        trailingOnClick = { select ->
                            if (select) {
                                albumInfoViewModel.selectControl.removeSelectMusicId(music.itemId)
                            } else {
                                albumInfoViewModel.selectControl.setSelectMusicListData(music)
                            }
                        }
                    )
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
    onIfOpenSelect: () -> Boolean,
    onSortClick: () -> Unit,
    onSelectClick: () -> Unit,
    onIfAllSelect: () -> Boolean,
    onSetIfAllSelect: (Boolean) -> Unit,
    onSetIfOpenSelect: (Boolean) -> Unit,
    albumInfoViewModel: AlbumInfoViewModel
) {
    val coroutineScope = rememberCoroutineScope()

    XyItemTextIconRow(
        onClick = {
            if (onIfOpenSelect()) {
                onSetIfAllSelect(!onIfAllSelect())
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

        },
        text = if (onIfOpenSelect()) if (!onIfAllSelect()) "全选" else "取消全选" else if (onMusicAlbum()?.itemId == onPlayAlbumId()) {
            if (onPlayState() == PlayStateEnum.Playing)
                "暂停播放"
            else
                "继续播放"
        } else if (onAlbumPlayerHistoryProgress() != null)
            "继续播放:${onAlbumPlayerHistoryProgress()?.musicName}"
        else
            "开始播放",
        icon = if (onIfOpenSelect()) null else if (onMusicAlbum()?.itemId == onPlayAlbumId())
            if (onPlayState() == PlayStateEnum.Playing)
                Icons.Rounded.PauseCircle
            else
                Icons.Rounded.PlayCircle
        else
            Icons.Rounded.PlayCircle
    ) {
        if (onIfOpenSelect())
            IconButton(onClick = {
                onSetIfAllSelect(false)
                albumInfoViewModel.selectControl.dismiss()
                onSetIfOpenSelect(false)
            }) {
                Icon(imageVector = Icons.Rounded.Close, contentDescription = "关闭选择")
            }
        else
            if (onMusicAlbum()?.itemId != onPlayAlbumId() && onAlbumPlayerHistoryProgress() != null) {
                IconButton(onClick = {
                    Log.i("=====", "删除播放历史")
                    onAlbumPlayerHistoryProgress()?.let {
                        onRemovePlayerHistory(it.musicId)
                    }
                }) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "删除播放历史")
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        onSortClick()
                    }) {
                        //倒叙或者正序
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = "排序"
                        )
                    }

                    IconButton(onClick = {
                        onSelectClick()
                        albumInfoViewModel.show(onOpenChange = {
                            onSetIfOpenSelect(it)
                        })
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.PlaylistAddCheck,
                            contentDescription = "选择"
                        )
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
    onIfSavePlaybackHistory: () -> Boolean,
    onSetIfSavePlaybackHistory: (Boolean) -> Unit,
    dataSourceManager: IDataSourceManager,
    musicController: MusicController
) {
    val coroutineScope = rememberCoroutineScope()

    //收藏信息
    var ifFavorite by remember {
        mutableStateOf(onData()?.ifFavorite == true)
    }

    XyColumnNotPadding(
        shape = RoundedCornerShape(0.dp),
        backgroundColor = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = XyTheme.dimens.outerHorizontalPadding,
                    vertical = XyTheme.dimens.outerVerticalPadding
                )
                .height(124.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start
        ) {
            XyImage(
                model = onData()?.pic,
                contentDescription = "专辑封面",
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
        //修改为切换开启播放历史
        XyRow {
            XyItemSwitcherNotPadding(
                state = onIfSavePlaybackHistory(),
                onChange = { onSetIfSavePlaybackHistory(it) },
                text = "开启存储播放历史"
            )

            IconButton(onClick = composeClick {
                coroutineScope.launch {
                    val ifFavoriteData = dataSourceManager.setFavoriteData(
                        type = MusicTypeEnum.ALBUM,
                        itemId = onData()?.itemId ?: "",
                        musicController = musicController,
                        ifFavorite = ifFavorite
                    )
                    ifFavorite = ifFavoriteData
                }
            }) {
                Icon(
                    imageVector = if (ifFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (ifFavorite) "已收藏-可取消收藏" else "未收藏-可以收藏",
                    tint = if (ifFavorite) Color.Red else LocalContentColor.current
                )
            }
        }
    }

}


@Composable
private fun StickyHeaderOperation(
    modifier: Modifier = Modifier,
    dataType: MusicDataTypeEnum,
    onIfOpenSelect: () -> Boolean,
    onSetIfOpenSelect: (Boolean) -> Unit,
    onIfAllSelect: () -> Boolean,
    onSetIfAllSelect: (Boolean) -> Unit,
    onSetIsSortBottomSheet: (Boolean) -> Unit,
    onMusicListPage: () -> LazyPagingItems<XyMusic>,
    albumInfoViewModel: AlbumInfoViewModel
) {

    val selectListSize by remember {
        derivedStateOf {
            albumInfoViewModel.selectControl.selectMusicDataList.size
        }
    }

    LaunchedEffect(selectListSize) {
        snapshotFlow { selectListSize }.collect {
            onSetIfAllSelect(onMusicListPage().itemCount == selectListSize && onMusicListPage().itemCount > 0)
        }
    }

    XyColumnNotPadding(
        modifier = modifier,
        shape = RoundedCornerShape(0.dp),
        backgroundColor = Color.Transparent
    ) {
        XyColumnNotPadding(
            backgroundColor = Color.Transparent,
            shape = RoundedCornerShape(
                topStart = XyTheme.dimens.corner,
                topEnd = XyTheme.dimens.corner
            )
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
                    if (albumInfoViewModel.musicController.state == PlayStateEnum.Playing) {
                        albumInfoViewModel.musicController.pause()
                    } else {
                        albumInfoViewModel.musicController.resume()
                    }
                },
                onIfOpenSelect = { onIfOpenSelect() },
                onSortClick = { onSetIsSortBottomSheet(true) },
                onSelectClick = {
                    onSetIfAllSelect(false)
                    onSetIfOpenSelect(!onIfOpenSelect())
                },
                onIfAllSelect = { onIfAllSelect() },
                onSetIfAllSelect = {
                    onSetIfAllSelect(it)
                    if (onIfAllSelect()) {
                        val tmpList = mutableListOf<XyMusic>()
                        for (index in 0 until onMusicListPage().itemCount) {
                            onMusicListPage()[index]?.let { music ->
                                tmpList.add(
                                    music
                                )
                            }
                        }
                        albumInfoViewModel.selectControl.setSelectMusicListAllData(
                            tmpList
                        )
                    } else
                        albumInfoViewModel.selectControl.clearSelectMusicList()
                },
                onSetIfOpenSelect = { onSetIfOpenSelect(it) },
                albumInfoViewModel = albumInfoViewModel
            )

            /*SelectRowComponent(
                onIfOpenSelect = { onIfOpenSelect() },
                onSetIfOpenSelect = { onSetIfOpenSelect(it) },
                onSelectMusicDataList = { albumInfoViewModel.selectControl.selectMusicDataList },
                onRemoveSelectListResource = { albumInfoViewModel.selectControl.removeSelectListResource() },
                onPlaySelect = {
                    albumInfoViewModel.addPlayerList()
                },
                removePlaylistMusic = {
                    if (dataType == MusicDataTypeEnum.PLAYLIST)
                        XyItemTabButton(
                            enabled = albumInfoViewModel.selectMusicData.selectMusicDataList.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                //从歌单中清除选中的歌单音乐
                                albumInfoViewModel.removePlaylistMusic()
                            },
                            text = "从歌单中移除",
                            imageVector = Icons.Rounded.Delete,
                        )
                })*/
        }
    }
}