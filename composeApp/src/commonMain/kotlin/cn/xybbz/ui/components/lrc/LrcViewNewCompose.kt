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

package cn.xybbz.ui.components.lrc

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.utils.DateUtil.toSecondMs
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.LrcUtils.formatTime
import cn.xybbz.common.utils.LrcUtils.getIndex
import cn.xybbz.entity.data.LrcConfigData
import cn.xybbz.entity.data.LrcEntryData
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.LrcViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.add_24px
import xymusic_kmp.composeapp.generated.resources.backward_offset
import xymusic_kmp.composeapp.generated.resources.check_24px
import xymusic_kmp.composeapp.generated.resources.confirm
import xymusic_kmp.composeapp.generated.resources.forward_offset
import xymusic_kmp.composeapp.generated.resources.lrc_config
import xymusic_kmp.composeapp.generated.resources.no_lyrics
import xymusic_kmp.composeapp.generated.resources.offset
import xymusic_kmp.composeapp.generated.resources.remove_24px
import xymusic_kmp.composeapp.generated.resources.reset
import xymusic_kmp.composeapp.generated.resources.restart_alt_24px


//歌词横向的padding
private val lyricHorizontalPadding = 30.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LrcViewNewCompose(
    modifier: Modifier = Modifier,
    onSetLrcOffset: (Long) -> Unit,
    showConfigButton: Boolean = true,
    externalOffsetMillis: Long? = null,
    currentLineTopInset: Dp? = null,
    highlightScaleEnabled: Boolean = true,
    // 主歌词基础字号，翻译字号和行高会基于它联动计算。
    primaryFontSize: TextUnit = 20.sp,
    previewEntries: List<LrcEntryData>? = null,
    previewCurrentTimeMillis: Long? = null,
    lrcViewModel: LrcViewModel = koinViewModel<LrcViewModel>(),
    listState: LazyListState = rememberLazyListState(),
) {

    val realLcrEntryList by lrcViewModel.lrcServer.lcrEntryListFlow.collectAsStateWithLifecycle(
        emptyList()
    )
    val lcrEntryList = previewEntries ?: realLcrEntryList
    val coroutineScope = rememberCoroutineScope()
    val usePreviewLyrics = previewEntries != null && previewCurrentTimeMillis != null

    var tmpOffsetMs by remember {
        mutableStateOf(lrcViewModel.lrcServer.lrcConfig?.lrcOffsetMs ?: 0L)
    }
    val effectiveOffsetMs = externalOffsetMillis ?: tmpOffsetMs
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }


    DisposableEffect(Unit) {
        Log.i("=====", "歌词偏移 ${lrcViewModel.lrcServer.lrcConfig?.lrcOffsetMs}")
        onDispose {
            fabMenuExpanded = false
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current

        val maxHeight = this.maxHeight
        //得到歌词的最大宽度
        val lyricWidth = remember(this.maxWidth) {
            with(density) {
                //lyricHorizontalPadding 是 LazyColumn横向的padding，可以自行设置
                (maxWidth - lyricHorizontalPadding * 2).roundToPx()
            }
        }
        val topContentPadding = (
            currentLineTopInset ?: (maxHeight / 2 - XyTheme.dimens.outerVerticalPadding)
            ).coerceAtLeast(0.dp)
        val bottomContentPadding = if (currentLineTopInset == null) {
            topContentPadding
        } else {
            (maxHeight - currentLineTopInset).coerceAtLeast(0.dp)
        }
        //获取歌词列表

        // 记录拖到哪一行
        var dragLineIndex by remember { mutableIntStateOf(0) }
        //是否在拖动列表
//        val isDragState = isDrag(listState.interactionSource)
        val isDragState = remember { mutableStateOf(false) }
        var isDragStateTmp by remember { mutableStateOf(false) }
        var currentTimeMillis by remember {
            mutableLongStateOf(0L)
        }
        val derivedIndex by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex
            }
        }


        // 监听滑动中的当前位置
        LaunchedEffect(derivedIndex) {
            snapshotFlow { derivedIndex }
                .collect { index ->
                    dragLineIndex = if (isDragState.value) {
                        index
                    } else {
                        0
                    }
                }
        }


        //播放歌词的位置
        val playIndex by produceState(
            initialValue = 0,
            lcrEntryList,
            effectiveOffsetMs,
            previewCurrentTimeMillis
        ) {
            if (usePreviewLyrics) {
                val previewTime = previewCurrentTimeMillis
                currentTimeMillis = previewTime
                lcrEntryList.let { entries ->
                    val index = entries.getIndex(previewTime, effectiveOffsetMs)
                    if (index >= 0) {
                        this.value = index
                    }
                }
                awaitDispose {
                    this.value = 0
                }
            } else {
                //播放进度的flow，每秒钟发射一次
                lrcViewModel.getProgressStateFlow().collect {
                    currentTimeMillis = it
                    lcrEntryList.let { entries ->
                        //播放器的播放进度，单位毫秒
                        val index = entries.getIndex(it, effectiveOffsetMs)
                        if (index >= 0) {
                            this.value = index
                        }
                    }
                }
                awaitDispose {
                    this.value = 0
                }
            }
        }

        val ifScrollTo by remember {
            derivedStateOf {
                playIndex >= 0 && playIndex != listState.firstVisibleItemIndex && !isDragStateTmp
            }
        }

        LaunchedEffect(ifScrollTo) {
            snapshotFlow { ifScrollTo }.collect {
                if (ifScrollTo) {
                    listState.animateScrollToItem(playIndex)
                }
            }
        }

        LaunchedEffect(isDragState.value) {
            snapshotFlow { isDragState.value }.collect {
                if (!isDragState.value) {
                    delay(1500) // 给点时间，避免立即触发
                    isDragStateTmp = false
                    coroutineScope.launch {
                        listState.animateScrollToItem(playIndex)
                    }
                } else {
                    isDragStateTmp = true
                }
            }

        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(lyricWidth.dp)
        ) {

            AnimatedContent(
                targetState = lcrEntryList.isEmpty(),
                label = "Animated Content"
            ) { isEmpty ->

                if (!isEmpty) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .detectDragState { dragging ->
                                isDragState.value = dragging
                            },
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(
                            top = topContentPadding,
                            bottom = bottomContentPadding
                        ),
                    ) {
                        itemsIndexed(lcrEntryList) { index, line ->

                            if (line.wordTimings.size > 1) {
                                KaraokeLyricLineNew(
                                    line = line,
                                    highlight = index == playIndex,
                                    highlightScaleEnabled = highlightScaleEnabled,
                                    primaryFontSize = primaryFontSize,
                                    currentTimeMillis = currentTimeMillis,
                                    onClick = {
                                        if (!usePreviewLyrics) {
                                            lrcViewModel.seekTo(line.startTime)
                                        }
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(index)
                                        }
                                    }
                                )
                            } else {
                                KaraokeLyricLineNew(
                                    line = line,
                                    highlight = index == playIndex,
                                    highlightScaleEnabled = highlightScaleEnabled,
                                    primaryFontSize = primaryFontSize,
                                    onClick = {
                                        if (!usePreviewLyrics) {
                                            lrcViewModel.seekTo(line.startTime)
                                        }
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(index)
                                        }
                                    }
                                )
                            }
                        }
                    }


                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(Res.string.no_lyrics),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            if (isDragState.value && dragLineIndex in lcrEntryList.indices) {
                val dragLine = lcrEntryList[dragLineIndex]

                // 绘制辅助线和时间
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (currentLineTopInset == null) {
                                Modifier.align(Alignment.Center)
                            } else {
                                Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = currentLineTopInset)
                            }
                        )
                        .padding(vertical = 8.dp)

                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatTime(dragLine.startTime),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.92f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }

            if (showConfigButton) {
                LrcConfigComponent(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onTmpOffsetMillis = { tmpOffsetMs },
                    onFabMenuExpanded = { fabMenuExpanded },
                    onSetFabMenuExpanded = { fabMenuExpanded = it },
                    onSetLrcOffset = {
                        onSetLrcOffset(it)
                    },
                    onSetTmpLrcOffset = { offset ->
                        tmpOffsetMs = offset
                    })

                SuggestionChip(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = XyTheme.dimens.outerHorizontalPadding),
                    onClick = {
                        fabMenuExpanded = !fabMenuExpanded
                    },
                    label = {
                        Text(text = stringResource(Res.string.lrc_config))
                    })
            }
        }
    }
}

