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

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.R
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.localdata.enums.PlayerTypeEnum
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.LocalViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalScreen(localViewModel: LocalViewModel = hiltViewModel<LocalViewModel>()) {

    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val downloadMusicList by localViewModel.musicDownloadInfo.collectAsStateWithLifecycle()
    val favoriteSet by localViewModel.favoriteRepository.favoriteSet.collectAsState()

    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = localViewModel.backgroundConfig.localMusicBrash[0],
            bottomVerticalColor = localViewModel.backgroundConfig.localMusicBrash[0]
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = stringResource(R.string.local_music)
                )
            }, navigationIcon = {
                IconButton(onClick = composeClick { navigator.goBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_home)
                    )
                }
            })

        ScreenLazyColumn {
            itemsIndexed(
                downloadMusicList,
                key = { _, item -> item.id },
                contentType = { _, _ -> MusicTypeEnum.MUSIC }
            ) { _, download ->
                download.music?.let { music ->
                    MusicItemComponent(
                        itemId = music.itemId,
                        name = music.name,
                        album = music.album,
                        artists = music.artists?.joinToString(),
                        pic = music.pic,
                        codec = music.codec,
                        bitRate = music.bitRate,
                        enabledPic = false,
                        onIfFavorite = {
                            music.itemId in favoriteSet
                        },
                        ifDownload = true,
                        ifPlay = localViewModel.musicController.musicInfo?.itemId == music.itemId,
                        backgroundColor = Color.Transparent,
                        onMusicPlay = {
                            localViewModel.musicList(
                                it,
                                downloadList = downloadMusicList,
                                playerTypeEnum = PlayerTypeEnum.SEQUENTIAL_PLAYBACK
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
}