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
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.R
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.SwipeRefreshListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.FavoriteViewModel
import kotlinx.coroutines.launch

/**
 * 收藏界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    favoriteViewModel: FavoriteViewModel = hiltViewModel<FavoriteViewModel>(),
) {
    val favoriteMusicListPage =
        favoriteViewModel.favoriteMusicList.collectAsLazyPagingItems()
    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    val favoriteList by favoriteViewModel.favoriteSet.collectAsState(emptyList())
    val downloadMusicIdList by favoriteViewModel.downloadMusicIdsFlow.collectAsState(emptyList())


    XyColumnScreen(
        modifier = Modifier
            .brashColor(
                topVerticalColor = favoriteViewModel.backgroundConfig.favoriteBrash[0],
                bottomVerticalColor = favoriteViewModel.backgroundConfig.favoriteBrash[0]
            )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = stringResource(R.string.my_favorites)
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
            collectAsLazyPagingItems = favoriteMusicListPage,
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
                            ifPlay = favoriteViewModel.musicController.musicInfo?.itemId == music.itemId,
                            backgroundColor = Color.Transparent,
                            onMusicPlay = {

                                favoriteViewModel.musicPlayContext.favorite(
                                    it,
                                    index = index
                                )

                            },
                            trailingOnClick = {
                                coroutineScope.launch {
                                    favoriteViewModel.getMusicInfo(music.itemId)?.show()
                                }
                            }
                        )
                    }
                }
            }
        )
    }
}

