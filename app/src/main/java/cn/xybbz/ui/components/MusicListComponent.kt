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


import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.entity.data.joinToString
import cn.xybbz.entity.data.music.ifNextPageNumList
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnParentComponent
import cn.xybbz.ui.xy.ModalBottomSheetExtendComponent
import cn.xybbz.ui.xy.XyItemText
import cn.xybbz.ui.xy.XyRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicListComponent(
    musicListState: Boolean,
    curOriginIndex: Int,
    originMusicList: List<XyPlayMusic>,
    onSetState: (Boolean) -> Unit,
    onClearPlayerList: () -> Unit,
    onSeekToIndex: (Int) -> Unit,
    onRemovePlayerMusicItem: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val mainViewModel = LocalMainViewModel.current
    ModalBottomSheetExtendComponent(
        onIfDisplay = { musicListState },
        onClose = {
            mainViewModel.putIterations(1)
            onSetState(false)
        },
        bottomSheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(this.maxHeight.times(0.8f))
            ) {

                XyRow(
                    paddingValues = PaddingValues(
                        horizontal = XyTheme.dimens.outerHorizontalPadding,
                        vertical = XyTheme.dimens.outerVerticalPadding / 2
                    )
                ) {
                    Text(
                        text = stringResource(R.string.current_playlist),
                        fontWeight = FontWeight.W900,
                        fontSize = 19.sp
                    )
                    TextButton(onClick = {
                        //删除数据库内容
                        onClearPlayerList()
                    }) {
                        XyItemText(
                            text = stringResource(R.string.clear),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                }
                HorizontalDivider(
                    modifier = Modifier.padding(2.dp),
                    thickness = 1.dp,
                    color = Color(0x565C5E6F)
                )
                MusicList(
                    curOriginIndex = curOriginIndex,
                    originMusicList = originMusicList,
                    onSeekToIndex = onSeekToIndex,
                    onRemovePlayerMusicItem = onRemovePlayerMusicItem
                )
            }

        }

    }


}


@Composable
fun MusicList(
    curOriginIndex: Int,
    originMusicList: List<XyPlayMusic>,
    onSeekToIndex: (Int) -> Unit,
    onRemovePlayerMusicItem: (Int) -> Unit
) {

    LazyColumnParentComponent(
        modifier = Modifier.fillMaxSize(),
        lazyListState = rememberLazyListState(
            initialFirstVisibleItemIndex = curOriginIndex
        ),
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding
        )
    ) {
        itemsIndexed(originMusicList) { index, it ->
            MusicListItem(
                curOriginIndex = curOriginIndex,
                onIndex = { index },
                xyMusic = it,
                onSeekToIndex = onSeekToIndex,
                onRemovePlayerMusicItem = onRemovePlayerMusicItem
            )
        }
        item {
            LazyLoadingAndStatus(
                stringResource(R.string.reached_bottom),
                ifLoading = ifNextPageNumList
            )
        }
    }
}

@Composable
fun MusicListItem(
    curOriginIndex: Int,
    onIndex: () -> Int,
    xyMusic: XyPlayMusic,
    onSeekToIndex: (Int) -> Unit,
    onRemovePlayerMusicItem: (Int) -> Unit
) {
    val index by remember {
        mutableIntStateOf(onIndex())
    }
    val text = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                color = if (curOriginIndex == index) Color(
                    0xffFB6580
                ) else MaterialTheme.colorScheme.onSurface
            ), block = {
                append(xyMusic.name)
            })
        if (xyMusic.artists != null) {
            withStyle(
                style = SpanStyle(
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    color = if (curOriginIndex == index) Color(
                        0xffFB6580
                    ) else MaterialTheme.colorScheme.onSurface
                ), block = {
                    append("  - ")
                    append(xyMusic.artists?.joinToString())
                })
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .debounceClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onSeekToIndex(index)
            }
            .padding(
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (curOriginIndex == index) FontWeight.W500 else FontWeight.W400,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (curOriginIndex == index) {
                Icon(
                    imageVector = Icons.Outlined.SignalCellularAlt,
                    contentDescription = stringResource(R.string.playing),
                    modifier = Modifier
                        .padding(horizontal = 3.dp),
                    tint = Color(0xffFB6580)
                )
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.remove_from_playlist),
                modifier = Modifier
                    .size(16.dp)
                    .debounceClickable {
                        onRemovePlayerMusicItem(index)
                    },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}