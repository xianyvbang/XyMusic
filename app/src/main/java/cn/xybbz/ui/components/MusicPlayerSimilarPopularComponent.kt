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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyItemMedium

@Composable
fun MusicPlayerSimilarPopularComponent(listState: LazyListState) {
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
        items(10) {
            MusicItemComponent(
                name = "1",
                itemId = "111",
                onIfFavorite = { false },
                ifDownload = false,
                ifPlay = false,
                onMusicPlay = {},
                trailingOnClick = {},
            )
        }
        item {
            XyItemMedium(
                modifier = Modifier.padding(vertical = XyTheme.dimens.outerVerticalPadding),
                text = "相似歌曲",
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(10) {
            MusicItemComponent(
                name = "1",
                itemId = "111",
                onIfFavorite = { false },
                ifDownload = false,
                ifPlay = false,
                onMusicPlay = {},
                trailingOnClick = {},
            )
        }
    }
}