package cn.xybbz.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Download
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.UiConstants.MusicCardImageSize
import cn.xybbz.common.enums.img
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum
import cn.xybbz.router.RouterConstants
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.BottomSheetObject
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.MusicMusicCardComponent
import cn.xybbz.ui.components.MusicPlaylistItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.popup.XyDropdownMenu
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnComponent
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyItemBig
import cn.xybbz.ui.xy.XyItemMedium
import cn.xybbz.ui.xy.XyItemTabBigButton
import cn.xybbz.ui.xy.XyItemText
import cn.xybbz.ui.xy.XyItemTextHorizontal
import cn.xybbz.ui.xy.XyItemTextLarge
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel<HomeViewModel>()
) {
    SideEffect {
        Log.d("=====", "HomeScreen重组一次")
    }
    val context = LocalContext.current
    val navHostController = LocalNavController.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    var playlistName by remember {
        mutableStateOf("")
    }
    val state = rememberPullToRefreshState()

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
            homeViewModel.dataSourceManager.dataSourceType?.ifShowCount == true
        }
    }

    val coroutineScope = rememberCoroutineScope()

    XyColumnScreen(
        modifier = modifier
            .brashColor(
                topVerticalColor = homeViewModel.backgroundConfig.homeBrash[0],
                bottomVerticalColor = homeViewModel.backgroundConfig.homeBrash[1]
            )
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
                            contentDescription = stringResource(R.string.open_add_or_switch_data_sources)
                        )
                    }
                    XyDropdownMenu(
                        offset = DpOffset(x = 0.dp, y = XyTheme.dimens.outerVerticalPadding / 2),
                        onIfShowMenu = { ifShowConnectionMenu },
                        onSetIfShowMenu = { ifShowConnectionMenu = it },
                        modifier = Modifier
                            .width(200.dp),
                        itemDataList = listOf(
                            MenuItemDefaultData(
                                title = stringResource(R.string.add_connection), leadingIcon = {
                                    Icon(
                                        Icons.Rounded.ChevronRight,
                                        contentDescription = stringResource(R.string.add_connection)
                                    )
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
                                            contentDescription = stringResource(R.string.add_connection)
                                        )
                                    }

                                },
                                onClick = {
                                    ifShowConnectionMenu = false
                                    navHostController.navigate(RouterConstants.ConnectionManagement) {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }),
                            *homeViewModel.connectionList.map { connection ->
                                MenuItemDefaultData(
                                    title = connection.name, leadingIcon = {
                                        if (homeViewModel.connectionConfigServer.getConnectionId() == connection.id)
                                            Icon(
                                                Icons.Rounded.Check,
                                                contentDescription = connection.name + stringResource(
                                                    R.string.connection_link
                                                )
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
                                                painter = painterResource(connection.type.img),
                                                contentDescription = connection.name + stringResource(
                                                    R.string.icon
                                                ),
                                                modifier = Modifier.size(25.dp)
                                            )
                                        }

                                    },
                                    onClick = {
                                        coroutineScope.launch {
                                            ifShowConnectionMenu = false
                                            homeViewModel.changeDataSource(connection)
                                            homeViewModel.initGetData()
                                        }.invokeOnCompletion {

                                        }

                                    })
                            }.toTypedArray()
                        )
                    )
                }

            }, title = {
                XyItemBig(
                    text = stringResource(R.string.home),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }, actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (homeViewModel.dataSourceManager.loading)
                        LoadingIndicator()

                    if (homeViewModel.dataSourceManager.ifLoginError)
                        IconButton(onClick = {
                            BottomSheetObject(
                                sheetState = sheetState,
                                state = true,
                                titleText = context.getString(R.string.login_exception_info),
                                content = {
                                    Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
                                    HorizontalDivider()
                                    LazyColumnComponent(
                                        modifier = Modifier
                                            .height(400.dp)
                                    ) {
                                        item {
                                            Column {
                                                Text(
                                                    text = stringResource(homeViewModel.dataSourceManager.errorHint),
                                                    overflow = TextOverflow.Visible,
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                                HorizontalDivider()
                                                XyItemText(text = homeViewModel.dataSourceManager.errorMessage)
                                            }

                                        }
                                    }
                                }
                            ).show()
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = stringResource(R.string.login_failed),
                                tint = Color.Red
                            )
                        }


                    IconButton(onClick = {
                        homeViewModel.dataSourceManager.initDataSource()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.refresh_login)
                        )
                    }

                    IconButton(onClick = {
                        navHostController.navigate(RouterConstants.Setting) {
                            popUpTo(RouterConstants.Home) {
                                saveState = true
                            }
                            restoreState = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.open_settings_page_button)
                        )
                    }
                    IconButton(onClick = {
                        navHostController.navigate(RouterConstants.Search)
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = stringResource(R.string.search_page_switch_button)
                        )
                    }

                    IconButton(onClick = {
                        navHostController.navigate(RouterConstants.Download)
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = stringResource(R.string.download_failed)
                        )
                    }
                }
            })
        PullToRefreshBox(
            state = state,
            isRefreshing = homeViewModel.isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    homeViewModel.refreshDataAll(isRefresh = true)
                }.invokeOnCompletion {
                }
            },
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    state = state,
                    isRefreshing = homeViewModel.isRefreshing
                )
            }
        ) {
            LazyColumnNotComponent {
                item {
                    XyRow {
                        XyItemTabBigButton(
                            text = stringResource(R.string.all_music),
                            sub = if (ifShowCount) homeViewModel.musicCount ?: stringResource(
                                Constants.UNKNOWN
                            ) else null,
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
                            text = stringResource(R.string.local),
                            sub = if (ifShowCount) homeViewModel.musicCount ?: stringResource(
                                Constants.UNKNOWN
                            ) else null,
                            imageVector = Icons.Rounded.MusicNote,
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                navHostController.navigate(RouterConstants.Music)
                            },
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF6BFFF4), Color(0xFFFFBA6C)),
                                start = Offset(x = Float.POSITIVE_INFINITY, y = 0f), // 右上角
                                end = Offset(x = 0f, y = Float.POSITIVE_INFINITY)   // 左下角
                            )
                        )

                        XyItemTabBigButton(
                            text = stringResource(R.string.album),
                            sub = if (ifShowCount) homeViewModel.albumCount ?: stringResource(
                                Constants.UNKNOWN
                            ) else null,
                            imageVector = Icons.Rounded.Album,
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                navHostController.navigate(RouterConstants.Album) {
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
                            text = stringResource(R.string.artist),
                            sub = if (ifShowCount) homeViewModel.artistCount ?: stringResource(
                                Constants.UNKNOWN
                            ) else null,
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
                            text = stringResource(R.string.favorite),
                            sub = if (ifShowCount) homeViewModel.favoriteCount ?: stringResource(
                                Constants.UNKNOWN
                            ) else null,
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
                            text = stringResource(R.string.genres),
                            sub = if (ifShowCount) homeViewModel.genreCount ?: stringResource(
                                Constants.UNKNOWN
                            ) else null,
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
                                text = stringResource(R.string.no_data),
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
                                text = stringResource(R.string.most_played),
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
                                text = stringResource(R.string.most_played),
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
                                        contentDescription = stringResource(R.string.random_play) + stringResource(
                                            R.string.most_played
                                        )
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
                                        contentDescription = stringResource(R.string.list_loop) + stringResource(
                                            R.string.most_played
                                        )
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


                if (homeViewModel.recommendedMusicList.isNotEmpty()) {
                    item {
                        XyRow {
                            XyItemMedium(
                                modifier = Modifier.padding(vertical = XyTheme.dimens.outerVerticalPadding),
                                text = stringResource(R.string.recommended_music),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = composeClick {
                                    if (homeViewModel.recommendedMusicList.isNotEmpty())
                                        homeViewModel.playMusicList(
                                            musicList = homeViewModel.recommendedMusicList,
                                            onMusicPlayParameter = OnMusicPlayParameter(musicId = ""),
                                            playerTypeEnum = PlayerTypeEnum.RANDOM_PLAY
                                        )
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Shuffle,
                                        contentDescription = stringResource(R.string.random_play) + stringResource(
                                            R.string.recommended_music
                                        )
                                    )
                                }
                                IconButton(onClick = composeClick {
                                    if (homeViewModel.mostPlayerMusicList.isNotEmpty())
                                        homeViewModel.playMusicList(
                                            musicList = homeViewModel.recommendedMusicList,
                                            onMusicPlayParameter = OnMusicPlayParameter(musicId = ""),
                                            playerTypeEnum = PlayerTypeEnum.SEQUENTIAL_PLAYBACK
                                        )
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Repeat,
                                        contentDescription = stringResource(R.string.list_loop) + stringResource(
                                            R.string.recommended_music
                                        )
                                    )
                                }

                                TextButton(onClick = composeClick {
                                    navHostController.navigate(RouterConstants.DailyRecommend)
                                }, contentPadding = PaddingValues()) {
                                    XyItemTextLarge(text = "查看更多", color = Color(0xFFC6E5F5))
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
                                homeViewModel.recommendedMusicList.take(5),
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
                                            homeViewModel.recommendedMusicList
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
                                text = stringResource(R.string.latest_albums),
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
                                text = stringResource(R.string.recently_played_albums),
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
                                text = stringResource(R.string.recently_played_music),
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
                                        contentDescription = stringResource(R.string.random_play) + stringResource(
                                            R.string.recently_played_music
                                        )
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
                                        contentDescription = stringResource(R.string.list_loop) + stringResource(
                                            R.string.recently_played_music
                                        )
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
                            text = stringResource(R.string.playlists),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(contentAlignment = Alignment.CenterEnd) {
                            XyDropdownMenu(
                                offset = DpOffset(
                                    x = 0.dp,
                                    y = XyTheme.dimens.outerVerticalPadding / 2
                                ),
                                onIfShowMenu = { ifShowPlaylistMenu },
                                onSetIfShowMenu = { ifShowPlaylistMenu = it },
                                modifier = Modifier
                                    .width(200.dp),
                                itemDataList = listOf(
                                    MenuItemDefaultData(
                                        title = stringResource(R.string.create_playlist),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Add,
                                                contentDescription = stringResource(R.string.create_playlist)
                                            )
                                        },
                                        onClick = {
                                            ifShowPlaylistMenu = false
                                            //新增歌单
                                            playlistName =
                                                context.getString(R.string.new_playlist) + homeViewModel.playlists.size

                                            AlertDialogObject(
                                                title = R.string.create_playlist,
                                                content = {
                                                    XyEdit(text = playlistName, onChange = {
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
                                        }),
                                    MenuItemDefaultData(
                                        title = stringResource(R.string.refresh_playlist),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Refresh,
                                                contentDescription = stringResource(R.string.refresh_playlist)
                                            )
                                        },
                                        onClick = composeClick {
                                            ifShowPlaylistMenu = false
                                            coroutineScope.launch {
                                                homeViewModel.getServerPlaylists()
                                            }
                                        })
                                )
                            )
                            IconButton(onClick = composeClick {
                                ifShowPlaylistMenu = true
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreHoriz,
                                    contentDescription = stringResource(R.string.open_playlist_operations_popup)
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
                                    title = R.string.delete_playlist,
                                    content = {
                                        XyItemTextHorizontal(
                                            text = stringResource(
                                                R.string.confirm_delete_playlist,
                                                item.name
                                            )
                                        )
                                    },
                                    ifWarning = true,
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
                                    title = R.string.modify_playlist_name,
                                    content = {
                                        XyEdit(text = item.name, onChange = {
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
                        XyRow(
                            modifier = Modifier.height(40.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            XyItemText(text = stringResource(R.string.no_playlists))
                        }
                    }
                }
            }
        }
    }
}