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


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalPlayerChromeState
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.ext.platformHoverClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnParentComponent
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.ui.xy.rememberXyTextHoverColor
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.clear
import xymusic.composeapp.generated.resources.close_24px
import xymusic.composeapp.generated.resources.current_playlist
import xymusic.composeapp.generated.resources.playing
import xymusic.composeapp.generated.resources.reached_bottom
import xymusic.composeapp.generated.resources.remove_from_playlist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicListComponent(
    musicListState: Boolean,
    curOriginIndex: Int,
    musicController: MusicCommonController,
    originMusicList: List<XyPlayMusic>,
    onSetState: (Boolean) -> Unit,
    onClearPlayerList: () -> Unit,
    onSeekToIndex: (Int) -> Unit,
    onRemovePlayerMusicItem: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    // 清空按钮的悬停交互源与文字变色状态，桌面端移入时文字切换为主题色
    val clearHoverSource = remember { MutableInteractionSource() }
    val clearTextColorState = rememberXyTextHoverColor(
        interactionSource = clearHoverSource,
        color = MaterialTheme.colorScheme.onSurface,
    )

    // 播放列表弹层关闭时恢复迷你播放条标题跑马灯。
    val playerChromeState = LocalPlayerChromeState.current
    val state by musicController.stateFlow.collectAsStateWithLifecycle()
    val isPlaying = state == PlayStateEnum.Playing || state == PlayStateEnum.Loading
    MusicBottomMenuPlatformSheet(
        onIfDisplay = { musicListState },
        onClose = {
            // 关闭播放列表后恢复迷你播放条标题滚动一次。
            playerChromeState.putMarqueeIterations(1)
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
                    .height(platformMusicListContentHeight(this.maxHeight))
            ) {

                XyRow(
                    paddingValues = PaddingValues(
                        horizontal = XyTheme.dimens.outerHorizontalPadding,
                        vertical = XyTheme.dimens.outerVerticalPadding / 2
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.current_playlist),
                        fontWeight = FontWeight.W900,
                        fontSize = 19.sp
                    )
                    // 清空按钮使用轻量文字点击：无水波纹、点击区域贴合文字，并保留桌面端移入变色与小手
                    XyTextSubSmall(
                        modifier = Modifier
                            // 保留 Material 最小 48dp 触控热区，避免移动端点击面积过小
                            .minimumInteractiveComponentSize()
                            .platformHoverClickable(
                                interactionSource = clearHoverSource,
                            )
                            .debounceClickable(
                                interactionSource = clearHoverSource,
                                indication = null,
                                role = Role.Button,
                            ) {
                                //删除数据库内容
                                onClearPlayerList()
                            }
                            // 内边距放在点击区域内，保证移动端也有足够的点击热区
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        text = stringResource(Res.string.clear),
                        color = clearTextColorState.color,
                    )

                }
                HorizontalDivider(
                    modifier = Modifier.padding(2.dp),
                    thickness = 1.dp,
                    color = Color(0x565C5E6F)
                )
                MusicList(
                    curOriginIndex = curOriginIndex,
                    isPlaying = isPlaying,
                    originMusicList = originMusicList,
                    onSeekToIndex = onSeekToIndex,
                    onRemovePlayerMusicItem = onRemovePlayerMusicItem
                )
            }

        }

    }


}


internal expect fun platformMusicListContentHeight(maxHeight: Dp): Dp

@Composable
fun MusicList(
    curOriginIndex: Int,
    isPlaying: Boolean,
    originMusicList: List<XyPlayMusic>,
    onSeekToIndex: (Int) -> Unit,
    onRemovePlayerMusicItem: (Int) -> Unit
) {
    val mainViewModel = LocalMainViewModel.current
    val ifNextPageNumList by mainViewModel.ifNextPageNumListFlow.collectAsStateWithLifecycle()

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
                isPlaying = isPlaying,
                onIndex = { index },
                xyMusic = it,
                onSeekToIndex = onSeekToIndex,
                onRemovePlayerMusicItem = onRemovePlayerMusicItem
            )
        }
        item {
            LazyLoadingAndStatus(
                stringResource(Res.string.reached_bottom),
                ifLoading = ifNextPageNumList
            )
        }
    }
}