@Composable
fun KaraokeLyricLineNew(
    line: LrcEntryData,
    highlight: Boolean,
    highlightScaleEnabled: Boolean = true,
    primaryFontSize: TextUnit = 20.sp,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val defaultPrimaryFontSize = primaryFontSize.value
    val highlightPrimaryFontSize =
        if (highlightScaleEnabled) defaultPrimaryFontSize + 4f else defaultPrimaryFontSize
    val targetPrimaryFontSize =
        if (highlight && highlightScaleEnabled) highlightPrimaryFontSize else defaultPrimaryFontSize
    val targetPrimaryLineHeight = (targetPrimaryFontSize + 8f).sp
    val defaultSecondaryFontSize = (defaultPrimaryFontSize - 4f).coerceAtLeast(12f)
    val secondaryFontSize =
        if (highlight && highlightScaleEnabled) (defaultSecondaryFontSize + 2f).sp
        else defaultSecondaryFontSize.sp
    val secondaryLineHeight = (secondaryFontSize.value + 8f).sp

    val animatedFontSize by animateFloatAsState(
        targetValue = targetPrimaryFontSize,
        animationSpec = tween(durationMillis = 280),
        label = "FontSizeAnimation"
    )
    val animatedColor by animateColorAsState(
        targetValue = if (highlight) {
            colorScheme.onSurface
        } else {
            colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
        },
        label = "ColorAnimation"
    )
    val animatedSecondTextColor by animateColorAsState(
        targetValue = if (highlight) {
            colorScheme.primary
        } else {
            colorScheme.onSurfaceVariant.copy(alpha = 0.64f)
        },
        label = "SecondTextColorAnimation"
    )

    XyColumn(
        modifier = Modifier
            .fillMaxWidth()
//            .heightIn(XyTheme.dimens.itemHeight)
            .debounceClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        backgroundColor = Color.Transparent,
        paddingValues = PaddingValues()
    ) {
        Text(
            text = line.text,
            fontSize = animatedFontSize.sp,
            lineHeight = targetPrimaryLineHeight,
            color = animatedColor,
            fontWeight = if (highlight && highlightScaleEnabled) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )

        if (line.secondText.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = line.secondText,
                fontSize = secondaryFontSize,
                lineHeight = secondaryLineHeight,
                color = animatedSecondTextColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun KaraokeLyricLineNew(
    line: LrcEntryData,
    currentTimeMillis: Long,
    highlight: Boolean,
    highlightScaleEnabled: Boolean = true,
    primaryFontSize: TextUnit = 20.sp,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val highlightIndex = line.wordTimings.indexOfLast { it <= currentTimeMillis }
    val nextWordTime = line.wordTimings.getOrNull(highlightIndex + 1) ?: Long.MAX_VALUE
    val currentWordTime = line.wordTimings.getOrNull(highlightIndex) ?: Long.MAX_VALUE
    val resolvedPrimaryFontSize = if (highlight && highlightScaleEnabled) {
        (primaryFontSize.value + 4f).sp
    } else {
        primaryFontSize
    }
    val primaryLineHeight = (resolvedPrimaryFontSize.value + 8f).sp

    val progress = when {
        currentTimeMillis < currentWordTime -> 0f
        currentTimeMillis > nextWordTime -> 1f
        else -> (currentTimeMillis - currentWordTime).toFloat() / (nextWordTime - currentWordTime)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (highlight) progress else 0f,
        label = "LyricLineProgress"
    )

    XyColumn(
        modifier = Modifier
            .fillMaxWidth()
//            .heightIn(XyTheme.dimens.itemHeight)
            .debounceClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        backgroundColor = Color.Transparent,
        paddingValues = PaddingValues(

        )
    ) {
        val inactiveCharColor = colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        val activeCharColor = if (highlight) {
            colorScheme.onSurface
        } else {
            colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
        }
        Row(horizontalArrangement = Arrangement.Center) {
            line.text.forEachIndexed { index, char ->
                val intensity = when {
                    index < highlightIndex -> 1f
                    index == highlightIndex -> progress
                    else -> 0f
                }
                val animatedAlpha by animateFloatAsState(
                    targetValue = intensity,
                    label = "LyricCharAlpha"
                )

                Text(
                    text = char.toString(),
                    fontSize = resolvedPrimaryFontSize,
                    lineHeight = primaryLineHeight,
                    color = lerp(inactiveCharColor, activeCharColor, animatedAlpha),
                    fontWeight = if (highlightScaleEnabled && animatedAlpha > 0.8f) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                    modifier = Modifier.padding(horizontal = 1.dp)
                )
            }
        }

        // 进度条
        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth(0.6f) // 进度条宽度控制
                .clip(RoundedCornerShape(50))
                .background(colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .fillMaxWidth(animatedProgress)
                    .background(colorScheme.primary.copy(alpha = 0.9f))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun LrcConfigComponent(
    modifier: Modifier = Modifier,
    onTmpOffsetMillis: () -> Long,
    onFabMenuExpanded: () -> Boolean,
    onSetFabMenuExpanded: (Boolean) -> Unit,
    onSetLrcOffset: (Long) -> Unit,
    onSetTmpLrcOffset: (Long) -> Unit
) {

    var offsetMillis by remember {
        mutableStateOf(onTmpOffsetMillis())
    }

    val items =
        listOf(
            LrcConfigData(Res.drawable.add_24px, stringResource(Res.string.forward_offset)) {
                offsetMillis += 500
                onSetTmpLrcOffset(offsetMillis)
            },
            LrcConfigData(Res.drawable.remove_24px, stringResource(Res.string.backward_offset)) {
                offsetMillis -= 500
                onSetTmpLrcOffset(offsetMillis)
            },
            LrcConfigData(Res.drawable.restart_alt_24px, stringResource(Res.string.reset)) {
                offsetMillis = 0
                onSetTmpLrcOffset(offsetMillis)
            },
            LrcConfigData(Res.drawable.check_24px, stringResource(Res.string.confirm)) {
                onSetLrcOffset(offsetMillis)
                onSetFabMenuExpanded(false)
            }
        )


    AnimatedVisibility(
        visible = onFabMenuExpanded(),
        label = "lrcConfigScreen",
        modifier = modifier.padding(end = XyTheme.dimens.outerHorizontalPadding)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            XyTextSubSmall(text = "${stringResource(Res.string.offset)}: ${offsetMillis.toSecondMs()}s")
            items.forEachIndexed { _, item ->
                AssistChip(
                    modifier = Modifier.semantics {
                        isTraversalGroup = true
                        // Add a custom a11y action to allow closing the menu when focusing
                        // the last menu item, since the close button comes before the first
                        // menu item in the traversal order.
                        contentDescription = item.title
                    },
                    onClick = {
                        item.onClick()
                    }, label = {
                        XyTextSubSmall(text = item.title)
                    }, leadingIcon = {
                        Icon(painterResource(item.icon), contentDescription = item.title)
                    })
            }
        }
    }
}
