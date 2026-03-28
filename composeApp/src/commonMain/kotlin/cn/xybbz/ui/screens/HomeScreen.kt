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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.img
import cn.xybbz.common.utils.Log
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum
import cn.xybbz.router.Album
import cn.xybbz.router.AlbumInfo
import cn.xybbz.router.Artist
import cn.xybbz.router.ConnectionManagement
import cn.xybbz.router.DailyRecommend
import cn.xybbz.router.Download
import cn.xybbz.router.FavoriteList
import cn.xybbz.router.Genres
import cn.xybbz.router.Local
import cn.xybbz.router.Music
import cn.xybbz.router.Search
import cn.xybbz.router.Setting
import cn.xybbz.ui.common.UiConstants.MusicCardImageSize
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.BottomSheetObject
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.MusicMusicCardComponent
import cn.xybbz.ui.components.MusicPlaylistItemComponent
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.popup.XyDropdownMenu
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnBottomSheetComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyItemLabel
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyScreenTitle
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.add_24px
import xymusic_kmp.composeapp.generated.resources.add_circle_24px
import xymusic_kmp.composeapp.generated.resources.add_connection
import xymusic_kmp.composeapp.generated.resources.album
import xymusic_kmp.composeapp.generated.resources.album_24px
import xymusic_kmp.composeapp.generated.resources.all_music
import xymusic_kmp.composeapp.generated.resources.artist
import xymusic_kmp.composeapp.generated.resources.check_24px
import xymusic_kmp.composeapp.generated.resources.chevron_right_24px
import xymusic_kmp.composeapp.generated.resources.confirm_delete_playlist
import xymusic_kmp.composeapp.generated.resources.connection_link
import xymusic_kmp.composeapp.generated.resources.create_playlist
import xymusic_kmp.composeapp.generated.resources.daily_recommendations
import xymusic_kmp.composeapp.generated.resources.delete_playlist
import xymusic_kmp.composeapp.generated.resources.download_24px
import xymusic_kmp.composeapp.generated.resources.download_list
import xymusic_kmp.composeapp.generated.resources.favorite
import xymusic_kmp.composeapp.generated.resources.favorite_24px
import xymusic_kmp.composeapp.generated.resources.genres
import xymusic_kmp.composeapp.generated.resources.home
import xymusic_kmp.composeapp.generated.resources.icon
import xymusic_kmp.composeapp.generated.resources.label_24px
import xymusic_kmp.composeapp.generated.resources.latest_albums
import xymusic_kmp.composeapp.generated.resources.list_loop
import xymusic_kmp.composeapp.generated.resources.local
import xymusic_kmp.composeapp.generated.resources.login_exception_info
import xymusic_kmp.composeapp.generated.resources.login_failed
import xymusic_kmp.composeapp.generated.resources.modify_playlist_name
import xymusic_kmp.composeapp.generated.resources.more_horiz_24px
import xymusic_kmp.composeapp.generated.resources.most_played
import xymusic_kmp.composeapp.generated.resources.music_note_24px
import xymusic_kmp.composeapp.generated.resources.new_playlist
import xymusic_kmp.composeapp.generated.resources.no_playlists
import xymusic_kmp.composeapp.generated.resources.open_add_or_switch_data_sources
import xymusic_kmp.composeapp.generated.resources.open_playlist_operations_popup
import xymusic_kmp.composeapp.generated.resources.open_settings_page_button
import xymusic_kmp.composeapp.generated.resources.person_24px
import xymusic_kmp.composeapp.generated.resources.playlist
import xymusic_kmp.composeapp.generated.resources.random_play
import xymusic_kmp.composeapp.generated.resources.recently_played_albums
import xymusic_kmp.composeapp.generated.resources.recently_played_music
import xymusic_kmp.composeapp.generated.resources.refresh_24px
import xymusic_kmp.composeapp.generated.resources.refresh_login
import xymusic_kmp.composeapp.generated.resources.refresh_playlist
import xymusic_kmp.composeapp.generated.resources.repeat_24px
import xymusic_kmp.composeapp.generated.resources.search_24px
import xymusic_kmp.composeapp.generated.resources.search_page_switch_button
import xymusic_kmp.composeapp.generated.resources.settings_24px
import xymusic_kmp.composeapp.generated.resources.shuffle_24px
import xymusic_kmp.composeapp.generated.resources.splitscreen_add_24px
import xymusic_kmp.composeapp.generated.resources.view_more
import xymusic_kmp.composeapp.generated.resources.warning_24px

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = koinViewModel<HomeViewModel>()
) {
    SideEffect {
        Log.d("=====", "HomeScreen重组一次")
    }
    val mostPlayerMusicList by homeViewModel.homeDataRepository.mostPlayedMusic.collectAsStateWithLifecycle()
    val newAlbumList by homeViewModel.homeDataRepository.newestAlbums.collectAsStateWithLifecycle()
    val musicRecentlyList by homeViewModel.homeDataRepository.recentMusic.collectAsStateWithLifecycle()
    val albumRecentlyList by homeViewModel.homeDataRepository.recentAlbums.collectAsStateWithLifecycle()
    val mostPlayerAlbumList by homeViewModel.homeDataRepository.mostPlayedAlbums.collectAsStateWithLifecycle()
    val recommendedMusicList by homeViewModel.homeDataRepository.recommendedMusic.collectAsStateWithLifecycle()
    val playlists by homeViewModel.homeDataRepository.playlists.collectAsStateWithLifecycle()
    val dataCount by homeViewModel.homeDataRepository.dataCount.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val deletePlaylist = stringResource(Res.string.delete_playlist)
    val loginExceptionInfo = stringResource(Res.string.login_exception_info)
    val newPlaylist = stringResource(Res.string.new_playlist)
    val createPlaylist = stringResource(Res.string.create_playlist)
    val modifyPlaylistName = stringResource(Res.string.modify_playlist_name)

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
     * 歌单刷新状态
     */
    var ifRefreshingPlaylist by remember {
        mutableStateOf(false)
    }

    /**
     * 歌单列表是否存在
     */
    val ifShowPlaylist by remember {
        derivedStateOf {
            playlists.isNotEmpty()
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

    XyColumnScreen {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            navigationIcon = {
                Box {
                    IconButton(onClick = {
                        ifShowConnectionMenu = true
                    }) {
                        Icon(
                            painter = painterResource(Res.drawable.splitscreen_add_24px),
                            contentDescription = stringResource(Res.string.open_add_or_switch_data_sources)
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
                                title = stringResource(Res.string.add_connection), leadingIcon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.chevron_right_24px),
                                        contentDescription = stringResource(Res.string.add_connection)
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
                                            painter = painterResource(Res.drawable.add_circle_24px),
                                            contentDescription = stringResource(Res.string.add_connection)
                                        )
                                    }

                                },
                                onClick = {
                                    ifShowConnectionMenu = false
                                    navigator.navigate(ConnectionManagement)
                                }),
                            *homeViewModel.connectionList.map { connection ->
                                MenuItemDefaultData(
                                    title = connection.name,
                                    leadingIcon = {
                                        if (homeViewModel.dataSourceManager.getConnectionId() == connection.id)
                                            Icon(
                                                painterResource(Res.drawable.check_24px),
                                                contentDescription = connection.name + stringResource(
                                                    Res.string.connection_link
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
                                                    Res.string.icon
                                                ),
                                                modifier = Modifier.size(25.dp)
                                            )
                                        }

                                    },
                                    onClick = {
                                        coroutineScope.launch {
                                            ifShowConnectionMenu = false
                                            homeViewModel.changeDataSource(connection)
                                        }.invokeOnCompletion {
                                        }

                                    })
                            }.toTypedArray()
                        )
                    )
                }

            }, title = {
                XyScreenTitle(
                    text = stringResource(Res.string.home)
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
                                titleText = loginExceptionInfo,
                                dragHandle = null,
                                content = {
                                    LazyColumnBottomSheetComponent(
                                        modifier = Modifier
                                            .height(400.dp),
                                        horizontal = XyTheme.dimens.outerHorizontalPadding
                                    ) {
                                        item {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                XyText(
                                                    text = stringResource(homeViewModel.dataSourceManager.errorHint),
                                                    overflow = TextOverflow.Visible,
                                                    maxLines = Int.MAX_VALUE
                                                )
                                            }

                                        }
                                        if (homeViewModel.dataSourceManager.errorMessage.isNotBlank())
                                            item {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    XyTextSubSmall(text = homeViewModel.dataSourceManager.errorMessage)
                                                }
                                            }
                                    }
                                }
                            ).show()
                        }) {
                            Icon(
                                painter = painterResource(Res.drawable.warning_24px),
                                contentDescription = stringResource(Res.string.login_failed),
                                tint = Color.Red
                            )
                        }

                    IconButton(onClick = {
                        homeViewModel.autoLogin()
                    }) {
                        Icon(
                            painter = painterResource(Res.drawable.refresh_24px),
                            contentDescription = stringResource(Res.string.refresh_login)
                        )
                    }

                    IconButton(onClick = {
                        navigator.navigate(Setting)
                    }) {
                        Icon(
                            painter = painterResource(Res.drawable.settings_24px),
                            contentDescription = stringResource(Res.string.open_settings_page_button)
                        )
                    }
                    IconButton(onClick = {
                        navigator.navigate(Search)
                    }) {
                        Icon(
                            painter = painterResource(Res.drawable.search_24px),
                            contentDescription = stringResource(Res.string.search_page_switch_button)
                        )
                    }

                    IconButton(onClick = {
                        navigator.navigate(Download)
                    }) {
                        BadgedBox(badge = {
                            if (homeViewModel.downloadCount > 0) {
                                Badge(containerColor = Color.Red)
                            }
                        }) {
                            Icon(
                                painter = painterResource(Res.drawable.download_24px),
                                contentDescription = stringResource(Res.string.download_list)
                            )
                        }
                    }
                }
            })
        PullToRefreshBox(
            state = state,
            isRefreshing = homeViewModel.isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    homeViewModel.onManualRefresh()
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
            ScreenLazyColumn {
                item(key = "top") {
                    XyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        XyItemLabel(
                            modifier = Modifier
                                .weight(1f),
                            text = stringResource(Res.string.all_music),
                            sub = if (ifShowCount) dataCount?.musicCount?.toString()
                                ?: stringResource(
                                    Constants.UNKNOWN
                                ) else null,
                            painter = painterResource(Res.drawable.music_note_24px),
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                navigator.navigate(Music)
                            }
                        )

                        XyItemLabel(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.local),
                            sub = if (ifShowCount) homeViewModel.localCount ?: stringResource(
                                Constants.UNKNOWN
                            ) else null,
                            painter = painterResource(Res.drawable.music_note_24px),
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                navigator.navigate(Local)
                            }
                        )

                        XyItemLabel(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.album),
                            sub = if (ifShowCount) dataCount?.albumCount?.toString()
                                ?: stringResource(
                                    Constants.UNKNOWN
                                ) else null,
                            painter = painterResource(Res.drawable.album_24px),
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                navigator.navigate(Album)
                            }
                        )
                        XyItemLabel(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.artist),
                            sub = if (ifShowCount) dataCount?.artistCount?.toString()
                                ?: stringResource(
                                    Constants.UNKNOWN
                                ) else null,
                            painter = painterResource(Res.drawable.person_24px),
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                navigator.navigate(Artist)
                            }
                        )
                        XyItemLabel(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.favorite),
                            sub = if (ifShowCount) dataCount?.favoriteCount?.toString()
                                ?: stringResource(
                                    Constants.UNKNOWN
                                ) else null,
                            painter = painterResource(Res.drawable.favorite_24px),
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                navigator.navigate(FavoriteList)
                            }
                        )

                        XyItemLabel(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.genres),
                            sub = if (ifShowCount) dataCount?.genreCount?.toString()
                                ?: stringResource(
                                    Constants.UNKNOWN
                                ) else null,
                            painter = painterResource(Res.drawable.label_24px),
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                navigator.navigate(Genres)
                            }
                        )
                    }
                }

                if (mostPlayerAlbumList.isNotEmpty()) {
                    item(key = "most_played_album_title") {
                        HomeSectionTitle(title = stringResource(Res.string.most_played))
                    }

                    item(key = "most_played_album") {
                        HomeAlbumItemLazyRow(mostPlayerAlbumList)
                    }
                }

                if (mostPlayerMusicList.isNotEmpty()) {
                    item(key = "most_played_music_title") {
                        HomeSectionTitle(title = stringResource(Res.string.most_played)) {
                            IconButton(onClick = composeClick {
                                if (mostPlayerMusicList.isNotEmpty())
                                    homeViewModel.musicList(
                                        musicList = mostPlayerMusicList,
                                        onMusicPlayParameter = OnMusicPlayParameter(musicId = ""),
                                        playerTypeEnum = PlayerTypeEnum.RANDOM_PLAY
                                    )
                            }) {
                                Icon(
                                    painter = painterResource(Res.drawable.shuffle_24px),
                                    contentDescription = stringResource(Res.string.random_play) + stringResource(
                                        Res.string.most_played
                                    )
                                )
                            }
                            IconButton(onClick = composeClick {
                                if (mostPlayerMusicList.isNotEmpty())
                                    homeViewModel.musicList(
                                        musicList = mostPlayerMusicList,
                                        onMusicPlayParameter = OnMusicPlayParameter(musicId = ""),
                                        playerTypeEnum = PlayerTypeEnum.SEQUENTIAL_PLAYBACK
                                    )
                            }) {
                                Icon(
                                    painter = painterResource(Res.drawable.repeat_24px),
                                    contentDescription = stringResource(Res.string.list_loop) + stringResource(
                                        Res.string.most_played
                                    )
                                )
                            }
                        }
                    }
                    item(key = "most_played_music") {
                        HomeMusicItemLazyRow(
                            musicList = mostPlayerMusicList,
                            homeViewModel = homeViewModel
                        )
                    }
                }


                if (recommendedMusicList.isNotEmpty()) {
                    item(key = "daily_recommendations_title") {
                        HomeSectionTitle(title = stringResource(Res.string.daily_recommendations)) {
                            IconButton(onClick = composeClick {
                                if (recommendedMusicList.isNotEmpty())
                                    homeViewModel.musicList(
                                        musicList = recommendedMusicList,
                                        onMusicPlayParameter = OnMusicPlayParameter(musicId = ""),
                                        playerTypeEnum = PlayerTypeEnum.RANDOM_PLAY
                                    )
                            }) {
                                Icon(
                                    painter = painterResource(Res.drawable.shuffle_24px),
                                    contentDescription = stringResource(Res.string.random_play) + stringResource(
                                        Res.string.daily_recommendations
                                    )
                                )
                            }
                            IconButton(onClick = composeClick {
                                if (recommendedMusicList.isNotEmpty())
                                    homeViewModel.musicList(
                                        musicList = recommendedMusicList,
                                        onMusicPlayParameter = OnMusicPlayParameter(musicId = ""),
                                        playerTypeEnum = PlayerTypeEnum.SEQUENTIAL_PLAYBACK
                                    )
                            }) {
                                Icon(
                                    painter = painterResource(Res.drawable.repeat_24px),
                                    contentDescription = stringResource(Res.string.list_loop) + stringResource(
                                        Res.string.daily_recommendations
                                    )
                                )
                            }

                            TextButton(onClick = composeClick {
                                navigator.navigate(DailyRecommend)
                            }, contentPadding = PaddingValues()) {
                                XyText(
                                    text = stringResource(Res.string.view_more)
                                )
                            }
                        }
                    }
                    item(key = "daily_recommendations") {

                        HomeMusicItemLazyRow(
                            musicList = recommendedMusicList,
                            homeViewModel = homeViewModel
                        )
                    }
                }

                if (newAlbumList.isNotEmpty()) {
                    item(key = "new_album_title") {
                        HomeSectionTitle(title = stringResource(Res.string.latest_albums))
                    }
                    item(key = "new_album") {
                        HomeAlbumItemLazyRow(newAlbumList)
                    }
                }

                if (albumRecentlyList.isNotEmpty()) {
                    item(key = "album_recently_title") {
                        HomeSectionTitle(title = stringResource(Res.string.recently_played_albums))
                    }

                    item(key = "album_recently") {
                        HomeAlbumItemLazyRow(albumRecentlyList)
                    }
                }

                if (musicRecentlyList.isNotEmpty()) {
                    item(key = "music_recently_title") {
                        HomeSectionTitle(title = stringResource(Res.string.recently_played_music)) {
                            IconButton(onClick = composeClick {
                                if (musicRecentlyList.isNotEmpty())
                                    homeViewModel.musicList(
                                        musicList = musicRecentlyList,
                                        onMusicPlayParameter = OnMusicPlayParameter(musicId = ""),
                                        playerTypeEnum = PlayerTypeEnum.RANDOM_PLAY
                                    )
                            }) {
                                Icon(
                                    painter = painterResource(Res.drawable.shuffle_24px),
                                    contentDescription = stringResource(Res.string.random_play) + stringResource(
                                        Res.string.recently_played_music
                                    )
                                )
                            }
                            IconButton(onClick = composeClick {
                                if (musicRecentlyList.isNotEmpty())
                                    homeViewModel.musicList(
                                        musicList = musicRecentlyList,
                                        onMusicPlayParameter = OnMusicPlayParameter(musicId = ""),
                                        playerTypeEnum = PlayerTypeEnum.SEQUENTIAL_PLAYBACK
                                    )
                            }) {
                                Icon(
                                    painter = painterResource(Res.drawable.repeat_24px),
                                    contentDescription = stringResource(Res.string.list_loop) + stringResource(
                                        Res.string.recently_played_music
                                    )
                                )
                            }
                        }
                    }

                    item(key = "music_recently") {
                        HomeMusicItemLazyRow(
                            musicList = musicRecentlyList,
                            homeViewModel = homeViewModel
                        )
                    }
                }

                item(key = "playlist_title") {
                    HomeSectionTitle(title = stringResource(Res.string.playlist)) {
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
                                        title = createPlaylist,
                                        trailingIcon = {
                                            Icon(
                                                painter = painterResource(Res.drawable.add_24px),
                                                contentDescription = createPlaylist
                                            )
                                        },
                                        onClick = {
                                            ifShowPlaylistMenu = false
                                            //新增歌单
                                            playlistName =
                                                newPlaylist + playlists.size

                                            AlertDialogObject(
                                                title = createPlaylist,
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
                                        title = stringResource(Res.string.refresh_playlist),
                                        trailingIcon = {
                                            Icon(
                                                painter = painterResource(Res.drawable.refresh_24px),
                                                contentDescription = stringResource(Res.string.refresh_playlist)
                                            )
                                        },
                                        onClick = composeClick {
                                            ifShowPlaylistMenu = false
                                            coroutineScope.launch {
                                                ifRefreshingPlaylist = true
                                                homeViewModel.getServerPlaylists()
                                            }.invokeOnCompletion {
                                                ifRefreshingPlaylist = false
                                            }
                                        })
                                )
                            )
                            IconButton(onClick = composeClick {
                                ifShowPlaylistMenu = true
                            }) {
                                Icon(
                                    painter = painterResource(Res.drawable.more_horiz_24px),
                                    contentDescription = stringResource(Res.string.open_playlist_operations_popup)
                                )
                            }

                        }
                    }
                }

                if (ifRefreshingPlaylist) {
                    item(key = "refresh_playlist") {
                        XyRow(horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        }

                    }
                } else
                    items(playlists, key = { item -> item.itemId }) { item ->
                        //歌单信息
                        XyRow {
                            MusicPlaylistItemComponent(
                                name = item.name,
                                imgUrl = item.pic,
                                onClick = {
                                    navigator.navigate(
                                        AlbumInfo(
                                            item.itemId,
                                            MusicDataTypeEnum.PLAYLIST
                                        )
                                    )
                                },
                                removePlaylistClick = {
                                    AlertDialogObject(
                                        title = deletePlaylist,
                                        content = {
                                            XyTextSubSmall(
                                                text = stringResource(
                                                    Res.string.confirm_delete_playlist,
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
                                        title = modifyPlaylistName,
                                        content = {
                                            XyEdit(text = playlistName, onChange = {
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
                    item(key = "no_playlist") {
                        XyRow(
                            modifier = Modifier.height(40.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            XyTextSubSmall(text = stringResource(Res.string.no_playlists))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeMusicItemLazyRow(
    musicList: List<XyMusicExtend>,
    homeViewModel: HomeViewModel
) {
    LazyRow(
        modifier = Modifier.height(MusicCardImageSize + 50.dp),
        contentPadding = PaddingValues(horizontal = XyTheme.dimens.outerHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding / 2)
    ) {
        items(
            musicList,
            key = { item -> item.music.itemId }) { musicExtend ->
            MusicMusicCardComponent(
                onItem = { musicExtend.music },
                imageSize = MusicCardImageSize,
                onRouter = {
                    //点击播放
                    homeViewModel.musicList(
                        onMusicPlayParameter = OnMusicPlayParameter(
                            musicId = musicExtend.music.itemId,
                            albumId = musicExtend.music
                                .album
                        ),
                        musicList
                    )
                }
            )
        }
    }
}

@Composable
private fun HomeSectionTitle(
    title: String,
    actions: @Composable RowScope.() -> Unit = {}
) {
    XyRow {
        XyText(
            modifier = Modifier.padding(vertical = XyTheme.dimens.outerVerticalPadding),
            text = title
        )

        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}

@Composable
private fun HomeAlbumItemLazyRow(
    albumList: List<XyAlbum>
) {
    val navigator = LocalNavigator.current
    LazyRow(
        modifier = Modifier.height(MusicCardImageSize + 50.dp),
        contentPadding = PaddingValues(horizontal = XyTheme.dimens.outerHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding / 2)
    ) {
        items(
            albumList,
            key = { item -> item.itemId }) { album ->
            MusicAlbumCardComponent(
                onItem = { album },
                imageSize = MusicCardImageSize,
                onRouter = {
                    navigator.navigate(
                        AlbumInfo(
                            it,
                            MusicDataTypeEnum.ALBUM
                        )
                    )
                }
            )
        }
    }
}