@Composable
fun MusicListItem(
    curOriginIndex: Int,
    isPlaying: Boolean,
    onIndex: () -> Int,
    xyMusic: XyPlayMusic,
    onSeekToIndex: (Int) -> Unit,
    onRemovePlayerMusicItem: (Int) -> Unit
) {
    val index by remember {
        mutableIntStateOf(onIndex())
    }
    val isCurrentMusic = curOriginIndex == index
    val currentTextStyle = MaterialTheme.typography.bodyLarge
    val normalTextStyle = MaterialTheme.typography.bodySmall
    val normalTextScale = normalTextStyle.fontSize.value / currentTextStyle.fontSize.value
    val animatedTextScale by animateFloatAsState(
        targetValue = if (isCurrentMusic) 1f else normalTextScale,
        animationSpec = tween(durationMillis = 220),
        label = "musicListItemTextScale"
    )
    val itemTextColor = if (isCurrentMusic) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    // 整行点击切歌的悬停交互源：桌面端移入时文字变色并显示小手光标
    val itemInteractionSource = remember { MutableInteractionSource() }
    val itemTextColorState = rememberXyTextHoverColor(
        interactionSource = itemInteractionSource,
        color = itemTextColor,
    )
    // 行尾删除图标的悬停交互源：移入时图标高亮并显示小手光标
    val removeInteractionSource = remember { MutableInteractionSource() }
    val removeColorState = rememberXyTextHoverColor(
        interactionSource = removeInteractionSource,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val text = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontSize = currentTextStyle.fontSize,
                color = itemTextColorState.color
            ), block = {
                append(xyMusic.name)
            })
        if (xyMusic.artists != null) {
            withStyle(
                style = SpanStyle(
                    fontSize = currentTextStyle.fontSize,
                    color = itemTextColorState.color
                ), block = {
                    append("  - ")
                    append(xyMusic.artists?.joinToString())
                })
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .platformHoverClickable(
                interactionSource = itemInteractionSource,
            )
            .debounceClickable(
                interactionSource = itemInteractionSource,
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
            style = currentTextStyle,
            fontWeight = if (isCurrentMusic) FontWeight.W500 else FontWeight.W400,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0.5f)
                    scaleX = animatedTextScale
                    scaleY = animatedTextScale
                }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isCurrentMusic) {
                PlayingEqualizer(
                    modifier = Modifier.padding(horizontal = 3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    isPlaying = isPlaying,
                    contentDescription = stringResource(Res.string.playing),
                )
            }
            Icon(
                painter = painterResource(Res.drawable.close_24px),
                contentDescription = stringResource(Res.string.remove_from_playlist),
                modifier = Modifier
                    .size(16.dp)
                    // 桌面端悬停时显示小手光标并让图标高亮，移出后恢复
                    .platformHoverClickable(
                        interactionSource = removeInteractionSource,
                    )
                    // 保留平台默认水波纹反馈，维持移动端原有点击手感
                    .debounceClickable {
                        onRemovePlayerMusicItem(index)
                    },
                tint = removeColorState.color
            )
        }
    }
}

@Composable
private fun PlayingEqualizer(
    color: Color,
    isPlaying: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    var elapsedNanos by remember { mutableStateOf(0L) }
    val barPhases = remember {
        listOf(0f, 0.42f, 0.78f)
    }
    LaunchedEffect(isPlaying) {
        if (!isPlaying) {
            return@LaunchedEffect
        }

        var lastFrameNanos: Long? = null
        while (true) {
            val frameNanos = withFrameNanos { it }
            val previousFrameNanos = lastFrameNanos
            if (previousFrameNanos != null) {
                val deltaNanos = frameNanos - previousFrameNanos
                elapsedNanos =
                    (elapsedNanos + deltaNanos) % PlayingEqualizerAnimationDurationNanos
            }
            lastFrameNanos = frameNanos
        }
    }
    val progress = elapsedNanos.toFloat() / PlayingEqualizerAnimationDurationNanos

    Canvas(
        modifier = modifier
            .size(width = 18.dp, height = 16.dp)
            .semantics {
                this.contentDescription = contentDescription
            }
    ) {
        val barCount = barPhases.size
        val gap = size.width * 0.16f
        val barWidth = (size.width - gap * (barCount - 1)) / barCount
        val minBarHeight = size.height * 0.28f
        val maxBarHeight = size.height * 0.92f

        barPhases.forEachIndexed { index, phase ->
            val wave = kotlin.math.sin((progress + phase) * kotlin.math.PI.toFloat() * 2f)
            val normalized = (wave + 1f) / 2f
            val barHeight = minBarHeight + (maxBarHeight - minBarHeight) * normalized
            val left = index * (barWidth + gap)
            val top = size.height - barHeight

            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

private const val PlayingEqualizerAnimationDurationNanos = 760L * 1_000_000L
