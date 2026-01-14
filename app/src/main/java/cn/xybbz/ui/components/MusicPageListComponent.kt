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

package cn.xybbz.ui.components

import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.R
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.music.MusicController
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.download.DownloadRepository
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnScreen
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPageListComponent(
    brashList: List<Color>,
    title: String,
    musicListPage: LazyPagingItems<XyMusic>,
    favoriteRepository: FavoriteRepository,
    downloadRepository: DownloadRepository,
    musicController: MusicController,
    getMusicInfo: suspend (String) -> XyMusic?,
    onMusicPlay: (OnMusicPlayParameter, Int) -> Unit,
) {

    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    val favoriteList by favoriteRepository.favoriteSet.collectAsState()
    val downloadMusicIdList by downloadRepository.musicIdsFlow.collectAsState()


    XyColumnScreen(
        modifier = Modifier
            .brashColor(
                topVerticalColor = brashList[0],
                bottomVerticalColor = brashList[0]
            )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = title
                )
            }, navigationIcon = {
                IconButton(onClick = composeClick { navigator.goBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_home)
                    )
                }
            })
        SwipeRefreshListComponent(
            collectAsLazyPagingItems = musicListPage,
            listContent = { collectAsLazyPagingItems ->
                items(
                    collectAsLazyPagingItems.itemCount,
                    key = collectAsLazyPagingItems.itemKey { item -> item.itemId },
                    contentType = collectAsLazyPagingItems.itemContentType { MusicTypeEnum.MUSIC }
                ) { index ->
                    collectAsLazyPagingItems[index]?.let { music ->
                        MusicItemComponent(
                            itemId = music.itemId,
                            name = music.name,
                            album = music.album,
                            artists = music.artists?.joinToString(),
                            pic = music.pic,
                            codec = music.codec,
                            bitRate = music.bitRate,
                            onIfFavorite = {
                                favoriteList.contains(music.itemId)
                            },
                            ifDownload = music.itemId in downloadMusicIdList,
                            ifPlay = musicController.musicInfo?.itemId == music.itemId,
                            backgroundColor = Color.Transparent,
                            onMusicPlay = {
                                onMusicPlay(it, index)
                            },
                            trailingOnClick = {
                                coroutineScope.launch {
                                    getMusicInfo(music.itemId)?.show()
                                }
                            }
                        )
                    }
                }
            }
        )
    }
}