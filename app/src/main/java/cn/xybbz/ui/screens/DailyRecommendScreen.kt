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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.DailyRecommendViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRecommendScreen(
    dailyRecommendViewModel: DailyRecommendViewModel = hiltViewModel<DailyRecommendViewModel>()
) {

    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    val favoriteList by dailyRecommendViewModel.favoriteSet.collectAsState(emptyList())
    val downloadMusicIds by dailyRecommendViewModel.downloadMusicIdsFlow.collectAsState(emptyList())
    val state = rememberPullToRefreshState()

    var isRefreshing by remember {
        mutableStateOf(false)
    }

    XyColumnScreen(
        modifier = Modifier
            .brashColor(
                topVerticalColor = dailyRecommendViewModel.backgroundConfig.dailyRecommendBrash[0],
                bottomVerticalColor = dailyRecommendViewModel.backgroundConfig.dailyRecommendBrash[0]
            )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = stringResource(R.string.daily_recommendations)
                )
            }, navigationIcon = {
                IconButton(onClick = composeClick { navigator.goBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_home)
                    )
                }
            })


        PullToRefreshBox(
            state = state,
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    dailyRecommendViewModel.generateRecommendedMusicList()
                }.invokeOnCompletion {
                    isRefreshing = false
                }
            }
        ) {
            ScreenLazyColumn {
                items(
                    dailyRecommendViewModel.recommendedMusicList,
                    key = { extend -> extend.music.itemId },
                    contentType = { _-> MusicTypeEnum.MUSIC }
                ) { musicExtend ->
                    MusicItemComponent(
                        itemId = musicExtend.music.itemId,
                        name = musicExtend.music.name,
                        album = musicExtend.music.album,
                        artists = musicExtend.music.artists?.joinToString(),
                        pic = musicExtend.music.pic,
                        codec = musicExtend.music.codec,
                        bitRate = musicExtend.music.bitRate,
                        onIfFavorite = {
                            musicExtend.music.itemId in favoriteList
                        },
                        ifDownload = musicExtend.music.itemId in downloadMusicIds,
                        ifPlay = dailyRecommendViewModel.musicController.musicInfo?.itemId == musicExtend.music.itemId,
                        backgroundColor = Color.Transparent,
                        onMusicPlay = {
                            coroutineScope.launch {
                                dailyRecommendViewModel.musicList(
                                    it
                                )
                            }
                        },
                        trailingOnClick = {
                            musicExtend.music.show()
                        },
                        trailingOnSelectClick = {
                            coroutineScope.launch {
                                dailyRecommendViewModel.getMusicInfo(musicExtend.music.itemId)
                                    ?.show()
                            }
                        }
                    )
                }
            }
        }

    }
}

