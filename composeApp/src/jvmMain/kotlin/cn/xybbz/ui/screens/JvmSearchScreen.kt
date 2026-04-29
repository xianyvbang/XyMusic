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


import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.utils.Log
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.router.ArtistInfo
import cn.xybbz.ui.components.LazyRowComponent
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.MusicArtistCardComponent
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.SongRow
import cn.xybbz.ui.components.SongTableColumns
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.viewmodel.SearchViewModel
import com.github.panpf.sketch.ability.bindPauseLoadWhenScrolling
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.album
import xymusic_kmp.composeapp.generated.resources.artist
import xymusic_kmp.composeapp.generated.resources.music
import xymusic_kmp.composeapp.generated.resources.search

private val SearchMusicTableColumns = SongTableColumns(
    showFavoriteColumn = true,
    showInlineActions = true,
    showAlbumColumn = true,
    showMetaColumn = false,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JvmSearchScreen(
    searchQuery: String = "",
    searchViewModel: SearchViewModel = koinViewModel<SearchViewModel>()
) {
    val playbackState by searchViewModel.musicController.playbackStateFlow.collectAsStateWithLifecycle()
    val favoriteList by searchViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val routeSearchQuery = searchQuery.trim()

    LaunchedEffect(routeSearchQuery) {
        if (routeSearchQuery.isNotBlank()) {
            searchViewModel.updateSearchInput(
                TextFieldValue(
                    text = routeSearchQuery,
                    selection = TextRange(routeSearchQuery.length)
                )
            )
            searchViewModel.onSearch(routeSearchQuery)
        }
    }


    SideEffect {
        Log.i("=====", "SearchScreen重载")
    }

    XyColumnScreen {
        // 顶部搜索栏
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            title = {
                TopAppBarTitle(
                    title = "${stringResource(Res.string.search)}：${searchQuery}"
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        JvmSearchResultScreen(
            musicList = searchViewModel.musicList,
            albumList = searchViewModel.albumList,
            artistList = searchViewModel.artistList,
            onAddMusic = {
                searchViewModel.addMusic(it)
            },
            onLoadingState = {
                searchViewModel.isSearchLoad
            },
            onFavoriteList = { favoriteList },
            currentPlayingMusicId = playbackState.musicInfo?.itemId,
            musicController = searchViewModel.musicController
        )
    }
}


/**
 * 搜索结果页面
 */
@Composable
fun JvmSearchResultScreen(
    musicList: List<XyMusic>,
    albumList: List<XyAlbum>,
    artistList: List<XyArtist>,
    onAddMusic: (XyMusic) -> Unit,
    onLoadingState: () -> Boolean,
    onFavoriteList: () -> List<String>,
    currentPlayingMusicId: String?,
    musicController: MusicCommonController
) {
    val navigator = LocalNavigator.current

    val lazyListState = rememberLazyListState()
    bindPauseLoadWhenScrolling(lazyListState)
    ScreenLazyColumn(
        modifier = Modifier
            .padding(top = XyTheme.dimens.outerVerticalPadding)
            .fillMaxSize(),
        state = lazyListState,
        ifLoading = onLoadingState()
    ) {

        if (artistList.isNotEmpty()) {
            item {
                XyRow {
                    XyText(
                        text = stringResource(Res.string.artist),
                    )
                }

            }
            item {
                LazyRowComponent {
                    items(artistList, key = { it.artistId }) { artist ->
                        MusicArtistCardComponent(
                            onItem = { artist },
                            onRouter = {
                                navigator.navigate(ArtistInfo(it, artist.name ?: ""))
                            }
                        )
                    }
                }
            }
        }

        if (albumList.isNotEmpty()) {
            item {
                XyRow {
                    XyText(text = stringResource(Res.string.album))
                }
            }
            item {
                LazyRowComponent {
                    items(albumList, key = { it.itemId }) { album ->
                        MusicAlbumCardComponent(
                            onItem = { album },
                            onRouter = {
                                navigator.navigate(
                                    AlbumInfo(
                                        it,
                                        MusicDataTypeEnum.ALBUM
                                    )
                                )
                            })

                    }
                }
            }
        }

        if (musicList.isNotEmpty()) {
            item {
                XyRow {
                    XyText(text = stringResource(Res.string.music))
                }
            }
            itemsIndexed(
                items = musicList,
                key = { _, music -> music.itemId }
            ) { index, music ->
                SongRow(
                    music = music,
                    index = index,
                    columns = SearchMusicTableColumns,
                    ifFavorite = music.itemId in onFavoriteList(),
                    ifPlay = music.itemId == currentPlayingMusicId,
                    onClick = {
                        onAddMusic(music)
                    },
                    onOpenAlbum = {
                        if (music.album.isNotBlank()) {
                            navigator.navigate(
                                AlbumInfo(
                                    music.album,
                                    MusicDataTypeEnum.ALBUM
                                )
                            )
                        }
                    },
                    onFavoriteClick = {
                        musicController.invokingOnFavorite(music.itemId)
                    },
                )
            }
        }
    }
}




