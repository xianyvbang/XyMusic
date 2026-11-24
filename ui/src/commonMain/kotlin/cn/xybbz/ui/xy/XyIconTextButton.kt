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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.ext.platformHoverClickable
import cn.xybbz.ui.theme.XyTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** 按钮最小宽度，保持与原来的 Material 按钮一致。 */
private val XyIconTextButtonMinWidth = 58.dp

/** 按钮最小高度，保持与原来的 Material 按钮一致。 */
private val XyIconTextButtonMinHeight = 40.dp

/** 按钮图标尺寸。 */
private val XyIconTextButtonIconSize = 18.dp

/** 悬停时叠加在按钮底色上的内容色透明度，参考设置页恢复默认按钮的移入反馈。 */
private const val XyIconTextButtonHoverTintAlpha = 0.10f

/** 禁用态按钮底色透明度。 */
private const val XyIconTextButtonDisabledContainerAlpha = 0.10f

/** 禁用态按钮内容透明度。 */
private const val XyIconTextButtonDisabledContentAlpha = 0.38f

/**
 * 左侧带图标的文字按钮，适合设置页底部操作和管理入口复用。
 *
 * 悬停时不再使用 Material 按钮自带的状态层，而是与设置页「恢复默认」按钮一致：
 * 只在底色上叠加一层轻微的内容色，移出后立即恢复原色，避免整块灰色遮罩。
 *
 * @param modifier 外层修饰符。
 * @param onClick 点击按钮时触发。
 * @param text 按钮文字。
 * @param icon 左侧图标资源。
 * @param contentDescription 图标语义说明，默认沿用按钮文字。
 * @param enabled 是否启用按钮。
 * @param color 按钮文字和图标颜色。
 * @param backgroundColor 按钮背景色。
 * @param borderColor 可选边框颜色，适合次级按钮。
 * @param paddingValues 按钮内容内边距。
 * @param textStyle 按钮文字样式。
 */
@Composable
fun XyIconTextButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    icon: DrawableResource,
    contentDescription: String? = text,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.onPrimary,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color? = null,
    paddingValues: PaddingValues = PaddingValues(
        horizontal = XyTheme.dimens.contentPadding * 2,
        vertical = XyTheme.dimens.outerVerticalPadding,
    ),
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val buttonHovered = enabled && hovered
    // 悬停时用内容色轻叠一层到原底色上，只改变色调深浅，不覆盖整块按钮。
    val hoverContainerColor = color
        .copy(alpha = XyIconTextButtonHoverTintAlpha)
        .compositeOver(backgroundColor)
    val containerColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = XyIconTextButtonDisabledContainerAlpha)
        buttonHovered -> hoverContainerColor
        else -> backgroundColor
    }
    val contentColor = if (enabled) {
        color
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = XyIconTextButtonDisabledContentAlpha)
    }

    Surface(
        modifier = modifier
            .defaultMinSize(
                minWidth = XyIconTextButtonMinWidth,
                minHeight = XyIconTextButtonMinHeight,
            )
            // 悬停与手型光标由平台扩展提供，点击交给无涟漪的 clickable，避免 Material 状态层参与绘制。
            .platformHoverClickable(
                interactionSource = interactionSource,
                enabled = enabled,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = containerColor,
        contentColor = contentColor,
        border = borderColor?.let { BorderStroke(1.dp, it) },
    ) {
        Row(
            modifier = Modifier.padding(paddingValues),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = contentDescription,
                modifier = Modifier.size(XyIconTextButtonIconSize),
                tint = LocalContentColor.current,
            )
            Text(
                text = text,
                color = LocalContentColor.current,
                textAlign = TextAlign.Center,
                style = textStyle,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
    }
}
