package cn.xybbz.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.common.utils.LrcUtils.formatTime

@Composable
fun MusicProgressBar(
    currentTime: Long,
    totalTime: Long,
    progress: Float,
    cacheProgress: Float,
    onProgressChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    progressBarColor: Color = MaterialTheme.colorScheme.onSurface,
    cacheProgressBarColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    backgroundBarColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    barHeight: Float = 4f,
    thumbRadius: Float = 6f,
    timeTextStyle: TextStyle = TextStyle(fontSize = 12.sp, color = Color.Gray),
) {
    var isDragging by remember { mutableStateOf(false) }
    var updatedProgress by remember { mutableFloatStateOf(progress) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (isDragging) updatedProgress else progress,
        label = "Progress Animation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // 时间显示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BasicText(
                text = formatTime(currentTime),
                style = timeTextStyle
            )
            BasicText(
                text = formatTime(totalTime),
                style = timeTextStyle
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 进度条
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            updatedProgress = newProgress
                            onProgressChanged(newProgress)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDrag = { change, _ ->
                            val position = change.position.x.coerceIn(0f, size.width.toFloat())
                            updatedProgress = position / size.width
                            onProgressChanged(updatedProgress)
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false }
                    )
                }
        ) {
            val barWidth = size.width
            val progressWidth = barWidth * animatedProgress
            val cacheProgressWidth = barWidth * cacheProgress

            // 背景条
            drawLine(
                color = backgroundBarColor,
                start = Offset(0f, size.height / 2),
                end = Offset(barWidth, size.height / 2),
                strokeWidth = barHeight.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 缓存前景条
            drawLine(
                color = cacheProgressBarColor,
                start = Offset(0f, size.height / 2),
                end = Offset(cacheProgressWidth, size.height / 2),
                strokeWidth = barHeight.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 前景条
            drawLine(
                color = progressBarColor,
                start = Offset(0f, size.height / 2),
                end = Offset(progressWidth, size.height / 2),
                strokeWidth = barHeight.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 滑块
            drawCircle(
                color = progressBarColor,
                radius = thumbRadius.dp.toPx(),
                center = Offset(progressWidth, size.height / 2)
            )
        }
    }
}
