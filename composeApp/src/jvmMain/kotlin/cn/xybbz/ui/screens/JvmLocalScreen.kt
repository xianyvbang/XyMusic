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

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.PlayerModeEnum
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.LocalViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.local_music
import xymusic_kmp.composeapp.generated.resources.return_home
import cn.xybbz.ui.xy.XyIconButton as IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmLocalScreen(localViewModel: LocalViewModel = koinViewModel<LocalViewModel>()) {

    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val downloadMusicList by localViewModel.musicDownloadInfo.collectAsStateWithLifecycle()
    val playbackState by localViewModel.musicController.playbackStateFlow.collectAsStateWithLifecycle()
    val favoriteSet by localViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())

    XyColumnScreen {
        TopAppBarComponent(
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.local_music)
                )
            }, navigationIcon = {
                IconButton(onClick = composeClick { navigator.goBack() }) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_back_24px),
                        contentDescription = stringResource(Res.string.return_home)
                    )
                }
            })

        ScreenLazyColumn {
            itemsIndexed(
                downloadMusicList,
                key = { index, item -> item.itemId + index },
                contentType = { _, _ -> MusicTypeEnum.MUSIC }
            ) { _, music ->
                MusicItemComponent(
                    music = music,
                    enabledPic = false,
                    onIfFavorite = {
                        music.itemId in favoriteSet
                    },
                    ifDownload = true,
                    ifPlay = playbackState.musicInfo?.itemId == music.itemId,
                    backgroundColor = Color.Transparent,
                    onMusicPlay = {
                        localViewModel.musicList(
                            it,
                            downloadList = downloadMusicList,
                            playerModeEnum = PlayerModeEnum.SEQUENTIAL_PLAYBACK
                        )
                    },
                    trailingOnClick = {
                        music.show()
                    },
                    trailingOnSelectClick = {
                        coroutineScope.launch {
                            music.show()
                        }
                    }
                )
            }
        }
    }
}



