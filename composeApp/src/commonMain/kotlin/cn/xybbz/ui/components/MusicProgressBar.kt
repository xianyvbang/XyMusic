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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.common.utils.LrcUtils.formatTime
import cn.xybbz.extension.playProgress
import cn.xybbz.ui.xy.XySmallSlider
import kotlinx.coroutines.flow.Flow

@Composable
fun MusicProgressBar(
    currentTime: Long,
    progressStateFlow: Flow<Long>,
    totalTime: Long,
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

    val progress by playProgress(totalTime, progressStateFlow)


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
