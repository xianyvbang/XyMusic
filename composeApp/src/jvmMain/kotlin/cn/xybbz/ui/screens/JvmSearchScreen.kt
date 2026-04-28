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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.utils.Log
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.search.SearchHistory
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.router.ArtistInfo
import cn.xybbz.ui.components.LazyRowComponent
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.MusicArtistCardComponent
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.SearchRecordComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.viewmodel.SearchViewModel
import com.github.panpf.sketch.ability.bindPauseLoadWhenScrolling
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.album
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.artist
import xymusic_kmp.composeapp.generated.resources.cancel_24px
import xymusic_kmp.composeapp.generated.resources.clear
import xymusic_kmp.composeapp.generated.resources.music
import xymusic_kmp.composeapp.generated.resources.return_home
import xymusic_kmp.composeapp.generated.resources.search_24px
import xymusic_kmp.composeapp.generated.resources.search_box_icon
import xymusic_kmp.composeapp.generated.resources.search_history
import xymusic_kmp.composeapp.generated.resources.search_music_album_artist
import cn.xybbz.ui.xy.XyIconButton as IconButton


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JvmSearchScreen(
    searchViewModel: SearchViewModel = koinViewModel<SearchViewModel>()
) {
    val navigator = LocalNavigator.current

    val playbackState by searchViewModel.musicController.playbackStateFlow.collectAsStateWithLifecycle()
    val favoriteList by searchViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val downloadMusicIds by searchViewModel.downloadMusicIdsFlow.collectAsStateWithLifecycle(
        emptyList()
    )


    SideEffect {
        Log.i("=====", "SearchScreen重载")
    }

    XyColumnScreen {
        // 顶部搜索栏
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            title = {
            },
            navigationIcon = {
                IconButton(onClick = {
                    navigator.goBack()
                }) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_back_24px),
                        contentDescription = stringResource(Res.string.return_home)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedContent(targetState = searchViewModel.ifShowSearchResult) { bool ->
            if (bool) {
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
                    onDownloadMusicIdList = { downloadMusicIds },
                    currentPlayingMusicId = playbackState.musicInfo?.itemId,
                    musicController = searchViewModel.musicController
                )
            } else {
                JvmHistoryAndHintList(
                    onClick = { search ->
                        searchViewModel.updateSearchInput(
                            TextFieldValue(
                                text = search,
                                selection = TextRange(search.length)
                            )
                        )
                        searchViewModel.onSearch(search)
                    },
                    onClear = {
                        //清空搜索历史
                        searchViewModel.clearSearchHistory()
                    },
                    onHistoryList = { searchViewModel.searchHistory })
            }
        }
    }
}

/**
 * 列列表更改
 * @param [onClick] 点击方法
 * @param [onClear] 清除方法
 * @param [onHistoryList] 搜索历史列表
 */
@Composable
private fun JvmHistoryAndHintList(
    onClick: (String) -> Unit,
    onClear: () -> Unit,
    onHistoryList: () -> List<SearchHistory>,
) {

    XyColumn(
        paddingValues = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding
        ),
        backgroundColor = Color.Transparent,
        horizontalAlignment = Alignment.Start,
        clipSize = 0.dp
    ) {
        SearchRecordComponent(
            onHistoryList(),
            title = stringResource(Res.string.search_history),
            onClick = onClick,
            onClear = onClear
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
    onDownloadMusicIdList: () -> List<String>,
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
            items(musicList, key = { music -> music.itemId }) { music ->
                MusicItemComponent(
                    music = music,
                    onIfFavorite = {
                        music.itemId in onFavoriteList()
                    },
                    ifDownload = music.itemId in onDownloadMusicIdList(),
                    ifPlay = music.itemId == currentPlayingMusicId,
                    onMusicPlay = {
                        onAddMusic(
                            music
                        )
                    },
                    trailingOnClick = {
                        music.show()
                    },
                    trailingOnSelectClick = {
                        music.show()
                    }
                )
            }
        }
    }
}




