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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.PlayerModeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.SongTableColumns
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.rememberMusicArtistClickHandler
import cn.xybbz.ui.components.show
import cn.xybbz.ui.components.songTableItems
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.LocalViewModel
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.local_music

private val JvmLocalMusicTableColumns = SongTableColumns(
    showFavoriteColumn = true,
    showAlbumColumn = true,
    showMetaColumn = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmLocalScreen(localViewModel: LocalViewModel = koinViewModel<LocalViewModel>()) {

    val navigator = LocalNavigator.current
    val artistClickHandler = rememberMusicArtistClickHandler()
    val downloadMusicList by localViewModel.musicDownloadInfo.collectAsStateWithLifecycle()
    val favoriteSet by localViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val currentPlayingMusicIdFlow = remember(localViewModel) {
        localViewModel.musicController.musicInfoFlow.map { musicInfo ->
            musicInfo?.itemId
        }
    }

    XyColumnScreen {
        TopAppBarComponent(
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.local_music)
                )
            })

        ScreenLazyColumn {
            songTableItems(
                tableKey = "local_music",
                songs = downloadMusicList,
                columns = JvmLocalMusicTableColumns,
                ifFavorite = { music -> music.itemId in favoriteSet },
                currentPlayingMusicIdFlow = currentPlayingMusicIdFlow,
                onSongClick = { _, music ->
                    localViewModel.musicList(
                        OnMusicPlayParameter(
                            musicId = music.itemId,
                            albumId = music.album,
                        ),
                        downloadList = downloadMusicList,
                        playerModeEnum = PlayerModeEnum.SEQUENTIAL_PLAYBACK,
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
                    localViewModel.musicController.invokingOnFavorite(music.itemId)
                },
                onDownloadClick = {},
                onMoreClick = { music ->
                    music.show()
                },
            )
        }
    }
}



