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

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.DailyRecommendViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.daily_recommendations
import xymusic_kmp.composeapp.generated.resources.return_home
import cn.xybbz.ui.xy.XyIconButton as IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmDailyRecommendScreen(
    dailyRecommendViewModel: DailyRecommendViewModel = koinViewModel<DailyRecommendViewModel>()
) {

    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    val favoriteList by dailyRecommendViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val downloadMusicIds by dailyRecommendViewModel.downloadMusicIdsFlow.collectAsStateWithLifecycle(
        emptyList()
    )
    val state = rememberPullToRefreshState()

    var isRefreshing by remember {
        mutableStateOf(false)
    }

    XyColumnScreen {
        TopAppBarComponent(
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.daily_recommendations)
                )
            }, navigationIcon = {
                IconButton(onClick = composeClick { navigator.goBack() }) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_back_24px),
                        contentDescription = stringResource(Res.string.return_home)
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
                    key = { music -> music.itemId },
                    contentType = { _ -> MusicTypeEnum.MUSIC }
                ) { music ->
                    MusicItemComponent(
                        music = music,
                        onIfFavorite = {
                            music.itemId in favoriteList
                        },
                        ifDownload = music.itemId in downloadMusicIds,
                        ifPlay = dailyRecommendViewModel.musicController.musicInfo?.itemId == music.itemId,
                        backgroundColor = Color.Transparent,
                        onMusicPlay = {
                            coroutineScope.launch {
                                dailyRecommendViewModel.musicList(
                                    it
                                )
                            }
                        },
                        trailingOnClick = {
                            music.show()
                        },
                        trailingOnSelectClick = {
                            coroutineScope.launch {
                                dailyRecommendViewModel.getMusicInfo(music.itemId)
                                    ?.show()
                            }
                        }
                    )
                }
            }
        }

    }
}



