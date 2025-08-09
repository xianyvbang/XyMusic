package cn.xybbz.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.common.constants.UiConstants.MusicCardImageSize
import cn.xybbz.common.enums.img
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.PlaylistFileUtils
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum
import cn.xybbz.router.RouterConstants
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.BottomSheetObject
import cn.xybbz.ui.components.LazyColumnComponent
import cn.xybbz.ui.components.LazyColumnNotComponent
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.MusicMusicCardComponent
import cn.xybbz.ui.components.MusicPlaylistItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnNotPaddingScreen
import cn.xybbz.ui.xy.XyItemBig
import cn.xybbz.ui.xy.XyItemBigTitle
import cn.xybbz.ui.xy.XyItemEdit
import cn.xybbz.ui.xy.XyItemMedium
import cn.xybbz.ui.xy.XyItemTabBigButton
import cn.xybbz.ui.xy.XyItemText
import cn.xybbz.ui.xy.XyItemTextHorizontal
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyRowCenter
import cn.xybbz.viewmodel.HomeViewModel
import com.kongzue.dialogx.dialogs.WaitDialog
import kotlinx.coroutines.launch
import java.io.BufferedReader

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel<HomeViewModel>()
) {

    val navHostController = LocalNavController.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    var playlistName by remember {
        mutableStateOf("")
    }
    val state = rememberPullToRefreshState()
    var isRefreshing by remember {
        mutableStateOf(false)
    }

    var ifShowConnectionMenu by remember {
        mutableStateOf(false)
    }

    var ifShowPlaylistMenu by remember {
        mutableStateOf(false)
    }

    /**
     * 是否显示无数据信息
     */
    val ifShowNotData by remember {
        derivedStateOf {
            homeViewModel.newAlbumList.isEmpty() && homeViewModel.musicRecentlyList.isEmpty()
                    && homeViewModel.mostPlayerMusicList.isEmpty()
        }
    }

    /**
     * 歌单列表是否存在
     */
    val ifShowPlaylist by remember {
        derivedStateOf {
            homeViewModel.playlists.isNotEmpty()
        }
    }

    /**
     * 是否显示数量
     */
    val ifShowCount by remember {
        derivedStateOf {
            homeViewModel._dataSourceManager.dataSourceType?.ifShowCount == true
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val openSelectPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { selectImageUri ->
            selectImageUri?.let {
                try {
                    context.contentResolver.openInputStream(it)?.use { fis ->
                        val configInfo =
                            fis.bufferedReader().use(BufferedReader::readText)
                        //开始对象转换
                        //读取
                        homeViewModel.createObject(
                            configInfo
                        )
                    }
                } catch (_: Exception) {

                }

            }
        }
    )

    XyColumnNotPaddingScreen(
        modifier = modifier
            .brashColor()
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            navigationIcon = {
                Box {
                    IconButton(onClick = {
                        ifShowConnectionMenu = true
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.splitscreen_add_24px),
                            contentDescription = "新增或切换数据源"
                        )
                    }
                    DropdownMenu(
                        offset = DpOffset(x = 0.dp, y = XyTheme.dimens.outerVerticalPadding / 2),
                        expanded = ifShowConnectionMenu,
                        onDismissRequest = { ifShowConnectionMenu = false },
                        shape = RoundedCornerShape(XyTheme.dimens.corner),
                        modifier = Modifier
                            .width(200.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF5A524C), Color(0xFF726B66)),
                                    tileMode = TileMode.Repeated
                                )
                            ),
                        containerColor = Color.Transparent
                    ) {

                        DropdownMenuItem(
                            text = { XyItemText(text = "添加连接") },
                            leadingIcon = {
                                Icon(Icons.Rounded.ChevronRight, contentDescription = "添加连接")
                            },
                            trailingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AddCircle,
                                        contentDescription = "添加连接"
                                    )
                                }

                            },
                            onClick = {
                                ifShowConnectionMenu = false
                                navHostController.navigate(RouterConstants.ConnectionManagement)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = XyTheme.dimens.outerHorizontalPadding))
                        homeViewModel.connectionList.forEachIndexed { index, value ->
                            DropdownMenuItem(
                                text = { XyItemText(text = value.name) },
                                leadingIcon = {
                                    if (value.ifEnable)
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = "${value.name}链接"
                                        )
                                },
                                trailingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(value.type.img),
                                            contentDescription = "${value.name}的图标",
                                            modifier = Modifier.size(25.dp)
                                        )
                                    }

                                },
                                onClick = {
                                    coroutineScope.launch {
                                        isRefreshing = true
                                        ifShowConnectionMenu = false
                                        homeViewModel.changeDataSource(value)
                                        homeViewModel.initGetData(onEnd = {
                                            isRefreshing = false
                                        })
                                    }.invokeOnCompletion {

                                    }


                                }
                            )
                            if (index != homeViewModel.connectionList.size - 1)
                                HorizontalDivider(modifier = Modifier.padding(horizontal = XyTheme.dimens.outerHorizontalPadding))
                        }
                    }
                }

            }, title = {
                XyItemBig(
                    text = "首页",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }, actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (homeViewModel._dataSourceManager.loading)
                        LoadingIndicator()

                    if (homeViewModel._dataSourceManager.ifLoginError)
                        IconButton(onClick = {
                            BottomSheetObject(
                                sheetState = sheetState,
                                state = true,
                                dragHandle = null,
                                content = {
                                    LazyColumnComponent(
                                        modifier = Modifier
                                            .height(400.dp)
                                            .brashColor()
                                    ) {
                                        item {
                                            BottomSheetDefaults.DragHandle()
                                        }
                                        item {
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                XyItemBig(text = "登陆异常信息")
                                            }
                                        }
                                        item {
                                            HorizontalDivider()
                                        }
                                        item {
                                            Text(
                                                text = homeViewModel._dataSourceManager.errorMessage,
                                                overflow = TextOverflow.Visible,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                        }
                                    }
                                }
                            ).show()
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = "登陆失败",
                                tint = Color.Red
                            )
                        }

                    IconButton(onClick = {
                        navHostController.navigate(RouterConstants.Setting)
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "打开设置页面按钮"
                        )
                    }
                    IconButton(onClick = {
                        navHostController.navigate(RouterConstants.Search)
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "搜索页面切换按钮"
                        )
                    }
                }
            })
        PullToRefreshBox(
            state = state,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    homeViewModel.refreshDataAll(onEnd = {
                        isRefreshing = false
                    }, true)
                }.invokeOnCompletion {
                }
            },
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    state = state,
                    isRefreshing = isRefreshing
                )
            }
        ) {
            LazyColumnNotComponent {
                item {
                    XyRow {
                        XyItemTabBigButton(
                            text = "所有音乐",
                            sub = if (ifShowCount) homeViewModel.musicCount.toString() else null,
                            imageVector = Icons.Rounded.MusicNote,
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                navHostController.navigate(RouterConstants.Music)
                            },
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xff3b82f6), Color(0xff8b5cf6)),
                                start = Offset(x = Float.POSITIVE_INFINITY, y = 0f), // 右上角
                                end = Offset(x = 0f, y = Float.POSITIVE_INFINITY)   // 左下角
                            )
                        )
                        XyItemTabBigButton(
                            text = "专辑",
                            sub = if (ifShowCount) homeViewModel.albumCount.toString() else null,
                            imageVector = Icons.Rounded.Album,
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                navHostController.navigate(RouterConstants.Album){
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xffec4899), Color(0xffa855f7)),
                                start = Offset(x = Float.POSITIVE_INFINITY, y = 0f), // 右上角
                                end = Offset(x = 0f, y = Float.POSITIVE_INFINITY)   // 左下角
                            )
                        )
                        XyItemTabBigButton(
                            text = "艺术家",
                            sub = if (ifShowCount) homeViewModel.artistCount.toString() else null,
                            imageVector = Icons.Rounded.Person,
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                navHostController.navigate(RouterConstants.Artist)
                            },
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xff10b981), Color(0xff06b6d4)),
                                start = Offset(x = Float.POSITIVE_INFINITY, y = 0f), // 右上角
                                end = Offset(x = 0f, y = Float.POSITIVE_INFINITY)   // 左下角
                            )
                        )
                        XyItemTabBigButton(
                            text = "收藏",
                            sub = if (ifShowCount) homeViewModel.favoriteCount.toString() else null,
                            imageVector = Icons.Rounded.Favorite,
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                navHostController.navigate(RouterConstants.FavoriteList)
                            },
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xfff97316), Color(0xfffb7185)),
                                start = Offset(x = Float.POSITIVE_INFINITY, y = 0f), // 右上角
                                end = Offset(x = 0f, y = Float.POSITIVE_INFINITY)   // 左下角
                            )
                        )

                        XyItemTabBigButton(
                            text = "流派",
                            sub = if (ifShowCount) homeViewModel.genreCount.toString() else null,
                            imageVector = Icons.AutoMirrored.Rounded.Label,
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                navHostController.navigate(RouterConstants.Genres)
                            },
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xffc026d3), Color(0xff7e22ce)),
                                start = Offset(x = Float.POSITIVE_INFINITY, y = 0f), // 右上角
                                end = Offset(x = 0f, y = Float.POSITIVE_INFINITY)   // 左下角
                            )
                        )
                    }
                }

                if (ifShowNotData) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(MusicCardImageSize + 50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            XyItemBig(
                                text = "暂无数据",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (homeViewModel.mostPlayerAlbumList.isNotEmpty()) {
                    item {
                        XyRow {
                            XyItemMedium(
                                modifier = Modifier.padding(vertical = XyTheme.dimens.outerVerticalPadding),
                                text = "最多播放",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    item {
                        LazyRow(
                            modifier = Modifier.height(MusicCardImageSize + 50.dp),
                            contentPadding = PaddingValues(horizontal = XyTheme.dimens.outerHorizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding / 2)
                        ) {
                            items(
                                homeViewModel.mostPlayerAlbumList,
                                key = { item -> item.itemId }) { album ->
                                MusicAlbumCardComponent(
                                    onItem = { album },
                                    imageSize = MusicCardImageSize,
                                    onRouter = {
                                        navHostController.navigate(
                                            RouterConstants.AlbumInfo(
                                                it,
                                                MusicDataTypeEnum.ALBUM
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                if (homeViewModel.mostPlayerMusicList.isNotEmpty()) {
                    item {
                        XyRow {
                            XyItemMedium(
                                modifier = Modifier.padding(vertical = XyTheme.dimens.outerVerticalPadding),
                                text = "最多播放",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = composeClick {
                                    if (homeViewModel.mostPlayerMusicList.isNotEmpty())
                                        homeViewModel.playMusicList(
                                            musicList = homeViewModel.mostPlayerMusicList,
                                            onMusicPlayParameter = OnMusicPlayParameter(musicId = ""),
                                            playerTypeEnum = PlayerTypeEnum.RANDOM_PLAY
                                        )
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Shuffle,
                                        contentDescription = "随机播放"
                                    )
                                }
                                IconButton(onClick = composeClick {
                                    if (homeViewModel.mostPlayerMusicList.isNotEmpty())
                                        homeViewModel.playMusicList(
                                            musicList = homeViewModel.mostPlayerMusicList,
                                            onMusicPlayParameter = OnMusicPlayParameter(musicId = ""),
                                            playerTypeEnum = PlayerTypeEnum.SEQUENTIAL_PLAYBACK
                                        )
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Repeat,
                                        contentDescription = "列表循环"
                                    )
                                }

                            }
                        }
                    }
                    item {
                        LazyRow(
                            modifier = Modifier.height(MusicCardImageSize + 50.dp),
                            contentPadding = PaddingValues(horizontal = XyTheme.dimens.outerHorizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding / 2)
                        ) {
                            itemsIndexed(
                                homeViewModel.mostPlayerMusicList,
                                key = { index, item -> item.itemId + index }) { index, music ->
                                MusicMusicCardComponent(
                                    onItem = { music },
                                    imageSize = MusicCardImageSize,
                                    onRouter = {
                                        //点击播放
                                        homeViewModel.musicPlayContext.musicList(
                                            onMusicPlayParameter = OnMusicPlayParameter(
                                                musicId = music.itemId,
                                                albumId = music
                                                    .album
                                            ),
                                            homeViewModel.mostPlayerMusicList
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                if (homeViewModel.newAlbumList.isNotEmpty()) {
                    item {
                        XyRow {
                            XyItemMedium(
                                modifier = Modifier.padding(vertical = XyTheme.dimens.outerVerticalPadding),
                                text = "最新专辑",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    item {
                        LazyRow(
                            modifier = Modifier.height(MusicCardImageSize + 50.dp),
                            contentPadding = PaddingValues(horizontal = XyTheme.dimens.outerHorizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding / 2)
                        ) {
                            items(
                                homeViewModel.newAlbumList,
                                key = { item -> item.itemId }) { album ->
                                MusicAlbumCardComponent(
                                    onItem = { album },
                                    imageSize = MusicCardImageSize,
                                    onRouter = {
                                        navHostController.navigate(
                                            RouterConstants.AlbumInfo(
                                                it,
                                                MusicDataTypeEnum.ALBUM
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                if (homeViewModel.albumRecentlyList.isNotEmpty()) {
                    item {
                        XyRow {
                            XyItemMedium(
                                modifier = Modifier.padding(vertical = XyTheme.dimens.outerVerticalPadding),
                                text = "最近播放专辑",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    item {
                        LazyRow(
                            modifier = Modifier.height(MusicCardImageSize + 50.dp),
                            contentPadding = PaddingValues(horizontal = XyTheme.dimens.outerHorizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding / 2)
                        ) {
                            items(
                                homeViewModel.albumRecentlyList,
                                key = { item -> item.itemId }) { album ->
                                MusicAlbumCardComponent(
                                    onItem = { album },
                                    imageSize = MusicCardImageSize,
                                    onRouter = {
                                        navHostController.navigate(
                                            RouterConstants.AlbumInfo(
                                                it,
                                                MusicDataTypeEnum.ALBUM
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                if (homeViewModel.musicRecentlyList.isNotEmpty()) {
                    item {
                        XyRow {
                            XyItemMedium(
                                modifier = Modifier.padding(vertical = XyTheme.dimens.outerVerticalPadding),
                                text = "最近播放音乐",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = composeClick {
                                    if (homeViewModel.musicRecentlyList.isNotEmpty())
                                        homeViewModel.playMusicList(
                                            musicList = homeViewModel.musicRecentlyList,
                                            onMusicPlayParameter = OnMusicPlayParameter(musicId = ""),
                                            playerTypeEnum = PlayerTypeEnum.RANDOM_PLAY
                                        )
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Shuffle,
                                        contentDescription = "随机播放"
                                    )
                                }
                                IconButton(onClick = composeClick {
                                    if (homeViewModel.musicRecentlyList.isNotEmpty())
                                        homeViewModel.playMusicList(
                                            musicList = homeViewModel.musicRecentlyList,
                                            onMusicPlayParameter = OnMusicPlayParameter(musicId = ""),
                                            playerTypeEnum = PlayerTypeEnum.SEQUENTIAL_PLAYBACK
                                        )
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Repeat,
                                        contentDescription = "列表循环"
                                    )
                                }

                            }

                        }
                    }

                    item {
                        LazyRow(
                            modifier = Modifier.height(MusicCardImageSize + 50.dp),
                            contentPadding = PaddingValues(horizontal = XyTheme.dimens.outerHorizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding / 2)
                        ) {
                            itemsIndexed(
                                homeViewModel.musicRecentlyList,
                                key = { index, item -> item.itemId + index }) { index, music ->
                                MusicMusicCardComponent(
                                    onItem = { music },
                                    imageSize = MusicCardImageSize,
                                    onRouter = {
                                        //点击播放
                                        homeViewModel.musicPlayContext.musicList(
                                            onMusicPlayParameter = OnMusicPlayParameter(
                                                musicId = music.itemId,
                                                albumId = music
                                                    .album
                                            ),
                                            homeViewModel.musicRecentlyList
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    XyRow {
                        XyItemMedium(
                            modifier = Modifier.padding(vertical = XyTheme.dimens.outerVerticalPadding),
                            text = "歌单",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(contentAlignment = Alignment.CenterEnd) {
                            DropdownMenu(
                                offset = DpOffset(
                                    x = 0.dp,
                                    y = XyTheme.dimens.outerVerticalPadding / 2
                                ),
                                expanded = ifShowPlaylistMenu,
                                onDismissRequest = { ifShowPlaylistMenu = false },
                                shape = RoundedCornerShape(XyTheme.dimens.corner),
                                modifier = Modifier
                                    .width(200.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF4A9494), Color(0xFF726B66)),
                                            tileMode = TileMode.Repeated
                                        )
                                    ),
                                containerColor = Color.Transparent
                            ) {

                                DropdownMenuItem(
                                    text = { XyItemText(text = "创建歌单") },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "创建歌单"
                                        )
                                    },
                                    onClick = {
                                        ifShowPlaylistMenu = false
                                        //新增歌单
                                        playlistName =
                                            "新建歌单${homeViewModel.playlists.size}"

                                        AlertDialogObject(
                                            title = {
                                                XyItemBigTitle(text = "设置歌单")
                                            },
                                            content = {
                                                XyItemEdit(text = playlistName, onChange = {
                                                    playlistName = it
                                                })
                                            },
                                            onDismissRequest = {
                                            },
                                            onConfirmation = {
                                                //新增歌单
                                                coroutineScope.launch {
                                                    homeViewModel.savePlaylist(playlistName)
                                                }
                                            }
                                        ).show()
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = XyTheme.dimens.outerHorizontalPadding))
                                DropdownMenuItem(
                                    text = { XyItemText(text = "导入歌单") },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Login,
                                            contentDescription = "导入歌单"
                                        )
                                    },
                                    onClick = {
                                        ifShowPlaylistMenu = false
                                        //查询指定目录
                                        openSelectPhotoLauncher.launch("application/json")
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = XyTheme.dimens.outerHorizontalPadding))
                                DropdownMenuItem(
                                    text = { XyItemText(text = "导出歌单") },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                                            contentDescription = "导出歌单"
                                        )
                                    },
                                    onClick = {
                                        ifShowPlaylistMenu = false
                                        //生成文件
                                        MessageUtils.sendWaitDialog(
                                            "歌单导出中...",
                                            onBackPressedListener = {
                                                PlaylistFileUtils.closeOutputStream(
                                                    homeViewModel.outputStreamKey
                                                )
                                                WaitDialog.dismiss()
                                                true
                                            })
                                        coroutineScope.launch {
                                            homeViewModel.createJsonStr(context)
                                        }.invokeOnCompletion {
                                            MessageUtils.sendTipDialog(
                                                "导出已完成",
                                                WaitDialog.TYPE.SUCCESS,
                                                1000
                                            )
                                        }
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = XyTheme.dimens.outerHorizontalPadding))
                                DropdownMenuItem(
                                    text = { XyItemText(text = "刷新数据") },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Refresh,
                                            contentDescription = "导出歌单"
                                        )
                                    },
                                    onClick = {
                                        ifShowPlaylistMenu = false
                                        coroutineScope.launch {
                                            homeViewModel.getServerPlaylists()
                                        }
                                    }
                                )
                            }
                            IconButton(onClick = composeClick {
                                ifShowPlaylistMenu = true
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreHoriz,
                                    contentDescription = "打开歌单操作弹窗"
                                )
                            }

                        }
                    }
                }
                itemsIndexed(homeViewModel.playlists) { _, item ->
                    //歌单信息
                    XyRow {
                        MusicPlaylistItemComponent(
                            name = item.name,
                            imgUrl = item.pic,
                            onClick = {
                                navHostController.navigate(
                                    RouterConstants.AlbumInfo(
                                        item.itemId,
                                        MusicDataTypeEnum.PLAYLIST
                                    )
                                )
                            },
                            removePlaylistClick = {
                                AlertDialogObject(
                                    title = {
                                        XyItemBigTitle(text = "删除歌单", color = Color.Red)
                                    },
                                    content = {
                                        XyItemTextHorizontal(
                                            text = "确定要删除 ${item.name} 歌单吗?"
                                        )
                                    },
                                    onConfirmation = {
                                        homeViewModel.removePlaylist(
                                            item.itemId
                                        )
                                    },
                                    onDismissRequest = {

                                    }
                                ).show()
                            },
                            editPlaylistClick = {
                                playlistName = item.name
                                AlertDialogObject(
                                    title = {
                                        XyItemBigTitle(text = "修改歌单名称")
                                    },
                                    content = {
                                        XyItemEdit(text = item.name, onChange = {
                                            playlistName = it
                                        })
                                    },
                                    onConfirmation = {
                                        homeViewModel.editPlaylistName(
                                            item.itemId,
                                            playlistName
                                        )
                                    },
                                    onDismissRequest = {
                                        playlistName = ""
                                    }
                                ).show()
                            }
                        )
                    }
                }
                if (!ifShowPlaylist) {
                    item {
                        XyRowCenter(modifier = Modifier.height(40.dp)) {
                            XyItemText(text = "暂无歌单")
                        }
                    }
                }
            }
        }
    }
}