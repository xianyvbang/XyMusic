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


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.components.JvmLazyListComponent
import cn.xybbz.ui.components.SongTableColumns
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.rememberMusicArtistClickHandler
import cn.xybbz.ui.components.show
import cn.xybbz.ui.components.songTableItems
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.FavoriteViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.my_favorites

private val JvmFavoriteMusicTableColumns = SongTableColumns(
    showFavoriteColumn = true,
    showInlineActions = true,
    showAlbumColumn = true,
    showMetaColumn = false,
)

/**
 * 收藏界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmFavoriteScreen(
    favoriteViewModel: FavoriteViewModel = koinViewModel<FavoriteViewModel>(),
) {
    val favoriteMusicListPage =
        favoriteViewModel.favoriteMusicList.collectAsLazyPagingItems()
    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    val artistClickHandler = rememberMusicArtistClickHandler()
    val favoriteList by favoriteViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val currentPlayingMusicIdFlow = remember(favoriteViewModel) {
        favoriteViewModel.musicController.musicInfoFlow.map { musicInfo ->
            musicInfo?.itemId
        }
    }


    XyColumnScreen {
        TopAppBarComponent(
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.my_favorites)
                )
            })
        JvmLazyListComponent(
            pagingItems = favoriteMusicListPage,
        ) {
            songTableItems(
                tableKey = "favorite_music",
                pagingItems = favoriteMusicListPage,
                columns = JvmFavoriteMusicTableColumns,
                ifFavorite = { music -> favoriteList.contains(music.itemId) },
                currentPlayingMusicIdFlow = currentPlayingMusicIdFlow,
                onSongClick = { index, music ->
                    favoriteViewModel.musicPlayContext.favorite(
                        OnMusicPlayParameter(
                            musicId = music.itemId,
                            albumId = music.album,
                        ),
                        index = index,
                    )
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
                    favoriteViewModel.musicController.invokingOnFavorite(music.itemId)
                },
                onDownloadClick = { music ->
                    favoriteViewModel.downloadMusic(music)
                },
                onMoreClick = { music ->
                    coroutineScope.launch {
                        favoriteViewModel.getMusicInfo(music.itemId)?.show()
                    }
                },
            )
        }
    }
}




