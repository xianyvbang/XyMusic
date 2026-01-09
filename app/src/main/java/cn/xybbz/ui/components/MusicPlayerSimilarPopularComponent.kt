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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyItemMedium

@Composable
fun MusicPlayerSimilarPopularComponent(
    listState: LazyListState,
    itemId: String?,
    onFavoriteSet: () -> Set<String>,
    onDownloadMusicIds: () -> List<String>,
    onSimilarMusicList: () -> List<XyMusicExtend>,
    onPopularMusicList: () -> List<XyMusicExtend>
) {

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
        items(onPopularMusicList(), key = { it.music.itemId }) { musicExt ->
            MusicItemComponent(
                itemId = musicExt.music.itemId,
                name = musicExt.music.name,
                album = musicExt.music.album,
                artists = musicExt.music.artists,
                pic = musicExt.music.pic,
                codec = musicExt.music.codec,
                bitRate = musicExt.music.bitRate,
                onIfFavorite = {
                    musicExt.music.itemId in onFavoriteSet()
                },
                ifDownload = musicExt.music.itemId in onDownloadMusicIds(),
                ifPlay = itemId == musicExt.music.itemId,
                onMusicPlay = {

                },
                backgroundColor = Color.Transparent,
                trailingIcon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                trailingOnClick = {

                }
            )
        }
        item {
            XyItemMedium(
                modifier = Modifier.padding(vertical = XyTheme.dimens.outerVerticalPadding),
                text = "相似歌曲",
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(onSimilarMusicList(), key = { it.music.itemId }) { musicExt ->
            MusicItemComponent(
                itemId = musicExt.music.itemId,
                name = musicExt.music.name,
                album = musicExt.music.album,
                artists = musicExt.music.artists,
                pic = musicExt.music.pic,
                codec = musicExt.music.codec,
                bitRate = musicExt.music.bitRate,
                onIfFavorite = {
                    musicExt.music.itemId in onFavoriteSet()
                },
                ifDownload = musicExt.music.itemId in onDownloadMusicIds(),
                ifPlay = itemId == musicExt.music.itemId,
                onMusicPlay = {

                },
                backgroundColor = Color.Transparent,
                trailingIcon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                trailingOnClick = {

                }
            )
        }
    }
}