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

package cn.xybbz.ui.xy

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.ext.platformHoverClickable

/** 开关轨道宽度。 */
private val XySwitchWidth = 36.dp

/** 开关轨道高度。 */
private val XySwitchHeight = 20.dp

/** 开关滑块尺寸。 */
private val XySwitchThumbSize = 14.dp

/** 开关滑块开启态的最大横向位移。 */
private val XySwitchThumbOffset = 18.dp

/**
 * 跨端紧凑开关。
 *
 * 使用 36×20 的胶囊轨道与 14dp 圆形滑块，替代 Material 默认的大尺寸开关，
 * 供设置页、专辑详情等需要紧凑开关的页面跨端复用。
 *
 * @param checked 当前开关状态。
 * @param onCheckedChange 状态变化回调。
 * @param modifier 修饰符。
 * @param enabled 是否允许交互。
 */
@Composable
fun XySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    // 轨道颜色：开启态使用主题色，关闭态使用低透明度前景色
    val trackColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        },
        animationSpec = tween(durationMillis = 220),
        label = "xy_switch_track",
    )
    // 轨道描边颜色：与轨道颜色保持同步变化
    val borderColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
        },
        animationSpec = tween(durationMillis = 220),
        label = "xy_switch_border",
    )
    // 滑块颜色：开启时使用主题容器色，关闭时使用前景色
    val thumbColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = 220),
        label = "xy_switch_thumb",
    )
    // 滑块横向位移：开启态滑到右侧
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) XySwitchThumbOffset else 0.dp,
        animationSpec = tween(durationMillis = 220),
        label = "xy_switch_offset",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(width = XySwitchWidth, height = XySwitchHeight)
            .clip(CircleShape)
            .background(trackColor)
            .border(BorderStroke(1.dp, borderColor), CircleShape)
            // 悬停时显示小手光标，点击时切换开关状态且不显示波纹
            .platformHoverClickable(
                interactionSource = interactionSource,
                enabled = enabled,
            )
            .debounceClickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Switch,
            ) {
                onCheckedChange(!checked)
            }
            .padding(horizontal = 2.dp, vertical = 2.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(XySwitchThumbSize)
                .background(thumbColor, CircleShape)
        )
    }
}
