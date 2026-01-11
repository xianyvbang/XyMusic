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

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.xybbz.R
import cn.xybbz.common.constants.UiConstants.MusicCardImageSize
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.entity.data.joinToString
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyItemBig
import cn.xybbz.ui.xy.XyItemMedium

@Composable
fun MusicPlayerSimilarPopularComponent(
    listState: LazyListState,
    onFavoriteSet: () -> Set<String>,
    onDownloadMusicIds: () -> List<String>,
    playMusicList: List<XyPlayMusic>,
    onAddPlayMusic: (XyMusicExtend) -> Unit
) {
    val mainViewModel = LocalMainViewModel.current

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
            XyItemMedium(
                modifier = Modifier.padding(
                    vertical = XyTheme.dimens.outerVerticalPadding,
                    horizontal = XyTheme.dimens.outerHorizontalPadding
                ),
                text = "热门歌曲",
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (mainViewModel.popularMusicList.isEmpty())
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MusicCardImageSize + 50.dp),
                contentAlignment = Alignment.Center
            ) {
                XyItemBig(
                    text = stringResource(R.string.no_data),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        items(mainViewModel.popularMusicList) { musicExt ->

            MusicItemComponent(
                itemId = musicExt.music.itemId,
                name = musicExt.music.name,
                album = musicExt.music.album,
                artists = musicExt.music.artists?.joinToString(),
                pic = musicExt.music.pic,
                codec = musicExt.music.codec,
                bitRate = musicExt.music.bitRate,
                onIfFavorite = {
                    musicExt.music.itemId in onFavoriteSet()
                },
                ifDownload = musicExt.music.itemId in onDownloadMusicIds(),
                ifPlay = false,
                onMusicPlay = {
                    if (musicExt.music.itemId !in playIdSet)
                        onAddPlayMusic(musicExt)
                },
                backgroundColor = Color.Transparent,
                ifShowTrailingContent = musicExt.music.itemId !in playIdSet,
                trailingIcon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                trailingOnClick = {
                    if (musicExt.music.itemId !in playIdSet)
                        onAddPlayMusic(musicExt)
                }
            )
        }
        item {
            XyItemMedium(
                modifier = Modifier.padding(
                    vertical = XyTheme.dimens.outerVerticalPadding,
                    horizontal = XyTheme.dimens.outerHorizontalPadding
                ),
                text = "相似歌曲",
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (mainViewModel.similarMusicList.isEmpty())
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MusicCardImageSize + 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    XyItemBig(
                        text = stringResource(R.string.no_data),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

        items(mainViewModel.similarMusicList) { musicExt ->

            MusicItemComponent(
                itemId = musicExt.music.itemId,
                name = musicExt.music.name,
                album = musicExt.music.album,
                artists = musicExt.music.artists?.joinToString(),
                pic = musicExt.music.pic,
                codec = musicExt.music.codec,
                bitRate = musicExt.music.bitRate,
                onIfFavorite = {
                    musicExt.music.itemId in onFavoriteSet()
                },
                ifDownload = musicExt.music.itemId in onDownloadMusicIds(),
                ifPlay = false,
                onMusicPlay = {
                    if (musicExt.music.itemId !in playIdSet)
                        onAddPlayMusic(musicExt)
                },
                backgroundColor = Color.Transparent,
                ifShowTrailingContent = musicExt.music.itemId !in playIdSet,
                trailingIcon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                trailingOnClick = {
                    if (musicExt.music.itemId !in playIdSet)
                        onAddPlayMusic(musicExt)
                }
            )
        }
    }
}