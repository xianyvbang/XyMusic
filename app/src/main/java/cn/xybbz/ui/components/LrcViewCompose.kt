package cn.xybbz.ui.components


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.style.TextOverflow.Companion.Visible
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.xybbz.R
import cn.xybbz.common.utils.LrcUtils
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.viewmodel.LrcViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

//歌词的字体大小
private val lyricFontSize = 15.sp

//选中后字体大小
private val selectLyricFontSize = 23.sp

//歌词横向的padding
private val lyricHorizontalPadding = 30.dp

//单行歌词纵向的padding
private val lyricRowVerticalPadding = 10.dp

//底部距离
private val lyricBottomPadding = 150.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LrcViewCompose(
    modifier: Modifier = Modifier,
    lrcViewModel: LrcViewModel = viewModel(),
    onSetState: (Boolean) -> Unit,
    fontSizeZoom: Float = 1.0f
) {
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {

        val density = LocalDensity.current
        //得到歌词的最大宽度
        val lyricWidth = remember(this.maxWidth) {
            with(density) {
                //lyricHorizontalPadding 是 LazyColumn横向的padding，可以自行设置
                (maxWidth - lyricHorizontalPadding * 2).roundToPx()
            }
        }
        //获取歌词列表

        val state = rememberLazyListState()
        val firstVisibleItemIndex by remember { derivedStateOf { state.firstVisibleItemIndex } }
        //是否在拖动列表
        val isDragState = isDrag(state.interactionSource)


        //播放歌词的位置
        val playIndex by produceState<Int>(initialValue = 0, LrcServer.lcrEntryList) {
            //播放进度的flow，每秒钟发射一次
            lrcViewModel.getProgressStateFlow().collect {
                LrcServer.lcrEntryList.let { lcrEntryList ->
                    //播放器的播放进度，单位毫秒
                    val index =
                        lcrEntryList.indexOfFirst { item -> item.startTime <= it && it < item.endTime }
                    if (index >= 0) {
                        if (index != state.firstVisibleItemIndex) {
                            //判断用户是否在拖动歌词，如果再拖动歌词，则不ScrollToItem
                            if (!isDragState.value) {
                                launch {
                                    //滑动到正中间
                                    val playItemsInfo =
                                        state.layoutInfo.visibleItemsInfo.find { item -> item.index == index }
                                    if (playItemsInfo != null) {
                                        state.animateScrollToItem(index)
                                    } else {
                                        if (!state.isScrollInProgress) {
                                            state.animateScrollToItem(index)
                                        }
                                    }
                                }.invokeOnCompletion {
                                }
                            }
                        }
                        this.value = index
                    }
                }
            }
            awaitDispose {
                this.value = 0
            }
        }
        LrcLazyColum(
            modifier = Modifier.align(Alignment.CenterStart),
            maxHeight = { maxHeight },
            state = { state },
            onPlayIndex = { playIndex },
            onFirstVisibleItemIndex = { firstVisibleItemIndex },
            onIsDragState = { isDragState },
            fontSizeZoom = fontSizeZoom,
            lyricWidth = lyricWidth,
            onSeekTo = {
                lrcViewModel.seekTo(it)
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(lyricBottomPadding),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(lyricBottomPadding / 3)
                    //todo 转换
//                    .height(with(density){lyricFontSize.toDp()})
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            1.0f to MaterialTheme.colorScheme.background,
                            tileMode = TileMode.Clamp
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(lyricBottomPadding / 3 * 2)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.TextFields, contentDescription = null,
                        modifier = Modifier
                            .clickable {
//                            onBack()
                                coroutineScope.launch {
                                    onSetState(true)
                                }
                            }
                            .padding(8.dp)
                    )

                }

                PlayerStateComponent(
                    size = 36.dp,
                    musicController = lrcViewModel.musicFinalNewController
                )
            }

        }


    }
}


