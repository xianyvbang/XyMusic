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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

/** 文字按钮禁用态文字透明度，与 Material 禁用内容透明度保持一致。 */
internal const val XyTextButtonDisabledAlpha = 0.38f

/**
 * 文字按钮移入变色状态。
 *
 * 同时持有交互源、悬停状态和当前文字颜色，供通用文字按钮、菜单文字项
 * 以及自定义文字布局复用。
 */
@Stable
class XyTextHoverColorState internal constructor(
    /** 交互源，调用方需要继续复用它挂载点击与悬停检测。 */
    val interactionSource: MutableInteractionSource,
    /** 当前是否处于可用状态下的悬停。 */
    val hovered: Boolean,
    /** 当前应展示的文字颜色。 */
    val color: Color,
)

/**
 * 文字按钮统一的移入变色逻辑。
 *
 * 鼠标移入时文字从基础色切换为悬停色，移出后恢复；禁用时使用禁用色，
 * 避免各页面自行维护一份 hover 状态与颜色判断。
 *
 * @param interactionSource 复用的交互源，传入已挂载悬停检测的交互源可让文字与整行联动。
 * @param enabled 是否可用。
 * @param color 基础文字颜色。
 * @param hoverColor 移入后的文字颜色。
 * @param disabledColor 禁用时的文字颜色。
 */
@Composable
fun rememberXyTextHoverColor(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.onSurface,
    hoverColor: Color = MaterialTheme.colorScheme.primary,
    disabledColor: Color = color.copy(alpha = XyTextButtonDisabledAlpha),
): XyTextHoverColorState {
    val hovered by interactionSource.collectIsHoveredAsState()
    val currentColor = when {
        !enabled -> disabledColor
        hovered -> hoverColor
        else -> color
    }
    return XyTextHoverColorState(
        interactionSource = interactionSource,
        hovered = hovered && enabled,
        color = currentColor,
    )
}
