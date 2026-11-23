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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import cn.xybbz.ui.ext.jvmHoverDebounceClickable

/** 文字按钮禁用态文字透明度，与 Material 禁用内容透明度保持一致。 */
private const val XyTextButtonDisabledAlpha = 0.38f

/**
 * 文字按钮移入变色状态。
 *
 * 同时持有交互源、悬停状态和当前文字颜色，供通用文字按钮及自定义文字布局复用。
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
 * @param enabled 是否可用。
 * @param color 基础文字颜色。
 * @param hoverColor 移入后的文字颜色。
 * @param disabledColor 禁用时的文字颜色。
 */
@Composable
fun rememberXyTextHoverColor(
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.onSurface,
    hoverColor: Color = MaterialTheme.colorScheme.primary,
    disabledColor: Color = color.copy(alpha = XyTextButtonDisabledAlpha),
): XyTextHoverColorState {
    val interactionSource = remember { MutableInteractionSource() }
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

/**
 * JVM 桌面端通用文字按钮。
 *
 * 点击无水波纹背景，鼠标移入时文字颜色切换到悬停色并显示小手光标，
 * 禁用时点击失效并使用禁用色；所有可点击的纯文字入口统一走该组件。
 *
 * @param text 按钮文案。
 * @param onClick 点击回调。
 * @param modifier 外层修饰符。
 * @param enabled 是否可点击。
 * @param color 基础文字颜色。
 * @param hoverColor 移入后的文字颜色。
 * @param disabledColor 禁用时的文字颜色。
 * @param style 文字样式。
 * @param fontWeight 文字字重，为空时使用样式自带字重。
 * @param maxLines 最大行数。
 * @param overflow 文本溢出处理方式。
 * @param textAlign 文字对齐方式，为空时使用样式自带对齐。
 * @param role 语义角色。
 * @param onClickLabel 点击语义说明。
 * @param onTextLayout 文本排版回调，可用于判断是否发生省略。
 */
@Composable
fun XyTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.onSurface,
    hoverColor: Color = MaterialTheme.colorScheme.primary,
    disabledColor: Color = color.copy(alpha = XyTextButtonDisabledAlpha),
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    fontWeight: FontWeight? = null,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    textAlign: TextAlign? = null,
    role: Role? = Role.Button,
    onClickLabel: String? = null,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    // 文字颜色由通用移入变色逻辑统一计算，页面只关心基础色与悬停色
    val colorState = rememberXyTextHoverColor(
        enabled = enabled,
        color = color,
        hoverColor = hoverColor,
        disabledColor = disabledColor,
    )
    Text(
        text = text,
        modifier = modifier.jvmHoverDebounceClickable(
            interactionSource = colorState.interactionSource,
            enabled = enabled,
            role = role,
            onClickLabel = onClickLabel,
            onClick = onClick,
        ),
        color = colorState.color,
        style = style,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        onTextLayout = { layoutResult -> onTextLayout?.invoke(layoutResult) },
    )
}
