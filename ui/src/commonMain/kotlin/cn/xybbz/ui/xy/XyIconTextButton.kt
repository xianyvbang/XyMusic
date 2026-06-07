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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.ext.platformHoverClickable
import cn.xybbz.ui.theme.XyTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * 左侧带图标的文字按钮，适合设置页底部操作和管理入口复用。
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
    paddingValues: PaddingValues = ButtonDefaults.ContentPadding,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        modifier = modifier.platformHoverClickable(
            interactionSource = interactionSource,
            enabled = enabled,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = color,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
        border = borderColor?.let { BorderStroke(1.dp, it) },
        contentPadding = paddingValues,
        interactionSource = interactionSource,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(XyTheme.dimens.outerVerticalPadding))
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = textStyle,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}
