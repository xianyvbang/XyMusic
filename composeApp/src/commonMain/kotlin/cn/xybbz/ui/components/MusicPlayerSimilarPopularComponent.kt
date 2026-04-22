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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyNoData
import cn.xybbz.ui.xy.XyText
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.playlist_add_24px
import xymusic_kmp.composeapp.generated.resources.similar_music
import xymusic_kmp.composeapp.generated.resources.top_music

@Composable
fun MusicPlayerSimilarPopularComponent(
    listState: LazyListState,
    onFavoriteSet: () -> Set<String>,
    onDownloadMusicIds: () -> List<String>,
    playMusicList: List<XyPlayMusic>,
    onAddPlayMusic: (XyMusic) -> Unit
) {
    val mainViewModel = LocalMainViewModel.current
    val recommendationState by mainViewModel.recommendationStateFlow.collectAsStateWithLifecycle()

    val playIdSet by remember(playMusicList) {
        derivedStateOf {
            playMusicList.map { it.itemId }.toSet()
        }
    }


    LazyColumnNotComponent(
        state = listState,
        bottomItem = null,
    ) {
        item {
            XyText(
                modifier = Modifier.padding(
                    vertical = XyTheme.dimens.outerVerticalPadding,
                    horizontal = XyTheme.dimens.outerHorizontalPadding
                ),
                text = stringResource(Res.string.top_music)
            )
        }
        if (recommendationState.popularMusicList.isEmpty())
            item {
                XyNoData()
            }
        items(recommendationState.popularMusicList) { music ->

            MusicItemComponent(
                music = music,
                onIfFavorite = {
                    music.itemId in onFavoriteSet()
                },
                ifDownload = music.itemId in onDownloadMusicIds(),
                ifPlay = false,
                onMusicPlay = {
                    if (music.itemId !in playIdSet)
                        onAddPlayMusic(music)
                },
                backgroundColor = Color.Transparent,
                ifShowTrailingContent = music.itemId !in playIdSet,
                trailingIcon = Res.drawable.playlist_add_24px,
                trailingOnClick = {
                    if (music.itemId !in playIdSet)
                        onAddPlayMusic(music)
                }
            )
        }
        item {
            XyText(
                modifier = Modifier.padding(
                    vertical = XyTheme.dimens.outerVerticalPadding,
                    horizontal = XyTheme.dimens.outerHorizontalPadding
                ),
                text = stringResource(Res.string.similar_music)
            )
        }

        if (recommendationState.similarMusicList.isEmpty())
            item {
                XyNoData()
            }

        items(recommendationState.similarMusicList) { music ->

            MusicItemComponent(
                music = music,
                onIfFavorite = {
                    music.itemId in onFavoriteSet()
                },
                ifDownload = music.itemId in onDownloadMusicIds(),
                ifPlay = false,
                onMusicPlay = {
                    if (music.itemId !in playIdSet)
                        onAddPlayMusic(music)
                },
                backgroundColor = Color.Transparent,
                ifShowTrailingContent = music.itemId !in playIdSet,
                trailingIcon = Res.drawable.playlist_add_24px,
                trailingOnClick = {
                    if (music.itemId !in playIdSet)
                        onAddPlayMusic(music)
                }
            )
        }
    }
}