@Composable
fun LrcLazyColum(
    modifier: Modifier,
    maxHeight: () -> Dp,
    state: () -> LazyListState,
    onPlayIndex: () -> Int,
    onFirstVisibleItemIndex: () -> Int,
    onIsDragState: () -> MutableState<Boolean>,
    fontSizeZoom: Float,
    lyricWidth: Int,
    onSeekTo: ((Long) -> Unit)? = null,
) {
    val mainViewModel = LocalMainViewModel.current

    if (LrcServer.lcrEntryList.isNotEmpty() == true) {
        LrcServer.lcrEntryList.let { lcrEntryList ->
            //lazyColumn 的纵向内容padding
            val verticalContentPadding = maxHeight() / 2

            val density = LocalDensity.current

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = lyricHorizontalPadding),
                state = state(),
                contentPadding = PaddingValues(
                    bottom = verticalContentPadding
                ),
                //verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                itemsIndexed(
                    items = lcrEntryList,
                    key = { index, it -> "$index-${it.startTime}-${it.endTime}" }) { index, item ->

                    val scale by animateFloatAsState(
                        targetValue = if (index == onPlayIndex()) (1.0f) else 1f,
                        animationSpec = tween(1000),
                        label = "scale"
                    )

                    /*Text(
                        text = "This is an example of how to share text",
                        modifier = Modifier
                            .wrapContentWidth()
                            .sharedBounds(
                                rememberSharedContentState(
                                    key = "shared Text"
                                ),
                                animatedVisibilityScope = this,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                            )
                    )*/

                    Text(
                        text = item.displayText,
                        color = when (index) {
                            onPlayIndex() -> MaterialTheme.colorScheme.primary
                            onFirstVisibleItemIndex() -> MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.8f
                            )

                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        },
                        fontWeight = if (index == onPlayIndex()) FontWeight.Medium else FontWeight.Normal,
                        textAlign = TextAlign.Start,
                        lineHeight = (if (index == onPlayIndex()) selectLyricFontSize else lyricFontSize).times(
                            fontSizeZoom
                        ),
                        overflow = Visible,
                        fontSize = (if (index == onPlayIndex()) selectLyricFontSize else lyricFontSize).times(
                            fontSizeZoom
                        ),
                        style = LocalTextStyle.current.copy(textMotion = TextMotion.Animated),
                        modifier = Modifier
                            .fillMaxWidth()
                            //lyricRowVerticalPadding 是 item纵向的padding，可以自行设置
                            .padding(vertical = lyricRowVerticalPadding)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                transformOrigin = TransformOrigin(0f, 0f)
                            }
//                            . wrapContentSize()
                            .animateContentSize()


                    )
                }
            }


            //用户滑动lazyColumn,显示中间的横线、播放按钮、该行词的时间
            AnimatedVisibility(
                onIsDragState().value,
//                modifier = Modifier.align(Alignment.CenterStart),
                modifier = modifier,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Row(
                    Modifier
                        .padding(horizontal = 15.dp)
                        .fillMaxWidth()
                        .debounceClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (state().firstVisibleItemIndex in lcrEntryList.indices) {
                                onSeekTo?.invoke(lcrEntryList[state().firstVisibleItemIndex].startTime)
                                onIsDragState().value = false
                            }
                        }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "")
                    HorizontalDivider(Modifier.fillMaxWidth(0.9f))
                    Text(
                        text = lcrEntryList.getOrNull(state().firstVisibleItemIndex)?.startTimeFormat
                            ?: "",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                    )
                }
            }
        }

    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.no_lyrics))
        }
    }
}

@Composable
fun isDrag(interactionSource: InteractionSource): MutableState<Boolean> {
    val isDragged = remember { mutableStateOf(false) }
    LaunchedEffect(interactionSource) {
        var delayJob: Job? = null
        val interactions = mutableSetOf<Interaction>()
        interactionSource.interactions.map { interaction ->
            when (interaction) {
                is DragInteraction.Start -> {
                    interactions.add(interaction)
                }

                is DragInteraction.Stop -> {
                    interactions.remove(interaction.start)
                }

                is DragInteraction.Cancel -> {
                    interactions.remove(interaction.start)
                }
            }
            interactions.isNotEmpty()
        }.collect { isDrag ->
            delayJob?.cancel()
            if (!isDrag) {
                delayJob = launch {
                    delay(TOUCH_DELAY)
                    isDragged.value = false
                }
            } else {
                isDragged.value = true
            }
        }
    }
    return isDragged
}

private const val TOUCH_DELAY = 2000L


/**
 * @author 刘梦龙
 * @date 2025/04/27
 * @constructor 创建[LrcEntry]
 * @param [startTime] 歌词开始时间
 * @param [text] 歌词主文本
 * @param [secondText] 歌词翻译文本
 * @param [wordTimings] 逐字时间
 */
@Stable
data class LrcEntry(
    val startTime: Long,
    val text: String,
    var secondText: String = "",
    //todo 这个字段没有赋值
    val wordTimings: List<Long> = emptyList(),
) : Comparable<LrcEntry> {
    var endTime: Long = 0

    //分秒 mm:ss
    val startTimeFormat: String get() = LrcUtils.formatTime(startTime)

    //该行显示的歌词
    val displayText: String get() = if (secondText.isNotBlank()) "${text}\n$secondText" else text

    override fun compareTo(other: LrcEntry): Int {
        val l = startTime - other.startTime
        return if (l > 0) 1 else if (l == 0L) 0 else -1
    }
}
