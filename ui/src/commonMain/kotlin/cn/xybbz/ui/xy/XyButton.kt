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

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.ext.platformHoverClickable
import cn.xybbz.ui.theme.XyTheme

@Composable
fun XyButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    color: Color = Color.Unspecified,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    paddingValues: PaddingValues = PaddingValues(),
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    // 按钮圆角沿用项目统一尺寸。
    val shape = RoundedCornerShape(XyTheme.dimens.corner)
    // hover 源只负责桌面端小手光标，避免 Material 默认状态层叠加半透明背景。
    val hoverInteractionSource = remember { MutableInteractionSource() }
    // 点击源只负责防抖点击，不绘制默认 ripple/hover 状态层。
    val clickInteractionSource = remember { MutableInteractionSource() }
    // 禁用背景沿用 Material3 FilledButton 的弱化颜色。
    val containerColor = if (enabled) {
        backgroundColor
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    }
    // 未显式传入文字色时沿用 FilledButton 默认前景色。
    val contentColor = when {
        color.isSpecified -> color
        enabled -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    Box(
        modifier = Modifier
            .then(modifier)
            .platformHoverClickable(
                interactionSource = hoverInteractionSource,
                enabled = enabled,
            )
            .padding(paddingValues)
            .minimumInteractiveComponentSize()
            .clip(shape)
            .background(containerColor)
            .debounceClickable(
                interactionSource = clickInteractionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .defaultMinSize(
                minWidth = ButtonDefaults.MinWidth,
                minHeight = ButtonDefaults.MinHeight,
            )
            .padding(ButtonDefaults.ContentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            modifier = Modifier,
            color = contentColor,
            textAlign = TextAlign.Center,
            style = textStyle,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}
