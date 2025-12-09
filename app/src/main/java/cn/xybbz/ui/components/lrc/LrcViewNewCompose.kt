package cn.xybbz.ui.components.lrc

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.common.utils.LrcUtils.formatTime
import cn.xybbz.entity.data.LrcEntryData
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.viewmodel.LrcViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


//歌词横向的padding
private val lyricHorizontalPadding = 30.dp

@Composable
fun LrcViewNewCompose(
    modifier: Modifier = Modifier,
    lcrEntryList: List<LrcEntryData>,
    lrcViewModel: LrcViewModel = hiltViewModel(),
    listState: LazyListState = rememberLazyListState(),
) {

    val coroutineScope = rememberCoroutineScope()
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
        val verticalContentPadding = maxHeight / 2
        //获取歌词列表

        // 记录拖到哪一行
        var dragLineIndex by remember { mutableIntStateOf(0) }
        //是否在拖动列表
//        val isDragState = isDrag(listState.interactionSource)
        var isDragState = remember { mutableStateOf(false) }
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
        val playIndex by produceState<Int>(initialValue = 0, lcrEntryList) {
            //播放进度的flow，每秒钟发射一次
            lrcViewModel.getProgressStateFlow().collect {
                currentTimeMillis = it
                lcrEntryList.let { lcrEntryList ->
                    //播放器的播放进度，单位毫秒
                    val index =
                        lcrEntryList.indexOfFirst { item -> item.startTime <= it && it < item.endTime }
                    if (index >= 0) {
                        this.value = index
                    }
                }
            }
            awaitDispose {
                this.value = 0
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

        AnimatedContent(targetState = lcrEntryList.isEmpty(),label = "Animated Content"){isEmpty->
            if (!isEmpty)
            Box(modifier = Modifier.width(lyricWidth.dp)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .detectDragState { dragging ->
                            isDragState.value = dragging
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(
                        vertical = verticalContentPadding
                    ),
                ) {
                    itemsIndexed(lcrEntryList) { index, line ->

                        if (line.wordTimings.size > 1) {
                            KaraokeLyricLineNew(
                                line = line,
                                highlight = index == playIndex,
                                currentTimeMillis = currentTimeMillis,
                                onClick = {
                                    lrcViewModel.seekTo(line.startTime)
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                }
                            )
                        } else {
                            KaraokeLyricLineNew(
                                line = line,
                                highlight = index == playIndex,
                                onClick = {
                                    lrcViewModel.seekTo(line.startTime)
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                }
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
                            .align(Alignment.Center)
                            .padding(vertical = 8.dp)

                    ) {
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth(),
                            thickness = 1.dp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatTime(dragLine.startTime),
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
            else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(R.string.no_lyrics))
                }
            }
        }

    }

}

@Composable
fun KaraokeLyricLineNew(
    line: LrcEntryData,
    highlight: Boolean,
    onClick: () -> Unit
) {
    // 使用 Float 来动画变化，然后转换为 sp
    val animatedFontSize by animateFloatAsState(
        targetValue = if (highlight) 24f else 16f,
        animationSpec = tween(durationMillis = 300),
        label = "FontSizeAnimation"
    )
    val animatedColor by animateColorAsState(
        targetValue = if (highlight) Color.Cyan else Color.White,
        label = "ColorAnimation"
    )

    XyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(XyTheme.dimens.itemHeight)
            .debounceClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        backgroundColor = Color.Transparent
    ) {
        Text(
            text = line.text,
            fontSize = animatedFontSize.sp,
            color = animatedColor,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )

        if (line.secondText.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = line.secondText,
                fontSize = 14.sp,
                color = Color.LightGray,
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
    onClick: () -> Unit
) {
    val highlightIndex = line.wordTimings.indexOfLast { it <= currentTimeMillis }
    val nextWordTime = line.wordTimings.getOrNull(highlightIndex + 1) ?: Long.MAX_VALUE
    val currentWordTime = line.wordTimings.getOrNull(highlightIndex) ?: Long.MAX_VALUE

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
            .heightIn(XyTheme.dimens.itemHeight)
            .debounceClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        backgroundColor = Color.Transparent
    ) {
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
                    fontSize = if (highlight) 24.sp else 16.sp,
                    color = Color.White.copy(alpha = 0.5f + 0.5f * animatedAlpha),
                    fontWeight = if (animatedAlpha > 0.8f) FontWeight.Bold else FontWeight.Normal,
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
                .background(Color.Gray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .fillMaxWidth(animatedProgress)
                    .background(Color.Cyan.copy(alpha = 0.8f))
            )
        }
    }
}