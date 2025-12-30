package cn.xybbz.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.common.utils.LrcUtils.formatTime
import cn.xybbz.ui.xy.XySmallSlider

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
        XySmallSlider(
            progress = progress,
            cacheProgress = cacheProgress,
            onProgressChanged = onProgressChanged,
            progressBarColor = progressBarColor,
            cacheProgressBarColor = cacheProgressBarColor,
            backgroundBarColor = backgroundBarColor,
            barHeight = barHeight,
            thumbRadius = thumbRadius
        )
    }
}
